package io.github.xpakx.battleships.game.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Ship {
    private Integer headX;
    private Integer headY;
    private Integer size;
    private ShipOrientation orientation;
}
