package org.steam2.plateforme.plateforme.entites.type

/**
 * Enumération des types d'éditeurs
 * @author Wilhem
 */
enum class TypeEditeur(val libelle: String) {
    ENTREPRISE("Entreprise"),
    INDEPENDANT("Indépendant");

    override fun toString(): String {
        return libelle
    }
}