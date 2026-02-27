package org.steam2.client.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "session")
@Getter
@Setter
public class Session{
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

    @Column(name="temps_joue_m")
    private Integer tempsJoueM;

    @Column(name="date_session")
    private LocalDateTime datePlayed;
}
