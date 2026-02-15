package org.steam2.client;

import com.github.javafaker.Faker;
import com.github.javafaker.Name;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.steam2.client.entites.Bibliotheque;
import org.steam2.client.entites.JeuVideo;
import org.steam2.client.entites.Joueur;
import org.steam2.client.exceptions.GameNotOwnedException;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestLibrary {

    Faker faker;
    Bibliotheque library;

    @BeforeEach
    public void setupEach(){
        faker = new Faker();
        Name name = faker.name();
        library = new Bibliotheque(new Joueur(name.fullName(),name.lastName(), name.firstName(), faker.date().between(Date.from(Instant.EPOCH), Date.from(Instant.now()))));
    }

    @Test
    public void testExceptionWhenPlayingUnownedGame(){
        JeuVideo game = new JeuVideo("a");
        assertThrows(GameNotOwnedException.class, () -> library.playGame(game, Duration.ofMinutes(50)));
    }

    @Test
    public void testExceptionWhenGettingPlayedHours(){
        JeuVideo game = new JeuVideo("a");
        assertThrows(GameNotOwnedException.class, () -> library.getPlayedHoursForGame(game));
    }

    @Test
    public void testWithOneSession(){
        JeuVideo game = new JeuVideo("a");
        library.add(game);
        Duration time = Duration.ofMinutes(1);
        library.playGame(game,time);
        assertEquals(60, library.getPlayedHoursForGame(game).getSeconds());
    }

    @Test
    public void testMultipleSessions(){
        JeuVideo game = new JeuVideo("a");
        library.add(game);
        library.playGame(game,Duration.ofMinutes(1));
        library.playGame(game,Duration.ofMinutes(2));
        assertEquals(180, library.getPlayedHoursForGame(game).getSeconds());
    }
}
