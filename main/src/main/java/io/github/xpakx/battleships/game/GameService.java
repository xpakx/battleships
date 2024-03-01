package io.github.xpakx.battleships.game;

import io.github.xpakx.battleships.game.dto.*;
import io.github.xpakx.battleships.game.error.GameNotFoundException;
import io.github.xpakx.battleships.game.error.RequestProcessedException;
import io.github.xpakx.battleships.game.error.UnauthorizedGameRequestChangeException;
import io.github.xpakx.battleships.game.error.UserNotFoundException;
import io.github.xpakx.battleships.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class GameService {
    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    public NewGameResponse newGame(String username, GameRequest request) {
        Game game;
        if (request.getType() == GameType.AI) {
            game = newGameAgainstAI(username);
        } else {
            game = newGameAgainstUser(username, request.getOpponent());
        }
        return new NewGameResponse(game.getId());
    }

    private Game newGameAgainstUser(String username, String opponent) {
        var newGame = new Game();
        newGame.setUser(userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new));
        newGame.setOpponent(userRepository.findByUsername(opponent).orElseThrow(UserNotFoundException::new));
        newGame.setType(GameType.USER);
        newGame.setRuleset(GameRuleset.CLASSIC);
        newGame.setAiType(AIType.NONE);
        var emptyState = createEmptyGameState(10, 10);
        newGame.setUserCurrentState(emptyState);
        newGame.setOpponentCurrentState(emptyState);
        newGame.setUserShips("[]");
        newGame.setOpponentShips("[]");
        Random random = new Random();
        newGame.setUserStarts(random.nextBoolean());
        newGame.setUserTurn(newGame.isUserStarts());
        return gameRepository.save(newGame);
    }

    private Game newGameAgainstAI(String username) {
        var newGame = new Game();
        newGame.setUser(userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new));
        newGame.setType(GameType.AI);
        newGame.setRuleset(GameRuleset.CLASSIC);
        newGame.setAiType(AIType.RANDOM);
        var emptyState = createEmptyGameState(10, 10);
        newGame.setUserCurrentState(emptyState);
        newGame.setOpponentCurrentState(emptyState);
        newGame.setUserShips("[]");
        newGame.setOpponentShips("[]");
        newGame.setAccepted(true);
        Random random = new Random();
        newGame.setUserStarts(random.nextBoolean());
        newGame.setUserTurn(newGame.isUserStarts());
        return gameRepository.save(newGame);
    }

    private String createEmptyGameState(int width, int height) {
        return "?".repeat(width * height);
    }

    public List<GameSummary> getRequests(String username) {
        return gameRepository.findRequests(
                userRepository.findByUsername(username)
                        .orElseThrow(UserNotFoundException::new)
                        .getId()
                ).stream()
                .map((a) -> GameSummary.of(a, username)).toList();
    }

    public List<GameSummary> getActiveGames(String username) {
        return gameRepository.findActiveGames(
                userRepository.findByUsername(username)
                        .orElseThrow(UserNotFoundException::new)
                        .getId()
                ).stream()
                .map((a) -> GameSummary.of(a, username)).toList();
    }

    public List<GameSummary> getOldGames(String username) {
        return gameRepository.findFinishedGames(
                userRepository.findByUsername(username)
                        .orElseThrow(UserNotFoundException::new)
                        .getId()
                ).stream()
                .map((a) -> GameSummary.of(a, username)).toList();
    }

    public boolean acceptRequest(String username, Long requestId, AcceptRequest decision) {
        var game = gameRepository.findWithOpponentById(requestId)
                .orElseThrow(GameNotFoundException::new);
        if (game.isAccepted() || game.isRejected()) {
            throw new RequestProcessedException(
                    "Request already " + (game.isAccepted() ? "accepted!" : "rejected!")
            );
        }
        if (!game.getOpponent().getUsername().equals(username)) {
            throw new UnauthorizedGameRequestChangeException();
        }
        if (decision.isAccepted()) {
            game.setAccepted(true);
            game.setStartedAt(LocalDateTime.now());
        } else {
            game.setRejected(true);
        }
        gameRepository.save(game);
        return decision.isAccepted();
    }
}
