package org.steam2.client

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
import org.steam2.client.application.EnvoiCommentaires
import org.steam2.client.application.EnvoiIncidents
import org.steam2.client.application.Menus
import org.steam2.client.application.RecupJeu
import org.steam2.client.daos.*;
import org.steam2.client.entites.Joueur
import java.util.Properties

private val log = LoggerFactory.getLogger("Main")

fun main() = runBlocking {
    log.info("Démarrage de l'application client")

    // DAOs
    val emf = Persistence.createEntityManagerFactory("steam2-client")
    val jeuVideoDAO = JeuVideoDAO(emf)
    val commentaireDAO = CommentaireDAO(emf)
    val joueurDAO = JoueurDAO(emf)
    val jeuJoueurDAO = JeuJoueurDAO(emf)
    val incidentDAO = IncidentDAO(emf)
    val genreDAO = GenreDAO(emf)

    // Récupération du paramétrage Kafka
    val propsJeuxKafka = Properties()
    val inputStreamCommonJeux = Thread.currentThread().contextClassLoader.getResourceAsStream("kafka.properties")
    val inputStreamJeux = Thread.currentThread().contextClassLoader.getResourceAsStream("kafka/jeux.properties")
    propsJeuxKafka.load(inputStreamCommonJeux)
    propsJeuxKafka.load(inputStreamJeux)
    val topicJeux = propsJeuxKafka.getProperty("topic.name")


    // Kafka
    val consumerJeux = KafkaConsumer<String, GenericRecord>(propsJeuxKafka)
    consumerJeux.subscribe(listOf(topicJeux))

    val propsCommentairesKafka = Properties()
    val inputStreamCommonCommentaires = Thread.currentThread().contextClassLoader.getResourceAsStream("kafka.properties")
    val inputStreamCommentaires = Thread.currentThread().contextClassLoader.getResourceAsStream("kafka/commentaires.properties")
    propsCommentairesKafka.load(inputStreamCommonCommentaires)
    propsCommentairesKafka.load(inputStreamCommentaires)
    val topicCommentaires = propsCommentairesKafka.getProperty("topic.name")

    val producerCommentaires = KafkaProducer<String, GenericRecord>(propsCommentairesKafka)
    val serviceEnvoiCommentaires = EnvoiCommentaires(producerCommentaires,topicCommentaires)

    val propsIncidentsKafka = Properties()
    val inputStreamCommonIncidents = Thread.currentThread().contextClassLoader.getResourceAsStream("kafka.properties")
    val inputStreamIncidents = Thread.currentThread().contextClassLoader.getResourceAsStream("kafka/incidents.properties")
    propsIncidentsKafka.load(inputStreamCommonIncidents)
    propsIncidentsKafka.load(inputStreamIncidents)
    val topicIncidents = propsIncidentsKafka.getProperty("topic.name")

    val producerIncidents = KafkaProducer<String, GenericRecord>(propsIncidentsKafka)
    val serviceEnvoiIncidents = EnvoiIncidents(producerIncidents,topicIncidents)

    // services de récupération Kafka
    val serviceRecupJeu = RecupJeu(consumerJeux, jeuVideoDAO, genreDAO)
    val serviceScopeJeu = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val jobServiceJeu = serviceScopeJeu.launch(Dispatchers.IO){
        log.info("lancement du service de récupération des jeux")
        serviceRecupJeu.launch()
    }

    // préparation de l'interface
    val terminal = DefaultTerminalFactory().setTerminalEmulatorTitle("Steam2 - Joueur").createTerminal()
    val screen = TerminalScreen(terminal)
    screen.startScreen()
    val gui = MultiWindowTextGUI(screen)
    val menus = Menus(gui,joueurDAO,jeuVideoDAO, jeuJoueurDAO, commentaireDAO, incidentDAO, serviceEnvoiCommentaires, serviceEnvoiIncidents)
    log.info("L'application client est prête")

    // lancement de l'interface
    val joueur : Joueur? = menus.login();

    if (joueur == null){
        log.error("Joueur est null")
    } else {
        log.info("Le joueur ${joueur.username} s'est connecté")
        menus.mainMenu(joueur)
    }

    // fin du programme
    log.info("Fermeture de l'application client")
    //hibernate
    emf.close()
    AbandonedConnectionCleanupThread.checkedShutdown()
    //kafka
    producerCommentaires.flush()
    producerCommentaires.close()
    producerIncidents.flush()
    producerIncidents.close()
    //fenêtre
    screen.close()

    // services
    serviceRecupJeu.stop()
    log.info("Attente de la fermeture du service de récupération des jeux...")
    jobServiceJeu.join()
    serviceScopeJeu.cancel()

    log.info("Merci d'avoir utilisé notre application !")
}