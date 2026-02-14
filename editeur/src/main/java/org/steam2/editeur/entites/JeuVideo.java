package org.steam2.editeur.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.steam2.editeur.entities.type.Plateforme;

import java.util.List;

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

    @Column(name = "plateforme")
    @Enumerated(EnumType.STRING)
    private Plateforme plateforme;

    @ManyToMany
    @JoinTable(
        name = "jeu_genre",
        joinColumns = { @JoinColumn(name = "jeu_id") },
        inverseJoinColumns = { @JoinColumn(name = "genre_id") }
    )
    private List<Genre> genres;

    @OneToMany(mappedBy = "jeu")
    private List<VersionJeu> versions;

    @OneToMany(mappedBy = "jeu")
    private List<Commentaire> commentaires;
}
