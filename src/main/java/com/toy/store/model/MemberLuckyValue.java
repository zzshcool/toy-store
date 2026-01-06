package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 會員幸運值實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberLuckyValue {
    private Long id;
    private Long memberId;
    private String gameType; // ICHIBAN, ROULETTE, BINGO
    private Long gameId;
    private Integer luckyValue = 0;
    private LocalDateTime lastUpdated = LocalDateTime.now();
}
