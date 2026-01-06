package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 會員標籤關聯實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberTagRelation {
    private Long id;
    private Long memberId;
    private Long tagId;
    private LocalDateTime createdAt = LocalDateTime.now();
}
