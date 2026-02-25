package org.steam2.client.CleComposite;

import java.io.Serializable;
import java.util.Objects;

/**
 * Clé composite pour la table JeuJoeur
 * @author Wilhem
 */
public class JeuJoueurId implements Serializable {

    private Integer jeuVideo;
    private Integer joueur;

    public JeuJoueurId() {}

    public JeuJoueurId(Integer jeuId, Integer joueurId){
        this.jeuVideo = jeuId;
        this.joueur = joueurId;
    }


    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof JeuJoueurId)) return false;
        JeuJoueurId other = (JeuJoueurId) o;
        return Objects.equals(jeuVideo, other.jeuVideo) && Objects.equals(joueur, other.joueur);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jeuVideo, joueur);
    }
}