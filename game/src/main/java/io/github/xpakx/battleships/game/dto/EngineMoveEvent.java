package io.github.xpakx.battleships.game.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EngineMoveEvent {
    Long gameId;
    boolean legal;
    Integer row;
    Integer column;
    boolean finished;
    MoveResult result;
    String newState;
}
