package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 扭蛋物品實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GachaItem {
    private Long id;
    private Long themeId;
    private String name;
    private String description;
    private String imageUrl;
    private String rarity = "NORMAL";
    private Integer weight = 1;
}
