package org.steam2.client.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.steam2.client.entites.*;
import java.math.BigDecimal;
import java.util.List;
import org.steam2.client.entites.type.*;

/**
 * Entité JeuVideo
 * @author Wilhem
 */

@Entity
@Table(name = "jeu")
@Getter
@Setter
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

    @ManyToOne
    @JoinColumn(name = "jeu_parent_id")
    private JeuVideo jeuParent;

    @OneToMany(mappedBy = "jeuParent")
    private List<JeuVideo> dlcs;

    @Column(name="prix_editeur")
    private BigDecimal prix_editeur;

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

    @OneToMany(mappedBy = "jeuVideo", cascade = CascadeType.ALL)
    private List<JeuJoueur> joueurs;

    @Override
    public String toString() {
        return this.nom;
    }
}
