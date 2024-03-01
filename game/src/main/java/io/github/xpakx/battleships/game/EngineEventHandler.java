package io.github.xpakx.battleships.game;

import io.github.xpakx.battleships.game.dto.EngineMoveEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EngineEventHandler {
    private final GameService service;

    @RabbitListener(queues = "${amqp.queue.validation.moves}")
    void handleMove(final EngineMoveEvent event) {
        try {
            service.doMakeMove(event);
        } catch (final Exception e) {
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }
}
