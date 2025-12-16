package com.toy.store.repository;

import com.toy.store.model.IchibanSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IchibanSlotRepository extends JpaRepository<IchibanSlot, Long> {

    List<IchibanSlot> findByBoxIdOrderBySlotNumberAsc(Long boxId);

    Optional<IchibanSlot> findByBoxIdAndSlotNumber(Long boxId, Integer slotNumber);

    List<IchibanSlot> findByBoxIdAndStatus(Long boxId, IchibanSlot.Status status);

    // 查詢過期的鎖定格子
    @Query("SELECT s FROM IchibanSlot s WHERE s.status = 'LOCKED' AND s.lockedAt < :expireTime")
    List<IchibanSlot> findExpiredLocks(@Param("expireTime") LocalDateTime expireTime);

    // 統計可用格子數量
    @Query("SELECT COUNT(s) FROM IchibanSlot s WHERE s.box.id = :boxId AND s.status = 'AVAILABLE'")
    int countAvailableSlots(@Param("boxId") Long boxId);

    // 批量釋放過期鎖定
    @Modifying
    @Query("UPDATE IchibanSlot s SET s.status = 'AVAILABLE', s.lockedByMemberId = null, s.lockedAt = null " +
            "WHERE s.status = 'LOCKED' AND s.lockedAt < :expireTime")
    int releaseExpiredLocks(@Param("expireTime") LocalDateTime expireTime);
}
