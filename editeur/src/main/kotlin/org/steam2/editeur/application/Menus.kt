package org.steam2.editeur.application

import com.google.common.hash.Hashing
import com.googlecode.lanterna.TerminalSize
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
import org.steam2.editeur.daos.IncidentDAO
import org.steam2.editeur.daos.JeuVideoDAO
import org.steam2.editeur.daos.VersionDAO
import org.steam2.editeur.entites.Genre
import org.steam2.editeur.entites.JeuVideo
import org.steam2.editeur.entites.VersionJeu
import org.steam2.editeur.entities.type.Plateforme
import org.steam2.editeur.entities.type.TypeModificationPatch
import org.steam2.editeur.exceptions.LoginException
import java.nio.charset.StandardCharsets
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import java.util.Properties

class Menus(private val gui: MultiWindowTextGUI,
            private val editeurDAO: EditeurDAO,
            private val commentaireDAO: CommentaireDAO,
            private val incidentDAO: IncidentDAO,
            private val jeuVideoDAO: JeuVideoDAO,
            private val genreDAO: GenreDAO,
            private val versionDAO: VersionDAO
) {

    private val log = LoggerFactory.getLogger(Menus::class.java)

    private var editeurCourant: Editeur? = null

    fun mainMenu(editeur: Editeur) {
        var quitter = false;

        this.editeurCourant = editeur

        while(!quitter) {
            ActionListDialogBuilder()
                .setTitle("Menu principal - $editeurCourant")
                .setDescription("Choisissez une action :")
                .setCanCancel(false)
                .addAction("Publier un jeu") {
                    menuPublicationJeu()
                }
                .addAction("Publier un patch") {
                    menuPublicationPatch()
                }
                .addAction("Publier un DLC") {
                    MessageDialog.showMessageDialog(gui, "Erreur", "Option non disponible pour le moment.")
                }
                .addAction("Consulter les commentaires utilisateurs") {
                    menuConsultationCommentaires()
                }
                .addAction("Consulter les rapports d'incidents") {
                    menuConsultationIncidents()
                }
                .addAction("Quitter l'application") {
                    quitter = true
                }
                .build()
                .showDialog(gui)
        }
    }

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
        val btnOK = Button("OK") {
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
        }
        panel.addComponent(btnOK)

        // Remplissage automatique des champs pendant le développement
        val props = Properties()
        val inputStream = this::class.java.classLoader.getResourceAsStream("steam2.properties")
        if (inputStream != null) {
            props.load(inputStream)
        }
        val environment = props.getProperty("environment")
        if(environment == "DEV") {
            panel.addComponent(EmptySpace())
            val btnAutoFill = Button("DEV : auto-fill") {
                txtLogin.text = "remiCorp"
                txtPassword.text = "motdepasse"
                btnOK.takeFocus()
            }
            panel.addComponent(btnAutoFill)
            btnAutoFill.takeFocus()
        }

        window.component = panel
        window.setHints(listOf(Window.Hint.CENTERED))

        gui.addWindowAndWait(window)
        return editeurConnecte
    }

    fun menuConsultationCommentaires(page: Int = 0) {
        val pageSize = 10
        val commentaires = commentaireDAO.commentaires
        var totalPages = (commentaires.size + pageSize - 1) / pageSize
        if(totalPages == 0) totalPages = 1

        val builder = ActionListDialogBuilder()
            .setTitle("Commentaires (Page ${page + 1}/$totalPages)")

        // on determine quels commentaires sont affichés sur la page actuelle
        val startIndex = page * pageSize
        val pageItems = commentaires.drop(startIndex).take(pageSize)

        // bouton precedent
        if (page > 0) {
            builder.addAction("<<< PAGE PRÉCÉDENTE") { menuConsultationCommentaires(page - 1) }
        }

        // Mise en forme de la date
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
            .withLocale(Locale.FRENCH)

        // contenu
        pageItems.forEach { com ->
            // date au format jj/mm/aaaa
            val dateAffichee = com.date.format(formatter)

            // Si le commentaire est long, on le coupe
            val commentaireAffiche = if (com.commentaire.length > 15) {
                "${com.commentaire.take(15)}…"
            } else {
                com.commentaire
            }
            builder.addAction("${com.jeu} (${dateAffichee}) : ${commentaireAffiche}") {
                MessageDialog.showMessageDialog(gui, "Détail", com.commentaire)
                menuConsultationCommentaires(page) // On relance la page après lecture
            }
        }
        if (commentaires.isEmpty()) {
            builder.setDescription("Aucun commentaire en base.")
        }

        // bouton suivant
        if (startIndex + pageSize < commentaires.size) {
            builder.addAction("PAGE SUIVANTE >>>") { menuConsultationCommentaires(page + 1) }
        }

        builder.build().showDialog(gui)
    }

    fun menuConsultationIncidents(page: Int = 0) {
        val pageSize = 10
        val incidents = incidentDAO.incidents
        var totalPages = (incidents.size + pageSize - 1) / pageSize
        if(totalPages == 0) totalPages = 1

        val builder = ActionListDialogBuilder()
            .setTitle("Rapports d'incidents (Page ${page + 1}/$totalPages)")

        // on determine quels incidents sont affichés sur la page actuelle
        val startIndex = page * pageSize
        val pageItems = incidents.drop(startIndex).take(pageSize)

        // bouton precedent
        if (page > 0) {
            builder.addAction("<<< PAGE PRÉCÉDENTE") { menuConsultationIncidents(page - 1) }
        }


        // Mise en forme de la date
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
            .withLocale(Locale.FRENCH)

        // contenu
        pageItems.forEach { inc ->
            val dateAffichee = inc.date.format(formatter)
            builder.addAction("${dateAffichee} : ${inc.jeu}") {
                MessageDialog.showMessageDialog(gui, "Détails", inc.details)
                menuConsultationIncidents(page) // On relance la page après lecture
            }
        }
        if (incidents.isEmpty()) {
            builder.setDescription("Aucun rapport d'incident en base.")
        }

        // bouton suivant
        if (startIndex + pageSize < incidents.size) {
            builder.addAction("PAGE SUIVANTE >>>") { menuConsultationIncidents(page + 1) }
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
                    editeur = editeurCourant
                    plateforme = cbPlateforme.selectedItem
                    genres = listGenres.checkedItems
                }

                try {
                    jeuVideoDAO.persister(nouveauJeu)

                    val version = VersionJeu().apply {
                        jeu = nouveauJeu
                        commentaireEditeur = ""
                        generation = 1
                        revision = 0
                        correction = 0
                    }

                    versionDAO.persister(version);

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

    fun menuPublicationPatch() {
        val window = BasicWindow("Publier un nouveau patch")
        window.setHints(listOf(Window.Hint.CENTERED))

        // Label | Input
        val panel = Panel(GridLayout(2))

        // jeu concerné
        val listeJeux = jeuVideoDAO.allJeuxVideos
        if (listeJeux.isEmpty()) {
            MessageDialog.showMessageDialog(gui, "Erreur", "Aucun jeu n'est enregistré. Impossible de publier un patch.")
            return
        }
        panel.addComponent(Label("Jeu :"))
        val cbJeux = ComboBox<JeuVideo>(listeJeux)
        panel.addComponent(cbJeux)

        // commentaire
        panel.addComponent(Label("Commentaire :"))
        val txtCommentaire = TextBox().addTo(panel)

        // modifications
        panel.addComponent(Label("Modifications :"))
        val modifsContainer = Panel(LinearLayout(Direction.VERTICAL))
        val listeModifsEntrees = mutableListOf<Pair<ComboBox<TypeModificationPatch>, TextBox>>()

        // On crée le bouton d'ajout
        val btnAjouter = Button("[+] Ajouter une modification") {
            val ligne = Panel(LinearLayout(Direction.HORIZONTAL))
            val cbType = ComboBox<TypeModificationPatch>(*TypeModificationPatch.entries.toTypedArray())

            // taille fixe pour pouvoir ecrire
            val txtDesc = TextBox(TerminalSize(30, 1))

            val btnSuppr = Button("X") {
                modifsContainer.removeComponent(ligne)
                listeModifsEntrees.removeIf { it.first == cbType }
                cbJeux.takeFocus()
            }.setPreferredSize(TerminalSize(3, 1))

            ligne.addComponent(cbType)
            ligne.addComponent(txtDesc)
            ligne.addComponent(btnSuppr)

            // On ajoute la ligne au container
            modifsContainer.addComponent(ligne)
            listeModifsEntrees.add(cbType to txtDesc)
        }

        // On ajoute d'abord le container puis le bouton
        val zoneModifs = Panel(LinearLayout(Direction.VERTICAL))
        zoneModifs.addComponent(modifsContainer)
        zoneModifs.addComponent(btnAjouter)

        panel.addComponent(zoneModifs)

        // vide pour alignement
        panel.addComponent(EmptySpace())

        val btnPanel = Panel(LinearLayout(Direction.HORIZONTAL))

        val btnValider = Button("Enregistrer") {
            val modifications: List<Pair<TypeModificationPatch, String>> = listeModifsEntrees.map { (comboBox, textBox) ->
                val type = comboBox.selectedItem
                val description = textBox.text
                Pair(type, description)
            }

            var oneIsEmpty = false

            for (modification in modifications) {
                if (modification.second.isBlank()) {
                    oneIsEmpty = true
                }
            }

            if(oneIsEmpty) {
                MessageDialog.showMessageDialog(gui, "Erreur", "Toutes les descriptions sont obligatoires")
            } else if(modifications.isEmpty()) {
                MessageDialog.showMessageDialog(gui, "Erreur", "Merci de renseigner des modifications")
            } else if(txtCommentaire.text.isBlank()) {
                MessageDialog.showMessageDialog(gui, "Erreur", "Merci de renseigner un commentaire")
            } else {
                try {
                    versionDAO.publierPatch(cbJeux.selectedItem, modifications, txtCommentaire.text)
                    MessageDialog.showMessageDialog(gui, "Succès", "Patch publié")
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
