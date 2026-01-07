package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

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
    private TagType type;
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum TagType {
        SYSTEM, // 系統自動標籤
        MANUAL // 手動標籤
    }
}
