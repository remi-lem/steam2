package org.steam2.plateforme.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.steam2.plateforme.entites.type.TypeEditeur;

import java.util.List;

/**
 * Entité Editeur
 * @author Wilhem
 */
@Entity
@Table(name = "editeur")
@Getter
@Setter
public class Editeur {

    @Id
    @Column(name="id")
    private Integer id;

    @Column(name="type")
    @Enumerated(EnumType.STRING)
    private TypeEditeur typeEditeur;

    @Column(name = "nom")
    private String nom;

    @OneToMany(mappedBy = "editeur", fetch = FetchType.EAGER)
    @OrderBy("nom ASC")
    private List<JeuVideo> jeuxPublies;

    @Override
    public String toString() {
        return nom;
    }
}
