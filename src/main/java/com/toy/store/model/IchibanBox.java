package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 一番賞箱體實體 - 純 POJO (MyBatis)
 * 每個箱體包含多個格子(Slot)和多種獎品(Prize)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IchibanBox {

    private Long id;

    private Long ipId; // IP ID（手動關聯）
    private transient GachaIp ip;

    private String name; // 箱體名稱，如：洛克人一番賞 第一彈

    private String description;

    private String imageUrl;

    private BigDecimal pricePerDraw; // 單抽價格

    private Integer maxSlots = 80; // 最大格數，預設80

    private Integer totalSlots; // 實際設定的格數

    private Status status = Status.ACTIVE;

    private LocalDateTime startTime; // 開賣時間
    private LocalDateTime endTime; // 結束時間

    private LocalDateTime createdAt = LocalDateTime.now();

    // 關聯列表（非持久化，由 Service 層填充）
    private transient List<IchibanSlot> slots;
    private transient List<IchibanPrize> prizes;

    public enum Status {
        DRAFT, // 草稿
        ACTIVE, // 進行中
        SOLD_OUT, // 售罄
        ENDED // 已結束
    }

    // 取得 IP 名稱（方便顯示）
    public String getIpName() {
        return ip != null ? ip.getName() : "";
    }
}
