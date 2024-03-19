package io.github.xpakx.battleships.clients.event;

import io.github.xpakx.battleships.game.GameRuleset;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MoveEvent {
    private String gameState;
    private String targets;
    private Long gameId;
    private Integer column;
    private Integer row;
    private GameRuleset ruleset;
}
