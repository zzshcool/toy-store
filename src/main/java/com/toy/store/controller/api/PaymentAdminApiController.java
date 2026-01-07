package com.toy.store.controller.api;

import com.toy.store.dto.ApiResponse;
import com.toy.store.model.Invoice;
import com.toy.store.model.Member;
import com.toy.store.model.PaymentOrder;
import com.toy.store.model.Transaction;
import com.toy.store.mapper.InvoiceMapper;
import com.toy.store.mapper.MemberMapper;
import com.toy.store.mapper.PaymentOrderMapper;
import com.toy.store.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 金流/交易後台管理 API
 */
@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasAuthority('FINANCE_MANAGE')")
public class PaymentAdminApiController {

    private final PaymentOrderMapper orderMapper;
    private final TransactionMapper transactionMapper;
    private final InvoiceMapper invoiceMapper;
    private final MemberMapper memberMapper;

    /**
     * 取得所有支付訂單（最新 100 筆）
     */
    @GetMapping("/orders")
    public ApiResponse<List<Map<String, Object>>> getPaymentOrders() {
        List<PaymentOrder> orders = orderMapper.findAllPaged(0, 100);

        List<Map<String, Object>> result = orders.stream()
                .map(this::mapPaymentOrder)
                .collect(Collectors.toList());
        return ApiResponse.ok(result);
    }

    /**
     * 依狀態篩選支付訂單
     */
    @GetMapping("/orders/status/{status}")
    public ApiResponse<List<Map<String, Object>>> getOrdersByStatus(@PathVariable String status) {
        try {
            PaymentOrder.PaymentStatus s = PaymentOrder.PaymentStatus.valueOf(status.toUpperCase());
            List<PaymentOrder> orders = orderMapper.findByStatus(s.name());
            List<Map<String, Object>> result = orders.stream()
                    .map(this::mapPaymentOrder)
                    .collect(Collectors.toList());
            return ApiResponse.ok(result);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("無效的狀態");
        }
    }

    /**
     * 取得所有交易紀錄（最新 100 筆）
     */
    @GetMapping("/transactions")
    public ApiResponse<List<Map<String, Object>>> getTransactions() {
        List<Transaction> transactions = transactionMapper.findAllPaged(0, 100);

        List<Map<String, Object>> result = transactions.stream()
                .map(this::mapTransaction)
                .collect(Collectors.toList());
        return ApiResponse.ok(result);
    }

    /**
     * 取得所有發票（最新 100 筆）
     */
    @GetMapping("/invoices")
    public ApiResponse<List<Map<String, Object>>> getInvoices() {
        List<Invoice> invoices = invoiceMapper.findAllPaged(0, 100);

        List<Map<String, Object>> result = invoices.stream()
                .map(this::mapInvoice)
                .collect(Collectors.toList());
        return ApiResponse.ok(result);
    }

    /**
     * 取得統計摘要
     */
    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> getSummary() {
        long totalOrders = orderMapper.count();
        long pendingOrders = orderMapper.findByStatus(PaymentOrder.PaymentStatus.PENDING.name()).size();
        long paidOrders = orderMapper.findByStatus(PaymentOrder.PaymentStatus.PAID.name()).size();
        long failedOrders = orderMapper.findByStatus(PaymentOrder.PaymentStatus.FAILED.name()).size();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalOrders", totalOrders);
        summary.put("pendingOrders", pendingOrders);
        summary.put("paidOrders", paidOrders);
        summary.put("failedOrders", failedOrders);
        return ApiResponse.ok(summary);
    }

    private Map<String, Object> mapPaymentOrder(PaymentOrder o) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", o.getId());
        map.put("orderNo", o.getOrderNo());
        map.put("memberId", o.getMemberId());
        map.put("amount", o.getAmount());
        map.put("method", o.getPaymentMethod());
        map.put("status", o.getStatus() != null ? o.getStatus().name() : null);
        map.put("createdAt", o.getCreatedAt() != null ? o.getCreatedAt().toString() : null);
        map.put("paidAt", o.getPaidAt() != null ? o.getPaidAt().toString() : null);
        return map;
    }

    private Map<String, Object> mapTransaction(Transaction t) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", t.getId());
        map.put("memberId", t.getMemberId());

        String memberName = memberMapper.findById(t.getMemberId())
                .map(Member::getUsername)
                .orElse("未知用戶");
        map.put("memberName", memberName);

        map.put("amount", t.getAmount());
        map.put("type", t.getType());
        map.put("referenceId", t.getReferenceId());
        map.put("timestamp", t.getCreatedAt() != null ? t.getCreatedAt().toString() : null);
        return map;
    }

    private Map<String, Object> mapInvoice(Invoice i) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", i.getId());
        map.put("invoiceNo", i.getInvoiceNo());
        map.put("memberId", i.getMemberId());
        map.put("amount", i.getAmount());
        map.put("taxAmount", i.getBuyerTaxId()); // Original code had taxAmount which doesn't exist in model, using
                                                 // buyerTaxId
        map.put("type", i.getInvoiceType());
        map.put("status", i.getStatus());
        map.put("createdAt", i.getCreatedAt() != null ? i.getCreatedAt().toString() : null);
        return map;
    }
}
