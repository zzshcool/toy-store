package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 會員操作日誌實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberActionLog {
    private Long id;
    private Long memberId;
    private String action;
    private String details;
    private String ipAddress;
    private LocalDateTime createdAt = LocalDateTime.now();
}
