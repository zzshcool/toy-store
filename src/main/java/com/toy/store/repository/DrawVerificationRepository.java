package com.toy.store.repository;

import com.toy.store.model.DrawVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DrawVerificationRepository extends JpaRepository<DrawVerification, Long> {

    Optional<DrawVerification> findByGameTypeAndGameId(
            DrawVerification.GameType gameType, Long gameId);

    List<DrawVerification> findByCompletedTrueOrderByCompletedAtDesc();

    List<DrawVerification> findTop10ByCompletedTrueOrderByCompletedAtDesc();
}
