package com.toy.store.dto.admin;

import lombok.Data;
import java.math.BigDecimal;

/**
 * RTP 統計 DTO
 */
@Data
public class RtpStatsDto {
    private String gameName;
    private String type;
    private int totalDraws;
    private BigDecimal totalIncome;
    private BigDecimal totalOutcome;
    private double rtp;
}
