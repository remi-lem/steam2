package org.steam2.client;

import java.time.Duration;
import java.util.Date;

public record GameSession (
    Game game,
    Duration playedTime,
    Date dayPlayed
) {

}
