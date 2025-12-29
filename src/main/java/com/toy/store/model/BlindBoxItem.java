package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 盲盒單品實體（中盒內的每個小盒）
 * 對應規格書 §4.D 動漫周邊系統
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "blind_box_items")
public class BlindBoxItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blind_box_id", nullable = false)
    @JsonIgnore
    private BlindBox blindBox;

    @Column(nullable = false)
    private Integer boxNumber; // 盒號（1-12）

    @Column(nullable = false)
    private String prizeName; // 內容物名稱（如：路飛公仔、索隆模型）

    private String prizeDescription;
    private String prizeImageUrl;
    private BigDecimal estimatedValue; // 估計價值

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rarity rarity = Rarity.NORMAL; // 稀有度

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.AVAILABLE;

    // 鎖定資訊（倒數確認期）
    private Long lockedByMemberId;
    private LocalDateTime lockedAt;

    // 購買資訊
    private Long purchasedByMemberId;
    private LocalDateTime purchasedAt;

    public enum Rarity {
        NORMAL("普通款"),
        RARE("稀有款"),
        ULTRA_RARE("超稀有款"),
        SECRET("隱藏款");

        private final String displayName;

        Rarity(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Status {
        AVAILABLE, // 可選擇
        LOCKED, // 鎖定中（180秒倒數）
        SOLD // 已售出
    }

    /**
     * 檢查鎖定是否已過期（180秒）
     */
    public boolean isLockExpired() {
        if (status != Status.LOCKED || lockedAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(lockedAt.plusSeconds(180));
    }

    /**
     * 鎖定盒子（開始倒數確認期）
     */
    public void lock(Long memberId) {
        this.status = Status.LOCKED;
        this.lockedByMemberId = memberId;
        this.lockedAt = LocalDateTime.now();
    }

    /**
     * 釋放鎖定
     */
    public void releaseLock() {
        this.status = Status.AVAILABLE;
        this.lockedByMemberId = null;
        this.lockedAt = null;
    }

    /**
     * 確認購買
     */
    public void purchase(Long memberId) {
        this.status = Status.SOLD;
        this.purchasedByMemberId = memberId;
        this.purchasedAt = LocalDateTime.now();
    }

    /**
     * 取得剩餘鎖定秒數
     */
    public int getRemainingLockSeconds() {
        if (status != Status.LOCKED || lockedAt == null) {
            return 0;
        }
        long elapsed = java.time.Duration.between(lockedAt, LocalDateTime.now()).getSeconds();
        return Math.max(0, 180 - (int) elapsed);
    }
}
