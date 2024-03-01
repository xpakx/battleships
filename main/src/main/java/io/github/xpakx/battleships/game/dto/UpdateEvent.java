package io.github.xpakx.battleships.game.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateEvent {
    private Long gameId;

    private boolean finished;
    private boolean won;
    private boolean lost;
    private boolean drawn;

    private String userCurrentState;
    private String opponentCurrentState;
    private String userShips;
    private String opponentShips;

    private boolean userTurn;

    private Integer lastMoveX;
    private Integer lastMoveY;
}
