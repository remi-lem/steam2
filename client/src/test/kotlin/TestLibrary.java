import com.github.javafaker.Faker;
import com.github.javafaker.Name;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestLibrary {

    Faker faker;
    GameLibrary library;

    @BeforeEach
    public void setupEach(){
        faker = new Faker();
        Name name = faker.name();
        library = new GameLibrary(new Player(name.fullName(),name.lastName(), name.firstName(), faker.date().between(Date.from(Instant.EPOCH), Date.from(Instant.now()))));
    }

    @Test
    public void testExceptionWhenPlayingUnownedGame(){
        Game game = new Game("a");
        assertThrows(GameNotOwnedException.class, () -> library.playGame(game, Duration.ofMinutes(50)));
    }

    @Test
    public void testExceptionWhenGettingPlayedHours(){
        Game game = new Game("a");
        assertThrows(GameNotOwnedException.class, () -> library.getPlayedHoursForGame(game));
    }

    @Test
    public void testWithOneSession(){
        Game game = new Game("a");
        library.add(game);
        Duration time = Duration.ofMinutes(1);
        library.playGame(game,time);
        assertEquals(60, library.getPlayedHoursForGame(game).getSeconds());
    }

    @Test
    public void testMultipleSessions(){
        Game game = new Game("a");
        library.add(game);
        library.playGame(game,Duration.ofMinutes(1));
        library.playGame(game,Duration.ofMinutes(2));
        assertEquals(180, library.getPlayedHoursForGame(game).getSeconds());
    }
}
