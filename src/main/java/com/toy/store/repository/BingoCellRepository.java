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

    List<BingoCell> findByGame_IdOrderByPositionAsc(Long gameId);

    Optional<BingoCell> findByGame_IdAndPosition(Long gameId, Integer position);

    List<BingoCell> findByGame_IdAndIsRevealed(Long gameId, Boolean isRevealed);

    // 查詢指定行的所有格子
    List<BingoCell> findByGame_IdAndRow(Long gameId, Integer row);

    // 查詢指定列的所有格子
    List<BingoCell> findByGame_IdAndCol(Long gameId, Integer col);

    // 統計已揭曉格子數量
    @Query("SELECT COUNT(c) FROM BingoCell c WHERE c.game.id = :gameId AND c.isRevealed = true")
    int countRevealedCells(@Param("gameId") Long gameId);
}
