package org.steam2.client.entites;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entité composé reliant un jeu et un joueur avec son avis et son temps de jeu
 * @author Wilhem
 */
@Entity
@Table(name="jeu_joueur")
@IdClass(org.steam2.client.CleComposite.JeuJoueurId.class)
@Getter
@Setter
public class JeuJoueur {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="jeu_id")
    private JeuVideo jeuVideo;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="joueur_id")
    private Joueur joueur;

    @Column(name="temps_joue_m")
    private Integer temps_joue_m;
}