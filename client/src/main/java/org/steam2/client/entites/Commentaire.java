package org.steam2.client.entites;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entité Commentaire
 * @author remi
 */
@Entity
@Table(name = "commentaire")
@Getter
@Setter
public class Commentaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "jeu_id")
    private JeuVideo jeu;

    @ManyToOne
    @JoinColumn(name = "joueur_id")
    private Joueur joueur;

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "commentaire")
    private String commentaire;
}