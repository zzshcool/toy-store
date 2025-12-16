package com.toy.store.repository;

import com.toy.store.model.BingoCell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BingoCellRepository extends JpaRepository<BingoCell, Long> {

    List<BingoCell> findByGameIdOrderByPositionAsc(Long gameId);

    Optional<BingoCell> findByGameIdAndPosition(Long gameId, Integer position);

    List<BingoCell> findByGameIdAndIsRevealed(Long gameId, Boolean isRevealed);

    // 查詢指定行的所有格子
    List<BingoCell> findByGameIdAndRow(Long gameId, Integer row);

    // 查詢指定列的所有格子
    List<BingoCell> findByGameIdAndCol(Long gameId, Integer col);

    // 統計已揭曉格子數量
    @Query("SELECT COUNT(c) FROM BingoCell c WHERE c.game.id = :gameId AND c.isRevealed = true")
    int countRevealedCells(@Param("gameId") Long gameId);
}
