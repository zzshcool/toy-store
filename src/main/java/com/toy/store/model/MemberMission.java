package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "member_missions")
public class MemberMission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MissionType type;

    @Column(nullable = false)
    private Integer currentValue = 0;

    @Column(nullable = false)
    private Integer targetValue;

    @Column(nullable = false)
    private Integer rewardBonusPoints;

    @Column(nullable = false)
    private LocalDate missionDate;

    private boolean completed = false;
    private boolean rewardClaimed = false;

    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum MissionType {
        DAILY_LOGIN,
        SPEND_AMOUNT,
        DRAW_COUNT
    }

    public void addProgress(int amount) {
        this.currentValue += amount;
        if (this.currentValue >= this.targetValue) {
            this.completed = true;
        }
        this.updatedAt = LocalDateTime.now();
    }
}
