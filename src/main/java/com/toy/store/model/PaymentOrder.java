package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付訂單實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrder {
    private Long id;

    private Long memberId;

    private String orderNo;

    private BigDecimal amount;

    private String paymentMethod;

    private PaymentStatus status = PaymentStatus.PENDING;

    private LocalDateTime paidAt;

    private LocalDateTime createdAt = LocalDateTime.now();

    private String thirdPartyTradeNo;

    private LocalDateTime updatedAt;

    public enum PaymentStatus {
        PENDING, PAID, FAILED, CANCELLED, REFUNDED
    }

    public enum PaymentMethod {
        CREDIT_CARD, LINE_PAY, ECPAY, BANK_TRANSFER, WALLET
    }

    public void setMethod(PaymentMethod method) {
        this.paymentMethod = method != null ? method.name() : null;
    }
}
