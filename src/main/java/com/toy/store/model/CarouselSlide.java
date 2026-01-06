package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 輪播圖實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarouselSlide {
    private Long id;
    private String title;
    private String imageUrl;
    private String linkUrl;
    private Integer sortOrder = 0;
    private Boolean active = true;
}
