package io.github.xpakx.battleships.game;

import io.github.xpakx.battleships.clients.GamePublisher;
import io.github.xpakx.battleships.game.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GameService {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final GameRepository repository;
    private final GamePublisher gamePublisher;

    public MoveMessage move(Long gameId, MoveRequest move, String username) {
        var gameOpt = getGameById(gameId);
        if (gameOpt.isEmpty()) {
            gamePublisher.getGame(gameId);
            var msg = MoveMessage.rejected(move.getX(), move.getY(), username, "Game not loaded, please wait!");
            simpMessagingTemplate.convertAndSend("/topic/game/" + gameId, msg);
            return msg;
        }
        var game = gameOpt.get();
        // TODO: check if ship placement is ready
        if (game.isFinished()) {
            var msg =  MoveMessage.rejected(move.getX(), move.getY(), username, "Game is finished!");
            simpMessagingTemplate.convertAndSend("/topic/game/" + gameId, msg);
            return msg;
        }

        if (game.isBlocked() || !canPlayerMove(game, move, username)) {
            var msg = MoveMessage.rejected(move.getX(), move.getY(), username, "Cannot move now!");
            simpMessagingTemplate.convertAndSend("/topic/game/" + gameId, msg);
            return msg;
        }
        game.setBlocked(true);
        repository.save(game);
        var msg = MoveMessage.of(move.getX(), move.getY(), username, null);

        // TODO publish

        return msg;
    }

    public Optional<GameState> getGameById(Long id) {
        return repository.findById(id);
    }

    private boolean canPlayerMove(GameState game, MoveRequest move, String username) {
        return game.isUserInGame(username) &&
                ((username.equals(game.getUsername1()) && game.isFirstUserTurn()) ||
                (username.equals(game.getUsername2()) && game.isSecondUserTurn()));
    }

    public GameMessage subscribe(Long gameId) {
        var gameOpt = getGameById(gameId);
        if (gameOpt.isEmpty()) {
            gamePublisher.getGame(gameId);
            var msg = new GameMessage();
            msg.setError("Loading game, please wait…");
            return msg;
        }
        var game = gameOpt.get();
        return GameMessage.of(game);
    }
}
