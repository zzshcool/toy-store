package com.toy.store.controller.api;

import com.toy.store.dto.ApiResponse;
import com.toy.store.model.Invoice;
import com.toy.store.model.PaymentOrder;
import com.toy.store.model.Transaction;
import com.toy.store.repository.InvoiceRepository;
import com.toy.store.repository.PaymentOrderRepository;
import com.toy.store.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    private final PaymentOrderRepository orderRepository;
    private final TransactionRepository transactionRepository;
    private final InvoiceRepository invoiceRepository;

    /**
     * 取得所有支付訂單（最新 100 筆）
     */
    @GetMapping("/orders")
    public ApiResponse<List<Map<String, Object>>> getPaymentOrders() {
        List<PaymentOrder> orders = orderRepository.findAll(
                PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();

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
            List<PaymentOrder> orders = orderRepository.findByStatus(s);
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
        List<Transaction> transactions = transactionRepository.findAll(
                PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "timestamp"))).getContent();

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
        List<Invoice> invoices = invoiceRepository.findAll(
                PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();

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
        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.findByStatus(PaymentOrder.PaymentStatus.PENDING).size();
        long paidOrders = orderRepository.findByStatus(PaymentOrder.PaymentStatus.PAID).size();
        long failedOrders = orderRepository.findByStatus(PaymentOrder.PaymentStatus.FAILED).size();

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
        map.put("method", o.getMethod() != null ? o.getMethod().name() : null);
        map.put("status", o.getStatus() != null ? o.getStatus().name() : null);
        map.put("createdAt", o.getCreatedAt() != null ? o.getCreatedAt().toString() : null);
        map.put("paidAt", o.getPaidAt() != null ? o.getPaidAt().toString() : null);
        return map;
    }

    private Map<String, Object> mapTransaction(Transaction t) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", t.getId());
        map.put("memberId", t.getMember() != null ? t.getMember().getId() : null);
        map.put("memberName", t.getMember() != null ? t.getMember().getUsername() : null);
        map.put("amount", t.getAmount());
        map.put("type", t.getType() != null ? t.getType().name() : null);
        map.put("referenceId", t.getReferenceId());
        map.put("timestamp", t.getTimestamp() != null ? t.getTimestamp().toString() : null);
        return map;
    }

    private Map<String, Object> mapInvoice(Invoice i) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", i.getId());
        map.put("invoiceNo", i.getInvoiceNo());
        map.put("memberId", i.getMemberId());
        map.put("amount", i.getAmount());
        map.put("taxAmount", i.getTaxAmount());
        map.put("type", i.getType() != null ? i.getType().name() : null);
        map.put("status", i.getStatus() != null ? i.getStatus().name() : null);
        map.put("createdAt", i.getCreatedAt() != null ? i.getCreatedAt().toString() : null);
        return map;
    }
}
