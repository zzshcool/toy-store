package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;

/**
 * 轉盤獎格實體
 * 定義每個格子的類型、權重與獎品
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "roulette_slots")
public class RouletteSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    @JsonIgnore
    private RouletteGame game;

    @Column(nullable = false)
    private Integer slotOrder; // 格子順序 1-25

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlotType slotType; // 格子類型

    @Column(nullable = false)
    private String prizeName; // 獎品名稱

    private String prizeDescription;
    private String prizeImageUrl;
    private BigDecimal prizeValue; // 獎品價值

    @Column(nullable = false)
    private Integer weight = 100; // 權重（用於隨機計算）

    // 碎片數（如果是碎片類型）
    private Integer shardAmount;

    // 顏色（用於前端顯示）
    private String color = "#FFD700";

    public enum SlotType {
        JACKPOT("大獎", "#FF0000"), // 大獎
        RARE("稀有獎", "#FF6600"), // 稀有獎
        NORMAL("普通獎", "#FFD700"), // 普通獎
        FREE_SPIN("再來一次", "#00FF00"), // 再來一次
        SHARD("碎片", "#AAAAAA"); // 碎片獎勵

        private final String displayName;
        private final String defaultColor;

        SlotType(String displayName, String defaultColor) {
            this.displayName = displayName;
            this.defaultColor = defaultColor;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDefaultColor() {
            return defaultColor;
        }
    }

    // 是否為大獎（用於保底判斷）
    public boolean isJackpot() {
        return slotType == SlotType.JACKPOT || slotType == SlotType.RARE;
    }

    // 取得遊戲名稱
    public String getGameName() {
        return game != null ? game.getName() : "";
    }
}
