package io.github.xpakx.battleships.game;

import com.redis.testcontainers.RedisContainer;
import io.github.xpakx.battleships.clients.event.AIEvent;
import io.github.xpakx.battleships.clients.event.MoveEvent;
import io.github.xpakx.battleships.clients.event.Phase;
import io.github.xpakx.battleships.game.dto.EngineAIMoveEvent;
import io.github.xpakx.battleships.game.dto.EngineMoveEvent;
import io.github.xpakx.battleships.game.dto.EnginePlacementEvent;
import io.github.xpakx.battleships.game.dto.StateEvent;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class EngineEventHandlerTest {
    @Container
    static RabbitMQContainer rabbitMq = new RabbitMQContainer(
            DockerImageName.parse("rabbitmq:3.7.25-management-alpine")
    );

    @Container
    static RedisContainer redis = new RedisContainer(
            DockerImageName.parse("redis:6.2.6-alpine")
    );

    @Autowired
    RabbitAdmin rabbitAdmin;
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    GameRepository gameRepository;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.username", rabbitMq::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitMq::getAdminPassword);
        registry.add("spring.rabbitmq.host", rabbitMq::getHost);
        registry.add("spring.rabbitmq.port", rabbitMq::getAmqpPort);

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getRedisPort);
    }

    private boolean rabbitSetupIsDone = false;

    @Value("${amqp.exchange.moves}")
    String movesExchange;

    @Value("${amqp.exchange.engine}")
    String engineExchange;
    @Value("${amqp.queue.validation.moves}")
    String engineMovesQueue;

    @Value("${amqp.exchange.state}")
    String stateExchange;
    @Value("${amqp.queue.state}")
    String stateQueue;

    void config() {
        var queue = new Queue("test.queue", true);
        rabbitAdmin.declareQueue(queue);
        var exchange = ExchangeBuilder
                .topicExchange(movesExchange)
                .durable(true)
                .build();
        rabbitAdmin.declareExchange(exchange);
        Binding binding = new Binding(
                "test.queue",
                Binding.DestinationType.QUEUE,
                movesExchange,
                "ai",
                null
        );
        rabbitAdmin.declareBinding(binding);

        var queue2 = new Queue("test.move.queue", true);
        rabbitAdmin.declareQueue(queue2);
        Binding binding2 = new Binding(
                "test.move.queue",
                Binding.DestinationType.QUEUE,
                movesExchange,
                "move",
                null
        );
        rabbitAdmin.declareBinding(binding2);
        rabbitSetupIsDone = true;
    }
    @BeforeEach
    void setUp() {
        if (!rabbitSetupIsDone) {
            config();
        }
    }

    @AfterEach
    void tearDown() {
        rabbitAdmin.purgeQueue(engineMovesQueue);
        rabbitAdmin.purgeQueue(stateQueue);
        rabbitAdmin.purgeQueue("test.queue");
        rabbitAdmin.purgeQueue("test.move.queue");
        gameRepository.deleteAll();
    }

    @Test
    void shouldUpdateGameStateOnEvent() {
        var game = new GameState();
        game.setUsername1("user1");
        game.setUsername2("user2");
        game.setFirstUserStarts(false);
        game.setFirstUserTurn(true);
        game.setId(5L);
        game.setUserCurrentState("???|?x?|???");
        game.setOpponentCurrentState("???|?x?|???");
        gameRepository.save(game);

        var event = new EngineMoveEvent();
        event.setGameId(5L);
        event.setLegal(true);
        event.setColumn(0);
        event.setRow(0);
        event.setNewState("new state");
        rabbitTemplate.convertAndSend(engineExchange, "validation.move", event);
        await()
                .atMost(5, SECONDS)
                .untilAsserted(() -> assertThat(recordHasNewState(5L, "new state", false), is(true)));
        var gameOpt = gameRepository.findById(5L);
        assertThat(gameOpt.isPresent(), is(true));
        var gameDb = gameOpt.get();
        assertThat(gameDb.getOpponentCurrentState(), equalTo("new state"));
        assertThat(gameDb.isFirstUserTurn(), is(false));
    }

    @Test
    void shouldAskForAIMoveAfterUserMove() {
        var game = new GameState();
        game.setUsername1("user1");
        game.setUser2AI(true);
        game.setFirstUserStarts(true);
        game.setFirstUserTurn(true);
        game.setId(5L);
        game.setUserCurrentState("???|?x?|???");
        game.setOpponentCurrentState("???|?x?|???");
        game.setUserShips("[{\"headX\":2,\"headY\":1,\"size\":1,\"orientation\":\"Horizontal\"}]");
        game.setOpponentShips("[{\"headX\":2,\"headY\":1,\"size\":1,\"orientation\":\"Horizontal\"}]");
        gameRepository.save(game);

        var event = new EngineMoveEvent();
        event.setGameId(5L);
        event.setLegal(true);
        event.setColumn(0);
        event.setRow(0);
        event.setNewState("???|?xx|???");
        rabbitTemplate.convertAndSend(engineExchange, "validation.move", event);
        await()
                .atMost(5, TimeUnit.SECONDS)
                .until(isQueueNotEmpty("test.queue"), Matchers.is(true));
        var moveOpt = getAIMessage();
        assertThat(moveOpt.isPresent(), is(true));
        var move = moveOpt.get();
        assertThat(move.getGameState(), equalTo("???|?x?|???"));
        assertThat(move.getPhase(), equalTo(Phase.Move));
    }

    @Test
    void shouldAskForAIPlacement() {
        var event = new StateEvent();
        event.setId(5L);
        event.setUsername1("user1");
        event.setUser2AI(true);
        event.setFirstUserStarts(false);
        event.setUserCurrentState("???|?x?|???");
        event.setOpponentCurrentState("???|?x?|???");
        event.setUserShips("[]");
        event.setOpponentShips("[]");
        rabbitTemplate.convertAndSend(stateExchange, "state", event);
        await()
                .atMost(5, TimeUnit.SECONDS)
                .until(isQueueNotEmpty("test.queue"), Matchers.is(true));
        var moveOpt = getAIMessage();
        assertThat(moveOpt.isPresent(), is(true));
        var move = moveOpt.get();
        assertThat(move.getPhase(), equalTo(Phase.Placement));
    }

    @Test
    void shouldAskAIForMoveAfterPlacementIsFinished() {
        var game = new GameState();
        game.setUsername1("user1");
        game.setUsername2("user2");
        game.setFirstUserStarts(false);
        game.setFirstUserTurn(false);
        game.setUser2AI(true);
        game.setId(5L);
        game.setUserCurrentState("???|?x?|???");
        game.setOpponentCurrentState("???|?x?|???");
        game.setUserShips("[]");
        game.setOpponentShips("[{\"headX\":8,\"headY\":6,\"size\":1,\"orientation\":\"Horizontal\"}]");
        gameRepository.save(game);

        var event = new EnginePlacementEvent();
        event.setGameId(5L);
        event.setLegal(true);
        event.setShips("[{\\\"headX\\\":8,\\\"headY\\\":6,\\\"size\\\":1,\\\"orientation\\\":\\\"Horizontal\\\"}]");
        event.setFirstUser(true);
        rabbitTemplate.convertAndSend(engineExchange, "placement", event);
        await()
                .atMost(5, TimeUnit.SECONDS)
                .until(isQueueNotEmpty("test.queue"), Matchers.is(true));
        var aiMessage = getAIMessage();
        assert(aiMessage.isPresent());
        var aiEvent = aiMessage.get();
        assertThat(aiEvent.getGameId(), equalTo(5L));
        assertThat(aiEvent.getPhase(), equalTo(Phase.Move));
    }

    @Test
    void shouldResendAIMoveForValidation() {
        var game = new GameState();
        game.setUsername1("user1");
        game.setUsername2("user2");
        game.setFirstUserStarts(false);
        game.setFirstUserTurn(false);
        game.setUser2AI(true);
        game.setId(5L);
        game.setUserCurrentState("???|?x?|???");
        game.setOpponentCurrentState("???|?xx|???");
        game.setUserShips("[{\"headX\":8,\"headY\":6,\"size\":1,\"orientation\":\"Horizontal\"}]");
        game.setOpponentShips("[{\"headX\":7,\"headY\":6,\"size\":1,\"orientation\":\"Horizontal\"}]");
        gameRepository.save(game);

        var event = new EngineAIMoveEvent();
        event.setGameId(5L);
        event.setColumn(7);
        event.setRow(5);
        rabbitTemplate.convertAndSend(engineExchange, "ai.move", event);
        await()
                .atMost(5, TimeUnit.SECONDS)
                .until(isQueueNotEmpty("test.move.queue"), Matchers.is(true));
        var moveMsg = getMoveEvent();
        assert(moveMsg.isPresent());
        var moveEvent = moveMsg.get();
        assertThat(moveEvent.getGameId(), equalTo(5L));
        assertThat(moveEvent.getColumn(), equalTo(7));
        assertThat(moveEvent.getRow(), equalTo(5));
        assertThat(moveEvent.getGameState(), equalTo(game.getCurrentState()));
        assertThat(moveEvent.getTargets(), equalTo(game.getUserShips()));
    }

    private boolean recordHasNewState(Long id, String newState, boolean firstUser) {
        var gameOpt = gameRepository.findById(id);
        return gameOpt
                .map((a) -> firstUser ? a.getUserCurrentState() : a.getOpponentCurrentState())
                .map((a) -> a.equals(newState))
                .orElse(false);
    }

    private int getMessageCount(String queueName) {
        Properties queueProperties = rabbitAdmin.getQueueProperties(queueName);
        if (queueProperties != null) {
            return (int) queueProperties.get("QUEUE_MESSAGE_COUNT");
        } else {
            return 0;
        }
    }

    private Callable<Boolean> isQueueNotEmpty(String queueName) {
        return () -> getMessageCount(queueName) > 0;
    }

    private Optional<AIEvent> getAIMessage() {
        var queuedMessage = rabbitTemplate.receiveAndConvert("test.queue");
        if (Objects.isNull(queuedMessage)) {
            return Optional.empty();
        }
        if (queuedMessage instanceof AIEvent e) {
            return Optional.of(e);
        }
        return Optional.empty();
    }

    private Optional<MoveEvent> getMoveEvent() {
        var queuedMessage = rabbitTemplate.receiveAndConvert("test.move.queue");
        if (Objects.isNull(queuedMessage)) {
            return Optional.empty();
        }
        if (queuedMessage instanceof MoveEvent e) {
            return Optional.of(e);
        }
        return Optional.empty();
    }
}