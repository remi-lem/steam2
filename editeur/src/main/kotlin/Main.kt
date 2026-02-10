package org.steam2.editeur

import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import jakarta.persistence.Persistence
import org.steam2.editeur.application.MenuPrincipal
import org.steam2.entites.EditeurDAO

fun main() {
    println("Bienvenue dans l'application de l'éditeur !")

    // DAOs
    val emf = Persistence.createEntityManagerFactory("steam2-editeur")
    val editeurDAO = EditeurDAO(emf)

    // Préparation de l'interface
    val terminal = DefaultTerminalFactory().setTerminalEmulatorTitle("Steam2").createTerminal()
    val screen = TerminalScreen(terminal)
    screen.startScreen()

    // Lancement de l'interface
    MenuPrincipal(editeurDAO).afficher(screen)
}