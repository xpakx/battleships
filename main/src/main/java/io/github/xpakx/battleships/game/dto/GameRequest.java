package io.github.xpakx.battleships.game.dto;

import io.github.xpakx.battleships.game.AIType;
import io.github.xpakx.battleships.game.GameRuleset;
import io.github.xpakx.battleships.game.GameType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class GameRequest {
    @NotNull(message = "Game type cannot be null!")
    private GameType type;
    private String opponent;
    @NotNull(message = "Game must have a rule set")
    private GameRuleset rules;
    private AIType aiType;

    @AssertTrue(message = "User game request must have opponent username!")
    public boolean isOpponentIdSetForNonAIType() {
        return type != GameType.USER || Objects.nonNull(opponent);
    }

    @AssertTrue(message = "AI game request should not have opponent username!")
    public boolean isOpponentIdUnsetForNonUserType() {
        return type == GameType.USER || Objects.isNull(opponent);
    }

    @AssertTrue(message = "AI game must have specified AI type!")
    public boolean isAITypeSetForAIGame() {
        return type == GameType.USER || (Objects.nonNull(aiType) && aiType != AIType.None);
    }

    @AssertTrue(message = "non-AI game cannot have AI type!")
    public boolean isAITypeUnsetForNonAIGame() {
        return type == GameType.AI || Objects.isNull(aiType) || aiType == AIType.None;
    }
}
