package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 會員簽到實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberSignIn {
    private Long id;
    private Long memberId;
    private LocalDate signInDate;
    private Integer consecutiveDays = 1;
    private Integer rewardPoints = 0;
    private LocalDateTime createdAt = LocalDateTime.now();
}
