package org.steam2.client.exceptions;

import org.steam2.client.*;

public class GameNotOwnedException extends RuntimeException {
    public GameNotOwnedException(Player player, Game game) {
        super(player.getUsername() + "does not own the game " + game.getName());
    }
}