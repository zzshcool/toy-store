package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 碎片交易紀錄實體
 * 記錄碎片的獲得與消耗
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "shard_transactions")
public class ShardTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private Integer amount; // 正數為獲得，負數為消耗

    @Column(length = 500)
    private String description; // 交易說明

    private String sourceType; // 來源類型：ICHIBAN, ROULETTE, BINGO, REDEEM
    private Long sourceId; // 來源ID

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum TransactionType {
        EARN_DRAW("抽獎獲得"),
        EARN_DUPLICATE("重複款轉換"),
        EARN_BONUS("活動獎勵"),
        SPEND_REDEEM("兌換消耗");

        private final String displayName;

        TransactionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // 建立獲得碎片紀錄
    public static ShardTransaction createEarn(Long memberId, int amount, TransactionType type,
            String description, String sourceType, Long sourceId) {
        ShardTransaction tx = new ShardTransaction();
        tx.setMemberId(memberId);
        tx.setType(type);
        tx.setAmount(amount);
        tx.setDescription(description);
        tx.setSourceType(sourceType);
        tx.setSourceId(sourceId);
        return tx;
    }

    // 建立消耗碎片紀錄
    public static ShardTransaction createSpend(Long memberId, int amount, String description, Long redeemItemId) {
        ShardTransaction tx = new ShardTransaction();
        tx.setMemberId(memberId);
        tx.setType(TransactionType.SPEND_REDEEM);
        tx.setAmount(-amount);
        tx.setDescription(description);
        tx.setSourceType("REDEEM");
        tx.setSourceId(redeemItemId);
        return tx;
    }
}
