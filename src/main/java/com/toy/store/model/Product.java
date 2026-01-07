package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 產品實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    private Integer stock;

    private String category;

    private String subCategory;

    private String imageUrl;

    private Status status = Status.AVAILABLE;
    private Tag tag = Tag.NONE;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Status {
        AVAILABLE, PREORDER, OUT_OF_STOCK, DISCONTINUED
    }

    public enum Tag {
        NONE, NEW_ARRIVAL, BEST_SELLER, LIMITED
    }
}
