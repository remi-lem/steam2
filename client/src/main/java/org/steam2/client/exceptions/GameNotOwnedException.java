package org.steam2.client.exceptions;

public class GameNotOwnedException extends RuntimeException {
    public GameNotOwnedException(org.steam2.client.entites.Joueur player, org.steam2.client.entites.JeuVideo game) {
        super(player.getUsername() + "does not own the game " + game.getName());
    }
}