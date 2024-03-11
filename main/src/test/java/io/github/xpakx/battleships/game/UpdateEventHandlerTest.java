package io.github.xpakx.battleships.game;

import io.github.xpakx.battleships.game.dto.UpdateEvent;
import io.github.xpakx.battleships.user.User;
import io.github.xpakx.battleships.user.UserRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.reset;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UpdateEventHandlerTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:15.1")
    ).withDatabaseName("ships_test");
    @Container
    static RabbitMQContainer rabbitMq = new RabbitMQContainer(
            DockerImageName.parse("rabbitmq:3.7.25-management-alpine")
    );

    @Value("${amqp.queue.updates}")
    String updateQueue;

    @Value("${amqp.exchange.updates}")
    String updateExchange;

    @Autowired
    UserRepository userRepository;

    @Autowired
    GameRepository gameRepository;

    @Autowired
    MoveRepository moveRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RabbitAdmin rabbitAdmin;

    @SpyBean
    UpdateEventHandler updateHandler;

    Long userId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        User user = new User();
        user.setPassword("password");
        user.setUsername("test_user");
        this.userId = userRepository.save(user).getId();
    }

    @AfterEach
    void tearDown() {
        moveRepository.deleteAll();
        gameRepository.deleteAll();
        userRepository.deleteAll();
        rabbitAdmin.purgeQueue(updateQueue);
        reset(updateHandler);
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.rabbitmq.username", rabbitMq::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitMq::getAdminPassword);
        registry.add("spring.rabbitmq.host", rabbitMq::getHost);
        registry.add("spring.rabbitmq.port", rabbitMq::getAmqpPort);
    }

    @Test
    public void shouldCallListener() {
        var game = new UpdateEvent();
        game.setGameId(5L);
        game.setUserCurrentState("???|???|???");
        game.setOpponentCurrentState("???|???|???");
        game.setUserShips("{}");
        game.setOpponentShips("{}");
        game.setFinished(true);
        game.setWon(true);
        rabbitTemplate.convertAndSend(updateExchange, "update", game);
        await()
                .atMost(5, TimeUnit.SECONDS)
                .until(isMessageConsumed(), Matchers.is(true));
    }

    @Test
    public void shouldUpdateGame() {
        var opponentId = createUser("opponent");
        var gameId = createGame(userId, opponentId, false);
        var game = new UpdateEvent();
        game.setGameId(gameId);
        game.setUserCurrentState("new state");
        game.setOpponentCurrentState("new opponent state");
        game.setUserShips("{}");
        game.setOpponentShips("{}");
        game.setFinished(true);
        game.setWon(true);
        rabbitTemplate.convertAndSend(updateExchange, "update", game);
        await()
                .atMost(5, TimeUnit.SECONDS)
                .until(isMessageConsumed(), Matchers.is(true)); // TODO
        try {
            Thread.sleep(100);
        } catch (Exception e) {}
        var gameOpt = gameRepository.findById(gameId);
        assert(gameOpt.isPresent());
        assertThat(gameOpt.get().isFinished(), is(true));
        assertThat(gameOpt.get().isWon(), is(true));
        assertThat(gameOpt.get().getUserCurrentState(), equalTo("new state"));
        assertThat(gameOpt.get().getOpponentCurrentState(), equalTo("new opponent state"));
    }

    private Callable<Boolean> isMessageConsumed() {
        return () ->
                mockingDetails(updateHandler).getInvocations().stream()
                        .anyMatch(invocation -> invocation.getMethod().getName().equals("handleGame"));
    }

    private Long createUser(String username) {
        User user = new User();
        user.setPassword("password");
        user.setUsername(username);
        return userRepository.save(user).getId();
    }

    private Long createGame(Long userId, Long opponentId, boolean finished) {
        Game game = new Game();
        game.setUser(userRepository.getReferenceById(userId));
        game.setOpponent(userRepository.getReferenceById(opponentId));
        game.setUserCurrentState("???|???|???");
        game.setOpponentCurrentState("???|???|???");
        game.setUserShips("{}");
        game.setOpponentShips("{}");
        game.setType(GameType.USER);
        game.setRuleset(GameRuleset.Polish);
        game.setAiType(AIType.None);
        game.setUserStarts(true);
        game.setAccepted(true);
        game.setFinished(finished);
        return gameRepository.save(game).getId();
    }

}