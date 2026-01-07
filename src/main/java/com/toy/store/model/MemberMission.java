package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 會員任務實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberMission {
    private Long id;
    private Long memberId;
    private LocalDate missionDate;
    private MissionType type; // DAILY_LOGIN, SPEND_AMOUNT, DRAW_COUNT
    private Integer currentProgress = 0;
    private Integer targetValue;
    private Integer rewardBonusPoints;
    private Boolean isCompleted = false;
    private Boolean rewardClaimed = false;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt = LocalDateTime.now();

    // 枚舉類型（用於 Service 層便利使用）
    public enum MissionType {
        DAILY_LOGIN, SPEND_AMOUNT, DRAW_COUNT
    }

    // 便捷方法
    public boolean isCompleted() {
        return Boolean.TRUE.equals(isCompleted);
    }

    public void addProgress(int amount) {
        this.currentProgress = (this.currentProgress == null ? 0 : this.currentProgress) + amount;
        if (this.currentProgress >= this.targetValue) {
            this.isCompleted = true;
            this.completedAt = LocalDateTime.now();
        }
    }

    public int getProgress() {
        return currentProgress == null ? 0 : currentProgress;
    }

    public boolean isRewardClaimed() {
        return Boolean.TRUE.equals(rewardClaimed);
    }
}
