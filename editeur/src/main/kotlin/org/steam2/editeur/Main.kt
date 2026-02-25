package org.steam2.editeur

import org.steam2.editeur.application.RecupererCommentaires
import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread
import jakarta.persistence.Persistence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.slf4j.LoggerFactory
import org.steam2.editeur.application.EnvoiJeux
import org.steam2.editeur.daos.CommentaireDAO
import org.steam2.editeur.application.Menus
import org.steam2.editeur.application.RecupererIncidents
import org.steam2.editeur.entites.Editeur
import org.steam2.editeur.daos.EditeurDAO
import org.steam2.editeur.daos.GenreDAO
import org.steam2.editeur.daos.IncidentDAO
import org.steam2.editeur.daos.JeuVideoDAO
import org.steam2.editeur.daos.VersionDAO
import java.util.*

private val log = LoggerFactory.getLogger("Main")

/**
 * Fonction principale de l'application éditeur
 * @author remi
 */
fun main() = runBlocking {
    log.info("Démarrage de l'application de l'éditeur")

    // DAOs
    val emf = Persistence.createEntityManagerFactory("steam2-editeur")
    val editeurDAO = EditeurDAO(emf)
    val jeuVideoDAO = JeuVideoDAO(emf)
    val commentaireDAO = CommentaireDAO(emf)
    val incidentDAO = IncidentDAO(emf)
    val genreDAO = GenreDAO(emf)
    val versionDAO = VersionDAO(emf)

    // Récupération du paramétrage Kafka
    val propsCommentaires = Properties()
    val inputStreamCommonCommentaires = Thread.currentThread().contextClassLoader.getResourceAsStream("kafka/common.properties")
    val inputStreamCommentaires = Thread.currentThread().contextClassLoader.getResourceAsStream("kafka/commentaires.properties")
    propsCommentaires.load(inputStreamCommonCommentaires)
    propsCommentaires.load(inputStreamCommentaires)
    val topicCommentaires = propsCommentaires.getProperty("topic.name")

    val propsIncidents = Properties()
    val inputStreamCommonIncidents = Thread.currentThread().contextClassLoader.getResourceAsStream("kafka/common.properties")
    val inputStreamIncidents = Thread.currentThread().contextClassLoader.getResourceAsStream("kafka/incidents.properties")
    propsIncidents.load(inputStreamCommonIncidents)
    propsIncidents.load(inputStreamIncidents)
    val topicIncidents = propsIncidents.getProperty("topic.name")

    val propsJeux = Properties()
    val inputStreamCommonJeux = Thread.currentThread().contextClassLoader.getResourceAsStream("kafka/common.properties")
    val inputStreamJeux = Thread.currentThread().contextClassLoader.getResourceAsStream("kafka/jeux.properties")
    propsJeux.load(inputStreamCommonJeux)
    propsJeux.load(inputStreamJeux)
    val topicJeux = propsJeux.getProperty("topic.name")

    // Kafka
    val producerJeux = KafkaProducer<String, GenericRecord>(propsJeux)
    val serviceEnvoiJeux = EnvoiJeux(producerJeux, topicJeux, jeuVideoDAO)

    val consumerCommentaires = KafkaConsumer<String, GenericRecord>(propsCommentaires)
    consumerCommentaires.subscribe(listOf(topicCommentaires))

    val consumerIncidents = KafkaConsumer<String, GenericRecord>(propsIncidents)
    consumerIncidents.subscribe(listOf(topicIncidents))

    // Préparation de l'interface (fenêtre)
    val terminal = DefaultTerminalFactory().setTerminalEmulatorTitle("Steam2 - Editeur").createTerminal()
    val screen = TerminalScreen(terminal)
    screen.startScreen()
    val gui = MultiWindowTextGUI(screen)
    val menus = Menus(gui, editeurDAO, commentaireDAO, incidentDAO, jeuVideoDAO, genreDAO, versionDAO, serviceEnvoiJeux)

    log.info("L'application est prête")

    // Récupération en arrière plan des commentaires et rapports d'incidents
    // On utilise ici les Co Routines de Kotlin, ce qui équivaut à un pool de thread en Java
    val serviceScopeCommentaires = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val serviceRecuperationCommentaires = RecupererCommentaires(consumerCommentaires, jeuVideoDAO, commentaireDAO)

    val jobServiceCommentaires = serviceScopeCommentaires.launch(Dispatchers.IO) {
        serviceRecuperationCommentaires.launch()
    }

    val serviceScopeIncidents = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val serviceRecuperationIncidents = RecupererIncidents(consumerIncidents, jeuVideoDAO, incidentDAO)

    val jobServiceIncidents = serviceScopeIncidents.launch(Dispatchers.IO) {
        serviceRecuperationIncidents.launch()
    }

    // Lancement de l'interface
    val editeur: Editeur? = menus.login()

    if(editeur == null) {
        log.error("Editeur est null")
    }
    else {
        log.info("L'editeur ${editeur.nom} (${editeur.typeEditeur}) s'est connecté")

        // Menu principal : liste d'options possibles
        menus.mainMenu(editeur)
    }

    // Fin du programme : On ferme la fenêtre et on arrête les services de récupération

    log.info("On quitte l'application")
    //hibernate
    emf.close()
    AbandonedConnectionCleanupThread.checkedShutdown()
    //kafka
    producerJeux.flush()
    producerJeux.close()
    //fenetre
    screen.close()
    //services
    serviceRecuperationCommentaires.stop()
    serviceRecuperationIncidents.stop()
    log.info("Attente de la fermeture des services de récupération des commentaires... " +
            "(${RecupererCommentaires.DELAI_ATTENTE * 2 / 1000} secondes max)")
    jobServiceCommentaires.join()
    serviceScopeCommentaires.cancel()
    log.info("Attente de la fermeture des services de récupération des incidents... " +
            "(${RecupererIncidents.DELAI_ATTENTE * 2 / 1000} secondes max)")
    jobServiceIncidents.join()
    serviceScopeIncidents.cancel()

    log.info("Merci d'avoir utilisé notre application !")
}