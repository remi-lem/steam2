package org.steam2.client;

import com.github.javafaker.Faker;
import com.github.javafaker.Name;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.steam2.client.entites.Joueur;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestPlayer {

    Joueur player1;
    static Faker faker;

    @BeforeAll
    public static void setupAll(){
        faker = new Faker();
    }

    @BeforeEach
    public void setupTest(){
        Name name = faker.name();
        String pseudo = name.fullName();
        String firstName = name.firstName();
        String lastName = name.lastName();
        Date birthday = faker.date().between(Date.from(Instant.EPOCH), Date.from(Instant.now()));
        player1 = new Joueur(pseudo,lastName,firstName,birthday);
    }

    @Test
    void testExceptionWhenNegativeAmount(){
        assertThrows(IllegalArgumentException.class, () -> player1.rechargeSolde(-1f));
    }
    @Test
    void testExceptionWhenNullAmount(){
        assertThrows(IllegalArgumentException.class, () -> player1.rechargeSolde(0f));
    }
    @ParameterizedTest
    @ValueSource(floats = {5f, 100f, 1000f})
    void testRecharge(float i) {
        float newAmount = player1.rechargeSolde(i);
        assertEquals(newAmount, i);
        assertEquals(player1.getWallet(),i);
    }
}
