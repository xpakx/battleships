package io.github.xpakx.battleships.game.dto;

import io.github.xpakx.battleships.game.Game;
import io.github.xpakx.battleships.game.GameType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameSummary {
    private Long id;
    // private GameSymbol[][] currentState;
    private Integer lastMoveRow;
    private Integer lastMoveColumn;
    private GameType type;

    private boolean finished;
    private boolean won;
    private boolean lost;
    private boolean drawn;

    private String username1;
    private String username2;
    private boolean userStarts;

    public static GameSummary of(Game game) {
        var summary = new GameSummary();
        summary.setId(game.getId());
        // summary.setCurrentState(stringToBoard(game.getCurrentState()));
        summary.setType(game.getType());
        summary.setFinished(game.isFinished());
        summary.setWon(game.isWon());
        summary.setLost(game.isLost());
        summary.setDrawn(game.isDrawn());
        summary.setUsername1(game.getUser().getUsername());
        summary.setUsername2(
                game.getOpponent() != null ? game.getOpponent().getUsername() : "AI"
        );
        summary.setUserStarts(game.isUserStarts());
        return summary;
    }
}
