package io.github.xpakx.battleships.game;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MoveRepository extends JpaRepository<Move, Long> {
    List<Move> findByGameIdOrderByTimestampAsc(Long id);

}