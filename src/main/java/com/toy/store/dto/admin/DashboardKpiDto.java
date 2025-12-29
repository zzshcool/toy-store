package com.toy.store.dto.admin;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 後台儀表板 KPI DTO
 */
@Data
public class DashboardKpiDto {
    private long totalMembers;
    private long todayNewMembers;
    private BigDecimal todayRevenue;
    private BigDecimal monthRevenue;
    private long todayDraws;
    private long totalDraws;
    private long pendingShipments;
    private long totalPrizes;
}
