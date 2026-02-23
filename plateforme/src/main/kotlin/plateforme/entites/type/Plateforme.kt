package org.steam2.plateforme.plateforme.entites.type

/**
 * Enumération des plateformes supportées par les jeux
 * @author Wilhem
 */
enum class Plateforme(val libelle: String) {
    LINUX("Linux"),
    WINDOWS("Windows"),
    SWITCH("Nintendo switch");

    override fun toString(): String {
        return libelle
    }
}