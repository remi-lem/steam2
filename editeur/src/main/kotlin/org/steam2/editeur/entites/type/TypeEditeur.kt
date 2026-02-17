package org.steam2.editeur.entites.type

/**
 * Enumération des types d'éditeurs
 * @author remi
 */
enum class TypeEditeur(val libelle: String) {
    ENTREPRISE("Entreprise"),
    INDEPENDANT("Indépendant");

    override fun toString(): String {
        return libelle
    }
}