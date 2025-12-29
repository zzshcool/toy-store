package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "member_sign_ins")
public class MemberSignIn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private LocalDate signInDate;

    private LocalDateTime createdAt = LocalDateTime.now();

    // 連續簽到天數 (當前次數)
    @Column(nullable = false)
    private Integer consecutiveDays = 1;

    public MemberSignIn(Long memberId, LocalDate signInDate, Integer consecutiveDays) {
        this.memberId = memberId;
        this.signInDate = signInDate;
        this.consecutiveDays = consecutiveDays;
    }
}
