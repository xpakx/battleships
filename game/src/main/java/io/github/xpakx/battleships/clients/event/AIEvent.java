package io.github.xpakx.battleships.clients.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.xpakx.battleships.game.AIType;
import io.github.xpakx.battleships.game.GameRuleset;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AIEvent {
    private Long gameId;
    private String gameState;
    private Phase phase;
    private GameRuleset ruleset;
    private AIType type;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Integer> remainingShips;
}
