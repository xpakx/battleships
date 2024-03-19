package io.github.xpakx.battleships.game;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.Optional;

@Getter
@Setter
@RedisHash
public class GameState implements Serializable {
    private Long id;
    private boolean finished;
    private boolean won;
    private boolean lost;
    private boolean drawn;
    private String userCurrentState;
    private String opponentCurrentState;
    private String userShips;
    private String opponentShips;

    private String username1;
    private String username2;
    private boolean user2AI;
    private GameRuleset ruleset;
    private AIType aiType;

    private boolean firstUserStarts;
    private boolean firstUserTurn;

    private boolean blocked;

    public boolean isSecondUserTurn() {
        return !isFirstUserTurn();
    }

    public boolean isUserInGame(String username) {
        return username.equals(username1) ||
                (!user2AI && username.equals(username2));
    }

    public void nextPlayer() {
        firstUserTurn = !firstUserTurn;
    }

    public String getCurrentPlayer() {
        if (isFirstUserTurn()) {
            return username1;
        }
        return username2;
    }

    public String getCurrentTargets() {
        if (isFirstUserTurn()) {
            return opponentShips;
        }
        return userShips;
    }

    public String getCurrentState() {
        if (isFirstUserTurn()) {
            return opponentCurrentState;
        }
        return userCurrentState;
    }

    public boolean aiTurn() {
        return user2AI && isSecondUserTurn();
    }

    public Optional<String> getWinner() {
        if (!won) {
            return Optional.empty();
        }
        var winner = getCurrentPlayer();
        return Optional.of(winner != null ? winner : "AI");
    }

    public boolean isGameStarted() {
        return !userShips.equals("[]") && !opponentShips.equals("[]");

    }

    public void changeState(String newState) {
        if (isFirstUserTurn()) {
            opponentCurrentState = newState;
        } else {
            userCurrentState = newState;
        }
    }
}

