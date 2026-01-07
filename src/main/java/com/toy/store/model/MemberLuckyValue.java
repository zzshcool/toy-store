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
    private Integer shardBalance = 0; // 碎片餘額
    private LocalDateTime lastUpdated = LocalDateTime.now();

    // 獲取碎片餘額
    public Integer getShardBalance() {
        return shardBalance != null ? shardBalance : 0;
    }

    // 設置碎片餘額
    public void setShardBalance(int balance) {
        this.shardBalance = balance;
    }

    // 是否有足夠碎片
    public boolean hasEnoughShards(Integer cost) {
        return getShardBalance() >= (cost != null ? cost : 0);
    }

    // 消耗碎片
    public void spendShards(Integer amount) {
        if (amount != null && shardBalance != null) {
            shardBalance -= amount;
        }
    }

    // 增加碎片
    public void addShards(int amount) {
        shardBalance = getShardBalance() + amount;
    }
}
