package org.steam2.plateforme.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Scanner;

/*
Implementation d'une classe Utilisateur sur la plateforme
@author : Jules
 */
@Entity
@Table(name = "compteUtilisateur")
@Getter
@Setter
public class User implements Account{
    // Scanner commun pour toutes les instances
    private final static Scanner SCANNER = new Scanner(System.in);

    @Id
    private Long id;

    // Attributs des comptes utilisateurs
    @Column(name = "pseudo")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "balance")
    private double balance;

    @Column(name = "collection")
    private ArrayList<Game> collection;

    // *****************
    // ** Déclaration **

    public User(String name,String email,String password){
        this.name = name;
        this.email = email;
        this.password = password;
    }
    public User(String name,String email,String password, double balance){
        this.name = name;
        this.email = email;
        this.password = password;
        this.balance = balance;
    }
    // *****************


    // ************************
    // ** Acquisition de jeu **
    private boolean isPossibleToAddGame(org.steam2.plateforme.entites.Game game){
        /*
        Vérification de la possibilité de paiement
        Cas vérifié :
        - Jeu déjà présent dans la bibliothèque de l'utilisateur
        - Argent disponible sur le compte utilisateur
        */
        boolean alreadyGot = collection.add(game);
        if(alreadyGot){
            return false;
        }
        return !(balance < game.getPrice());

    }
    private boolean gamePayment(org.steam2.plateforme.entites.Game game){
        /*
        Procédure de paiement du jeu et ajout à sa bibliothèque
         */
        try {
            balance -= game.getPrice();
            collection.add(game);
            return true;
        } catch (Exception e){
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean addGame(org.steam2.plateforme.entites.Game game){
        /*
        Méthode d'ajout de jeu à la bibliothèque utilisateur via boite de dialogue et paiement

         @params
         Entrée :
         */
        System.out.println("\nLe jeu sélectionné vaut "+game.getPrice()+"€.");
        System.out.println("\nVérification de la possibilité d'achat...");
        boolean result = isPossibleToAddGame(game);
        if(!result){
            System.out.println("Désolé, il est impossible d'acheter ce jeu.");
            return false;
        }
        System.out.println("Êtes-vous sûr de vouloir acquérir ce jeu ? o/n");
        char  choice = SCANNER.next().charAt(0);
        while(choice != 'n' && choice != 'N' && choice != 'o' && choice != 'O'){
            System.out.println("Votre choix n'a pas été reconnu. Veuillez reessayer :");
            choice = SCANNER.next().charAt(0);
        }
        if(choice=='n' || choice=='N'){
            System.out.println("Paiement annulé.");
            return false;
        }
        System.out.println("Paiement en cours...");
        result = gamePayment(game);
        if(!result){
            System.out.println("Désolé, un problème temporaire empêche l'achat.");
            return false;
        }
        System.out.println("Le jeu a bien été ajouté à votre collection !");
        return  true;
    }
    // ************************

}
