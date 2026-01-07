package com.toy.store.service;

import com.toy.store.exception.AppException;
import com.toy.store.model.*;
import com.toy.store.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * 碎片服務
 * 處理碎片查詢、兌換等邏輯
 */
@Service
public class ShardService {

    private final MemberLuckyValueMapper luckyValueMapper;
    private final ShardTransactionMapper shardTransactionMapper;
    private final RedeemShopItemMapper redeemShopMapper;
    private final GachaRecordMapper recordMapper;
    private final SystemSettingService settingService;

    private final Random random = new Random();

    public ShardService(
            MemberLuckyValueMapper luckyValueMapper,
            ShardTransactionMapper shardTransactionMapper,
            RedeemShopItemMapper redeemShopMapper,
            GachaRecordMapper recordMapper,
            SystemSettingService settingService) {
        this.luckyValueMapper = luckyValueMapper;
        this.shardTransactionMapper = shardTransactionMapper;
        this.redeemShopMapper = redeemShopMapper;
        this.recordMapper = recordMapper;
        this.settingService = settingService;
    }

    /**
     * 取得會員碎片餘額
     */
    public int getShardBalance(Long memberId) {
        return luckyValueMapper.findByMemberId(memberId)
                .map(MemberLuckyValue::getShardBalance)
                .orElse(0);
    }

    /**
     * 取得會員碎片交易紀錄
     */
    public List<ShardTransaction> getTransactions(Long memberId) {
        return shardTransactionMapper.findTop20ByMemberIdOrderByCreatedAtDesc(memberId);
    }

    /**
     * 取得所有可兌換商品
     */
    public List<RedeemShopItem> getAvailableItems() {
        return redeemShopMapper.findByStockGreaterThanOrderBySortOrderAsc(0);
    }

    /**
     * 取得所有商品（含售罄）
     */
    public List<RedeemShopItem> getAllItems() {
        return redeemShopMapper.findAllByOrderBySortOrderAsc();
    }

    /**
     * 兌換商品
     */
    @Transactional
    public RedeemResult redeem(Long memberId, Long itemId) {
        RedeemShopItem item = redeemShopMapper.findById(itemId)
                .orElseThrow(() -> new AppException("商品不存在"));

        if (!item.hasStock()) {
            throw new AppException("商品已售罄");
        }

        MemberLuckyValue luckyValue = luckyValueMapper.findByMemberId(memberId)
                .orElseThrow(() -> new AppException("會員資料不存在"));

        if (!luckyValue.hasEnoughShards(item.getShardCost())) {
            throw new AppException("碎片不足，需要 " + item.getShardCost() + " 碎片");
        }

        // 扣除碎片
        luckyValue.spendShards(item.getShardCost());
        luckyValueMapper.update(luckyValue);

        // 減少庫存
        item.decreaseStock();
        redeemShopMapper.update(item);

        // 記錄交易
        ShardTransaction tx = ShardTransaction.createSpend(memberId, item.getShardCost(),
                "兌換: " + item.getName(), itemId);
        tx.setCreatedAt(LocalDateTime.now());
        shardTransactionMapper.insert(tx);

        return new RedeemResult(item, luckyValue.getShardBalance());
    }

    /**
     * 處理重複款轉換碎片
     */
    @Transactional
    public int convertDuplicate(Long memberId, String prizeName) {
        // 檢查是否有重複
        List<GachaRecord> records = recordMapper.findByMemberIdAndPrizeName(memberId, prizeName);
        if (records.size() < 2) {
            return 0; // 沒有重複
        }

        int duplicateShards = settingService.getIntSetting(SystemSetting.GACHA_DUPLICATE_SHARD, 300);

        MemberLuckyValue luckyValue = luckyValueMapper.findByMemberId(memberId)
                .orElseGet(() -> {
                    MemberLuckyValue newLuckyValue = new MemberLuckyValue();
                    newLuckyValue.setMemberId(memberId);
                    newLuckyValue.setLuckyValue(0);
                    newLuckyValue.setShardBalance(0);
                    return newLuckyValue;
                });
        luckyValue.addShards(duplicateShards);
        if (luckyValue.getId() == null) {
            luckyValueMapper.insert(luckyValue);
        } else {
            luckyValueMapper.update(luckyValue);
        }

        ShardTransaction tx = ShardTransaction.createEarn(memberId, duplicateShards,
                ShardTransaction.TransactionType.EARN_DUPLICATE,
                "重複款轉換: " + prizeName, "DUPLICATE", null);
        tx.setCreatedAt(LocalDateTime.now());
        shardTransactionMapper.insert(tx);

        return duplicateShards;
    }

    /**
     * 增加碎片（活動獎勵等）
     */
    @Transactional
    public void addBonusShards(Long memberId, int amount, String description) {
        MemberLuckyValue luckyValue = luckyValueMapper.findByMemberId(memberId)
                .orElseGet(() -> {
                    MemberLuckyValue newLuckyValue = new MemberLuckyValue();
                    newLuckyValue.setMemberId(memberId);
                    newLuckyValue.setLuckyValue(0);
                    newLuckyValue.setShardBalance(0);
                    return newLuckyValue;
                });
        luckyValue.addShards(amount);
        if (luckyValue.getId() == null) {
            luckyValueMapper.insert(luckyValue);
        } else {
            luckyValueMapper.update(luckyValue);
        }

        ShardTransaction tx = ShardTransaction.createEarn(memberId, amount,
                ShardTransaction.TransactionType.EARN_BONUS,
                description, "BONUS", null);
        tx.setCreatedAt(LocalDateTime.now());
        shardTransactionMapper.insert(tx);
    }

    // =====================================================
    // 統一碎片操作（供 Roulette/Bingo/Ichiban 調用）
    // =====================================================

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
     */
    @Transactional
    public void addGachaShards(Long memberId, int amount, String sourceType, Long sourceId, String description) {
        MemberLuckyValue luckyValue = luckyValueMapper.findByMemberId(memberId)
                .orElseGet(() -> {
                    MemberLuckyValue newLuckyValue = new MemberLuckyValue();
                    newLuckyValue.setMemberId(memberId);
                    newLuckyValue.setLuckyValue(0);
                    newLuckyValue.setShardBalance(0);
                    return newLuckyValue;
                });
        luckyValue.addShards(amount);
        if (luckyValue.getId() == null) {
            luckyValueMapper.insert(luckyValue);
        } else {
            luckyValueMapper.update(luckyValue);
        }

        ShardTransaction tx = ShardTransaction.createEarn(memberId, amount,
                ShardTransaction.TransactionType.EARN_DRAW,
                description, sourceType, sourceId);
        tx.setCreatedAt(LocalDateTime.now());
        shardTransactionMapper.insert(tx);
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
