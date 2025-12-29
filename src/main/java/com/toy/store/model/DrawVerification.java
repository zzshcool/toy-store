package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 完售驗證記錄
 * 用於透明化展示抽獎結果的公正性
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "draw_verification")
public class DrawVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameType gameType;

    @Column(name = "game_id", nullable = false)
    private Long gameId;

    @Column(nullable = false)
    private String gameName;

    // 隨機種子（抽獎開始時生成）
    @Column(name = "random_seed", nullable = false)
    private String randomSeed;

    // SHA256 哈希值
    @Column(name = "hash_value", nullable = false, length = 64)
    private String hashValue;

    // 結果 JSON（完售後記錄）
    @Column(name = "result_json", columnDefinition = "TEXT")
    private String resultJson;

    // 完售時間
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // 創建時間
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // 是否已完售
    @Column(name = "is_completed")
    private boolean completed = false;

    public enum GameType {
        ICHIBAN, ROULETTE, BINGO, GACHA, BLINDBOX
    }
}
