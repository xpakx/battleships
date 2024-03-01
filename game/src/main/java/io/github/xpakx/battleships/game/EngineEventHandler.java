package io.github.xpakx.battleships.game;

import io.github.xpakx.battleships.clients.MovePublisher;
import io.github.xpakx.battleships.game.dto.EngineAIMoveEvent;
import io.github.xpakx.battleships.game.dto.EngineAIPlacementEvent;
import io.github.xpakx.battleships.game.dto.EngineMoveEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EngineEventHandler {
    private final GameService service;
    private final MovePublisher publisher;
    private final GameRepository repository;

    @RabbitListener(queues = "${amqp.queue.ai.moves}")
    void handleAIMove(final EngineAIMoveEvent event) {
        try {
            var game = repository.findById(event.getGameId()).orElseThrow();
            publisher.sendHit(
                    event.getRow(),
                    event.getColumn(),
                    game.getCurrentState(),
                    game.getCurrentTargets(),
                    game.getId()
            );
        } catch (final Exception e) {
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

    @RabbitListener(queues = "${amqp.queue.ai.placement}")
    void handleAIPlacement(final EngineAIPlacementEvent event) {
        try {
            // TODO
            publisher.sendPlacement(event.getGameId(), "AI", event.getShips());
        } catch (final Exception e) {
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

    @RabbitListener(queues = "${amqp.queue.validation.moves}")
    void handleMove(final EngineMoveEvent event) {
        try {
            service.doMakeMove(event);
        } catch (final Exception e) {
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }
}
