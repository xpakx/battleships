package io.github.xpakx.battleships.clients;

import io.github.xpakx.battleships.clients.event.AIEvent;
import io.github.xpakx.battleships.clients.event.MoveEvent;
import io.github.xpakx.battleships.clients.event.Phase;
import io.github.xpakx.battleships.clients.event.PlacementEvent;
import io.github.xpakx.battleships.game.GameState;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MovePublisher {
    private final AmqpTemplate template;
    private final String movesTopic;

    public MovePublisher(AmqpTemplate template, @Value("${amqp.exchange.moves}") String movesTopic) {
        this.template = template;
        this.movesTopic = movesTopic;
    }

    public void sendHit(int x, int y, String gameState, String targets, Long gameId) {
        MoveEvent event = new MoveEvent();
        event.setGameId(gameId);
        event.setGameState(gameState);
        event.setTargets(targets);
        event.setRow(x);
        event.setColumn(y);
        template.convertAndSend(movesTopic, "move", event);
    }

    public void sendAIEvent(GameState game, Phase phase) {
        var event = new AIEvent();
        event.setGameId(game.getId());
        event.setGameState(game.getCurrentState());
        event.setPhase(phase);
        template.convertAndSend(movesTopic, "ai", event);
    }

    public void sendPlacement(Long gameId, boolean firstUser, String ships) {
        var event = new PlacementEvent();
        event.setGameId(gameId);
        event.setFirstUser(firstUser);
        event.setShips(ships);
        template.convertAndSend(movesTopic, "placement", event);
    }
}