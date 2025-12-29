package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 推薦碼使用記錄
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "promo_code_usages")
public class PromoCodeUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "code_id", nullable = false)
    private PromoCode promoCode;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    private LocalDateTime usedAt = LocalDateTime.now();
}
