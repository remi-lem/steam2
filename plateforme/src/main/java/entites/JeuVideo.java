package entites;

import jakarta.persistence.*;
import lombok.Getter;
import org.steam2.plateforme.plateforme.entites.type.Plateforme;
import entites.Editeur;

import java.util.List;

/**
 * Entité JeuVideo
 * @author Wilhem
 */

@Entity
@Table(name = "jeu")
@Getter
public class JeuVideo {
    @Id
    @Column(name = "id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "editeur_id")
    private Editeur editeur;

    @Column(name = "nom")
    private String nom;

    @Column(name = "plateforme")
    @Enumerated(EnumType.STRING)
    private Plateforme plateforme;

    @OneToMany(mappedBy = "jeuParent")
    private List<JeuVideo> dlcs;

    @Column(name="prix_editeur")
    private Double prix_editeur;

    @ManyToMany
    @JoinTable(
            name = "jeu_genre",
            joinColumns = { @JoinColumn(name = "jeu_id") },
            inverseJoinColumns = { @JoinColumn(name = "genre_id") }
    )
    private List<Genre> genres;

    @OneToMany(mappedBy = "jeu", fetch = FetchType.LAZY)
    private List<VersionJeu> versions;

    @OneToMany(mappedBy = "jeu", fetch = FetchType.LAZY)
    private List<Commentaire> commentaires;

    @OneToMany(mappedBy = "jeu", fetch = FetchType.LAZY)
    private List<Incident> incidents;

    @Override
    public String toString() {
        return this.nom;
    }
}
