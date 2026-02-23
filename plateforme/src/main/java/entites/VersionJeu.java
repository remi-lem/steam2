package entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entité représentant la version d'un jeu
 * @author Wilhem
 */
@Entity
@Table(name = "version_jeu")
@Getter
public class VersionJeu {
    @Id
    @Column(name = "id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "jeu_id")
    private JeuVideo jeu;

    @Column(name = "commentaire_editeur")
    private String commentaireEditeur;

    // Versionement GoRoCo

    @Column(name = "generation")
    private Integer generation;

    @Column(name = "revision")
    private Integer revision;

    @Column(name = "correction")
    private Integer correction;

    public String getVersionString() {
        return generation + "." + revision + "." + correction;
    }

    public boolean isVersionAnticipee() {
        return generation < 1;
    }

    public boolean isVersionPatch() {
        return correction > 0;
    }
}
