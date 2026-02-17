package org.steam2.editeur.entites.type

/**
 * Enumération des plateformes supportées
 * @author remi
 */
enum class Plateforme(val libelle: String) {
    LINUX("Linux"),
    WINDOWS("Windows"),
    SWITCH("Nintendo switch");

    override fun toString(): String {
        return libelle
    }
}