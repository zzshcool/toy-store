package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 發票實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {
    private Long id;
    private Long memberId;
    private Long paymentOrderId;
    private String invoiceNo;
    private String invoiceType;
    private String carrierType;
    private String carrierNo;
    private String buyerName;
    private String buyerTaxId;
    private BigDecimal amount;
    private BigDecimal taxAmount;
    private String status = "PENDING";
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum InvoiceType {
        ELECTRONIC, PAPER, DONATION, B2C
    }

    public enum InvoiceStatus {
        PENDING, ISSUED, VOID
    }

    public static String generateInvoiceNo() {
        return "INV" + System.currentTimeMillis();
    }

    public void setInvoiceType(InvoiceType type) {
        this.invoiceType = type != null ? type.name() : null;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status != null ? status.name() : null;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setType(String type) {
        this.invoiceType = type;
    }
}
