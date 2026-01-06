package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 促銷碼使用紀錄實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeUsage {
    private Long id;
    private Long promoCodeId;
    private Long memberId;
    private LocalDateTime usedAt = LocalDateTime.now();
}
