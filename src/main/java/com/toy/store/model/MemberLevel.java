package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

/**
 * 會員等級實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberLevel {
    private Long id;

    private String name;

    private Long minGrowthValue = 0L;

    private BigDecimal discountRate = new BigDecimal("100.00");

    private BigDecimal pointsMultiplier = BigDecimal.ONE;

    private String description;

    private String iconUrl;
}
