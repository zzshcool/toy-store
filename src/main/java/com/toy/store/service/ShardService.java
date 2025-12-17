package com.toy.store.service;

import com.toy.store.model.*;
import com.toy.store.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 碎片服務
 * 處理碎片查詢、兌換等邏輯
 */
@Service
public class ShardService {

    @Autowired
    private MemberLuckyValueRepository luckyValueRepository;

    @Autowired
    private ShardTransactionRepository shardTransactionRepository;

    @Autowired
    private RedeemShopItemRepository redeemShopRepository;

    @Autowired
    private GachaRecordRepository recordRepository;

    @Autowired
    private SystemSettingService settingService;

    /**
     * 取得會員碎片餘額
     */
    public int getShardBalance(Long memberId) {
        return luckyValueRepository.findByMemberId(memberId)
                .map(MemberLuckyValue::getShardBalance)
                .orElse(0);
    }

    /**
     * 取得會員碎片交易紀錄
     */
    public List<ShardTransaction> getTransactions(Long memberId) {
        return shardTransactionRepository.findTop20ByMemberIdOrderByCreatedAtDesc(memberId);
    }

    /**
     * 取得所有可兌換商品
     */
    public List<RedeemShopItem> getAvailableItems() {
        return redeemShopRepository.findByStockGreaterThanOrderBySortOrderAsc(0);
    }

    /**
     * 取得所有商品（含售罄）
     */
    public List<RedeemShopItem> getAllItems() {
        return redeemShopRepository.findAllByOrderBySortOrderAsc();
    }

    /**
     * 兌換商品
     */
    @Transactional
    public RedeemResult redeem(Long memberId, Long itemId) {
        RedeemShopItem item = redeemShopRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        if (!item.hasStock()) {
            throw new RuntimeException("商品已售罄");
        }

        MemberLuckyValue luckyValue = luckyValueRepository.findByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("會員資料不存在"));

        if (!luckyValue.hasEnoughShards(item.getShardCost())) {
            throw new RuntimeException("碎片不足，需要 " + item.getShardCost() + " 碎片");
        }

        // 扣除碎片
        luckyValue.spendShards(item.getShardCost());
        luckyValueRepository.save(luckyValue);

        // 減少庫存
        item.decreaseStock();
        redeemShopRepository.save(item);

        // 記錄交易
        ShardTransaction tx = ShardTransaction.createSpend(memberId, item.getShardCost(),
                "兌換: " + item.getName(), itemId);
        shardTransactionRepository.save(tx);

        return new RedeemResult(item, luckyValue.getShardBalance());
    }

    /**
     * 處理重複款轉換碎片
     */
    @Transactional
    public int convertDuplicate(Long memberId, String prizeName) {
        // 檢查是否有重複
        List<GachaRecord> records = recordRepository.findByMemberIdAndPrizeName(memberId, prizeName);
        if (records.size() < 2) {
            return 0; // 沒有重複
        }

        int duplicateShards = settingService.getIntSetting(SystemSetting.GACHA_DUPLICATE_SHARD, 300);

        MemberLuckyValue luckyValue = luckyValueRepository.findByMemberId(memberId)
                .orElse(new MemberLuckyValue(memberId));
        luckyValue.addShards(duplicateShards);
        luckyValueRepository.save(luckyValue);

        ShardTransaction tx = ShardTransaction.createEarn(memberId, duplicateShards,
                ShardTransaction.TransactionType.EARN_DUPLICATE,
                "重複款轉換: " + prizeName, "DUPLICATE", null);
        shardTransactionRepository.save(tx);

        return duplicateShards;
    }

    /**
     * 增加碎片（活動獎勵等）
     */
    @Transactional
    public void addBonusShards(Long memberId, int amount, String description) {
        MemberLuckyValue luckyValue = luckyValueRepository.findByMemberId(memberId)
                .orElse(new MemberLuckyValue(memberId));
        luckyValue.addShards(amount);
        luckyValueRepository.save(luckyValue);

        ShardTransaction tx = ShardTransaction.createEarn(memberId, amount,
                ShardTransaction.TransactionType.EARN_BONUS,
                description, "BONUS", null);
        shardTransactionRepository.save(tx);
    }

    // =====================================================
    // 統一碎片操作（供 Roulette/Bingo/Ichiban 調用）
    // =====================================================

    private final java.util.Random random = new java.util.Random();

    /**
     * 產生隨機碎片數量（從系統設定讀取範圍）
     */
    public int generateRandomShards() {
        int min = settingService.getIntSetting(SystemSetting.GACHA_SHARD_MIN, 10);
        int max = settingService.getIntSetting(SystemSetting.GACHA_SHARD_MAX, 50);
        return random.nextInt(max - min + 1) + min;
    }

    /**
     * 抽獎獲得碎片（統一入口）
     * 
     * @param memberId    會員ID
     * @param amount      碎片數量
     * @param sourceType  來源類型（ROULETTE/BINGO/ICHIBAN）
     * @param sourceId    來源ID
     * @param description 描述
     */
    @Transactional
    public void addGachaShards(Long memberId, int amount, String sourceType, Long sourceId, String description) {
        MemberLuckyValue luckyValue = luckyValueRepository.findByMemberId(memberId)
                .orElse(new MemberLuckyValue(memberId));
        luckyValue.addShards(amount);
        luckyValueRepository.save(luckyValue);

        ShardTransaction tx = ShardTransaction.createEarn(memberId, amount,
                ShardTransaction.TransactionType.EARN_DRAW,
                description, sourceType, sourceId);
        shardTransactionRepository.save(tx);
    }

    /**
     * 兌換結果封裝類
     */
    public static class RedeemResult {
        private final RedeemShopItem item;
        private final int remainingBalance;

        public RedeemResult(RedeemShopItem item, int remainingBalance) {
            this.item = item;
            this.remainingBalance = remainingBalance;
        }

        public RedeemShopItem getItem() {
            return item;
        }

        public int getRemainingBalance() {
            return remainingBalance;
        }
    }
}
