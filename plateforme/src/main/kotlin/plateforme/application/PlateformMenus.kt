package org.steam2.plateforme.plateforme.application

import com.google.common.hash.Hashing
import com.googlecode.lanterna.gui2.BasicWindow
import com.googlecode.lanterna.gui2.Button
import com.googlecode.lanterna.gui2.ComboBox
import com.googlecode.lanterna.gui2.Direction
import com.googlecode.lanterna.gui2.EmptySpace
import com.googlecode.lanterna.gui2.GridLayout
import com.googlecode.lanterna.gui2.Label
import com.googlecode.lanterna.gui2.LinearLayout
import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.gui2.TextBox
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder
import com.googlecode.lanterna.gui2.dialogs.MessageDialog
import kotlinx.coroutines.CoroutineScope
import org.slf4j.LoggerFactory
import org.steam2.plateforme.daos.CommentaireDAO
import org.steam2.plateforme.daos.EditeurDAO
import org.steam2.plateforme.daos.GenreDAO
import org.steam2.plateforme.daos.JeuVideoDAO
import org.steam2.plateforme.daos.JoueurDAO
import org.steam2.plateforme.entites.Genre
import org.steam2.plateforme.entites.JeuVideo
import org.steam2.plateforme.entites.Joueur
import org.steam2.plateforme.plateforme.entites.type.Plateforme
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import javax.security.auth.login.LoginException

/**
 * Classe gérant tous les menus différents de l'application Plateforme
 * @author : Jules
 */
class PlateformMenus(
        private val gui: MultiWindowTextGUI,
        private val editeurDAO: EditeurDAO,
        private val jeuVideoDAO: JeuVideoDAO,
        private val joueurDAO: JoueurDAO,
        private val genreDAO: GenreDAO,
        private val commentaireDAO: CommentaireDAO,
        private val serviceScopeJeux: CoroutineScope,
        private val serviceRecuperationJeux: RecupererJeuxVideos,
        private val serviceEnvoiJoueur: EnvoiJoueur
    ) {

    private val log = LoggerFactory.getLogger(PlateformMenus::class.java)
    private var accountConnected: Joueur? = null


    /**
     * Menu principal de l'application Plateforme
     * @author : Jules
     */
    fun mainMenu(){
        var exitApplication = false
        while(!exitApplication) {

            try {
                // Liste des options possibles
                val menu = ActionListDialogBuilder()
                    .setTitle("Menu principal - Plateforme")
                    .setDescription("Choisissez une action :")
                    .setCanCancel(false)
                    .addAction("Liste des jeux disponibles") {
                        // menuPublicationJeu(false)
                        // TODO : Affichage de la liste des jeux -> La récupérer
                        log.info("Liste des jeux")
                        menuVGList()
                    }
                    .addAction("Rechercher un jeu") {
                        log.info("Recherche de jeux")
                        searchGame()
                        // TODO : résultat de la requête
                    }

                // Si un compte est connecté
                if(accountConnected!=null) {
                    menu.addAction("Ma collection") {
                        log.info("Ma collection")
                        // TODO : Affichage de la collection reliée au compte

                    }.addAction("Ma liste de souhaits") {
                        log.info("Acces liste de souhaits")
                        menuWishlist()
                        // TODO : Affichage de la liste de souhait

                    }.addAction("Mes amis") {
                        log.info("Liste d'amis")
                        menuFriends()

                    }.addAction("Gérer mon compte") {
                        log.info("Gerer mon compte")
                        menuManageAccount()
                    }
                }else{
                    menu.addAction("Connexion / Inscription") {
                        log.info("connexion ou inscription")
                        menuLogRegAccount()
                    }
                }

                menu.addAction("Quitter l'application") {
                    exitApplication = true
                }
                .build()
                .showDialog(gui)

            } catch (e: Exception) {
                log.error("Error in main plateform menus", e)
                MessageDialog.showMessageDialog(gui, "Erreur fatale", e.message)
            }
        }
    }

    /**
     * Menu permettant la connexion ou l'inscription de l'utilisateur
     *
     * @author Jules
     */
    fun menuLogRegAccount(){
        var exitLogMenu = false
        while(!exitLogMenu) {
            try {
                // Liste des options possibles
                ActionListDialogBuilder()
                    .setTitle("- Connexion / Inscription-")
                    .setDescription("Choisissez une action :")
                    .setCanCancel(false)
                    .addAction("Connexion") {
                        log.info("Connexion")
                        login()

                    }
                    .addAction("Inscription") {
                        log.info("Inscription")
                        register()

                    }.addAction("Fermer le menu") {
                        exitLogMenu = true
                    }
                    .build()
                    .showDialog(gui)
            } catch (e: Exception) {
                log.error("Error in main plateform menus", e)
                MessageDialog.showMessageDialog(gui, "Erreur fatale", e.message)
            }
        }
    }

    fun register() {
        val window = BasicWindow("Création de compte")
        val panel = Panel(GridLayout(2))
        val txtUsername = TextBox()
        val txtName = TextBox()
        val txtFirstname = TextBox()

        // Date de naissance
        val txtDay = TextBox("JJ")
        val txtMonth = TextBox("MM")
        val txtYear = TextBox("AAAA")

        val txtPassword1 = TextBox().setMask('*')
        val txtPassword2 = TextBox().setMask('*')

        var cbPlateforme: ComboBox<Plateforme>? = null

        val btnPanel = Panel(LinearLayout(Direction.HORIZONTAL))
        val btnValider = Button("Enregistrer") {

            val playerPlateform = cbPlateforme?.selectedItem

            if (txtUsername.text.isBlank()) {
                MessageDialog.showMessageDialog(gui, "Erreur", "Le pseudo est obligatoire")
            } else if(txtName.text.isBlank()) {
                MessageDialog.showMessageDialog(gui, "Erreur", "Le nom est obligatoire")
            } else if(txtFirstname.text.isBlank()) {
                MessageDialog.showMessageDialog(gui, "Erreur", "Le prénom est obligatoire")
            } else if(txtDay.text.isBlank() or txtMonth.text.isBlank() or txtYear.text.isBlank()) {
                MessageDialog.showMessageDialog(gui, "Erreur", "La date de naissance est obligatoire")
            } else if(playerPlateform==null){
                MessageDialog.showMessageDialog(gui, "Erreur", "Plateforme non définie")
                return@Button
            }else{
                // ——— Conversion de la date de naissance récupérée ———
                // Récupération des valeurs saisies
                val day = txtDay.text.trim().toInt()
                val month = txtMonth.text.trim().toInt()
                val year = txtYear.text.trim().toInt()

                // --- Création du nouveau joueur ---
                val newPlayer = Joueur().apply {
                    username = txtUsername.text
                    prenom = txtFirstname.text
                    nom = txtName.text
                    date_naissance = LocalDateTime.of(year, month, day, 12,0)
                    date_creation = LocalDateTime.now()
                    plateforme = playerPlateform
                }

                try {
                    joueurDAO.persister(newPlayer)

                    // Envoi sur le topic kafka
                    serviceEnvoiJoueur.envoyer(newPlayer)

                    log.info("Le joueur ${newPlayer.username} a été publié !")
                    MessageDialog.showMessageDialog(gui, "Succès", "${newPlayer.nom} publié")
                    window.close()
                } catch (e: Exception) {
                    log.error("Error in main plateform menus", e)
                }
            }
        }

        val btnAnnuler = Button("Annuler") { window.close() }


        // Composants de la fenetre
        panel.addComponent(Label("Pseudo :"))
        panel.addComponent(txtUsername)

        panel.addComponent(Label("Prénom :"))
        panel.addComponent(txtFirstname)

        panel.addComponent(Label("Nom :"))
        panel.addComponent(txtName)

        panel.addComponent(EmptySpace())
        panel.addComponent(EmptySpace())

        panel.addComponent(Label("Date de naissance (JJ/MM/AAAA) :"))
        panel.addComponent(Panel(LinearLayout(Direction.HORIZONTAL)).apply {
            addComponent(txtDay)
            addComponent(Label("/"))
            addComponent(txtMonth)
            addComponent(Label("/"))
            addComponent(txtYear)
        })

        panel.addComponent(EmptySpace())
        panel.addComponent(EmptySpace())

        panel.addComponent(Label("Plateforme de jeu principale :"))
        cbPlateforme = ComboBox<Plateforme>(*Plateforme.entries.toTypedArray()).addTo(panel)

        panel.addComponent(EmptySpace())
        panel.addComponent(EmptySpace())

        panel.addComponent(Label("Mot de passe:"))
        panel.addComponent(txtPassword1)

        panel.addComponent(Label("Vérifier le mot de passe:"))
        panel.addComponent(txtPassword2)

        btnPanel.addComponent(btnValider)
        btnPanel.addComponent(btnAnnuler)
        panel.addComponent(btnPanel)

        window.component = panel
        gui.addWindowAndWait(window)
    }

    fun login(): Joueur? {
        var accountConnected:Joueur? = null

        val window = BasicWindow("Authentification")
        val panel = Panel(GridLayout(2))

        val txtLogin = TextBox()
        val txtPassword = TextBox().setMask('*')

        // Composant de la fenetre
        panel.addComponent(Label("Login:"))
        panel.addComponent(txtLogin)

        panel.addComponent(Label("Mot de passe:"))
        panel.addComponent(txtPassword)

        panel.addComponent(EmptySpace())

        // Boutons
        val btnOK = Button("OK") {
            // On vérifie le mot de passe
            val sha256hex = Hashing.sha256()
                .hashString(txtPassword.text, StandardCharsets.UTF_8)
                .toString()
            try {
                // val editeur = joueurDAO.identifier(txtLogin.text, sha256hex)
                 // accountConnected = editeur
                window.close()
            } catch (e: LoginException) {
                MessageDialog.showMessageDialog(gui, "Erreur", "Login ou mot de passe incorrect")
            }
        }
        panel.addComponent(btnOK)
        window.component = panel
        gui.addWindowAndWait(window)

        return Joueur()
    }

    /**
     * Menu permettant d'obtenir les jeux vidéos disponibles
     *
     * @author : Jules
     */
    fun menuVGList(){

        var exitVGListe = false
        while(!exitVGListe) {
            try {
                // Liste des options possibles
                ActionListDialogBuilder()
                    .setTitle("- Liste des jeux vidéos-")
                    .setDescription("Choisissez une action :")
                    .setCanCancel(false)
                    .addAction("Afficher par date d'ajout") {
                        // TODO : Résupérer la liste des jeux par ordre anti-chronologique
                        log.info("Affichage date d'ajout")

                        // Etape 1 : Afficher la liste des jeux dans la console
                        log.info(jeuVideoDAO.getJeuxByID(0).toString())

                        // Affichage des jeux
                        displayGameList(jeuVideoDAO.getJeuxByID(0))
                    }
                    .addAction("Afficher par ordre alphabétique") {
                        log.info("Affichage ordre alpha")
                        // Affichage des jeux
                        displayGameList(jeuVideoDAO.getJeuxByName(0))
                    }
                    .addAction("Afficher par popularité") {
                        // TODO : Récupérer la liste des jeux par rapport à leur popularité
                        // TODO : Mettre en place un système de vote / ranking
                        log.info("Affichage par avis")
                    }.addAction("Fermer le menu") {
                        exitVGListe = true
                    }
                    .build()
                    .showDialog(gui)
            } catch (e: Exception) {
                log.error("Error in main plateform menus", e)
                MessageDialog.showMessageDialog(gui, "Erreur fatale", e.message)
            }
        }

    }

    fun menuFriends(){
        var exitFriendsMenu = false
        while(!exitFriendsMenu) {
            try {
                // Liste des options possibles
                ActionListDialogBuilder()
                    .setTitle("- Liste d'amis -")
                    .setDescription("Choisissez une action :")
                    .setCanCancel(false)
                    .addAction("Voir mes amis") {
                        // TODO : Afficher la liste d'amis
                        log.info("Voir mes amis")
                        displayFriendList()

                    }
                    .addAction("Ajouter un ami") {
                        // TODO : Resultat de la recherche
                        searchFriend()
                        log.info("Ajouter un ami")
                    }
                    .addAction("Supprimer un ami") {
                        // TODO : Afficher la liste d'ami, pouvoir en sélectionner un puis suppression
                        log.info("Supprimer un ami")
                    }.addAction("Fermer le menu") {
                        exitFriendsMenu = true
                    }
                    .build()
                    .showDialog(gui)
            } catch (e: Exception) {
                log.error("Error in main plateform menus", e)
                MessageDialog.showMessageDialog(gui, "Erreur fatale", e.message)
            }
        }

    }

    /**
     * Menu correspondant aux actions possible sur la liste de souhait
     * - Consultation
     * - Suppression d'un jeu
     *
     * L'addition est possible via la recherche de jeu
     *
     * @author : Jules
     */
    fun menuWishlist(){
        var exitWishList = false

        while(!exitWishList) {
            try {
                // Liste des options possibles
                ActionListDialogBuilder()
                    .setTitle("- Wishlist -")
                    .setDescription("Choisissez une action :")
                    .setCanCancel(false)
                    .addAction("Voir ma liste de souhaits") {
                        // TODO : Afficher la liste d'amis
                        log.info("Voir mes amis")
                    }
                    .addAction("Supprimer un jeu de la liste de souhaits") {
                        // TODO : Afficher la liste d'ami, pouvoir en sélectionner un puis suppression
                        log.info("Supprimer un ami")
                    }.addAction("Fermer le menu") {
                        exitWishList = true
                    }
                    .build()
                    .showDialog(gui)
            } catch (e: Exception) {
                log.error("Error in main plateform menus", e)
                MessageDialog.showMessageDialog(gui, "Erreur fatale", e.message)
            }
        }
    }

    /**
     * Menu correspondant aux modifications possibles du compte
     * - Changer de pseudo
     * - Changer d'adresse mail
     * - Changer de mot de passe
     *
     * TODO : Pas de DAO Joueur ? On ne peut pas faire de requête ?
     * TODO : Pas d'élément adresse mail ? On s'en fiche de sa date de naissance
     *
     * @author : Jules
     */
    fun menuManageAccount(){
        var exitManageAccount = false

        while(!exitManageAccount) {
            try {
                // Liste des options possibles
                ActionListDialogBuilder()
                    .setTitle("- Gérer mon compte -")
                    .setDescription("Choisissez une action :")
                    .setCanCancel(false)
                    .addAction("Changer de pseudo") {
                        log.info("Changement de pseudo")
                        changeItem("pseudo")
                    }
                    .addAction("Changer d'adresse mail") {
                        log.info("Changement d'adresse mail")
                        changeItem("email")

                    }.addAction("Changer de mot de passe") {
                        log.info("Changement MDP")
                        changeItem("password")
                    }.addAction("Fermer le menu") {
                        exitManageAccount = true
                    }
                    .build()
                    .showDialog(gui)
            } catch (e: Exception) {
                log.error("Error in main plateform menus", e)
                MessageDialog.showMessageDialog(gui, "Erreur fatale", e.message)
            }
        }
    }


    /**
     * Fonction permettant de chercher un jeu
     *
     * Création d'une fênetre permettant la saisie d'un nom de jeu
     * TODO : Faire la recherche
     * TODO : Faire une requête dans DAO pour trier par nom par rapport à un nom donnée et pas que par ID
     *
     * @author : Jules
     */
    fun searchGame(){
        val panel = Panel().apply { layoutManager = GridLayout(2) }
        val txtNameGame = TextBox()
        val window = BasicWindow("Chercher un jeu")

        // Envoi de la requête
        val btnOK = Button("OK"){
            // Affichage des jeux
            displayGameList(
                jeuVideoDAO.researchByName(txtNameGame.text),
                "- Résultat de la recherche -"
            )
            window.close()
        }

        panel.addComponent(Label("Nom à chercher :"))
        panel.addComponent(txtNameGame)
        panel.addComponent(btnOK)

        window.component = panel
        gui.addWindowAndWait(window)
    }

    /**
     * Fonction gérant l'entrée du nom de l'ami à chercher et affichant le résultat
     *
     * TODO : Affichage du résultat de la recherche
     * @return None
     *
     * @author Jules
     */
    fun searchFriend(){
        val window = BasicWindow("Chercher un ami")

        val panel = Panel().apply { layoutManager = GridLayout(2) }
        val txtNameGame = TextBox()
        val btnOK = Button("OK"){
            // TODO : Requête à faire dans la base Client
            window.close()
        }

        panel.addComponent(Label("Pseudo à chercher :"))
        panel.addComponent(txtNameGame)
        panel.addComponent(btnOK)

        window.component = panel
        gui.addWindowAndWait(window)
    }

    /**
     * Fonction permettant de changer de mot de passe
     *
     * @param itemToChange : String, symbolise le paramètre du compte à mettre à jour
     * @return None
     *
     * @author : Jules
     */
    fun changeItem(itemToChange : String){

        /**
         * Fonction permettant de récupérer le nouveau mot de passe du compte
         *
         * @author : Jules
         */
        fun getInstance(item:String,txtToDisplay:String): String {
            var txtItem = ""
            if(item=="password"){
                txtItem="Changer de mot de passe"
            }else if (item=="email"){
                txtItem="Changer d'adresse mail"
            } else {
                txtItem="Changer de pseudo"
            }

            val window = BasicWindow(txtItem)
            val panel = Panel().apply { layoutManager = GridLayout(2) }
            val txtNewPassword = TextBox()

            var newPassword = ""

            panel.addComponent(Label(txtToDisplay))
            panel.addComponent(txtNewPassword)

            val btnOK = Button("OK"){
                newPassword = txtNewPassword.text
                window.close()
            }
            panel.addComponent(btnOK)

            window.component = panel
            gui.addWindowAndWait(window)
            return newPassword
        }

        /**
         * Fonction permettant d'afficher le résultat de la comparaison
         * entre les deux entrées du nouveau mot de passe
         *
         * @author : Jules
         */
        fun displayResult(item:String,isNewPasswordCorrect: Boolean) {
            var txtItem = ""
            if(item=="password"){
                txtItem="Changer de mot de passe"
            }else if (item=="email"){
                txtItem="Changer d'adresse mail"
            } else {
                txtItem="Changer de pseudo"
            }

            val window = BasicWindow(txtItem)
            val panel = Panel().apply { layoutManager = GridLayout(2) }

            if (isNewPasswordCorrect) {
                panel.addComponent(Label("Validation : Changement effectué"))
            } else {
                panel.addComponent(Label("Erreur : Les deux entrées sont différentes"))
            }

            val btnOK = Button("OK"){ window.close() }
            panel.addComponent(btnOK)

            window.component = panel
            gui.addWindowAndWait(window)
        }

        var firstInstance = ""
        var secondInstance = ""

        if (itemToChange=="password"){
            // Récupération du nouveau mot de passe et de sa confirmation
            firstInstance = getInstance("password","Nouveau mot de passe :")
            secondInstance = getInstance("password","Vérification du mot de passe :")
        }else if(itemToChange=="email"){
            // Récupération du nouveau mot de passe et de sa confirmation
            firstInstance = getInstance("email","Nouvelle adresse mail :")
            secondInstance = getInstance("email","Vérification de l'adresse :")
            // TODO : Aucun compte relié à l'email ?
        }else {
            // Récupération du nouveau mot de passe et de sa confirmation
            firstInstance = getInstance("pseudo","Nouveau pseudo :")
            secondInstance = getInstance("pseudo","Vérification du pseudo :")
            // TODO : Check pseudo non utilisé
        }

        // Affichage du résultat de la comparaison entre les deux entrées du nouveau mdp
        displayResult( itemToChange,firstInstance == secondInstance)
    }

    /**
     * Affiche des différentes listes de jeux, résultantes de requête à JeuVideoDAO
     *
     * @param gameList : List<JeuVideo>, résultat de la requête HQL du DAO
     * @param title : String, nom à donner à la fenêtre. De base, vaut "Liste de jeux"
     * @return None
     *
     * @author Jules
     */
    fun displayGameList(gameList:List<JeuVideo>, title:String="- Liste de jeux- "){
        var exitGameList = false

        while(!exitGameList) {
            try {
                // Liste des options possibles
                val builder = ActionListDialogBuilder()
                    .setTitle(title)
                    .setDescription("Choisissez une action :")
                    .setCanCancel(false)

                    for (game in gameList){
                        builder.addAction(game.nom) {
                            log.info(game.nom)
                            gamePage(game)
                        }
                    }
                    builder.addAction("Fermer le menu") {
                        exitGameList = true
                    }
                    .build()
                    .showDialog(gui)
            } catch (e: Exception) {
                log.error("Error in main plateform menus", e)
                MessageDialog.showMessageDialog(gui, "Erreur fatale", e.message)
            }
        }
    }

    /**
     * Affiche la page correspondante à un jeu
     * - Affichage de ses caractéristiques
     * - Possibilité d'achat
     * - Possibilité d'ajout à la wishlist
     *
     * @author Jules
     */
    fun gamePage(jv:JeuVideo){

        val panel = Panel().apply { layoutManager = GridLayout(2) }
        val window = BasicWindow(jv.nom)

        // Affichage des informations
        panel.addComponent(Label("Éditeur : "))
        panel.addComponent(Label(jv.editeur.nom))

        panel.addComponent(Label("Plateforme : "))
        panel.addComponent(Label(jv.plateforme.toString()))

        panel.addComponent(Label("Prix : "))
        panel.addComponent(Label(jv.prix_editeur.toString()))

        panel.addComponent(Label("Genre : "))
        for(genre:Genre in jv.genres){
            panel.addComponent(Label(genre.toString()))

        }
        // Séparateur visuel
        panel.addComponent(EmptySpace())
        panel.addComponent(EmptySpace())

        if(accountConnected!=null){
            // Bouton avec actions
            val btnBuy = Button("Acheter le jeu"){
                log.info("Acheter le jeu")
                window.close()
            }
            val btnWishList = Button("Ajouter à la liste de souhaits"){
                log.info("Ajout liste de souhait")
                window.close()
            }

            panel.addComponent(btnBuy)
            // Permet de mettre les boutons les uns en dessous des autres en occupant la seconde colonnes
            panel.addComponent(EmptySpace())
            panel.addComponent(btnWishList)
            panel.addComponent(EmptySpace())
        }

        val btnClose = Button("Retour"){
            window.close()
        }

        panel.addComponent(btnClose)

        window.component = panel
        gui.addWindowAndWait(window)
    }

    /**
     * Affichage des amis du compte Joueur chargé (accountConnected)
     * Permet d'acceder à la fiche de chaque ami pour voir son profil ou le supprimer
     *
     * @return None
     *
     * @author Jules
     */
    fun displayFriendList(){
        var exitFriendList = false

        while(!exitFriendList) {
            try {
                // Liste des options possibles
                val builder = ActionListDialogBuilder()
                    .setTitle(" - Liste d'amis -")
                    .setDescription("Choisissez une action :")
                    .setCanCancel(false)

                for (friend in accountConnected!!.amis){
                    builder.addAction(friend.username) {
                        friendPage(friend)
                        log.info(friend.username)

                    }
                }
                builder.addAction("Fermer le menu") {
                    exitFriendList = true
                }
                    .build()
                    .showDialog(gui)
            } catch (e: Exception) {
                log.error("Error in main plateform menus", e)
                MessageDialog.showMessageDialog(gui, "Erreur fatale", e.message)
            }
        }
    }

    fun displayDeleteFriendFromList(){
        var exitFriendList = false

        while(!exitFriendList) {
            try {
                val builder = ActionListDialogBuilder()
                    .setTitle(" - Liste d'amis -")
                    .setCanCancel(false)

                for (friend in accountConnected!!.amis){
                    builder.addAction(friend.username) {
                        accountConnected!!.amis.remove(friend)
                        // TODO : Supprimer l'ami dans la BDD
                    }
                }
                builder.addAction("Fermer le menu") {
                    exitFriendList = true
                }
                    .build()
                    .showDialog(gui)
            } catch (e: Exception) {
                log.error("Error in main plateform menus", e)
                MessageDialog.showMessageDialog(gui, "Erreur fatale", e.message)
            }
        }
    }

    /**
     * Affiche la page correspondante à un ami
     * - Affichage de ses caractéristiques
     * - Possibilité de suppression de l'amitié
     *
     * @author Jules
     */
    fun friendPage(friend:Joueur){

        val panel = Panel().apply { layoutManager = GridLayout(2) }
        val window = BasicWindow(friend.username)

        val btnDeleteFriend = Button("Supprimer l'ami"){
            accountConnected?.amis?.remove(friend)
            // TODO : Supprimer l'ami dans la BDD

            window.close()
        }

        val btnClose = Button("Retour"){
            window.close()
        }

        // Affichage des informations
        panel.addComponent(Label("Date de création de compte : "))
        panel.addComponent(Label(friend.date_creation.toString()))

        // Séparateur visuel
        panel.addComponent(EmptySpace())
        panel.addComponent(EmptySpace())

        // Ajout des boutons
        panel.addComponent(btnDeleteFriend)
        panel.addComponent(btnClose)

        window.component = panel
        gui.addWindowAndWait(window)
    }
}