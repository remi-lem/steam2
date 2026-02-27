package org.steam2.editeur.application

import com.google.common.hash.Hashing
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder
import com.googlecode.lanterna.gui2.dialogs.MessageDialog
import com.googlecode.lanterna.gui2.dialogs.TextInputDialog
import org.slf4j.LoggerFactory
import org.steam2.editeur.daos.*
import org.steam2.editeur.entites.Editeur
import org.steam2.editeur.entites.Genre
import org.steam2.editeur.entites.JeuVideo
import org.steam2.editeur.entites.VersionJeu
import org.steam2.editeur.entites.type.Plateforme
import org.steam2.editeur.entites.type.TypeModificationPatch
import org.steam2.editeur.exceptions.LoginException
import java.math.BigDecimal
import java.nio.charset.StandardCharsets
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import java.util.regex.Pattern

/**
 * Menus de l'application editeur
 * @author remi
 */
class Menus(private val gui: MultiWindowTextGUI,
            private val editeurDAO: EditeurDAO,
            private val commentaireDAO: CommentaireDAO,
            private val incidentDAO: IncidentDAO,
            private val jeuVideoDAO: JeuVideoDAO,
            private val genreDAO: GenreDAO,
            private val versionDAO: VersionDAO,
            private val serviceEnvoiJeux: EnvoiJeux
) {

    private val log = LoggerFactory.getLogger(Menus::class.java)

    private lateinit var editeurCourant: Editeur

    /**
     * Menu principal de l'application (liste des options)
     * @author remi
     */
    fun mainMenu(editeur: Editeur) {
        var quitter = false;

        this.editeurCourant = editeur

        while(!quitter) {
            try {
                ActionListDialogBuilder()
                    .setTitle("Menu principal - $editeurCourant")
                    .setDescription("Choisissez une action :")
                    .setCanCancel(false)
                    .addAction("Publier un jeu") {
                        menuPublicationJeu(false)
                    }
                    .addAction("Publier un patch") {
                        menuPublicationPatch()
                    }
                    .addAction("Publier un DLC") {
                        menuPublicationJeu(true)
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
            } catch (e: Exception) {
                // On catche et affiche l'erreur au lieu de bloquer l'interface
                log.error("Erreur critique", e)
                MessageDialog.showMessageDialog(gui, "Erreur critique", e.message)
            }
        }
    }

    /**
     * Formulaire d'identification (login et mot de passe)
     * Cette méthode appelle le DAO correspondant pour vérifier les identifiants fournis
     * @return Editeur l'éditeur une fois connecté
     * @author remi
     */
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

    /**
     * Page de consultation des commentaires écrits par les clients
     * @param page : le numéro de la page à consulter
     * @author remi
     */
    fun menuConsultationCommentaires(page: Int = 0) {
        val commentaires = commentaireDAO.getCommentaires(editeurCourant.id)
        menuConsultationGenerique(
            page = page,
            titre = "Commentaires",
            messageVide = "Aucun commentaire en base.",
            items = commentaires,
            itemMapper = { com, fDate, fDateTime ->
                val dateAffichee = com.date.format(fDate)
                val dateTimeAffichee = com.date.format(fDateTime)
                val message = "Date : $dateTimeAffichee\nCommentaire : ${com.commentaire}"

                val commentaireAffiche = if (com.commentaire.length > 15) {
                    "${com.commentaire.take(15)}…"
                } else {
                    com.commentaire
                }
                Pair("${com.jeu} ($dateAffichee) : $commentaireAffiche", message)
            },
            callback = { p -> menuConsultationCommentaires(p) }
        )
    }

    /**
     * Page de consultation des rapports d'incidents générés par les applications clientes
     * @param page : le numéro de la page à consulter
     * @author remi
     */
    fun menuConsultationIncidents(page: Int = 0) {
        val incidents = incidentDAO.getIncidents(editeurCourant.id)
        menuConsultationGenerique(
            page = page,
            titre = "Rapports d'incidents",
            messageVide = "Aucun rapport d'incident en base.",
            items = incidents,
            itemMapper = { inc, fDate, fDateTime ->
                val dateAffichee = inc.date.format(fDate)
                val dateTimeAffichee = inc.date.format(fDateTime)
                val message = "Date : $dateTimeAffichee\nDétails : ${inc.details}"
                Pair("${inc.jeu} ($dateAffichee)", message)
            },
            callback = { p -> menuConsultationIncidents(p) }
        )
    }

    /**
     * Menu générique de consultation des commentaires ou des rapports d'incidents
     * @param page l'index de la page à consulter
     * @param titre le titre de la fenêtre
     * @param messageVide le message affiché si aucune donnée ne peut être affichée
     * @param items les items à afficher (commentaires ou rapports d'incident)
     * @param itemMapper mapper pour récupérer les messages en fonction du contexte (commentaire ou rapport d'incident)
     * @param callback retour à la méthode appelante lors du changement de page ou de la fin de consultation d'un élément
     */
    private fun <T> menuConsultationGenerique(
        page: Int,
        titre: String,
        messageVide: String,
        items: List<T>,
        itemMapper: (T, DateTimeFormatter, DateTimeFormatter) -> Pair<String, String>,
        callback: (Int) -> Unit
    ) {
        val pageSize = 10
        var totalPages = (items.size + pageSize - 1) / pageSize
        if(totalPages == 0) totalPages = 1

        val builder = ActionListDialogBuilder()
            .setTitle("$titre (Page ${page + 1}/$totalPages)")

        // on determine quels items sont affichés sur la page actuelle
        val startIndex = page * pageSize
        val pageItems = items.drop(startIndex).take(pageSize)

        // bouton precedent
        if (page > 0) {
            builder.addAction("<<< PAGE PRÉCÉDENTE") { callback(page - 1) }
        }

        // Mise en forme des dates
        val formatterDate = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
            .withLocale(Locale.FRENCH)
        val formatterDateTime = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.MEDIUM)
            .withLocale(Locale.FRENCH)

        // contenu
        pageItems.forEach { item ->
            val (label, detail) = itemMapper(item, formatterDate, formatterDateTime)
            builder.addAction(label) {
                MessageDialog.showMessageDialog(gui, "Détail", detail)
                callback(page) // On relance la page après lecture
            }
        }
        if (items.isEmpty()) {
            builder.setDescription(messageVide)
        }

        // bouton suivant
        if (startIndex + pageSize < items.size) {
            builder.addAction("PAGE SUIVANTE >>>") { callback(page + 1) }
        }

        builder.build().showDialog(gui)
    }

    /**
     * Page de publication de nouveau jeu ou DLC
     * @param isDlc booléen indiquant si l'on souhaite ajouter un jeu ou un DLC
     * @author remi
     */
    fun menuPublicationJeu(isDlc: Boolean) {
        val label = if(isDlc) { "DLC" } else { "jeu" }

        val window = BasicWindow("Publier un nouveau $label")
        window.setHints(listOf(Window.Hint.CENTERED))

        // Label | Input
        val panel = Panel(GridLayout(2))

        var cbJeux: ComboBox<JeuVideo>? = null
        var cbPlateforme: ComboBox<Plateforme>? = null

        if(isDlc) {
            // Actualisation de la liste des jeux dans l'entité
            editeurCourant = editeurDAO.refresh(editeurCourant)

            // jeu concerné
            val listeJeux = editeurCourant.jeuxPublies
            if (listeJeux.isEmpty()) {
                MessageDialog.showMessageDialog(gui, "Erreur", "Aucun jeu n'est enregistré. Impossible de publier un DLC.")
                return
            }
            panel.addComponent(Label("Jeu :"))
            cbJeux = ComboBox<JeuVideo>(listeJeux)
            panel.addComponent(cbJeux)
        }

        panel.addComponent(Label("Nom du $label :"))
        val txtNom = TextBox().addTo(panel)

        if(!isDlc) {
            panel.addComponent(Label("Plateforme :"))
            cbPlateforme = ComboBox<Plateforme>(*Plateforme.entries.toTypedArray()).addTo(panel)
        }

        panel.addComponent(Label("Prix :"))
        val prixBox = TextBox(TerminalSize(10, 1))
        prixBox.setValidationPattern(Pattern.compile("[0-9]{1,3}([.,][0-9]{0,2})?"))
        panel.addComponent(prixBox)

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
            } else if(prixBox.text.isBlank()) {
                MessageDialog.showMessageDialog(gui, "Erreur", "Le prix est obligatoire")
            } else {
                // jeu parent dans le cas d'un DLC
                val parentSelectionne = if (isDlc) {
                    cbJeux?.selectedItem
                } else {
                    null
                }

                // Platefome séléctionnée pour un jeu, celle du jeu pour un DLC
                val nouvellePlateforme = if (isDlc) {
                    parentSelectionne?.plateforme
                } else {
                    cbPlateforme?.selectedItem
                }

                // vérification de la présence de la plateforme
                if (nouvellePlateforme == null) {
                    MessageDialog.showMessageDialog(gui, "Erreur", "Plateforme non définie")
                    return@Button
                }

                val prixTxt: String = prixBox.text.replace(',', '.')
                val prixFinal = BigDecimal(prixTxt)

                // nouveau jeu/dlc
                val nouveauJeu = JeuVideo().apply {
                    nom = txtNom.text
                    editeur = editeurCourant
                    plateforme = nouvellePlateforme
                    prix = prixFinal
                    genres = listGenres.checkedItems
                    jeuParent = parentSelectionne
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

                    versionDAO.persister(version)

                    // Envoi sur le topic kafka
                    serviceEnvoiJeux.envoyer(nouveauJeu, version)

                    log.info("Le $label ${nouveauJeu.nom} a été publié")
                    MessageDialog.showMessageDialog(gui, "Succès", "$label publié")
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

    /**
     * Page de publication de nouveau patch
     * @author remi
     */
    fun menuPublicationPatch() {
        val window = BasicWindow("Publier un nouveau patch")
        window.setHints(listOf(Window.Hint.CENTERED))

        // Actualisation de la liste des jeux dans l'entité
        editeurCourant = editeurDAO.refresh(editeurCourant)

        // Label | Input
        val panel = Panel(GridLayout(2))

        // jeu concerné
        val listeJeux = editeurCourant.jeuxPublies
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

                    // TODO envoi sur le topic kafka

                    log.info("Un patch a été publié sur le jeu ${cbJeux.selectedItem.nom}")
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
