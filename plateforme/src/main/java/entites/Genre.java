package entites;

import jakarta.persistence.*;
import lombok.Getter;
import entites.JeuVideo;
import java.util.List;

/**
 * Entité des genres de jeu
 * @author Wilhem
 */
@Entity
@Table(name = "genre")
@Getter
public class Genre {
    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "nom")
    private String nom;

    @ManyToMany(mappedBy = "genres")
    private List<JeuVideo> jeux;

    @Override
    public String toString() {
        return nom;
    }
}
