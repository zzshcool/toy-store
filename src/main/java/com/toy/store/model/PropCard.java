package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 道具卡實體
 * 對應規格書 §4.D 道具卡支援（提示卡、透視卡、換一盒）
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "prop_cards")
public class PropCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId; // 持有會員

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardType cardType;

    @Column(nullable = false)
    private Integer quantity = 1; // 持有數量

    private LocalDateTime obtainedAt; // 獲得時間
    private LocalDateTime expiresAt; // 過期時間（null 表示永不過期）

    public enum CardType {
        HINT("提示卡", "縮小獎項範圍，剔除一個稀有度等級"),
        PEEK("透視卡", "短暫查看盲盒內容物細節"),
        SWAP("換一盒", "若不滿意當前選擇，可重選其他盒子");

        private final String displayName;
        private final String description;

        CardType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    @PrePersist
    protected void onCreate() {
        if (obtainedAt == null) {
            obtainedAt = LocalDateTime.now();
        }
    }

    /**
     * 是否已過期
     */
    public boolean isExpired() {
        if (expiresAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 是否可用
     */
    public boolean isUsable() {
        return quantity > 0 && !isExpired();
    }

    /**
     * 使用一張
     */
    public boolean use() {
        if (!isUsable()) {
            return false;
        }
        quantity--;
        return true;
    }
}
