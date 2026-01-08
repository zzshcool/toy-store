package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 扭蛋主題實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GachaTheme {
    private Long id;
    private Long ipId;
    private transient GachaIp ip;
    private String name;
    private String description;
    private String imageUrl;
    private java.math.BigDecimal pricePerGacha;
    private String status = "ACTIVE";
    private LocalDateTime createdAt = LocalDateTime.now();

    public java.math.BigDecimal getPrice() {
        return pricePerGacha;
    }

    public void setPrice(java.math.BigDecimal price) {
        this.pricePerGacha = price;
    }

    private transient java.util.List<GachaItem> items = new java.util.ArrayList<>();

    public java.util.List<GachaItem> getItems() {
        return items;
    }

    public void setItems(java.util.List<GachaItem> items) {
        this.items = items;
    }

    // 取得 IP 名稱（方便顯示）
    public String getIpName() {
        return ip != null ? ip.getName() : "";
    }
}
