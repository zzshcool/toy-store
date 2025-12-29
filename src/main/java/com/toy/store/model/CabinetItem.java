package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 盒櫃獎品實體
 * 對應規格書 §4.D, §8.A - 獎品進入盒櫃，滿 5 件免運
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cabinet_items")
public class CabinetItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    // 來源資訊
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SourceType sourceType;

    private Long sourceId; // 對應的抽獎記錄 ID

    @Column(nullable = false)
    private String prizeName;

    private String prizeDescription;
    private String prizeImageUrl;
    private String prizeRank; // A賞、B賞 等

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.IN_CABINET;

    private LocalDateTime obtainedAt; // 獲得時間
    private LocalDateTime requestedAt; // 申請發貨時間
    private Long shipmentRequestId; // 關聯的發貨申請

    public enum SourceType {
        ICHIBAN("一番賞"),
        ROULETTE("轉盤"),
        BINGO("九宮格"),
        GACHA("扭蛋"),
        BLINDBOX("盲盒"),
        REDEEM("積分兌換");

        private final String displayName;

        SourceType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Status {
        IN_CABINET, // 在盒櫃中
        PENDING_SHIP, // 待發貨
        SHIPPED, // 已發貨
        DELIVERED, // 已送達
        EXCHANGED // 已兌換（轉積分）
    }

    @PrePersist
    protected void onCreate() {
        if (obtainedAt == null) {
            obtainedAt = LocalDateTime.now();
        }
    }
}
