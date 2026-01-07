package com.toy.store.service;

import com.toy.store.exception.AppException;
import com.toy.store.model.Cart;
import com.toy.store.model.CartItem;
import com.toy.store.model.Member;
import com.toy.store.model.Product;
import com.toy.store.mapper.CartMapper;
import com.toy.store.mapper.MemberMapper;
import com.toy.store.mapper.ProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class CartService {

    private final CartMapper cartMapper;
    private final ProductMapper productMapper;
    private final MemberMapper memberMapper;

    public CartService(
            CartMapper cartMapper,
            ProductMapper productMapper,
            MemberMapper memberMapper) {
        this.cartMapper = cartMapper;
        this.productMapper = productMapper;
        this.memberMapper = memberMapper;
    }

    public Cart getCartByMemberId(Long memberId) {
        return cartMapper.findByMemberId(memberId)
                .orElseGet(() -> {
                    Member member = memberMapper.findById(memberId)
                            .orElseThrow(() -> new AppException("會員不存在"));
                    Cart newCart = new Cart();
                    newCart.setMemberId(memberId);
                    newCart.setCreatedAt(LocalDateTime.now());
                    newCart.setItems(new ArrayList<>());
                    cartMapper.insert(newCart);
                    return newCart;
                });
    }

    @Transactional
    public Cart addToCart(Long memberId, Long productId, Integer quantity) {
        Cart cart = getCartByMemberId(memberId);
        Product product = productMapper.findById(productId)
                .orElseThrow(() -> new AppException("產品不存在"));

        if (product.getStock() < quantity) {
            throw new AppException("庫存不足");
        }

        // 查找現有項目
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCartId(cart.getId());
            newItem.setProductId(productId);
            newItem.setQuantity(quantity);
            cart.getItems().add(newItem);
        }

        cartMapper.update(cart);
        return cart;
    }

    @Transactional
    public Cart updateQuantity(Long memberId, Long cartItemId, Integer quantity) {
        Cart cart = getCartByMemberId(memberId);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new AppException("購物車項目不存在"));

        if (quantity <= 0) {
            cart.getItems().remove(item);
        } else {
            Product product = productMapper.findById(item.getProductId()).orElse(null);
            if (product != null && product.getStock() < quantity) {
                throw new AppException("庫存不足");
            }
            item.setQuantity(quantity);
        }

        cartMapper.update(cart);
        return cart;
    }

    @Transactional
    public void clearCart(Long memberId) {
        Cart cart = getCartByMemberId(memberId);
        cart.getItems().clear();
        cartMapper.update(cart);
    }
}
