package io.github.xpakx.battleships.game;

import io.github.xpakx.battleships.clients.GamePublisher;
import io.github.xpakx.battleships.game.dto.StateEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StateEventHandler {
    private final GameService service;
    Logger logger = LoggerFactory.getLogger(StateEventHandler.class);

    @RabbitListener(queues = "${amqp.queue.state}")
    void handleState(final StateEvent event) {
        logger.debug("Handling state event for game {}", event.getId());
        try {
            service.loadGame(event);
        } catch (final Exception e) {
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }
}
