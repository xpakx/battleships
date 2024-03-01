package io.github.xpakx.battleships.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AIType {
    RANDOM("Random"),
    GREEDY("Greedy"),
    NONE("None") ;

    private final String type;
}