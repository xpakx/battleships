package io.github.xpakx.battleships.game;

import io.github.xpakx.battleships.security.JwtUtils;
import io.github.xpakx.battleships.user.User;
import io.github.xpakx.battleships.user.UserRepository;
import io.restassured.http.Header;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MoveControllerTest {

    @LocalServerPort
    private int port;
    private String baseUrl;
    private Long userId;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:15.1")
    ).withDatabaseName("ships_test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    UserRepository userRepository;

    @Autowired
    GameRepository gameRepository;

    @Autowired
    MoveRepository moveRepository;

    @Autowired
    JwtUtils jwt;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost".concat(":").concat(String.valueOf(port));
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
    }

    @Test
    void unauthorizedUserShouldNotBeAbleToViewMoveHistory() {
        given()
                .when()
                .get(baseUrl + "/game/1/history")
                .then()
                .statusCode(UNAUTHORIZED.value());
    }

    @Test
    void gameShouldExist() {
        given()
                .header(getHeaderForUser("test_user"))
                .when()
                .get(baseUrl + "/game/1/history")
                .then()
                .statusCode(NOT_FOUND.value())
                .body("message", containsStringIgnoringCase("game not found"));
    }

    @Test
    void shouldRespondWithEmptyListOfMoves() {
        var gameId = createGame();
        given()
                .header(getHeaderForUser("test_user"))
                .when()
                .get(baseUrl + "/game/{gameId}/history", gameId)
                .then()
                .statusCode(OK.value())
                .body("$", hasSize(0));
    }

    @Test
    void shouldRespondWithMoves() {
        var gameId = createGame();
        createMove(gameId, 0, 0, LocalDateTime.of(1993, Month.SEPTEMBER, 1, 13, 0));
        createMove(gameId, 1, 1, LocalDateTime.of(1993, Month.SEPTEMBER, 1, 13, 1));
        createMove(gameId, 3, 3, LocalDateTime.of(1993, Month.SEPTEMBER, 1, 13, 15));
        createMove(gameId, 2, 2, LocalDateTime.of(1993, Month.SEPTEMBER, 1, 13, 4));

        given()
                .header(getHeaderForUser("test_user"))
                .when()
                .get(baseUrl + "/game/{gameId}/history", gameId)
                .then()
                .statusCode(OK.value())
                .body("$", hasSize(4))
                .body("[0].row", equalTo(0))
                .body("[0].column", equalTo(0))
                .body("[1].row", equalTo(1))
                .body("[1].column", equalTo(1))
                .body("[2].row", equalTo(2))
                .body("[2].column", equalTo(2))
                .body("[3].row", equalTo(3))
                .body("[3].column", equalTo(3)) ;
    }

    private Header getHeaderForUser(String username) {
        var token = jwt.generateToken(new org.springframework.security.core.userdetails.User(username, "", List.of()));
        var header = "Bearer " + token;
        return new Header("Authorization", header);
    }

    private Long createGame() {
        Game game = new Game();
        game.setUser(userRepository.getReferenceById(userId));
        game.setUserCurrentState("???|???|???");
        game.setOpponentCurrentState("???|???|???");
        game.setUserShips("{}");
        game.setOpponentShips("{}");
        game.setType(GameType.AI);
        game.setRuleset(GameRuleset.Polish);
        game.setAiType(AIType.Random);
        game.setUserStarts(true);
        game.setAccepted(true);
        return gameRepository.save(game).getId();
    }

    private void createMove(Long gameId, Integer row, Integer column, LocalDateTime when) {
        Move move = new Move();
        move.setUser(userRepository.getReferenceById(userId));
        move.setUserCurrentState("???|???|???");
        move.setOpponentCurrentState("???|???|???");
        move.setRow(row);
        move.setColumn(column);
        move.setTimestamp(when);
        move.setGame(gameRepository.getReferenceById(gameId));
        moveRepository.save(move);
    }
}