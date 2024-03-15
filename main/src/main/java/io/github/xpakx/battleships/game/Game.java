package io.github.xpakx.battleships.game;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.xpakx.battleships.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private boolean accepted;
    private boolean rejected;
    private GameType type;
    private GameRuleset ruleset;
    private AIType aiType;

    private boolean finished;
    private boolean won;
    private boolean lost;
    private boolean drawn;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime startedAt;
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime lastMoveAt;

    // ?=empty, .=hit, x=sunk, o=miss, |=end of row
    private String userCurrentState;
    private String opponentCurrentState;

    // json
    private String userShips;
    private String opponentShips;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opponent_id")
    @JsonIgnore
    private User opponent;

    private boolean userStarts;
    private boolean userTurn;
}
