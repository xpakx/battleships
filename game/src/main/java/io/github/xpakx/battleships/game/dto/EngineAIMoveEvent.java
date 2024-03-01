package io.github.xpakx.battleships.game.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EngineAIMoveEvent {
    Long gameId;
    Integer row;
    Integer column;
}
