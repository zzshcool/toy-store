package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * IP 主題實體 - 純 POJO (MyBatis)
 * 例如：洛克人、航海王、鬼滅之刃等授權IP
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GachaIp {

    private Long id;

    private String name; // IP 名稱，如：洛克人

    private String description;

    private String imageUrl; // IP 主視覺圖

    private Status status = Status.ACTIVE;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    // 關聯列表（非持久化，由 Service 層填充）
    private transient List<IchibanBox> ichibanBoxes;
    private transient List<RouletteGame> rouletteGames;
    private transient List<BingoGame> bingoGames;
    private transient List<GachaTheme> gachaThemes;

    public enum Status {
        ACTIVE, // 啟用中
        INACTIVE, // 停用
        COMING_SOON // 即將推出
    }
}
