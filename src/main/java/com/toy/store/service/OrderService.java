package com.toy.store.service;

import com.toy.store.exception.AppException;
import com.toy.store.model.*;
import com.toy.store.mapper.MemberCouponMapper;
import com.toy.store.mapper.OrderMapper;
import com.toy.store.mapper.ProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final CartService cartService;
    private final TransactionService transactionService;
    private final ProductMapper productMapper;
    private final MemberCouponMapper memberCouponMapper;

    public OrderService(
            OrderMapper orderMapper,
            CartService cartService,
            TransactionService transactionService,
            ProductMapper productMapper,
            MemberCouponMapper memberCouponMapper) {
        this.orderMapper = orderMapper;
        this.cartService = cartService;
        this.transactionService = transactionService;
        this.productMapper = productMapper;
        this.memberCouponMapper = memberCouponMapper;
    }

    @Transactional
    public Order checkout(Long memberId, Long couponId) {
        Cart cart = cartService.getCartByMemberId(memberId);
        List<CartItem> cartItems = cart.getItems();

        if (cartItems.isEmpty()) {
            throw new AppException("購物車是空的");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem item : cartItems) {
            Product product = productMapper.findById(item.getProductId())
                    .orElseThrow(() -> new AppException("產品不存在"));
            if (product.getStock() < item.getQuantity()) {
                throw new AppException(
                        "商品 " + product.getName() + " 庫存不足 (需求: " + item.getQuantity() + ")");
            }
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

            product.setStock(product.getStock() - item.getQuantity());
            productMapper.update(product);
        }

        // Apply Coupon Logic (To be implemented)
        /*
         * if (couponId != null) {
         * // Logic Pending
         * }
         */

        // ... logic placeholders ...

        return null; // temporary
    }

    public List<Order> getMemberOrders(Long memberId) {
        return orderMapper.findByMemberIdOrderByCreateTimeDesc(memberId);
    }

    @Transactional
    public void refundOrder(Long orderId, Long memberId) {
        Order order = orderMapper.findById(orderId)
                .orElseThrow(() -> new AppException("找不到訂單"));

        if (!order.getMemberId().equals(memberId)) {
            throw new AppException("無權限申請退款");
        }

        if ("REFUNDED".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            throw new AppException("訂單已退款或取消");
        }

        // 1. Refund Amount to Wallet
        transactionService.updateWalletBalance(memberId, order.getTotalPrice(), Transaction.TransactionType.REFUND,
                "REFUND Order #" + orderId);

        // 2. Restore Stock - 需要另外查詢訂單項目
        // TODO: 需要 OrderItemMapper 來查詢訂單項目

        // 3. Update Order Status
        order.setStatus(Order.OrderStatus.REFUNDED.name());
        orderMapper.update(order);
    }
}
