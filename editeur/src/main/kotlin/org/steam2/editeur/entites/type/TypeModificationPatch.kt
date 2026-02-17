package org.steam2.editeur.entites.type

/**
 * Enumération des types de modifications possibles dans le cadre de la publication d'un patch
 * @see org.steam2.editeur.entites.VersionJeu
 * @author remi
 */
enum class TypeModificationPatch(val libelle: String) {
    CORRECTION("Correction"),
    AJOUT("Ajout"),
    OPTI("Optimisation");

    override fun toString(): String {
        return libelle
    }
}