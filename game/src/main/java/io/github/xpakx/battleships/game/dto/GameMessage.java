package io.github.xpakx.battleships.game.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.xpakx.battleships.game.GameState;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class GameMessage {
    private String username1;
    private String username2;
    private boolean ai;

    private String[][] state1;
    private String[][] state2;
    // TODO
    // private Integer lastMoveX;
    // private Integer lastMoveY;
    private String currentPlayer;
    private boolean gameStarted;

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
        msg.setGameStarted(game.isGameStarted());
        return msg;
    }

    private static String[][] stringToBoard(String str) {
        List<List<String>> fields = Arrays.stream(str.split("\\|"))
                .map(
                        (row) -> row
                                .chars()
                                .mapToObj((c) -> charToSymbol((char) c))
                                .toList()
                ).toList();
        String[][] board = new String[fields.size()][fields.getFirst().size()];
        for (int row=0; row< board.length; row++) {
            for (int column=0; column<board[row].length; column++) {
                board[row][column] = fields.get(row).get(column);
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
