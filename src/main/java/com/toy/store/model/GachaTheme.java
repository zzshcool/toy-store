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
    private String name;
    private String description;
    private String imageUrl;
    private java.math.BigDecimal pricePerGacha;
    private String status = "ACTIVE";
    private LocalDateTime createdAt = LocalDateTime.now();
}
