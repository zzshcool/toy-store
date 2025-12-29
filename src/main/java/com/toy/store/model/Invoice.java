package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 電子發票記錄
 * 模擬電子發票整合
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String invoiceNo;

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "payment_order_id")
    private Long paymentOrderId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private BigDecimal taxAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status = InvoiceStatus.ISSUED;

    // 載具類型（手機條碼/自然人憑證/會員載具）
    private String carrierType;
    private String carrierNo;

    // 捐贈碼（如捐贈發票）
    private String donationCode;

    // 發票日期
    private LocalDateTime invoiceDate = LocalDateTime.now();

    // 作廢日期
    private LocalDateTime voidDate;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum InvoiceType {
        B2C, // 個人發票
        B2B, // 公司發票
        DONATION // 捐贈發票
    }

    public enum InvoiceStatus {
        ISSUED, // 已開立
        VOID, // 已作廢
        ALLOWANCE // 已折讓
    }

    /**
     * 生成發票號碼（模擬）
     */
    public static String generateInvoiceNo() {
        // 格式：XX-12345678 (模擬)
        String prefix = String.valueOf((char) ('A' + (int) (Math.random() * 26)))
                + String.valueOf((char) ('A' + (int) (Math.random() * 26)));
        String number = String.format("%08d", (int) (Math.random() * 100000000));
        return prefix + "-" + number;
    }
}
