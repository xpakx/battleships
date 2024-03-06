package io.github.xpakx.battleships.game.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.xpakx.battleships.game.AIType;
import io.github.xpakx.battleships.game.Game;
import io.github.xpakx.battleships.game.GameRuleset;
import io.github.xpakx.battleships.game.GameType;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class GameSummary {
    private Long id;
    private Field[][] currentState1;
    private Field[][] currentState2;
    private Integer lastMoveRow;
    private Integer lastMoveColumn;
    private GameType type;
    private GameRuleset ruleset;
    private AIType aiType;

    private boolean finished;
    private boolean won;
    private boolean lost;
    private boolean drawn;

    private String username1;
    private String username2;
    private boolean userStarts;

    private boolean myTurn;
    private List<Ship> myShips;

    public static GameSummary of(Game game, String requester) {
        var summary = new GameSummary();
        summary.setId(game.getId());
        summary.setCurrentState1(stringToBoard(game.getUserCurrentState()));
        summary.setCurrentState2(stringToBoard(game.getOpponentCurrentState()));
        summary.setType(game.getType());
        summary.setRuleset(game.getRuleset());
        summary.setAiType(game.getAiType());
        summary.setFinished(game.isFinished());
        summary.setWon(game.isWon());
        summary.setLost(game.isLost());
        summary.setDrawn(game.isDrawn());
        summary.setUsername1(game.getUser().getUsername());
        summary.setUsername2(
                game.getOpponent() != null ? game.getOpponent().getUsername() : "AI"
        );
        if (requester.equals(summary.getUsername1())) {
            summary.setMyTurn(game.isUserTurn());
            summary.setMyShips(stringToShips(game.getUserShips()));
        } else if (requester.equals(summary.getUsername2())) {
            summary.setMyTurn(!game.isUserTurn());
            summary.setMyShips(stringToShips(game.getOpponentShips()));
        }
        summary.setUserStarts(game.isUserStarts());
        return summary;
    }

    private static List<Ship> stringToShips(String userShips) {

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(userShips, new TypeReference<List<Ship>>(){});
        } catch (Exception e) {
            return null;
        }
    }


    private static Field[][] stringToBoard(String str) {
        List<List<Field>> fields = Arrays.stream(str.split("\\|"))
                .map(
                        (row) -> row
                                .chars()
                                .mapToObj((c) -> charToSymbol((char) c))
                                .toList()
                ).toList();
        Field[][] board = new Field[fields.size()][fields.getFirst().size()];
        for (int row=0; row< board.length; row++) {
            for (int column=0; column<board[row].length; column++) {
                board[row][column] = fields.get(row).get(column);
            }
        }
        return board;
    }

    private static Field charToSymbol(char c) {
        if(c == 'x') {
            return Field.Sunk;
        }
        if(c == 'o') {
            return Field.Miss;
        }
        if(c == '.') {
            return Field.Hit;
        }
        return Field.Empty;

    }
}
