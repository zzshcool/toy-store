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
    private String memberUsername;
    private Boolean success;
    private LocalDateTime createdAt = LocalDateTime.now();

    public void setTimestamp(LocalDateTime timestamp) {
        this.createdAt = timestamp;
    }

    public LocalDateTime getTimestamp() {
        return createdAt;
    }

    // 兼容建構子
    public MemberActionLog(Long memberId, String action, String details, String ipAddress) {
        this.memberId = memberId;
        this.action = action;
        this.details = details;
        this.ipAddress = ipAddress;
        this.createdAt = LocalDateTime.now();
    }
}
