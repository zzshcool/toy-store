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
    private String type; // SYSTEM, PRIZE, SHIPPING, LEVEL_UP, WARNING
    private Boolean isRead = false;
    private String referenceId;
    private String actionUrl;
    private LocalDateTime createdAt = LocalDateTime.now();

    // 枚舉類型（用於 Service 層便利使用）
    public enum MessageType {
        SYSTEM, PRIZE, SHIPPING, LEVEL_UP, WARNING
    }

    // 便捷方法
    public void setRead(boolean read) {
        this.isRead = read;
    }
}
