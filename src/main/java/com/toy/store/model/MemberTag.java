package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 會員標籤實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberTag {
    private Long id;
    private String name;
    private String color;
    private String description;
}
