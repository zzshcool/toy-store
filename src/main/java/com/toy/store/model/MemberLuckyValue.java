package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 會員幸運值與碎片追蹤實體
 * 追蹤每個會員的幸運值累積與碎片餘額
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "member_lucky_values")
public class MemberLuckyValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long memberId;

    // 幸運值（每次未中大獎累積，滿1000觸發保底）
    @Column(nullable = false)
    private Integer luckyValue = 0;

    // 碎片餘額
    @Column(nullable = false)
    private Integer shardBalance = 0;

    // 總共累積過的幸運值（統計用）
    @Column(nullable = false)
    private Long totalLuckyValueEarned = 0L;

    // 總共獲得過的碎片（統計用）
    @Column(nullable = false)
    private Long totalShardsEarned = 0L;

    // 總共使用過的碎片（統計用）
    @Column(nullable = false)
    private Long totalShardsSpent = 0L;

    private LocalDateTime updatedAt = LocalDateTime.now();

    public MemberLuckyValue(Long memberId) {
        this.memberId = memberId;
    }

    // 增加幸運值
    public void addLuckyValue(int amount) {
        this.luckyValue += amount;
        this.totalLuckyValueEarned += amount;
        this.updatedAt = LocalDateTime.now();
    }

    // 重置幸運值（觸發保底後）
    public void resetLuckyValue() {
        this.luckyValue = 0;
        this.updatedAt = LocalDateTime.now();
    }

    // 檢查是否達到保底門檻
    public boolean hasReachedGuarantee(int threshold) {
        return luckyValue >= threshold;
    }

    // 增加碎片
    public void addShards(int amount) {
        this.shardBalance += amount;
        this.totalShardsEarned += amount;
        this.updatedAt = LocalDateTime.now();
    }

    // 扣除碎片
    public boolean spendShards(int amount) {
        if (shardBalance >= amount) {
            this.shardBalance -= amount;
            this.totalShardsSpent += amount;
            this.updatedAt = LocalDateTime.now();
            return true;
        }
        return false;
    }

    // 檢查碎片是否足夠
    public boolean hasEnoughShards(int required) {
        return shardBalance >= required;
    }
}
