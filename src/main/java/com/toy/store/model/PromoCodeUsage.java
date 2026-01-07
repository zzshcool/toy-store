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

    // 關聯對象（非資料庫欄位）
    private transient PromoCode promoCode;

    public void setPromoCode(PromoCode promoCode) {
        this.promoCode = promoCode;
        if (promoCode != null) {
            this.promoCodeId = promoCode.getId();
        }
    }
}
