package org.steam2.plateforme.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.steam2.plateforme.plateforme.entites.type.Plateforme;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    @Column(name="username")
    private String username;

    @Column(name="nom")
    private String nom;

    @Column(name="prenom")
    private String prenom;

    @Column(name="date_naissance")
    private LocalDateTime date_naissance;

    @Column(name="date_creation")
    private LocalDateTime date_creation;

    @Column(name="plateforme")
    private Plateforme plateforme;

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
        this.date_creation = LocalDateTime.now();
    }
}
