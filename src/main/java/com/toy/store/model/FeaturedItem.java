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
    private String imageUrl;
    private Integer sortOrder = 0;
    private Boolean active = true;
}
