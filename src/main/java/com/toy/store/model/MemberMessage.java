package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 會員訊息實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberMessage {
    private Long id;
    private Long memberId;
    private String title;
    private String content;
    private String type;
    private Boolean isRead = false;
    private LocalDateTime createdAt = LocalDateTime.now();
}
