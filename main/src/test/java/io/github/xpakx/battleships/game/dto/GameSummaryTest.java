package io.github.xpakx.battleships.game.dto;

import io.github.xpakx.battleships.game.Game;
import io.github.xpakx.battleships.user.User;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;

class GameSummaryTest {


    @Test
    void shouldMapShips() {
        var ships = """
                [{"headX":8,"headY":6,"size":1,"orientation":"Horizontal"},{"headX":7,"headY":9,"size":1,"orientation":"Vertical"}]
                """;
        var game = new Game();
        game.setUserShips(ships);
        var user = new User();
        user.setUsername("Test");
        game.setUser(user);
        game.setUserCurrentState("");
        game.setOpponentCurrentState("");

        var result = GameSummary.of(game, "Test");

        assertThat(result.getMyShips(), hasSize(2));
        assertThat(result.getMyShips().get(0).getHeadX(), equalTo(8));
        assertThat(result.getMyShips().get(0).getHeadY(), equalTo(6));
        assertThat(result.getMyShips().get(0).getSize(), equalTo(1));
        assertThat(result.getMyShips().get(0).getOrientation(), equalTo(ShipOrientation.Horizontal));
        assertThat(result.getMyShips().get(1).getHeadX(), equalTo(7));
        assertThat(result.getMyShips().get(1).getHeadY(), equalTo(9));
        assertThat(result.getMyShips().get(1).getSize(), equalTo(1));
        assertThat(result.getMyShips().get(1).getOrientation(), equalTo(ShipOrientation.Vertical));

    }
}