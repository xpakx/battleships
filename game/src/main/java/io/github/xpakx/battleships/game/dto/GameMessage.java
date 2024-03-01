package io.github.xpakx.battleships.game.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.xpakx.battleships.game.GameState;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GameMessage {
    private String username1;
    private String username2;
    private boolean ai;

    private String[][] state1; // TODO
    private String[][] state2; // TODO
    // private Integer lastMoveX;
    // private Integer lastMoveY;
    private String currentPlayer;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String error;

    public static GameMessage of(GameState game) {
        var msg = new GameMessage();
        msg.setUsername1(game.getUsername1());
        msg.setUsername2(game.getUsername2());
        msg.setAi(game.isUser2AI());
        msg.setState1(stringToBoard(game.getUserCurrentState()));
        msg.setState2(stringToBoard(game.getOpponentCurrentState()));
        msg.setCurrentPlayer(game.isFirstUserTurn() ? game.getUsername1() : game.getUsername2());
        // TODO placement phase?
        return msg;
    }

    private static String[][] stringToBoard(String str) {
        List<String> list = str.chars()
                .mapToObj((c) -> charToSymbol((char)c))
                .toList();
        var board = new String[3][3];
        for (int row=0; row<3; row++) {
            for (int column=0; column<3; column++) {
                board[row][column] = list.get(3*row+column);
            }
        }
        return board;
    }

    private static String charToSymbol(char c) {
        if(c == 'x' || c == 'X') {
            return "Sunk";
        }
        if(c == 'o' || c == 'O') {
            return "Miss";
        }
        if(c == '.') {
            return "Hit";
        }
        return "Empty";
    }

}
