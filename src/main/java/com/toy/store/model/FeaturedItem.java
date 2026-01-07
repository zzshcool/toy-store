package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 精選商品實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeaturedItem {
    private Long id;
    private String itemType;
    private Long itemId;
    private String title;
    private String description;
    private String imageUrl;
    private Integer sortOrder = 0;
    private Boolean active = true;
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();

    public enum Type {
        PRODUCT, ICHIBAN, GACHA, ROULETTE, BINGO, LINK, NEW_ARRIVAL, BEST_SELLER
    }

    private Long productId;
    private transient Product product;

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Long getProductId() {
        return productId != null ? productId : itemId;
    }
}
