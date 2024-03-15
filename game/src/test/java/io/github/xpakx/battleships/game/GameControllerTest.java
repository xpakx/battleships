package io.github.xpakx.battleships.game;

import com.redis.testcontainers.RedisContainer;
import io.github.xpakx.battleships.clients.event.GameEvent;
import io.github.xpakx.battleships.clients.event.MoveEvent;
import io.github.xpakx.battleships.game.dto.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class GameControllerTest {
    @LocalServerPort
    private int port;
    private String baseUrl;

    WebSocketStompClient stompClient;

    @Value("${jwt.secret}")
    String secret;

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;


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

    @Value("${amqp.exchange.games}")
    String gamesExchange;
    @Value("${amqp.exchange.moves}")
    String movesExchange;
    @Value("${amqp.exchange.state}")
    String stateExchange;
    @Value("${amqp.exchange.engine}")
    String engineExchange;

    void config() {
        var queue = new Queue("test.queue", true);
        rabbitAdmin.declareQueue(queue);
        var exchange = ExchangeBuilder
                .topicExchange(gamesExchange)
                .durable(true)
                .build();
        rabbitAdmin.declareExchange(exchange);
        Binding binding = new Binding(
                "test.queue",
                Binding.DestinationType.QUEUE,
                gamesExchange,
                "game",
                null
        );
        rabbitAdmin.declareBinding(binding);

        var queue2 = new Queue("test.move.queue", true);
        rabbitAdmin.declareQueue(queue2);
        var exchange2 = ExchangeBuilder
                .topicExchange(movesExchange)
                .durable(true)
                .build();
        rabbitAdmin.declareExchange(exchange2);
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
        baseUrl = "ws://localhost".concat(":").concat(String.valueOf(port));
        stompClient = new WebSocketStompClient(
                new StandardWebSocketClient()
        );
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        if (!rabbitSetupIsDone) {
            config();
        }
    }

    @AfterEach
    void tearDown() {
        rabbitAdmin.purgeQueue(stateQueue);
        rabbitAdmin.purgeQueue("test.queue");
        rabbitAdmin.purgeQueue("test.move.queue");
        gameRepository.deleteAll();
    }

    @Test
    void shouldConnectToWebsocket() throws Exception {
        StompSession session = stompClient
                .connectAsync(baseUrl + "/play/websocket", new StompSessionHandlerAdapter() {})
                .get(1, SECONDS);
        await()
                .atMost(1, SECONDS)
                .untilAsserted(() -> {
                    assertThat(session.isConnected(), equalTo(true));
                });
    }
}