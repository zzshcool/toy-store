package com.toy.store.service;

import com.toy.store.model.*;
import com.toy.store.repository.OrderRepository;
import com.toy.store.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public Order checkout(Long memberId) {
        Cart cart = cartService.getCartByMemberId(memberId);
        List<CartItem> cartItems = cart.getItems();

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;

        // 1. Calculate Total & Check Stock (Optimistic Lock or Pessimistic Lock usually
        // needed, simplified here)
        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            if (product.getStock() < item.getQuantity()) {
                throw new RuntimeException(
                        "Product " + product.getName() + " is out of stock (Requested: " + item.getQuantity() + ")");
            }
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

            // Deduct Stock
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);
        }

        // 2. Deduct Balance
        transactionService.updateWalletBalance(memberId, totalAmount.negate(), Transaction.TransactionType.PURCHASE,
                "ORDER");

        // 3. Create Order
        Order order = new Order();
        order.setMember(cart.getMember());
        order.setTotalPrice(totalAmount);
        order.setStatus(Order.OrderStatus.PAID);

        for (CartItem ci : cartItems) {
            OrderItem oi = new OrderItem();
            oi.setProduct(ci.getProduct());
            oi.setQuantity(ci.getQuantity());
            oi.setPriceAtPurchase(ci.getProduct().getPrice());
            order.addItem(oi);
        }

        Order savedOrder = orderRepository.save(order);

        // Update Transaction Ref
        // Ideally TransactionService should return the Tx ID or accept refId after
        // creation, but we passed "ORDER".
        // We could update the transaction here if we wanted to link exact Order ID.

        // 4. Clear Cart
        cartService.clearCart(memberId);

        return savedOrder;
    }

    public List<Order> getMemberOrders(Long memberId) {
        return orderRepository.findByMemberIdOrderByCreateTimeDesc(memberId);
    }

    @Transactional
    public void refundOrder(Long orderId, Long memberId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getMember().getId().equals(memberId)) {
            throw new RuntimeException("Unauthorized refund request");
        }

        if (order.getStatus() == Order.OrderStatus.REFUNDED || order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new RuntimeException("Order already refunded or cancelled");
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
