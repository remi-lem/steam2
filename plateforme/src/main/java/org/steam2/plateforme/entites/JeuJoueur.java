package org.steam2.plateforme.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

/**
 * Entité composé reliant un jeu et un joueur avec son avis et son temps de jeu
 * @author Wilhem
 */
@Entity
@Table(name="jeu_joueur")
@IdClass(org.steam2.plateforme.CleComposite.JeuJoueurId.class)
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

    @Column(name="temps_jeu")
    private LocalTime temps_jeu;
}
