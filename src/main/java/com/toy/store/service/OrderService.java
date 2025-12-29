package com.toy.store.service;

import com.toy.store.exception.AppException;
import com.toy.store.model.*;
import com.toy.store.repository.MemberCouponRepository;
import com.toy.store.repository.OrderRepository;
import com.toy.store.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final TransactionService transactionService;
    private final ProductRepository productRepository;
    private final MemberCouponRepository memberCouponRepository;

    @Transactional
    public Order checkout(Long memberId, Long couponId) {
        Cart cart = cartService.getCartByMemberId(memberId);
        List<CartItem> cartItems = cart.getItems();

        if (cartItems.isEmpty()) {
            throw new AppException("購物車是空的");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            if (product.getStock() < item.getQuantity()) {
                throw new AppException(
                        "商品 " + product.getName() + " 庫存不足 (需求: " + item.getQuantity() + ")");
            }
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);
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
        return orderRepository.findByMemberIdOrderByCreateTimeDesc(memberId);
    }

    @Transactional
    public void refundOrder(Long orderId, Long memberId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException("找不到訂單"));

        if (!order.getMember().getId().equals(memberId)) {
            throw new AppException("無權限申請退款");
        }

        if (order.getStatus() == Order.OrderStatus.REFUNDED || order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new AppException("訂單已退款或取消");
        }

        // 1. Refund Amount to Wallet
        transactionService.updateWalletBalance(memberId, order.getTotalPrice(), Transaction.TransactionType.REFUND,
                "REFUND Order #" + orderId);

        // 2. Restore Stock
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        // 3. Update Order Status
        order.setStatus(Order.OrderStatus.REFUNDED);
        orderRepository.save(order);
    }
}
