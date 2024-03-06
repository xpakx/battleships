package io.github.xpakx.battleships.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GameRuleset {
    Classic("Classic"),
    Polish("Polish");

    private final String name;
}
