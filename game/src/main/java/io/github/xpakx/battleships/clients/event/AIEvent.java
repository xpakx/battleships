package io.github.xpakx.battleships.clients.event;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AIEvent {
    private Long gameId;
    private String gameState;
    // TODO ruleset
    // TODO ai type
    // TODO move/placement
}
