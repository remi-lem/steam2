package org.steam2.editeur.application

import com.google.common.hash.Hashing
import com.googlecode.lanterna.gui2.BasicWindow
import com.googlecode.lanterna.gui2.Button
import com.googlecode.lanterna.gui2.CheckBoxList
import com.googlecode.lanterna.gui2.ComboBox
import com.googlecode.lanterna.gui2.Direction
import com.googlecode.lanterna.gui2.EmptySpace
import com.googlecode.lanterna.gui2.GridLayout
import com.googlecode.lanterna.gui2.Label
import com.googlecode.lanterna.gui2.LinearLayout
import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.gui2.TextBox
import com.googlecode.lanterna.gui2.Window
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder
import com.googlecode.lanterna.gui2.dialogs.MessageDialog
import com.googlecode.lanterna.gui2.dialogs.TextInputDialog
import org.slf4j.LoggerFactory
import org.steam2.editeur.daos.CommentaireDAO
import org.steam2.editeur.entites.Editeur
import org.steam2.editeur.daos.EditeurDAO
import org.steam2.editeur.daos.GenreDAO
import org.steam2.editeur.daos.JeuVideoDAO
import org.steam2.editeur.entites.Genre
import org.steam2.editeur.entites.JeuVideo
import org.steam2.editeur.entities.type.Plateforme
import org.steam2.editeur.exceptions.LoginException
import java.nio.charset.StandardCharsets

class Menus(private val gui: MultiWindowTextGUI,
            private val editeurDAO: EditeurDAO,
            private val commentaireDAO: CommentaireDAO,
            private val jeuVideoDAO: JeuVideoDAO,
            private val genreDAO: GenreDAO) {

    private val log = LoggerFactory.getLogger(Menus::class.java)

    fun login() : Editeur? {
        var editeurConnecte: Editeur? = null

        val panel = Panel().apply { layoutManager = GridLayout(2) }

        panel.addComponent(Label("Login:"))
        val txtLogin = TextBox()
        panel.addComponent(txtLogin)

        panel.addComponent(Label("Mot de passe:"))
        val txtPassword = TextBox().setMask('*')
        panel.addComponent(txtPassword)

        panel.addComponent(EmptySpace())

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

    fun mainMenu() {
        var quitter = false;

        while(!quitter) {
            ActionListDialogBuilder()
                .setTitle("Menu principal")
                .setDescription("Choisissez une action :")
                .setCanCancel(false)
                .addAction("Publier un jeu") {
                    menuPublicationJeu()
                }
                .addAction("Publier un patch") {
                    MessageDialog.showMessageDialog(gui, "Erreur", "Option non disponible pour le moment.")
                }
                .addAction("Publier un DLC") {
                    MessageDialog.showMessageDialog(gui, "Erreur", "Option non disponible pour le moment.")
                }
                .addAction("Consulter les commentaires utilisateurs") {
                    menuConsultationCommentaires()
                }
                .addAction("Quitter l'application") {
                    quitter = true
                }
                .build()
                .showDialog(gui)
        }
    }

    fun menuConsultationCommentaires() {
        val commentaires = commentaireDAO.recentCommentaires

        val builder = ActionListDialogBuilder()
            .setTitle("Derniers Commentaires")

        if (commentaires.isEmpty()) {
            builder.setDescription("Aucun commentaire en base.")
        } else {
            builder.setDescription("10 derniers commentaires :")
            commentaires.forEach { com ->
                builder.addAction("${com.date} : ${com.commentaire.take(15)}...") {
                    MessageDialog.showMessageDialog(gui, "Détail", com.commentaire)
                }
            }
        }

        builder.build().showDialog(gui)
    }

    fun menuPublicationJeu() {
        val window = BasicWindow("Publier un nouveau jeu")
        window.setHints(listOf(Window.Hint.CENTERED))

        // Label | Input
        val panel = Panel(GridLayout(2))

        panel.addComponent(Label("Nom du jeu :"))
        val txtNom = TextBox().addTo(panel)

        panel.addComponent(Label("Plateforme :"))
        val cbPlateforme = ComboBox<Plateforme>(*Plateforme.entries.toTypedArray()).addTo(panel)

        panel.addComponent(Label("Genres :"))
        val genreSubPanel = Panel(LinearLayout(Direction.VERTICAL))

        val listGenres = CheckBoxList<Genre>()
        genreDAO.genres.forEach { listGenres.addItem(it) }

        genreSubPanel.addComponent(listGenres)

        // bouton d'ajout d'un nouveau genre
        genreSubPanel.addComponent(Button("[+] Ajouter un genre") {
            val nouveauNom = TextInputDialog.showDialog(gui, "Nouveau Genre", "Nom :", "")
            if (!nouveauNom.isNullOrBlank()) {
                try {
                    val g = Genre().apply { nom = nouveauNom }
                    genreDAO.persister(g)
                    listGenres.addItem(g, true)
                } catch (e: Exception) {
                    log.error("Impossible de créer le genre", e)
                    MessageDialog.showMessageDialog(gui, "Erreur", "Impossible de créer le genre")
                }
            }
        })

        // On ajoute le sous-panel entier dans la cellule de droite du GridLayout
        panel.addComponent(genreSubPanel)

        // vide pour alignement
        panel.addComponent(EmptySpace())

        val btnPanel = Panel(LinearLayout(Direction.HORIZONTAL))

        val btnValider = Button("Enregistrer") {
            if (txtNom.text.isBlank()) {
                MessageDialog.showMessageDialog(gui, "Erreur", "Le nom est obligatoire")
            } else {
                val nouveauJeu = JeuVideo().apply {
                    nom = txtNom.text
                    plateforme = cbPlateforme.selectedItem
                    genres = listGenres.checkedItems
                }

                try {
                    jeuVideoDAO.persister(nouveauJeu)
                    MessageDialog.showMessageDialog(gui, "Succès", "Jeu publié")
                    window.close()
                } catch (e: Exception) {
                    MessageDialog.showMessageDialog(gui, "Erreur SQL", e.message ?: "Erreur inconnue")
                }
            }
        }

        val btnAnnuler = Button("Annuler") { window.close() }

        btnPanel.addComponent(btnValider)
        btnPanel.addComponent(btnAnnuler)
        panel.addComponent(btnPanel)

        window.component = panel
        gui.addWindowAndWait(window)
    }
}
