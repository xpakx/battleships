package io.github.xpakx.battleships.clients.event;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlacementEvent {
    private Long gameId;
    private String username;
    private String ships;
    // ruleset
}
