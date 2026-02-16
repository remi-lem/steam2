package org.steam2.editeur.entities.type

enum class TypeModificationPatch(val libelle: String) {
    CORRECTION("Correction"),
    AJOUT("Ajout"),
    OPTI("Optimisation");

    override fun toString(): String {
        return libelle
    }
}