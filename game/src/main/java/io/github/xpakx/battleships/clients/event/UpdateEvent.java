package io.github.xpakx.battleships.clients.event;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

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
    private LocalDateTime timestamp;

    private Integer lastMoveX;
    private Integer lastMoveY;
}
