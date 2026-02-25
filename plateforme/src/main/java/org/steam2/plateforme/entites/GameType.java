package org.steam2.plateforme.entites;

/*
Codé par Jules
 */
public enum GameType {
    ACTION, ADVENTURE, FIGHTING, HORROR, MMORPG, MOBA, PLATEFORMER, PUZZLE,
    RACING, RPGS, SANDBOX, SIMULATION, SHOOTER, SPORTS, SURVIVAL;

    @Override
    public String toString() {
        switch (this) {
            case ACTION:
                return "ACTION";
            case ADVENTURE:
                return "ADVENTURE";
            case FIGHTING:
                return "FIGHTING";
            case HORROR:
                return "HORROR";
            case MMORPG:
                return "MMORPG";
            case MOBA:
                return "MOBA";
            case PLATEFORMER:
                return "PLATEFORMER";
            case PUZZLE:
                return "PUZZLE";
            case RACING:
                return "RACING";
            case RPGS:
                return "RPGS";
            case SANDBOX:
                return "SANDBOX";
            case SIMULATION:
                return "SIMULATION";
            case SHOOTER:
                return "SHOOTER";
            case SURVIVAL:
                return "SURVIVAL";
            case SPORTS:
                return "SPORTS";
        }
        return "";
    }
}
