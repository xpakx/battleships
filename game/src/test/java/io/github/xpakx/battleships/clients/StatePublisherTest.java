package io.github.xpakx.battleships.clients;

import com.redis.testcontainers.RedisContainer;
import io.github.xpakx.battleships.clients.event.UpdateEvent;
import io.github.xpakx.battleships.game.GameRepository;
import io.github.xpakx.battleships.game.GameState;
import io.github.xpakx.battleships.game.dto.EngineMoveEvent;
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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class StatePublisherTest {
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

    @Value("${amqp.exchange.engine}")
    String engineExchange;

    @Value("${amqp.exchange.updates}")
    String updatesExchange;
    @Value("${amqp.queue.validation.moves}")
    String engineMovesQueue;

    void config() {
        var queue = new Queue("test.queue", true);
        rabbitAdmin.declareQueue(queue);
        var exchange = ExchangeBuilder
                .topicExchange(updatesExchange)
                .durable(true)
                .build();
        rabbitAdmin.declareExchange(exchange);
        Binding binding = new Binding(
                "test.queue",
                Binding.DestinationType.QUEUE,
                updatesExchange,
                "update",
                null
        );
        rabbitAdmin.declareBinding(binding);
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
        rabbitAdmin.purgeQueue("test.queue");
        gameRepository.deleteAll();
    }

    @Test
    void shouldSendUpdatesAfterMove() {
        var game = new GameState();
        game.setUsername1("user1");
        game.setUsername2("user2");
        game.setFirstUserStarts(false);
        game.setFirstUserTurn(true);
        game.setId(5L);
        game.setUserCurrentState("old state");
        game.setOpponentCurrentState("old state");
        gameRepository.save(game);

        var event = new EngineMoveEvent();
        event.setGameId(5L);
        event.setLegal(true);
        event.setColumn(3);
        event.setRow(7);
        event.setNewState("new state");
        rabbitTemplate.convertAndSend(engineExchange, "validation.move", event);
        await()
                .atMost(5, SECONDS)
                .until(isQueueNotEmpty("test.queue"));
        var updateOpt = getUpdateMessage();
        assertThat(updateOpt.isPresent(), is(true));
        var update = updateOpt.get();
        assertThat(update.getGameId(), equalTo(5L));
        assertThat(update.getLastMoveY(), equalTo(3));
        assertThat(update.getLastMoveX(), equalTo(7));
        assertThat(update.getUserCurrentState(), equalTo("old state"));
        assertThat(update.getOpponentCurrentState(), equalTo("new state"));
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

    private Optional<UpdateEvent> getUpdateMessage() {
        var queuedMessage = rabbitTemplate.receiveAndConvert("test.queue");
        if (Objects.isNull(queuedMessage)) {
            return Optional.empty();
        }
        if (queuedMessage instanceof UpdateEvent e) {
            return Optional.of(e);
        }
        return Optional.empty();
    }
}