package com.toy.store.service;

import com.toy.store.exception.AppException;
import com.toy.store.model.Cart;
import com.toy.store.model.CartItem;
import com.toy.store.model.Member;
import com.toy.store.model.Product;
import com.toy.store.repository.CartRepository;
import com.toy.store.repository.MemberRepository;
import com.toy.store.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    public Cart getCartByMemberId(Long memberId) {
        return cartRepository.findByMemberId(memberId)
                .orElseGet(() -> {
                    Member member = memberRepository.findById(memberId)
                            .orElseThrow(() -> new AppException("會員不存在"));
                    Cart newCart = new Cart();
                    newCart.setMember(member);
                    return cartRepository.save(newCart);
                });
    }

    @Transactional
    public Cart addToCart(Long memberId, Long productId, Integer quantity) {
        Cart cart = getCartByMemberId(memberId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException("產品不存在"));

        if (product.getStock() < quantity) {
            throw new AppException("庫存不足");
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
                .orElseThrow(() -> new AppException("購物車項目不存在"));

        if (quantity <= 0) {
            cart.removeItem(item);
        } else {
            if (item.getProduct().getStock() < quantity) {
                throw new AppException("庫存不足");
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
