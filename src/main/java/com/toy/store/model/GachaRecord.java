package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * 抽獎紀錄實體
 * 記錄所有抽獎行為（一番賞、轉盤、九宮格）
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "gacha_records")
public class GachaRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GachaType gachaType;

    private Long gameId; // 對應的遊戲ID

    @Column(nullable = false)
    private String prizeName; // 獲得的獎品名稱

    private String prizeRank; // 獎品等級（一番賞用）

    private Integer shardsEarned = 0; // 本次獲得碎片

    private Integer luckyValueEarned = 0; // 本次獲得幸運值

    private Boolean isGuarantee = false; // 是否為保底觸發

    private Boolean isDuplicate = false; // 是否為重複款

    private BigDecimal prizeValue = BigDecimal.ZERO; // 獎品當前價值 (用於 RTP 計算)

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum GachaType {
        ICHIBAN("一番賞"),
        ROULETTE("轉盤"),
        BINGO("九宮格"),
        GACHA("扭蛋");

        private final String displayName;

        GachaType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // 建立一番賞紀錄
    public static GachaRecord createIchibanRecord(Long memberId, Long boxId, String prizeName,
            String rank, int shards, BigDecimal value) {
        GachaRecord record = new GachaRecord();
        record.setMemberId(memberId);
        record.setGachaType(GachaType.ICHIBAN);
        record.setGameId(boxId);
        record.setPrizeName(prizeName);
        record.setPrizeRank(rank);
        record.setShardsEarned(shards);
        record.setPrizeValue(value);
        return record;
    }

    // 建立轉盤紀錄
    public static GachaRecord createRouletteRecord(Long memberId, Long gameId, String prizeName,
            int shards, int luckyValue, boolean isGuarantee, BigDecimal value) {
        GachaRecord record = new GachaRecord();
        record.setMemberId(memberId);
        record.setGachaType(GachaType.ROULETTE);
        record.setGameId(gameId);
        record.setPrizeName(prizeName);
        record.setShardsEarned(shards);
        record.setLuckyValueEarned(luckyValue);
        record.setIsGuarantee(isGuarantee);
        record.setPrizeValue(value);
        return record;
    }

    // 建立九宮格紀錄
    public static GachaRecord createBingoRecord(Long memberId, Long gameId, String prizeName, int shards,
            BigDecimal value) {
        GachaRecord record = new GachaRecord();
        record.setMemberId(memberId);
        record.setGachaType(GachaType.BINGO);
        record.setGameId(gameId);
        record.setPrizeName(prizeName);
        record.setShardsEarned(shards);
        record.setPrizeValue(value);
        return record;
    }
}
