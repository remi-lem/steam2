package org.steam2.client.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Entity
@Table(name="joueur")
@Getter
@Setter
public class Joueur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @Column(name="pseudo")
    private String pseudo;

    @Column(name="prenom")
    private String prenom;

    @Column(name="nom")
    private String nom;

    @Column(name="date_naissance")
    Date dateNaissance;

    @ManyToMany
    @JoinTable(
            name = "joueur_jeu",
            joinColumns = { @JoinColumn(name = "joueur_id") },
            inverseJoinColumns = { @JoinColumn(name = "eu_id") }
    )
    private List<JeuVideo> bibliotheque;

    @OneToMany(mappedBy = "joueur")
    private List<Session> sessions;

    @Column(name="solde")
    private Float solde;

    /**
     * Ajoute de l'argent au solde du joueur
     * @param quantite argent a ajouter
     * @return la nouvelle valeur du solde
     * @throws IllegalArgumentException si quantité <= 0
     */
    public float rechargeSolde(float quantite) throws IllegalArgumentException{
        if (quantite <= 0) throw new IllegalArgumentException("Can't add negative or null amont");
        solde += quantite;
        return solde;
    }
}
