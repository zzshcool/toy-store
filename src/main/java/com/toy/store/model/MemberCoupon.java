package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 會員優惠券實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberCoupon {
    private Long id;
    private Long memberId;
    private Long couponId;
    private String status = "UNUSED"; // UNUSED, USED, EXPIRED
    private LocalDateTime usedAt;
    private LocalDateTime createdAt = LocalDateTime.now();
}
