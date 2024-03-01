package io.github.xpakx.battleships.game.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StateEvent {
    private Long id;
    private boolean finished;
    private String userCurrentState;
    private String opponentCurrentState;
    private String userShips;
    private String opponentShips;

    private String username1;
    private String username2;
    private boolean user2AI;
    // private GameRuleset ruleset;
    // private AIType aiType;

    private boolean firstUserStarts;
    private boolean firstUserTurn;
    private boolean error;
    private String errorMessage;
}
