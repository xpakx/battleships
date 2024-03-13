package io.github.xpakx.battleships.game.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlacementMessage {
    private String player;
    private boolean legal;

    public static PlacementMessage rejected(String username) {
        var msg = new PlacementMessage();
        msg.setPlayer(username);
        msg.setLegal(false);
        return msg;
    }

    public static PlacementMessage accepted(String username) {
        var msg = new PlacementMessage();
        msg.setPlayer(username);
        msg.setLegal(true);
        return msg;
    }
}
