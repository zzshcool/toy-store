package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 通知實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    private Long id;
    private Long memberId;
    private String title;
    private String content;
    private Boolean read = false;
    private LocalDateTime createdAt = LocalDateTime.now();
}
