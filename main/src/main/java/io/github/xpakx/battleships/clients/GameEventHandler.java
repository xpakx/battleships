package io.github.xpakx.battleships.clients;

import io.github.xpakx.battleships.clients.event.GameEvent;
import io.github.xpakx.battleships.game.GameRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameEventHandler {
    private final StatePublisher publisher;
    private final GameRepository repository;

    Logger logger = LoggerFactory.getLogger(GameEventHandler.class);
    @RabbitListener(queues = "${amqp.queue.games}")
    void handleGame(final GameEvent event) {
        logger.debug("Got game event for game {}", event.getGameId());
        try {
            var game = repository.findWithUsersById(event.getGameId());
            game.ifPresent(publisher::sendGame);
            if (game.isEmpty()) {
                logger.debug("Game {} not found", event.getGameId());
                publisher.sendError("No such game!");
            }
        } catch (final Exception e) {
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }
}
