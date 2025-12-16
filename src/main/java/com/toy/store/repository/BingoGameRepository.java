package com.toy.store.repository;

import com.toy.store.model.BingoGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BingoGameRepository extends JpaRepository<BingoGame, Long> {

    List<BingoGame> findByIpId(Long ipId);

    List<BingoGame> findByStatus(BingoGame.Status status);

    List<BingoGame> findByIpIdAndStatus(Long ipId, BingoGame.Status status);

    List<BingoGame> findAllByOrderByCreatedAtDesc();
}
