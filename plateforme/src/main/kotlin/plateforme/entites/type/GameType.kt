package org.steam2.plateforme.plateforme.entites.type

/**
 * Enumération des types de jeux
 * @author Jules
 */
enum class GameType(val libelle: String) {
    ACTION("Action"),
    ADVENTURE("Adventure"),
    FIGHTING("Fighting"),
    HORROR("Horror"),
    MMORPG("MorpG"),
    MOBA("Moba"),
    PLATEFORMER("Plateformer"),
    PUZZLE("Puzzle"),
    RACING("Racing"),
    RPGS("Rpgs"),
    SANDBOX("Sandbox"),
    SIMULATION("Simulation"),
    SHOOTER("Shooter"),
    SPORTS("Sports"),
    SURVIVAL("Survival");

    override fun toString(): String {
        return libelle
    }
}