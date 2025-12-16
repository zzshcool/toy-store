package com.toy.store.repository;

import com.toy.store.model.RouletteGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RouletteGameRepository extends JpaRepository<RouletteGame, Long> {

    List<RouletteGame> findByIpId(Long ipId);

    List<RouletteGame> findByStatus(RouletteGame.Status status);

    List<RouletteGame> findByIpIdAndStatus(Long ipId, RouletteGame.Status status);

    List<RouletteGame> findAllByOrderByCreatedAtDesc();
}
