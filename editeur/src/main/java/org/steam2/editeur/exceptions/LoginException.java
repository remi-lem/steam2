package org.steam2.editeur.exceptions;

/**
 * Exception lancée si une erreur survient lors de l'identification
 * @author remi
 */
public class LoginException extends RuntimeException {
    public LoginException(String message) {
        super(message);
    }
}
