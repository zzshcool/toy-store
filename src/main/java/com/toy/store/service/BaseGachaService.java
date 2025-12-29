package com.toy.store.service;

import com.toy.store.model.GachaRecord;
import com.toy.store.model.Transaction;
import com.toy.store.repository.GachaRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

/**
 * Gacha 遊戲服務基類
 * 提供扣款、紀錄、碎片發放等共同邏輯
 */
@RequiredArgsConstructor
public abstract class BaseGachaService {

    protected final GachaRecordRepository recordRepository;
    protected final TransactionService transactionService;
    protected final ShardService shardService;
    protected final MissionService missionService;

    /**
     * 處理錢包扣款
     */
    @Transactional
    protected void deductWallet(Long memberId, BigDecimal amount,
            Transaction.TransactionType type, String description) {
        transactionService.updateWalletBalance(memberId, amount.negate(), type, description);
    }

    /**
     * 處理 Gacha 抽獎獲得隨機積分 (1~20)
     */
    @Transactional
    protected int processGachaShards(Long memberId, String sourceType, Long sourceId, String description) {
        // 按照用戶需求，產出 1~20 的隨機碎片 (積分)
        int min = 1;
        int max = 20;
        int shards = new java.util.Random().nextInt(max - min + 1) + min;

        shardService.addGachaShards(memberId, shards, sourceType, sourceId, description);
        return shards;
    }

    /**
     * 儲存 Gacha 紀錄 (一番賞)
     */
    protected void saveIchibanRecord(Long memberId, Long boxId, String prizeName, String prizeRank, int shards,
            BigDecimal value) {
        GachaRecord record = GachaRecord.createIchibanRecord(memberId, boxId, prizeName, prizeRank, shards, value);
        recordRepository.save(record);

        // 觸發抽獎任務
        try {
            missionService.updateMissionProgress(memberId,
                    com.toy.store.model.MemberMission.MissionType.DRAW_COUNT, 1);
        } catch (Exception e) {
        }
    }

    /**
     * 儲存 Gacha 紀錄 (轉盤)
     */
    protected void saveRouletteRecord(Long memberId, Long gameId, String prizeName, int shards,
            int luckyAdd, boolean isGuarantee, BigDecimal value) {
        GachaRecord record = GachaRecord.createRouletteRecord(memberId, gameId, prizeName, shards, luckyAdd,
                isGuarantee, value);
        recordRepository.save(record);

        // 觸發抽獎任務
        try {
            missionService.updateMissionProgress(memberId,
                    com.toy.store.model.MemberMission.MissionType.DRAW_COUNT, 1);
        } catch (Exception e) {
        }
    }

    /**
     * 儲存 Gacha 紀錄 (九宮格)
     */
    protected void saveBingoRecord(Long memberId, Long gameId, String prizeName, int shards, BigDecimal value) {
        GachaRecord record = GachaRecord.createBingoRecord(memberId, gameId, prizeName, shards, value);
        recordRepository.save(record);

        // 觸發抽獎任務
        try {
            missionService.updateMissionProgress(memberId,
                    com.toy.store.model.MemberMission.MissionType.DRAW_COUNT, 1);
        } catch (Exception e) {
        }
    }
}
