package io.github.xpakx.battleships.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AIType {
    Random("Random"),
    Greedy("Greedy"),
    Parity("Parity"),
    Probability("Probability"),
    None("None") ;

    private final String type;
}
