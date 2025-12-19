package com.toy.store.service;

import com.toy.store.model.GachaRecord;
import com.toy.store.model.Transaction;
import com.toy.store.repository.GachaRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

/**
 * Gacha 遊戲服務基類
 * 提供扣款、紀錄、碎片發放等共同邏輯
 */
public abstract class BaseGachaService {

    @Autowired
    protected GachaRecordRepository recordRepository;

    @Autowired
    protected TransactionService transactionService;

    @Autowired
    protected ShardService shardService;

    /**
     * 處理錢包扣款
     */
    @Transactional
    protected void deductWallet(Long memberId, BigDecimal amount,
            Transaction.TransactionType type, String description) {
        transactionService.updateWalletBalance(memberId, amount.negate(), type, description);
    }

    /**
     * 處理 Gacha 抽獎獲得碎片
     */
    @Transactional
    protected int processGachaShards(Long memberId, String sourceType, Long sourceId, String description) {
        int shards = shardService.generateRandomShards();
        shardService.addGachaShards(memberId, shards, sourceType, sourceId, description);
        return shards;
    }

    /**
     * 儲存 Gacha 紀錄 (一番賞)
     */
    protected void saveIchibanRecord(Long memberId, Long boxId, String prizeName, String prizeRank, int shards) {
        GachaRecord record = GachaRecord.createIchibanRecord(memberId, boxId, prizeName, prizeRank, shards);
        recordRepository.save(record);
    }

    /**
     * 儲存 Gacha 紀錄 (轉盤)
     */
    protected void saveRouletteRecord(Long memberId, Long gameId, String prizeName, int shards,
            int luckyAdd, boolean isGuarantee) {
        GachaRecord record = GachaRecord.createRouletteRecord(memberId, gameId, prizeName, shards, luckyAdd,
                isGuarantee);
        recordRepository.save(record);
    }

    /**
     * 儲存 Gacha 紀錄 (九宮格)
     */
    protected void saveBingoRecord(Long memberId, Long gameId, String prizeName, int shards) {
        GachaRecord record = GachaRecord.createBingoRecord(memberId, gameId, prizeName, shards);
        recordRepository.save(record);
    }
}
