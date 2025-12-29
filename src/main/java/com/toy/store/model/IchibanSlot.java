package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

import com.toy.store.service.GachaProbabilityEngine;

/**
 * 一番賞格子實體
 * 每個格子對應一個編號和一個獎品
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ichiban_slots")
public class IchibanSlot implements GachaProbabilityEngine.ProbableItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "box_id", nullable = false)
    @JsonIgnore
    private IchibanBox box;

    @Column(nullable = false)
    private Integer slotNumber; // 格子編號 1-80

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.AVAILABLE;

    // 鎖定資訊（防止併發衝突）
    private Long lockedByMemberId;
    private LocalDateTime lockedAt;

    // 揭曉資訊
    private Long revealedByMemberId;
    private LocalDateTime revealedAt;

    // 該格子對應的獎品
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "prize_id")
    private IchibanPrize prize;

    public enum Status {
        AVAILABLE, // 可選擇
        LOCKED, // 鎖定中（3分鐘內）
        REVEALED // 已揭曉
    }

    // 檢查鎖定是否已過期（3分鐘）
    public boolean isLockExpired() {
        if (status != Status.LOCKED || lockedAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(lockedAt.plusMinutes(3));
    }

    // 鎖定格子
    public void lock(Long memberId) {
        this.status = Status.LOCKED;
        this.lockedByMemberId = memberId;
        this.lockedAt = LocalDateTime.now();
    }

    // 釋放鎖定
    public void releaseLock() {
        this.status = Status.AVAILABLE;
        this.lockedByMemberId = null;
        this.lockedAt = null;
    }

    // 揭曉格子
    public void reveal(Long memberId) {
        this.status = Status.REVEALED;
        this.revealedByMemberId = memberId;
        this.revealedAt = LocalDateTime.now();
    }

    @Override
    public Integer getWeight() {
        return 1; // 一番賞每個格子機率相等
    }

    @Override
    public GachaProbabilityEngine.PrizeTier getTier() {
        if (prize == null)
            return GachaProbabilityEngine.PrizeTier.NORMAL;
        IchibanPrize.Rank rank = prize.getRank();
        if (rank == IchibanPrize.Rank.A || rank == IchibanPrize.Rank.B) {
            return GachaProbabilityEngine.PrizeTier.JACKPOT;
        }
        if (rank == IchibanPrize.Rank.C || rank == IchibanPrize.Rank.D) {
            return GachaProbabilityEngine.PrizeTier.RARE;
        }
        return GachaProbabilityEngine.PrizeTier.NORMAL;
    }

    // 格式化編號顯示（補零）
    public String getFormattedNumber() {
        return String.format("%02d", slotNumber);
    }
}
