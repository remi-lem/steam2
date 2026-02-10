import lombok.Getter;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

public class GameLibrary extends ArrayList<Game> {
    @Getter
    private final Player owner;

    @Getter
    private final ArrayList<GameSession> gameSessions;

    public GameLibrary(Player owner) {
        super();
        this.owner = owner;
        this.gameSessions = new ArrayList<>();
    }

    /**
     * Computes and returns the total played time for the game of the library
     * @param game the game to compute the total time
     * @return the total played time
     * @throws GameNotOwnedException if the game isn't in the library
     */
    public Duration getPlayedHoursForGame(Game game) throws GameNotOwnedException {
        checkOwnership(game);
        Duration playerHours = Duration.ZERO;
        for (GameSession session : gameSessions){
            if (session.game() == game){
                playerHours = playerHours.plus(session.playedTime());
            }
        }
        return playerHours;
    }

    /**
     * Add and return a game session at the current date
     * @param game the game played
     * @param duration the playing time
     * @return the new game session
     * @throws GameNotOwnedException if the game is not in the library
     */
    public GameSession playGame(Game game, Duration duration) throws GameNotOwnedException {
        checkOwnership(game);
        GameSession session = new GameSession(game,duration, Date.from(Instant.now()));
        gameSessions.add(session);
        return session;
    }

    /**
     * Check if the game is in the library, throws an exception if not
     * @param game the game to check
     * @throws GameNotOwnedException if the game if not in the library
     */
    private void checkOwnership(Game game) throws GameNotOwnedException{
        if (!contains(game)){
            throw new GameNotOwnedException(owner, game);
        }
    }
}
