package org.steam2.plateforme

import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal
import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread
import org.steam2.plateforme.daos.CommentaireDAO
import org.steam2.plateforme.daos.EditeurDAO
import org.steam2.plateforme.daos.GenreDAO
import org.steam2.plateforme.daos.JeuVideoDAO
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
import org.steam2.plateforme.daos.IncidentDAO
import org.steam2.plateforme.daos.JoueurDAO
import org.steam2.plateforme.plateforme.application.EnvoiJoueur
import org.steam2.plateforme.daos.VersionJeuDAO
import org.steam2.plateforme.plateforme.application.RecupererJeuxVideos
import org.steam2.plateforme.plateforme.application.PlateformMenus
import org.steam2.plateforme.plateforme.application.TransfererCommentaire
import org.steam2.plateforme.plateforme.application.TransfererIncidents
import java.util.Properties

private val log = LoggerFactory.getLogger("Main")

fun main() = runBlocking {
    log.info("Démarrage de l'application de la plateforme")

    // Créations des DAO
    val emf = Persistence.createEntityManagerFactory("steam2-plateforme")
    val editeurDAO = EditeurDAO(emf)
    val jeuVideoDAO = JeuVideoDAO(emf)
    val genreDAO = GenreDAO(emf)
    val commentaireDAO = CommentaireDAO(emf)
    val incidentDAO = IncidentDAO(emf)
    val joueurDAO = JoueurDAO(emf)
    val versionJeuDAO = VersionJeuDAO(emf)

    // —————— Paramètres Kafka ——————
    // ——— Jeux ———
    val propsJeux = Properties()
    val inputStreamCommonJeux = Thread.currentThread().contextClassLoader.getResourceAsStream("kafka/common.properties")
    val inputStreamJeux = Thread.currentThread().contextClassLoader.getResourceAsStream("kafka/jeux.properties")

    propsJeux.load(inputStreamCommonJeux)
    propsJeux.load(inputStreamJeux)

    val topicJeux = propsJeux.getProperty("topic.name")

    // ——— Joueurs ———

    // Définition du topic à utiliser
    val propsJoueur = Properties()
    val inputStreamCommonJoueurs = Thread.currentThread().contextClassLoader.getResourceAsStream("kafka/common.properties")
    val inputStreamJoueurs = Thread.currentThread().contextClassLoader.getResourceAsStream("kafka/joueur.properties")
    propsJoueur.load(inputStreamCommonJoueurs)
    propsJoueur.load(inputStreamJoueurs)
    val topicJoueur = propsJoueur.getProperty("topic.name")


    // ———————————————————————————————

    val propsIncidents = Properties()
    val propsRecevoirIncidents = Properties()
    val inputStreamCommonIncidents = Thread.currentThread().contextClassLoader.getResourceAsStream("kafka/common.properties")
    val inputStreamRecevoirIncidents = Thread.currentThread().contextClassLoader.getResourceAsStream("kafka/incidents.properties")
    propsRecevoirIncidents.load(inputStreamCommonIncidents)
    propsRecevoirIncidents.load(inputStreamRecevoirIncidents)
    val topicRecevoirIncidents = propsRecevoirIncidents.getProperty("topic.name")

    val propsEnvoyerIncidents = Properties()
    val inputStreamCommonEnvoyerIncidents = Thread.currentThread().contextClassLoader.getResourceAsStream("kafka/common.properties")
    val inputStreamEnvoyerIncidents = Thread.currentThread().contextClassLoader.getResourceAsStream("kafka/incidents-to-editeur.properties")
    propsEnvoyerIncidents.load(inputStreamCommonEnvoyerIncidents)
    propsEnvoyerIncidents.load(inputStreamEnvoyerIncidents)
    val topicEnvoyerIncidents = propsEnvoyerIncidents.getProperty("topic.name")



    val propsRecevoirCommentaires = Properties()
    val inputStreamCommonCommentaires = Thread.currentThread().contextClassLoader.getResourceAsStream("kafka/common.properties")
    val inputStreamRecevoirCommentaires = Thread.currentThread().contextClassLoader.getResourceAsStream("kafka/commentaires.properties")
    propsRecevoirCommentaires.load(inputStreamCommonCommentaires)
    propsRecevoirCommentaires.load(inputStreamRecevoirCommentaires)
    val topicRecevoirCommentaires = propsRecevoirCommentaires.getProperty("topic.name")

    val propsEnvoyerCommentaires = Properties()
    val inputStreamCommonEnvoyerCommentaires = Thread.currentThread().contextClassLoader.getResourceAsStream("kafka/common.properties")
    val inputStreamEnvoyerCommentaires = Thread.currentThread().contextClassLoader.getResourceAsStream("kafka/commentaires-to-editeur.properties")
    propsEnvoyerCommentaires.load(inputStreamCommonEnvoyerCommentaires)
    propsEnvoyerCommentaires.load(inputStreamEnvoyerCommentaires)
    val topicEnvoyerCommentaire = propsEnvoyerCommentaires.getProperty("topic.name")


    // —————— Kafka ——————
    // ——— Consumer ———
    val consumerJeux = KafkaConsumer<String, GenericRecord>(propsJeux)
    consumerJeux.subscribe(listOf(topicJeux))

    val consumerIncidents = KafkaConsumer<String, GenericRecord>(propsRecevoirIncidents)
    consumerIncidents.subscribe(listOf(topicRecevoirIncidents))

    val consumerCommentaires = KafkaConsumer<String, GenericRecord>(propsRecevoirCommentaires)
    consumerCommentaires.subscribe(listOf(topicRecevoirCommentaires))

    // ——— Producer ———
    val producerJoueur = KafkaProducer<String, GenericRecord>(propsJoueur)
    val serviceEnvoiJoueur = EnvoiJoueur(producerJoueur, topicJoueur)

    val producerIncidents = KafkaProducer<String, GenericRecord>(propsEnvoyerIncidents)

    val producerCommentaires = KafkaProducer<String, GenericRecord>(propsEnvoyerCommentaires)

    log.info("L'application Plateforme est prête")

    // Récupérer les infos en arrière plan avec Co Routines Kotlin

    // Utilisation de Coroutine et de Kafka
    val serviceScopeJeux = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val serviceRecuperationJeux = RecupererJeuxVideos(consumerJeux, jeuVideoDAO, editeurDAO, genreDAO, versionJeuDAO)

    val serviceScopeIncidents = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val serviceTransfereIncidents = TransfererIncidents(consumerIncidents, producerIncidents, topicEnvoyerIncidents, incidentDAO, jeuVideoDAO)

    val serviceScopeCommentaires = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val serviceTransfereCommentaires = TransfererCommentaire(consumerCommentaires, producerCommentaires, topicEnvoyerCommentaire, commentaireDAO, jeuVideoDAO,joueurDAO)

    // --- Lancement des service ---
    // Jeux
    val jobServiceJeux = serviceScopeJeux.launch(Dispatchers.IO) {
        serviceRecuperationJeux.launch()
    }

    // Incidents
    val jobServiceIncidents = serviceScopeIncidents.launch(Dispatchers.IO) {
        serviceTransfereIncidents.launch()
    }

    //Commentaires
    val jobServiceCommentaires = serviceScopeCommentaires.launch(Dispatchers.IO) {
        serviceTransfereCommentaires.launch()
    }

    // Préparation de l'interface (fenêtre)
    val terminal: Terminal = DefaultTerminalFactory().setTerminalEmulatorTitle("Steam2 - Plateforme").createTerminal()
    val screen = TerminalScreen(terminal)
    screen.startScreen()

    log.info("Interface préparée")

    // Préparation du menu principal
    val gui = MultiWindowTextGUI(screen)
    val menus = PlateformMenus(gui, editeurDAO, jeuVideoDAO, joueurDAO,genreDAO, commentaireDAO, serviceScopeJeux,
        serviceRecuperationJeux, serviceEnvoiJoueur)

    log.info("Menu principal préparée")


    // Lancement de l'interface
    val plateforme = menus.mainMenu()

    log.info("Interface lancé")

    //Fin de programme
    log.info("Fermeture de l'application Plateforme")

    //hibernate
    emf.close()
    AbandonedConnectionCleanupThread.checkedShutdown()

    //kafka
    producerIncidents.flush()
    producerIncidents.close()

    producerCommentaires.flush()
    producerCommentaires.close()

    // fenêtre
    screen.close()

    // Arrêt des Services
    serviceRecuperationJeux.stop()
    serviceRecuperationJeux.stop()
    serviceTransfereIncidents.stop()
    serviceTransfereCommentaires.stop()

    log.info("Attente de la fermeture des services de récupération des jeux... " +
            "(${RecupererJeuxVideos.DELAI_ATTENTE * 2 / 1000} secondes max)")
    jobServiceJeux.join()
    serviceScopeJeux.cancel()

    log.info("Attente de la fermeture des services de transfert des incidents... " +
            "(${TransfererIncidents.DELAI_ATTENTE * 2 / 1000} secondes max)")

    jobServiceIncidents.join()
    serviceScopeIncidents.cancel()

    log.info("Attente de la fermeture des services de transfert des commentaires... " +
            "(${TransfererCommentaire.DELAI_ATTENTE * 2 / 1000} secondes max)")
    jobServiceCommentaires.join()
    serviceScopeCommentaires.cancel()


    log.info("Merci d'avoir utilisé notre application !")
}