package org.steam2.client.application

import com.google.common.hash.Hashing
import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder
import com.googlecode.lanterna.gui2.dialogs.MessageDialog
import org.slf4j.LoggerFactory
import org.steam2.client.daos.CommentaireDAO
import org.steam2.client.daos.IncidentDAO
import org.steam2.client.daos.JeuJoueurDAO
import org.steam2.client.daos.JeuVideoDAO
import org.steam2.client.daos.JoueurDAO
import org.steam2.client.entites.Commentaire
import org.steam2.client.entites.Incident
import org.steam2.client.entites.JeuJoueur
import org.steam2.client.entites.JeuVideo
import org.steam2.client.entites.Joueur
import org.steam2.client.exceptions.LoginException
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import java.util.Properties

class Menus (
    private val gui: MultiWindowTextGUI,
    private val joueurDAO: JoueurDAO,
    private val jeuVideoDAO: JeuVideoDAO,
    private val jeuJoueurDAO: JeuJoueurDAO,
    private val commentaireDAO: CommentaireDAO,
    private val incidentDAO: IncidentDAO,
    private val envoiCommentaires: EnvoiCommentaires,
    private val envoiIncidents: EnvoiIncidents
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
                    .addAction("Consulter les jeux en magasin") { menuMagasin() }
                    .addAction ("Quitter l'application") {
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

    /**
     * Partie du menu pour voir les jeux du magasin
     * @author Nino
     */
    fun menuMagasin(page:Int = 0){
        try {
            val listJeux = jeuVideoDAO.recupListeJeux()
            menuConsultationGeneriqueJeu(
                page = page,
                titre = "Jeux en magasin",
                messageVide = "Aucun jeu en magasin",
                items = listJeux,
                itemMapper = {
                    jeu ->
                    var strPossede = "${jeu.prix_editeur} "
                    if (jeuJoueurDAO.possede(joueurCourant, jeu)) {strPossede = "possédé"};
                    "${jeu.nom} : $strPossede"
                },
                callback = {p -> menuMagasin(p)}
            )

        } catch (e: Exception) {
            TODO("Erreur pour affichage jeux")
        }

    }

    fun menuAfficherDLCs(jeuVideo: JeuVideo, page: Int = 0) {
        try {
            menuConsultationGeneriqueJeu(
                page = page,
                titre = "DLCs pour ${jeuVideo.nom}",
                messageVide = "Aucun DLC disponibles pour ${jeuVideo.nom}",
                items = jeuVideo.dlcs,
                itemMapper = { dlc ->
                    var strPossede = "${dlc.prix_editeur} "
                    if (jeuJoueurDAO.possede(joueurCourant, dlc)) {
                        strPossede = "possédé"
                    };
                    "${dlc.nom} : $strPossede"
                },
                callback = { p -> menuMagasin(p) }
            )
        } catch (e: Exception) {
            TODO("Erreur pour affichage DLCs")
        }
    }

    /**
     * Menu générique de consultation de jeu
     * @param page l'index de la page à consulter
     * @param titre le titre de la fenêtre
     * @param messageVide le message affiché si aucune donnée ne peut être affichée
     * @param items les items à afficher (commentaires ou rapports d'incident)
     * @param itemMapper mapper pour récupérer les messages en fonction du contexte (commentaire ou rapport d'incident)
     * @param callback retour à la méthode appelante lors du changement de page ou de la fin de consultation d'un élément
     * @author Remi
     * @author Nino
     */
    private fun menuConsultationGeneriqueJeu(
        page: Int,
        titre: String,
        messageVide: String,
        items: List<JeuVideo>,
        itemMapper: (JeuVideo) -> String,
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
            val label = itemMapper(item)
            builder.addAction(label) {
                menuAfficherJeu(item)
                callback(page) // On relance la page après lecture
            }
        }
        if (items.isEmpty()) {
            builder.setDescription(messageVide)
        }

        // bouton suivant
        if (startIndex + pageSize < items.size) {
            builder.addAction("PAGE SUIVANTE >>>") { callback(page+1) }
        }

        builder.build().showDialog(gui)
    }

    /**
     * Ouvre une fenètre pour afficher les informations sur un jeu
     * @param jeuVideoConsute le jeu à afficher
     * @author nino
     */
    fun menuAfficherJeu(jeuVideoConsute: JeuVideo){
        val actionsBuilder = ActionListDialogBuilder()

        actionsBuilder.setTitle("Jeu : ${jeuVideoConsute.nom}")

        actionsBuilder.setDescription("Choisicez une actions")

        if (jeuJoueurDAO.possede(joueurCourant,jeuVideoConsute)) {
            actionsBuilder.addAction ("Jouer"){
                TODO("Implementer l'action de jouer")
            }
            actionsBuilder.addAction ("Consulter les DLCs"){
                TODO("Implementer la visualisation des DLCs")
            //menuAfficherDLCs(jeuVideoConsute)
            }
            actionsBuilder.addAction("Envoyer un commentaire") { menuPublierCommentaire(jeuVideoConsute) }
            actionsBuilder.addAction("Envoyer un incident") { menuPublierIncident(jeuVideoConsute) }
        } else if (joueurCourant.solde >= jeuVideoConsute.prix_editeur) {
            actionsBuilder.addAction ("Acheter (prix:${jeuVideoConsute.prix_editeur}"){
                joueurCourant.solde = joueurCourant.solde.subtract(jeuVideoConsute.prix_editeur)
                joueurDAO.merge(joueurCourant);
                val achat = JeuJoueur().apply {
                    jeuVideo = jeuVideoConsute
                    joueur = joueurCourant
                    temps_joue_m = 0
                }
                try {
                    jeuJoueurDAO.persister(achat)
                    log.info("Le joueur ${joueurCourant.nom} a acheté le jeu $jeuVideoConsute")
                } catch (e :Exception){
                    log.error("Erreur en achetant le jeu : "+e.message)
                }
            }
        }

        actionsBuilder.build().showDialog(gui)

    }

    fun menuPublierCommentaire(jeuVideoACommenter: JeuVideo){
        val window = BasicWindow("Publier un commentaire sur ${jeuVideoACommenter.nom}")
        window.setHints(listOf(Window.Hint.CENTERED))

        val panel = Panel(GridLayout(2))

        panel.addComponent(Label("Commentaire : "))
        val txtCommentaire = TextBox().addTo(panel)

        panel.addComponent(EmptySpace())

        val btnPanel = Panel(LinearLayout(Direction.HORIZONTAL))

        val btnValider = Button("Envoyer") {
            if (txtCommentaire.text.isBlank()){
                MessageDialog.showMessageDialog(gui,"Erreur", "Commentaire vide")
            } else {
                // Nouveau Commentaire
                val nouveauCommentaire = Commentaire().apply {
                    commentaire = txtCommentaire.text
                    jeu = jeuVideoACommenter
                    joueur = joueurCourant
                    date = LocalDateTime.now();
                }
                try {
                    commentaireDAO.persister(nouveauCommentaire)
                    envoiCommentaires.envoyer(commentaire = nouveauCommentaire)
                    log.info("${joueurCourant.nom} à envoyé un commentaire sur ${jeuVideoACommenter.nom} : ${txtCommentaire.text}")
                    MessageDialog.showMessageDialog(gui,"Succès", "Commentaire publié sur ${jeuVideoACommenter.nom}")
                    window.close()
                } catch (e: Exception) {
                    MessageDialog.showMessageDialog(gui, "Erreur sql", e.message ?: "Erreur inconnue")
                    log.error(e.stackTrace.toString())
                }
            }
        }

        val btnAnnuler = Button("Annuler") {window.close()}
        btnPanel.addComponent(btnValider)
        btnPanel.addComponent(btnAnnuler)
        panel.addComponent(btnPanel)

        window.component = panel
        gui.addWindowAndWait(window)
    }

    fun menuPublierIncident(jeuConcerne: JeuVideo){
        val window = BasicWindow("Publier un incident sur ${jeuConcerne.nom}")
        window.setHints(listOf(Window.Hint.CENTERED))

        val panel = Panel(GridLayout(2))

        panel.addComponent(Label("Incident : "))
        val txtDetails = TextBox().addTo(panel)

        panel.addComponent(EmptySpace())

        val btnPanel = Panel(LinearLayout(Direction.HORIZONTAL))

        val btnValider = Button("Envoyer") {
            if (txtDetails.text.isBlank()){
                MessageDialog.showMessageDialog(gui,"Erreur", "Details vide")
            } else {
                // Nouveau Commentaire
                val nouvelIncident = Incident().apply {
                    details=txtDetails.text+"\nPlateforme : ${jeuConcerne.plateforme.libelle}"
                    date = LocalDateTime.now()
                    jeu = jeuConcerne
                }
                try {
                    incidentDAO.persister(nouvelIncident)
                    log.info("Envoie de l'incident ${nouvelIncident.jeu.id}")
                    envoiIncidents.envoyer(incident = nouvelIncident)
                    log.info("${joueurCourant.nom} à envoyé un incident sur ${jeuConcerne.nom} : ${txtDetails.text}")
                    MessageDialog.showMessageDialog(gui,"Succès", "Incident publié pour ${jeuConcerne.nom}")
                    window.close()
                } catch (e: Exception) {
                    MessageDialog.showMessageDialog(gui, "Erreur sql", e.message ?: "Erreur inconnue")
                    log.error(e.message)
                }
            }
        }

        val btnAnnuler = Button("Annuler") {window.close()}
        btnPanel.addComponent(btnValider)
        btnPanel.addComponent(btnAnnuler)
        panel.addComponent(btnPanel)

        window.component = panel
        gui.addWindowAndWait(window)
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