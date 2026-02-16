public class GameNotOwnedException extends RuntimeException {
    public GameNotOwnedException(Player player, Game game) {
        super(player.username + "does not own the game " + game.name);
    }
}
