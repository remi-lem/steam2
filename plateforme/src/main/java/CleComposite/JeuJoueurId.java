package CleComposite;

import java.io.Serializable;
import java.util.Objects;

/**
 * Clé composite pour la table JeuJoeur
 * @author Wilhem
 */
public class JeuJoueurId implements Serializable {

    private Integer jeu_id;
    private Integer joueur_id;

    public JeuJoueurId() {}

    public JeuJoueurId(Integer jeuId, Integer joueurId){
        this.jeu_id = jeuId;
        this.joueur_id = joueurId;
    }


    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof JeuJoueurId)) return false;
        JeuJoueurId other = (JeuJoueurId) o;
        return Objects.equals(jeu_id, other.jeu_id) && Objects.equals(joueur_id, other.joueur_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jeu_id,joueur_id);
    }
}
