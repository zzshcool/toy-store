package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
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
    private String missionType;
    private String missionName;
    private Integer progress = 0;
    private Integer target;
    private String rewardType;
    private Integer rewardAmount;
    private String status = "IN_PROGRESS"; // IN_PROGRESS, COMPLETED, CLAIMED
    private LocalDateTime completedAt;
    private LocalDateTime createdAt = LocalDateTime.now();
}
