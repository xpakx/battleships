package io.github.xpakx.battleships.game.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EngineAIPlacementEvent {
    private Long gameId;
    private String ships;
}
