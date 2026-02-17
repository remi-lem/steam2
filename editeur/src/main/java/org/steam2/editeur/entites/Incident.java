package org.steam2.editeur.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entité Incident
 * @author remi
 */
@Entity
@Table(name = "incident")
@Getter
@Setter
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "jeu_id")
    private JeuVideo jeu;

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "details")
    private String details;
}