package io.github.xpakx.battleships.game;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GameType {
    USER("User"),
    AI("AI");

    private final String type;
}
