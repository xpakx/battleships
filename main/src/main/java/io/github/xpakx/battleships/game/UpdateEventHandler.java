package io.github.xpakx.battleships.game;

import io.github.xpakx.battleships.game.dto.UpdateEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateEventHandler {
    private final GameService service;
    private final GameRepository repository;
    private final MoveService moveService;

    @RabbitListener(queues = "${amqp.queue.updates}")
    void handleGame(final UpdateEvent event) {
        try {
            var game = repository.findById(event.getGameId());
            game.ifPresent((g) -> {
                        service.updateGame(g, event);
                        moveService.saveMove(event);
                    }
            );
        } catch (final Exception e) {
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }
}
