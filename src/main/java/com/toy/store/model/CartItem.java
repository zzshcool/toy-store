package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * 購物車項目實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private Long id;

    private Long cartId;

    private transient Cart cart;

    private Long productId;

    private transient Product product;

    private Integer quantity;

    private BigDecimal price;
}
