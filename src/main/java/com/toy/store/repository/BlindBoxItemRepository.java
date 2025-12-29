package com.toy.store.repository;

import com.toy.store.model.BlindBoxItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BlindBoxItemRepository extends JpaRepository<BlindBoxItem, Long> {

    List<BlindBoxItem> findByBlindBox_IdOrderByBoxNumberAsc(Long blindBoxId);

    Optional<BlindBoxItem> findByBlindBox_IdAndBoxNumber(Long blindBoxId, Integer boxNumber);

    List<BlindBoxItem> findByBlindBox_IdAndStatus(Long blindBoxId, BlindBoxItem.Status status);

    // 查詢過期的鎖定盒子
    @Query("SELECT i FROM BlindBoxItem i WHERE i.status = 'LOCKED' AND i.lockedAt < :expireTime")
    List<BlindBoxItem> findExpiredLocks(@Param("expireTime") LocalDateTime expireTime);

    // 批量釋放過期鎖定
    @Modifying
    @Query("UPDATE BlindBoxItem i SET i.status = 'AVAILABLE', i.lockedByMemberId = null, i.lockedAt = null " +
            "WHERE i.status = 'LOCKED' AND i.lockedAt < :expireTime")
    int releaseExpiredLocks(@Param("expireTime") LocalDateTime expireTime);

    // 統計可用盒子數量
    @Query("SELECT COUNT(i) FROM BlindBoxItem i WHERE i.blindBox.id = :boxId AND i.status = 'AVAILABLE'")
    int countAvailableItems(@Param("boxId") Long boxId);
}
