package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 會員消息/通知
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "member_messages")
public class MemberMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_read")
    private boolean read = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // 關聯 ID（如訂單ID、抽獎ID等）
    private String referenceId;

    // 跳轉連結
    private String actionUrl;

    public enum MessageType {
        SYSTEM, // 系統通知
        PRIZE, // 中獎通知
        SHIPPING, // 發貨通知
        PROMOTION, // 優惠活動
        LEVEL_UP, // 升級通知
        WARNING // 警告通知（如餘額不足、紅利即將過期）
    }
}
