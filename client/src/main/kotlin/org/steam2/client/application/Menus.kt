package org.steam2.client.application

import com.google.common.hash.Hashing
import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder
import com.googlecode.lanterna.gui2.dialogs.MessageDialog
import org.slf4j.LoggerFactory
import org.steam2.client.daos.JeuVideoDAO
import org.steam2.client.daos.JoueurDAO
import org.steam2.client.entites.Joueur
import org.steam2.client.exceptions.LoginException
import java.nio.charset.StandardCharsets
import java.util.Properties

class Menus (
    private val gui: MultiWindowTextGUI,
    private val joueurDAO: JoueurDAO,
    private val jeuVideoDAO: JeuVideoDAO,

) {
    private val log = LoggerFactory.getLogger(Menus::class.java)

    private lateinit var joueurCourant: Joueur

    /**
     * Menu principal de l'application client
     * @author Nino
     */
    fun mainMenu(joueur: Joueur) {
        var quitter = false;

        this.joueurCourant = joueur;

        while (!quitter){
            try {
                ActionListDialogBuilder()
                    .setTitle("Menu principal $joueurCourant")
                    .setDescription("Choisissez une action")
                    .setCanCancel(false)
                    .addAction ("Quitter l'pplication") {
                        quitter = true
                    }
                    .build()
                    .showDialog(gui)
            } catch (e: Exception) {
                log.error("Erreur critique", e)
                MessageDialog.showMessageDialog(gui, "Erreur critique", e.message)
            }
        }
    }

    fun login() : Joueur? {
        var joueurConnecte: Joueur? = null

        val panel = Panel().apply { layoutManager = GridLayout(2) }

        panel.addComponent(Label("Login:"))
        val txtLogin = TextBox()
        panel.addComponent(txtLogin)

        panel.addComponent(Label("Mot de pass:"))
        val txtPassword = TextBox().setMask('*')
        panel.addComponent(txtPassword)

        panel.addComponent(EmptySpace())

        val window = BasicWindow("Authentification")
        val btnOK = Button("OK") {
            // On vérifie le mot de passe
            val sha256hex = Hashing.sha256()
                .hashString(txtPassword.text, StandardCharsets.UTF_8)
                .toString()
            try {
                val joueur = joueurDAO.identifier(txtLogin.text, sha256hex)
                joueurConnecte = joueur
                window.close()
            } catch (e: LoginException) {
                MessageDialog.showMessageDialog(gui, "Erreur", "Login ou mot de passe incorrect")
            }
        }
        panel.addComponent(btnOK)

        val props = Properties()
        val inputStream = this::class.java.classLoader.getResourceAsStream("steam2.properties")
        if (inputStream != null) {
            props.load(inputStream)
        }

        val environment = props.getProperty("environment")
        if(environment == "DEV") {
            panel.addComponent(EmptySpace())
            val btnAutoFill = Button("DEV : auto-fill") {
                txtLogin.text = "nino"
                txtPassword.text = "test"
                btnOK.takeFocus()
            }
            panel.addComponent(btnAutoFill)
            btnAutoFill.takeFocus()
        }

        window.component = panel
        window.setHints(listOf(Window.Hint.CENTERED))

        gui.addWindowAndWait(window)
        return joueurConnecte;
    }
}