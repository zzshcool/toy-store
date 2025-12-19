package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private BigDecimal amount; // Can be positive or negative

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    private String referenceId; // e.g., Order ID or Mystery Box ID

    public enum TransactionType {
        DEPOSIT, WITHDRAW, PURCHASE, PRIZE_INCOME, MYSTERY_BOX_COST,
        ICHIBAN_COST, ROULETTE_COST, BINGO_COST, REFUND
    }
}
