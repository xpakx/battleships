package io.github.xpakx.battleships.game.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MoveMessage {
    private String player;
    private int x;
    private int y;
    private boolean legal;
    private MoveResult result;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message;

    private boolean finished;
    private boolean won;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String winner;

    public static MoveMessage of(int x, int y, String username, MoveResult result) {
        return new MoveMessage(
                username,
                x,
                y,
                true,
                result,
                null,
                false,
                false,
                null
        );
    }

    public static MoveMessage rejected(int x, int y, String username, String msg) {
        var moveMessage = of(x, y, username, null);
        moveMessage.setMessage(msg);
        moveMessage.setLegal(false);
        return moveMessage;
    }
}
