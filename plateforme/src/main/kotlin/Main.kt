package org.steam2.plateforme

import org.steam2.plateforme.application.PlateformMenus

import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal
import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread
import jakarta.persistence.Persistence

import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("Main")

fun main() = runBlocking {
    log.info("Démarrage de l'application client")

    // val emf = Persistence.createEntityManagerFactory("steam2-plateforme")


    // Préparation de l'interface (fenêtre)
    val terminal: Terminal = DefaultTerminalFactory().setTerminalEmulatorTitle("Steam2 - Plateforme").createTerminal()
    val screen = TerminalScreen(terminal)
    screen.startScreen()

    log.info("Interface préparée")

    // Préparation du menu principal
    val gui = MultiWindowTextGUI(screen)
    val menus = PlateformMenus(gui, terminal)

    log.info("Menu principal préparée")


    // Lancement de l'interface
    val editeur = menus.mainMenu()

    log.info("Interface lancéd")


    // Fin du programme : On ferme la fenêtre et on arrête les services de récupération

    log.info("On quitte l'application")
    //hibernate
    // emf.close()
    // AbandonedConnectionCleanupThread.checkedShutdown()

    //kafka
    // producerCommentaires.flush()
    // producerCommentaires.close()
    //fenetre
    screen.close()

    //services
    //serviceRecuperationCommentaires.stop()
    //serviceRecuperationIncidents.stop()
    screen.refresh()


    log.info("L'application est prête")
}