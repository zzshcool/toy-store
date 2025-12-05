package com.toy.store.service;

import com.toy.store.model.Cart;
import com.toy.store.model.CartItem;
import com.toy.store.model.Member;
import com.toy.store.model.Product;
import com.toy.store.repository.CartRepository;
import com.toy.store.repository.MemberRepository;
import com.toy.store.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MemberRepository memberRepository;

    public Cart getCartByMemberId(Long memberId) {
        return cartRepository.findByMemberId(memberId)
                .orElseGet(() -> {
                    Member member = memberRepository.findById(memberId).orElseThrow();
                    Cart newCart = new Cart();
                    newCart.setMember(member);
                    return cartRepository.save(newCart);
                });
    }

    @Transactional
    public Cart addToCart(Long memberId, Long productId, Integer quantity) {
        Cart cart = getCartByMemberId(memberId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getStock() < quantity) {
            throw new RuntimeException("Insufficient stock");
        }

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cart.addItem(newItem);
        }

        return cartRepository.save(cart);
    }

    @Transactional
    public Cart updateQuantity(Long memberId, Long cartItemId, Integer quantity) {
        Cart cart = getCartByMemberId(memberId);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found in cart"));

        if (quantity <= 0) {
            cart.removeItem(item);
        } else {
            if (item.getProduct().getStock() < quantity) {
                throw new RuntimeException("Insufficient stock");
            }
            item.setQuantity(quantity);
        }

        return cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(Long memberId) {
        Cart cart = getCartByMemberId(memberId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}
