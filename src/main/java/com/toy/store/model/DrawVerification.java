package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 抽獎驗證實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DrawVerification {
    private Long id;
    private String gameType; // ICHIBAN, BINGO, ROULETTE, GACHA, BLIND_BOX
    private Long gameId;
    private String gameName;
    private String randomSeed;
    private String hashValue;
    private String resultJson;
    private Boolean completed = false;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt = LocalDateTime.now();

    // 枚舉類型
    public enum GameType {
        ICHIBAN, BINGO, ROULETTE, GACHA, BLIND_BOX
    }

    // 便捷方法
    public GameType getGameTypeEnum() {
        try {
            return gameType != null ? GameType.valueOf(gameType) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void setGameType(GameType type) {
        this.gameType = type != null ? type.name() : null;
    }

    public boolean isCompleted() {
        return Boolean.TRUE.equals(completed);
    }
}
