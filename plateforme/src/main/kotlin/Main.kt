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
import org.slf4j.LoggerFactory
import org.steam2.plateforme.plateforme.application.RecupererJeuxVideos
import org.steam2.plateforme.plateforme.application.PlateformMenus
import java.util.Properties

private val log = LoggerFactory.getLogger("Main")

fun main() = runBlocking {
    log.info("Démarrage de l'application de la plateforme")

    //DAOS
    val emf = Persistence.createEntityManagerFactory("steam2-plateforme")
    val editeurDAO = EditeurDAO(emf)
    val jeuVideoDAO = JeuVideoDAO(emf)
    val genreDAO = GenreDAO(emf)
    val commentaireDAO = CommentaireDAO(emf)

    //paramètres Kafka
    val propsJeux = Properties()
    val inputStreamCommonJeux = Thread.currentThread().contextClassLoader.getResourceAsStream("kafka/common.properties")
    val inputStreamJeux = Thread.currentThread().contextClassLoader.getResourceAsStream("kafka/jeux.properties")
    propsJeux.load(inputStreamCommonJeux)
    propsJeux.load(inputStreamJeux)
    val topicJeux = propsJeux.getProperty("topic.name")


    //Kafka
    val consumerJeux = KafkaConsumer<String, GenericRecord>(propsJeux)
    consumerJeux.subscribe(listOf(topicJeux))


    log.info("L'application Plateforme est prête")

    //récupérer les infos en arrière plan avec Co Routines Kotlin

    //JV
    val serviceScopeJeux = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val serviceRecuperationJeux = RecupererJeuxVideos(consumerJeux, jeuVideoDAO, editeurDAO, genreDAO)

    val jobServiceJeux = serviceScopeJeux.launch(Dispatchers.IO) {
        serviceRecuperationJeux.launch()
    }

    // Préparation de l'interface (fenêtre)
    val terminal: Terminal = DefaultTerminalFactory().setTerminalEmulatorTitle("Steam2 - Plateforme").createTerminal()
    val screen = TerminalScreen(terminal)
    screen.startScreen()

    log.info("Interface préparée")

    // Préparation du menu principal
    val gui = MultiWindowTextGUI(screen)
    val menus = PlateformMenus(gui, editeurDAO, jeuVideoDAO, genreDAO, commentaireDAO, serviceScopeJeux, serviceRecuperationJeux)

    log.info("Menu principal préparée")


    // Lancement de l'interface
    val plateforme = menus.mainMenu()

    log.info("Interface lancéd")

    //Fin de programme
    log.info("Fermeture de l'application Plateforme")

    //hibernate
    emf.close()
    AbandonedConnectionCleanupThread.checkedShutdown()
    //kafka
    //fenetre
    screen.close()

    //services
    serviceRecuperationJeux.stop()
    log.info("Attente de la fermeture des services de récupération des jeux... " +
            "(${RecupererJeuxVideos.DELAI_ATTENTE * 2 / 1000} secondes max)")
    jobServiceJeux.join()
    serviceScopeJeux.cancel()

    log.info("Merci d'avoir utilisé notre application !")
}