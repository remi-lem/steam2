package org.steam2.plateforme.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Entité des commentaires des joueurs sur un jeu
 * @author Wilhem
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

    @Column(name = "commentaire")
    private String commentaire;

    @Column(name = "note")
    private Integer note;

    @Column(name = "date")
    private LocalDateTime date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jeu_id")
    private JeuVideo jeu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="joueur_id")
    private Joueur joueur;
}
