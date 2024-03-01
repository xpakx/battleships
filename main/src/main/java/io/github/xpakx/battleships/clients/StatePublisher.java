package io.github.xpakx.battleships.clients;

import io.github.xpakx.battleships.clients.event.StateEvent;
import io.github.xpakx.battleships.game.Game;
import io.github.xpakx.battleships.game.GameType;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StatePublisher {
    private final AmqpTemplate template;
    private final String stateTopic;

    public StatePublisher(AmqpTemplate template, @Value("${amqp.exchange.state}") String stateTopic) {
        this.template = template;
        this.stateTopic = stateTopic;
    }

    public void sendGame(Game game) {
        StateEvent event = new StateEvent();
        event.setId(game.getId());
        event.setFinished(game.isFinished());
        if (game.isFinished()) {
            event.setError(true);
            event.setErrorMessage("Game is already finished!");
        }
        if (!game.isAccepted()) {
            event.setError(true);
            event.setErrorMessage("Game is not accepted!");
        }
        event.setUsername1(game.getUser().getUsername());
        if (game.getOpponent() != null) {
            event.setUsername2(game.getOpponent().getUsername());
        } else {
            event.setUsername2("AI");
        }
        event.setUser2AI(game.getType() == GameType.AI);
        event.setRuleset(game.getRuleset());
        event.setAiType(game.getAiType());
        event.setFirstUserStarts(game.isUserStarts());
        event.setFirstUserTurn(game.isUserTurn());
        event.setUserCurrentState(game.getUserCurrentState());
        event.setOpponentCurrentState(game.getOpponentCurrentState());
        event.setUserShips(game.getUserShips());
        event.setOpponentShips(game.getOpponentShips());
        template.convertAndSend(stateTopic, "state", event);
    }

    public void sendError(String msg) {
        StateEvent event = new StateEvent();
        event.setError(true);
        event.setErrorMessage(msg);
        template.convertAndSend(stateTopic, "state", event);
    }
}
