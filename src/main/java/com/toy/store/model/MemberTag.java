package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 會員標籤
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "member_tags")
public class MemberTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    // 標籤顏色（HEX）
    private String color = "#667eea";

    // 標籤類型
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TagType type;

    // 自動標籤的條件（JSON 格式）
    @Column(columnDefinition = "TEXT")
    private String autoCondition;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum TagType {
        MANUAL, // 手動標記
        AUTO, // 自動標記（基於條件）
        SYSTEM // 系統標籤
    }
}
