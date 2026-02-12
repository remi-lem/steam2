package org.steam2.editeur.entites;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "commentaire")
    private String commentaire;
}