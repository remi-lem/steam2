package org.steam2.editeur.application

import com.google.common.hash.Hashing
import com.googlecode.lanterna.gui2.BasicWindow
import com.googlecode.lanterna.gui2.Button
import com.googlecode.lanterna.gui2.EmptySpace
import com.googlecode.lanterna.gui2.GridLayout
import com.googlecode.lanterna.gui2.Label
import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.gui2.TextBox
import com.googlecode.lanterna.gui2.Window
import com.googlecode.lanterna.gui2.dialogs.MessageDialog
import com.googlecode.lanterna.screen.Screen
import org.steam2.entites.Editeur
import org.steam2.entites.EditeurDAO
import org.steam2.exceptions.LoginException
import java.nio.charset.StandardCharsets

class MenuPrincipal(val editeurDAO: EditeurDAO) {
    fun afficher(screen: Screen) : Editeur? {
        var editeurConnecte: Editeur? = null

        val panel = Panel().apply { layoutManager = GridLayout(2) }
        val gui = MultiWindowTextGUI(screen)

        panel.addComponent(Label("Login:"))
        val txtLogin = TextBox()
        panel.addComponent(txtLogin)

        panel.addComponent(Label("Mot de passe:"))
        val txtPassword = TextBox().setMask('*') // Sécurité des données
        panel.addComponent(txtPassword)

        panel.addComponent(EmptySpace())

        // Bouton OK qui ferme la fenêtre et stocke le résultat
        val window = BasicWindow("Authentification")
        panel.addComponent(Button("OK") {
            // On vérifie le mot de passe
            val sha256hex = Hashing.sha256()
                .hashString(txtPassword.text, StandardCharsets.UTF_8)
                .toString()
            try {
                val editeur = editeurDAO.identifier(txtLogin.text, sha256hex)
                editeurConnecte = editeur
                window.close()
            } catch (e: LoginException) {
                MessageDialog.showMessageDialog(gui, "Erreur", "Login ou mot de passe incorrect")
            }
        })

        window.component = panel
        window.setHints(listOf(Window.Hint.CENTERED))

        gui.addWindowAndWait(window)
        return editeurConnecte
    }
}
