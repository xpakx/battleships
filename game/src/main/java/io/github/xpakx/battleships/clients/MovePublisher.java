package io.github.xpakx.battleships.clients;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.xpakx.battleships.clients.event.AIEvent;
import io.github.xpakx.battleships.clients.event.MoveEvent;
import io.github.xpakx.battleships.clients.event.Phase;
import io.github.xpakx.battleships.clients.event.PlacementEvent;
import io.github.xpakx.battleships.game.GameRuleset;
import io.github.xpakx.battleships.game.GameState;
import io.github.xpakx.battleships.game.dto.GameMessage;
import io.github.xpakx.battleships.game.dto.Ship;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MovePublisher {
    private final AmqpTemplate template;
    private final String movesTopic;

    public MovePublisher(AmqpTemplate template, @Value("${amqp.exchange.moves}") String movesTopic) {
        this.template = template;
        this.movesTopic = movesTopic;
    }

    public void sendHit(int x, int y, String gameState, String targets, Long gameId, GameRuleset rules) {
        MoveEvent event = new MoveEvent();
        event.setGameId(gameId);
        event.setGameState(gameState);
        event.setTargets(targets);
        event.setRow(x);
        event.setColumn(y);
        event.setRuleset(rules);
        template.convertAndSend(movesTopic, "move", event);
    }

    public void sendAIEvent(GameState game, Phase phase) {
        var event = new AIEvent();
        event.setGameId(game.getId());
        event.setGameState(game.getCurrentState());
        event.setPhase(phase);
        event.setType(game.getAiType());
        event.setRuleset(game.getRuleset());
        event.setRemainingShips(getRemainingShips(game));
        template.convertAndSend(movesTopic, "ai", event);
    }

    private List<Integer> getRemainingShips(GameState game) {
        if (!game.isGameStarted()) {
            return null;
        }
        var ships = stringToShips(game.getUserShips());
        if (ships.isEmpty()) {
            return null;
        }

        var msg = GameMessage.of(game);
        return ships.get()
                .stream()
                .filter((ship) -> {
                    var value = msg.getState1()[ship.getHeadX()][ship.getHeadY()];
                    return !value.equals("Sunk");
                })
                .map(Ship::getSize)
                .toList();
    }

    private static Optional<List<Ship>> stringToShips(String userShips) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return Optional.ofNullable(objectMapper.readValue(userShips, new TypeReference<List<Ship>>(){}));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public void sendPlacement(Long gameId, GameState game, boolean firstUser, String ships) {
        var event = new PlacementEvent();
        event.setGameId(gameId);
        event.setFirstUser(firstUser);
        event.setShips(ships);
        event.setRuleset(game.getRuleset());
        template.convertAndSend(movesTopic, "placement", event);
    }
}