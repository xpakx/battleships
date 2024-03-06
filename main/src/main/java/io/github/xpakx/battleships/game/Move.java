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
public class Move {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer row;
    private Integer column;
    private String userCurrentState;
    private String opponentCurrentState;

    @Column(columnDefinition = "TIME")
    private LocalDateTime timestamp;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    @JsonIgnore
    private Game game;
}
