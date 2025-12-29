package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付訂單記錄
 * 模擬金流整合（實際需串接第三方支付）
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payment_orders")
public class PaymentOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String orderNo;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    // 第三方交易號
    private String thirdPartyTradeNo;

    // 支付時間
    private LocalDateTime paidAt;

    // 備註
    private String remark;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    public enum PaymentMethod {
        CREDIT_CARD, // 信用卡
        ATM, // ATM 轉帳
        CVS, // 超商代碼
        WEBATM, // 網路 ATM
        LINE_PAY, // LINE Pay
        APPLE_PAY // Apple Pay
    }

    public enum PaymentStatus {
        PENDING, // 待付款
        PAID, // 已付款
        FAILED, // 付款失敗
        CANCELLED, // 已取消
        REFUNDED // 已退款
    }
}
