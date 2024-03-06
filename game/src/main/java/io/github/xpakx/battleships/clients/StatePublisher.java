package io.github.xpakx.battleships.clients;

import io.github.xpakx.battleships.clients.event.UpdateEvent;
import io.github.xpakx.battleships.game.GameState;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class StatePublisher {
    private final AmqpTemplate template;
    private final String updatesTopic;

    public StatePublisher(AmqpTemplate template, @Value("${amqp.exchange.updates}") String updatesTopic) {
        this.template = template;
        this.updatesTopic = updatesTopic;
    }

    public void publish(GameState game) {
        UpdateEvent event = new UpdateEvent();
        event.setGameId(game.getId());

        event.setFinished(game.isFinished());
        event.setWon(game.isWon());
        event.setLost(game.isLost());
        event.setDrawn(game.isDrawn());

        event.setUserCurrentState(game.getCurrentState());
        event.setOpponentCurrentState(game.getOpponentCurrentState());
        event.setUserShips(game.getUserShips());
        event.setOpponentShips(game.getOpponentShips());

        event.setUserTurn(game.isFirstUserTurn());
        event.setTimestamp(LocalDateTime.now());

        template.convertAndSend(updatesTopic, "update", event);
    }
}
