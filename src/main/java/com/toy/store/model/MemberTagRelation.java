package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 會員-標籤關聯
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "member_tag_relations")
public class MemberTagRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private MemberTag tag;

    private LocalDateTime taggedAt = LocalDateTime.now();

    // 標記來源（手動/自動）
    private String source = "AUTO";
}
