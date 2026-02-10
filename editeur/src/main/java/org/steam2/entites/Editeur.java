package org.steam2.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.steam2.editeur.entities.type.TypeEditeur;

@Entity
@Table(name = "editeur")
@Getter
@Setter
public class Editeur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private TypeEditeur typeEditeur;

    @Column(name = "nom")
    private String nom;

    @Column(name = "password")
    private String password;
}
