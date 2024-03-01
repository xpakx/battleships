package io.github.xpakx.battleships.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GameRuleset {
    CLASSIC("Classic"),
    POLISH("Polish");

    private final String name;
}
