package org.steam2.plateforme.application

import com.googlecode.lanterna.gui2.BasicWindow
import com.googlecode.lanterna.gui2.Button
import com.googlecode.lanterna.gui2.GridLayout
import com.googlecode.lanterna.gui2.Label
import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.gui2.TextBox
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder
import com.googlecode.lanterna.gui2.dialogs.MessageDialog
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.terminal.Terminal
import org.slf4j.LoggerFactory

/**
 * Classe gérant tous les menus différents de l'application Plateforme
 * @author : Jules
 */
class PlateformMenus(private val gui: MultiWindowTextGUI, private var terminal: Terminal) {
    private val log = LoggerFactory.getLogger(PlateformMenus::class.java)

    /**
     * Menu principal de l'application Plateforme
     * @author : Jules
     */
    fun mainMenu(){
        var exitApplication = false
        while(!exitApplication) {

            try {
                // Liste des options possibles
                ActionListDialogBuilder()
                    .setTitle("Menu principal - Plateforme")
                    .setDescription("Choisissez une action :")
                    .setCanCancel(false)
                    .addAction("Liste des jeux disponibles") {
                        // menuPublicationJeu(false)
                        // TODO : Affichage de la liste des jeux -> La récupérer
                        log.info("Liste des jeux")
                    }
                    .addAction("Rechercher un jeu") {
                        log.info("Recherche de jeux")
                        searchGame()
                        // TODO : résultat de la requête
                    }
                    .addAction("Ma collection") {
                        log.info("Ma collection")
                        // TODO : Affichage de la collection reliée au compte
                    }
                    .addAction("Ma liste de souhaits") {
                        log.info("Acces liste de souhaits")
                        menuWishlist()
                        // TODO : Affichage de la liste de souhait
                    }
                    .addAction("Mes amis") {
                        log.info("Liste d'amis")
                        menuFriends()

                    }
                    .addAction("Gérer mon compte") {
                        log.info("Gerer mon compte")
                        menuManageAccount()
                    }
                    .addAction("Quitter l'application") {
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
     *
     * @author : Jules
     */
    fun searchGame(){
        val panel = Panel().apply { layoutManager = GridLayout(2) }
        val txtNameGame = TextBox()

        val window = BasicWindow("Chercher un jeu")

        panel.addComponent(Label("Nom à chercher :"))
        panel.addComponent(txtNameGame)

        val btnOK = Button("OK"){
            // TODO : Ajouter des effets
            window.close()
        }
        panel.addComponent(btnOK)

        window.component = panel
        gui.addWindowAndWait(window)
    }

    /**
     * Fonction gérant l'entrée du nom de l'ami à chercher et affichant le résultat
     *
     * TODO : Affichage du résultat de la recherche
     *
     * @author : Jules
     */
    fun searchFriend(){
        val panel = Panel().apply { layoutManager = GridLayout(2) }
        val txtNameGame = TextBox()

        val window = BasicWindow("Chercher un ami")

        panel.addComponent(Label("Pseudo à chercher :"))
        panel.addComponent(txtNameGame)


        val btnOK = Button("OK"){
            // TODO : Ajouter des effets
            window.close()
        }
        panel.addComponent(btnOK)

        window.component = panel
        gui.addWindowAndWait(window)
    }

    /**
     * Fonction permettant de changer de mot de passe
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
}