package org.steam2.editeur.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import org.steam2.editeur.entities.type.TypeModificationPatch;

@Entity
@Table(name = "detail_modif_patch")
@Getter
@Setter
public class DetailModifPatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "type_modification")
    @Enumerated(EnumType.STRING)
    private TypeModificationPatch typeModificationPatch;

    @Column(name = "commentaire")
    private String commentaire;

    @ManyToOne
    @JoinColumn(name = "version_id")
    private VersionJeu version;
}