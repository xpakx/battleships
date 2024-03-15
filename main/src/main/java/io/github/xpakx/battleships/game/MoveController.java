package io.github.xpakx.battleships.game;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MoveController {
    private final MoveService moveService;

    @GetMapping("/game/{gameId}/history")
    public ResponseEntity<List<Move>> getMoves(@PathVariable Long gameId) {
        return ResponseEntity.ok(
                moveService.getMoveHistory(gameId)
        );
    }
}
