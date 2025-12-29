package com.toy.store.service;

import com.toy.store.model.*;
import com.toy.store.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 金流服務（模擬第三方支付）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final InvoiceRepository invoiceRepository;
    private final MemberRepository memberRepository;

    /**
     * 創建支付訂單
     */
    @Transactional
    public PaymentOrder createPaymentOrder(Long memberId, BigDecimal amount, PaymentOrder.PaymentMethod method) {
        PaymentOrder order = new PaymentOrder();
        order.setOrderNo(generateOrderNo());
        order.setMemberId(memberId);
        order.setAmount(amount);
        order.setMethod(method);
        order.setStatus(PaymentOrder.PaymentStatus.PENDING);

        log.info("創建支付訂單: {} 金額: {}", order.getOrderNo(), amount);
        return paymentOrderRepository.save(order);
    }

    /**
     * 模擬支付成功回調
     */
    @Transactional
    public boolean processPaymentCallback(String orderNo, String thirdPartyTradeNo, boolean success) {
        PaymentOrder order = paymentOrderRepository.findByOrderNo(orderNo).orElse(null);
        if (order == null) {
            log.error("支付回調失敗：找不到訂單 {}", orderNo);
            return false;
        }

        order.setThirdPartyTradeNo(thirdPartyTradeNo);
        order.setUpdatedAt(LocalDateTime.now());

        if (success) {
            order.setStatus(PaymentOrder.PaymentStatus.PAID);
            order.setPaidAt(LocalDateTime.now());

            // 更新會員餘額
            memberRepository.findById(order.getMemberId()).ifPresent(member -> {
                member.setPlatformWalletBalance(
                        member.getPlatformWalletBalance().add(order.getAmount()));
                memberRepository.save(member);
            });

            // 開立發票
            issueInvoice(order);

            log.info("支付成功: {} 金額: {}", orderNo, order.getAmount());
        } else {
            order.setStatus(PaymentOrder.PaymentStatus.FAILED);
            log.warn("支付失敗: {}", orderNo);
        }

        paymentOrderRepository.save(order);
        return success;
    }

    /**
     * 開立電子發票
     */
    @Transactional
    public Invoice issueInvoice(PaymentOrder order) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNo(Invoice.generateInvoiceNo());
        invoice.setMemberId(order.getMemberId());
        invoice.setPaymentOrderId(order.getId());
        invoice.setAmount(order.getAmount());
        invoice.setTaxAmount(order.getAmount().multiply(BigDecimal.valueOf(0.05))); // 5% 稅
        invoice.setType(Invoice.InvoiceType.B2C);
        invoice.setStatus(Invoice.InvoiceStatus.ISSUED);
        invoice.setCarrierType("MEMBER"); // 會員載具

        log.info("開立發票: {} 金額: {}", invoice.getInvoiceNo(), invoice.getAmount());
        return invoiceRepository.save(invoice);
    }

    /**
     * 查詢會員發票紀錄
     */
    public List<Invoice> getMemberInvoices(Long memberId) {
        return invoiceRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
    }

    /**
     * 生成訂單號
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "PAY" + timestamp + random;
    }
}
