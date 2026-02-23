package org.steam2.client.exceptions;

import org.steam2.client.entites.Joueur;
import org.steam2.client.entites.JeuVideo;

public class GameNotOwnedException extends RuntimeException {
    public GameNotOwnedException(Joueur joueur, JeuVideo jeuVideo) {
        super(joueur.getPrenom() + " ne possède pas " + jeuVideo.getNom());
    }
}