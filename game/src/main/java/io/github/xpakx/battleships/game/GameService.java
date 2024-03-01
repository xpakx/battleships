package io.github.xpakx.battleships.game;

import io.github.xpakx.battleships.clients.GamePublisher;
import io.github.xpakx.battleships.clients.MovePublisher;
import io.github.xpakx.battleships.clients.event.Phase;
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
    private final MovePublisher movePublisher;
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
        if (!game.isGameStarted()) {
            var msg = MoveMessage.rejected(move.getX(), move.getY(), username, "Game not started, both players must place their ships!");
            simpMessagingTemplate.convertAndSend("/topic/game/" + gameId, msg);
            return msg;
        }
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

        movePublisher.sendHit(
                move.getX(),
                move.getY(),
                game.getCurrentState(),
                game.getCurrentTargets(),
                game.getId()
        );

        return MoveMessage.of(move.getX(), move.getY(), username, null);
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

    public void loadGame(StateEvent event) {
        if (event.isError()) {
            var msg = new GameMessage();
            msg.setError(event.getErrorMessage());
            simpMessagingTemplate.convertAndSend("/topic/board/" + event.getId(), msg);
            return;
        }
        if (event.isFinished()) {
            var msg = new GameMessage();
            msg.setError("Game is already finished!");
            simpMessagingTemplate.convertAndSend("/topic/board/" + event.getId(), msg);
            return;
        }

        var game = new GameState();
        game.setId(event.getId());
        game.setUsername1(event.getUsername1());
        game.setUsername2(event.getUsername2());
        game.setUser2AI(event.isUser2AI());

        game.setFirstUserStarts(event.isFirstUserStarts());
        game.setFirstUserTurn(event.isFirstUserTurn());

        game.setUserCurrentState(event.getUserCurrentState());
        game.setUserShips(event.getUserShips());
        game.setOpponentCurrentState(event.getOpponentCurrentState());
        game.setOpponentShips(event.getOpponentShips());
        repository.save(game);
        var msg = GameMessage.of(game);
        simpMessagingTemplate.convertAndSend("/topic/board/" + game.getId(), msg);
        if (game.aiTurn() && msg.isGameStarted()) {
            movePublisher.sendAIEvent(game, Phase.Move);
        } else if (!msg.isGameStarted()) {
            movePublisher.sendAIEvent(game, Phase.Placement);
        }
    }

    public void doMakeMove(EngineMoveEvent event) {
        var game = getGameById(event.getGameId()).orElseThrow();
        if (!event.isLegal()) {
            game.setBlocked(false);
            simpMessagingTemplate.convertAndSend(
                    "/topic/game/" + game.getId(),
                    MoveMessage.rejected(
                            event.getRow(),
                            event.getColumn(),
                            game.getCurrentPlayer(),
                            "Move is illegal!"
                    )
            );
            repository.save(game);
            return;
        }

        game.changeState(event.getNewState());
        if (event.isFinished()) {
            game.setFinished(true);
            if (game.isFirstUserTurn()) {
                game.setWon(true);
            } else {
                game.setLost(true);
            }
        }
        var msg = MoveMessage.of(event.getRow(), event.getColumn(), game.getCurrentPlayer(), event.getResult());
        if (game.isFinished()) {
            msg.setFinished(true);
            msg.setWon(game.isWon());
            msg.setWinner(game.getWinner().orElse(null));
            repository.deleteById(game.getId());
        } else {
            game.nextPlayer();
            game.setBlocked(false);
            repository.save(game);
        }

        simpMessagingTemplate.convertAndSend("/topic/game/" + game.getId(), msg);
        if (!game.isFinished() && game.aiTurn()) {
            movePublisher.sendAIEvent(game, Phase.Move);
        }
    }
}
