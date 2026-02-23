package org.steam2.editeur.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.steam2.editeur.entites.type.Plateforme;

import java.math.BigDecimal;
import java.util.List;

/**
 * Entité JeuVideo
 * @author remi
 */
@Entity
@Table(name = "jeu")
@Getter
@Setter
public class JeuVideo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "editeur_id")
    private Editeur editeur;

    @Column(name = "nom")
    private String nom;

    @Column(name = "prix")
    private BigDecimal prix;

    @Column(name = "plateforme")
    @Enumerated(EnumType.STRING)
    private Plateforme plateforme;

    @ManyToOne
    @JoinColumn(name = "jeu_parent_id")
    private JeuVideo jeuParent;

    @OneToMany(mappedBy = "jeuParent")
    private List<JeuVideo> dlcs;

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
    }}
