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

    public BigDecimal getThreshold() {
        return minGrowthValue != null ? BigDecimal.valueOf(minGrowthValue) : BigDecimal.ZERO;
    }

    public void setThreshold(Long threshold) {
        this.minGrowthValue = threshold;
    }

    public void setThreshold(BigDecimal threshold) {
        this.minGrowthValue = threshold != null ? threshold.longValue() : 0L;
    }

    private Integer sortOrder = 0;

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int order) {
        this.sortOrder = order;
    }

    private String monthlyReward;
    private boolean enabled = true;
}
