package org.steam2.client;

import lombok.Getter;

@Getter
public class Game {
    final String name;

    public Game(String name) {
        this.name = name;
    }
}
