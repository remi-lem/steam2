package entites;

import jakarta.persistence.*;
import lombok.Getter;
import java.time.LocalDateTime;

/**
 * Entité représentant les incidents des joueurs
 * @author Wilhem
 */
@Entity
@Table(name="incident")
@Getter
public class Incident {
    @Id
    @Column(name="id")
    private Integer id;

    @Column(name="details")
    private String details;

    @Column(name="date")
    private LocalDateTime date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="jeu_id")
    private JeuVideo jeu;
}
