package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 管理員操作日誌實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminActionLog {
    private Long id;

    private Long adminId;

    private String action;

    private String targetType;

    private Long targetId;

    private String details;

    private String ipAddress;

    private LocalDateTime createdAt = LocalDateTime.now();
}
