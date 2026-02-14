package org.steam2.editeur

import org.steam2.editeur.application.RecupererCommentaires
import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import jakarta.persistence.Persistence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.slf4j.LoggerFactory
import org.steam2.editeur.daos.CommentaireDAO
import org.steam2.editeur.application.Menus
import org.steam2.editeur.entites.Editeur
import org.steam2.editeur.daos.EditeurDAO
import org.steam2.editeur.daos.GenreDAO
import org.steam2.editeur.daos.JeuVideoDAO
import org.steam2.editeur.exceptions.LoginException
import java.util.*

private val log = LoggerFactory.getLogger("Main")

fun main() = runBlocking {
    log.info("Démarrage de l'application de l'éditeur")

    // DAOs
    val emf = Persistence.createEntityManagerFactory("steam2-editeur")
    val editeurDAO = EditeurDAO(emf)
    val jeuVideoDAO = JeuVideoDAO(emf)
    val commentaireDAO = CommentaireDAO(emf)
    val genreDAO = GenreDAO(emf)

    // Récupération du paramétrage Kafka
    val props = Properties()
    val inputStream = Thread.currentThread().contextClassLoader.getResourceAsStream("kafka.properties")
    props.load(inputStream)
    val topic = props.getProperty("topic.name")

    // Kafka
    val producer = KafkaProducer<String, GenericRecord>(props)
    val consumer = KafkaConsumer<String, GenericRecord>(props)
    consumer.subscribe(listOf(topic))

    // Préparation de l'interface (fenêtre)
    val terminal = DefaultTerminalFactory().setTerminalEmulatorTitle("Steam2 - Editeur").createTerminal()
    val screen = TerminalScreen(terminal)
    screen.startScreen()
    val gui = MultiWindowTextGUI(screen)
    val menus = Menus(gui, editeurDAO, commentaireDAO, jeuVideoDAO, genreDAO)

    log.info("L'application est prête")

    // Récupération en arrière plan des rapports d'incidents
    // On utilise ici les Co Routines de Kotlin, ce qui équivaut à un pool de thread en Java
    val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val serviceRecuperationCommentaires = RecupererCommentaires(consumer, jeuVideoDAO, commentaireDAO)

    val jobService = launch(Dispatchers.IO) {
        serviceRecuperationCommentaires.launch()
    }

    // Lancement de l'interface
    val editeur: Editeur? = menus.login()

    if(editeur == null) {
        throw LoginException("Editeur non connecté")
    }

    log.info("L'editeur ${editeur.nom} s'est connecté")

    // Menu principal : liste d'options possibles
    menus.mainMenu()

    // Fin du programme : On ferme la fenêtre et on arrête le service de récupération des commentaires

    log.info("On quitte l'application")
    //hibernate
    emf.close()
    //kafka
    producer.flush()
    producer.close()
    //fenetre
    screen.close()
    //service
    serviceRecuperationCommentaires.stop()
    log.info("Attente de la fermeture du service de récupération des commentaires...")
    jobService.join()

    log.info("Merci d'avoir utilisé notre application !")
}