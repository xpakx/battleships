package io.github.xpakx.battleships.game;

import com.redis.testcontainers.RedisContainer;
import io.github.xpakx.battleships.clients.event.AIEvent;
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

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class StateEventHandlerTest {

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

    @Value("${amqp.queue.state}")
    String stateQueue;
    @Value("${amqp.exchange.state}")
    String stateExchange;

    @Value("${amqp.exchange.moves}")
    String movesExchange;

    void config() {
        var queue = new Queue("test.queue", true);
        rabbitAdmin.declareQueue(queue);
        var exchange = ExchangeBuilder
                .topicExchange(movesExchange)
                .durable(true)
                .build();
        rabbitAdmin.declareExchange(exchange);
        var binding = new Binding(
                "test.queue",
                Binding.DestinationType.QUEUE,
                movesExchange,
                "ai",
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
        rabbitAdmin.purgeQueue(stateQueue);
        gameRepository.deleteAll();
    }

    @Test
    void shouldSaveGameInRedisOnEvent() {
        var event = new StateEvent();
        event.setId(5L);
        event.setUsername1("user1");
        event.setUsername2("user2");
        event.setUserCurrentState("???|?x?|???");
        event.setOpponentCurrentState("???|?x?|???");
        event.setUserShips("[]");
        event.setOpponentShips("[]");
        rabbitTemplate.convertAndSend(stateExchange, "state", event);
        await()
                .atMost(5, TimeUnit.SECONDS)
                .until(redisHasRecord());
        var gameOpt = gameRepository.findById(5L);
        assertThat(gameOpt.isPresent(), is(true));
        var game = gameOpt.get();
        assertThat(game.getCurrentState(), equalTo("???|?x?|???"));
        assertThat(game.getUsername1(), equalTo("user1"));
        assertThat(game.getUsername2(), equalTo("user2"));
    }

    @Test
    void shouldNotSaveGameWithError() throws Exception {
        var event = new StateEvent();
        event.setId(5L);
        event.setUsername1("user1");
        event.setUsername2("user2");
        event.setUserCurrentState("???|?x?|???");
        event.setOpponentCurrentState("???|?x?|???");
        event.setError(true);
        event.setErrorMessage("Error");
        rabbitTemplate.convertAndSend(stateExchange, "state", event);
        Thread.sleep(1000);
        assertThat(getRedisRecordCount(), equalTo(0L));
    }

    @Test
    void shouldAskAIForFirstMoveInStartedGame() {
        var event = new StateEvent();
        event.setId(5L);
        event.setUsername1("user1");
        event.setUser2AI(true);
        event.setFirstUserStarts(false);
        event.setUserCurrentState("???|?x?|???");
        event.setOpponentCurrentState("???|?x?|???");
        event.setUserShips("[{\"headX\":8,\"headY\":6,\"size\":1,\"orientation\":\"Horizontal\"}]");
        event.setOpponentShips("[{\"headX\":8,\"headY\":6,\"size\":1,\"orientation\":\"Horizontal\"}]");
        rabbitTemplate.convertAndSend(stateExchange, "state", event);

        await()
                .atMost(5, TimeUnit.SECONDS)
                .until(isQueueNotEmpty("test.queue"), Matchers.is(true));
        var moveOpt = getAIMessage();
        assertThat(moveOpt.isPresent(), is(true));
        var move = moveOpt.get();
        assertThat(move.getGameState(), equalTo("???|?x?|???"));
    }

    private Long getRedisRecordCount() {
        return gameRepository.count();
    }

    private Callable<Boolean> redisHasRecord() {
        return () -> getRedisRecordCount() > 0;
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
}