package io.github.xpakx.battleships.game;

import io.github.xpakx.battleships.game.dto.UpdateEvent;
import io.github.xpakx.battleships.game.error.GameNotFoundException;
import io.github.xpakx.battleships.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MoveService {
    private final MoveRepository moveRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    public void saveMove(UpdateEvent event) {
        var gameOpt = gameRepository.findWithUsersById(event.getGameId());
        if (gameOpt.isEmpty()) {
            return;
        }
        var game = gameOpt.get();
        var move = new Move();
        move.setGame(gameRepository.getReferenceById(event.getGameId()));
        move.setRow(event.getLastMoveX());
        move.setColumn(event.getLastMoveY());
        move.setTimestamp(event.getTimestamp());
        move.setUserCurrentState(event.getUserCurrentState());
        move.setOpponentCurrentState(event.getOpponentCurrentState());
        if (event.isUserTurn()) {
            move.setUser(game.getUser());
        } else {
            move.setUser(game.getOpponent());
        }
        moveRepository.save(move);
    }

    public List<Move> getMoveHistory(Long gameId) {
        if (!gameRepository.existsById(gameId)) {
            throw new GameNotFoundException();
        }
        return moveRepository.findByGameIdOrderByTimestampAsc(gameId);
    }

}
