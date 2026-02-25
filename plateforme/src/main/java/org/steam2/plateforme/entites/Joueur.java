package org.steam2.plateforme.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * Entité représentant les joueurs
 * @author Wilhem
 */
@Entity
@Table(name="joueur")
@Getter
@Setter
public class Joueur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name="username")
    private String username;

    @Column(name="password")
    private String password;

    @Column(name="nom")
    private String nom;

    @Column(name="prenom")
    private String prenom;

    @Column(name="date_naissance")
    private LocalDate date_naissance;

    @Column(name="date_creation")
    private LocalDate date_creation;

    @OneToMany(mappedBy = "joueur",fetch = FetchType.LAZY)
    private List<JeuJoueur> bibliotheque;

    @ManyToMany
    @JoinTable(
            name="amitie",
            joinColumns = @JoinColumn(name="joueur1_id"),
            inverseJoinColumns = @JoinColumn(name="joueur2_id")
    )
    private List<Joueur> amis;

    @ManyToMany
    @JoinTable(
            name="abonnement",
            joinColumns = @JoinColumn(name="joueur_id"),
            inverseJoinColumns = @JoinColumn(name="editeur_id")
    )
    private List<Editeur> abonnements;

    @OneToMany(mappedBy = "joueur", fetch = FetchType.LAZY)
    private List<Commentaire> commentaires;

    //Pour création à la date du jour
    @PrePersist
    protected void onCreate() {
        this.date_creation = LocalDate.now();
    }
}
