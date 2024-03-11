package io.github.xpakx.battleships.clients.event;

import io.github.xpakx.battleships.game.AIType;
import io.github.xpakx.battleships.game.GameRuleset;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AIEvent {
    private Long gameId;
    private String gameState;
    private Phase phase;
    private GameRuleset ruleset;
    private AIType type;
}
