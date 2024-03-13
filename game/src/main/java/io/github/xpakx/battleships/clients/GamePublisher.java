package io.github.xpakx.battleships.clients;

import io.github.xpakx.battleships.clients.event.GameEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GamePublisher {
    private final AmqpTemplate template;
    private final String gamesTopic;
    Logger logger = LoggerFactory.getLogger(GamePublisher.class);

    public GamePublisher(AmqpTemplate template, @Value("${amqp.exchange.games}") String gamesTopic) {
        this.template = template;
        this.gamesTopic = gamesTopic;
    }

    public void getGame(Long gameId) {
        logger.debug("Asking main service for state of game {}", gameId);
        GameEvent event = new GameEvent();
        event.setGameId(gameId);
        template.convertAndSend(gamesTopic, "game", event);
    }
}
