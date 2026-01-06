package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * 訂單項目實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private Long id;

    // 訂單 ID（手動關聯）
    private Long orderId;

    // 訂單物件（非持久化）
    private transient Order order;

    private Long productId;

    private String productName;

    private BigDecimal price;

    private Integer quantity;
}
