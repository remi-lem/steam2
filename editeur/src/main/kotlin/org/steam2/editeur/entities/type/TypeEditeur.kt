package org.steam2.editeur.entities.type

enum class TypeEditeur(val libelle: String) {
    ENTREPRISE("Entreprise"),
    INDEPENDANT("Indépendant");

    override fun toString(): String {
        return libelle
    }
}