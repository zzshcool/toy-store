package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 訂單實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Long id;

    // 會員 ID（手動關聯）
    private Long memberId;

    // 會員物件（非持久化，由 Service 層填充）
    private transient Member member;

    private BigDecimal totalPrice;

    private BigDecimal discountAmount = BigDecimal.ZERO;

    private String couponName;

    private OrderStatus status = OrderStatus.PENDING;

    // 訂單項目（非持久化，由 Service 層填充）
    private transient List<OrderItem> items = new ArrayList<>();

    private LocalDateTime createTime = LocalDateTime.now();

    public enum OrderStatus {
        PENDING, PAID, SHIPPED, COMPLETED, CANCELLED, REFUNDED
    }

    public void setStatus(String status) {
        if (status != null) {
            this.status = OrderStatus.valueOf(status.toUpperCase());
        }
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
}
