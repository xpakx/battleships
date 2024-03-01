package io.github.xpakx.battleships.game.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnginePlacementEvent {
    private Long gameId;
    private String ships;
    private boolean firstUser;
    private boolean legal;
}
