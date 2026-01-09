package com.toy.store.service;

import com.toy.store.exception.AppException;
import com.toy.store.model.*;
import com.toy.store.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 轉盤服務
 * 處理旋轉、保底機制等核心邏輯
 */
@Service
public class RouletteService extends BaseGachaService {

    private final RouletteGameMapper gameMapper;
    private final RouletteSlotMapper slotMapper;
    private final GachaProbabilityEngine probabilityEngine;
    private final MemberMapper memberMapper;
    private final SystemSettingService systemSettingService;

    public RouletteService(
            GachaRecordMapper recordMapper,
            TransactionService transactionService,
            ShardService shardService,
            MissionService missionService,
            RouletteGameMapper gameMapper,
            RouletteSlotMapper slotMapper,
            GachaProbabilityEngine probabilityEngine,
            MemberMapper memberMapper,
            SystemSettingService systemSettingService) {
        super(recordMapper, transactionService, shardService, missionService);
        this.gameMapper = gameMapper;
        this.slotMapper = slotMapper;
        this.probabilityEngine = probabilityEngine;
        this.memberMapper = memberMapper;
        this.systemSettingService = systemSettingService;
    }

    /**
     * 取得所有進行中的轉盤遊戲
     */
    public List<RouletteGame> getActiveGames() {
        return gameMapper.findByStatus(RouletteGame.Status.ACTIVE.name());
    }

    /**
     * 取得轉盤詳情（含獎格）
     */
    public RouletteGame getGameWithSlots(Long gameId) {
        if (gameId == null)
            return null;
        RouletteGame game = gameMapper.findById(gameId).orElse(null);
        if (game != null) {
            game.setSlots(slotMapper.findByGameIdOrderBySlotOrderAsc(gameId));
        }
        return game;
    }

    /**
     * 取得轉盤的獎格列表
     */
    public List<RouletteSlot> getSlots(Long gameId) {
        return slotMapper.findByGameIdOrderBySlotOrderAsc(gameId);
    }

    /**
     * 旋轉轉盤
     * 
     * @return SpinResult 包含中獎格子、是否保底觸發、獲得碎片等資訊
     */
    @Transactional
    public SpinResult spin(Long gameId, Long memberId) {
        if (memberId == null || gameId == null)
            throw new AppException("ID不能為空");
        RouletteGame game = gameMapper.findById(gameId)
                .orElseThrow(() -> new AppException("轉盤遊戲不存在"));

        // 扣款
        deductWallet(memberId, game.getPricePerSpin(), Transaction.TransactionType.ROULETTE_COST,
                "轉盤消費: " + game.getName());

        // 取得會員模型 (中央幸運值管理)
        Member member = memberMapper.findById(memberId)
                .orElseThrow(() -> new AppException("會員不存在"));

        int threshold = 100; // 規格書定義
        boolean isGuarantee = member.getLuckyValue() >= threshold;

        // 取得獎格列表
        List<RouletteSlot> slots = slotMapper.findByGameIdOrderBySlotOrderAsc(gameId);
        if (slots.isEmpty()) {
            throw new AppException("轉盤沒有獎格");
        }

        // 選擇中獎格子
        RouletteSlot winningSlot;
        if (isGuarantee) {
            // 保底：必定中大獎 (無視 70% 規則)
            winningSlot = selectJackpotSlot(slots);
            member.setLuckyValue(0); // 重置
        } else {
            // 收益保護核心邏輯：模擬 100 次為一個收益週期
            double revenueThreshold = systemSettingService.getRevenueThreshold();
            double progress = (game.getTotalDraws() % 100) / 100.0;

            winningSlot = probabilityEngine.draw(slots, progress, revenueThreshold);

            if (winningSlot.getSlotTypeEnum() != RouletteSlot.SlotType.JACKPOT &&
                    winningSlot.getSlotTypeEnum() != RouletteSlot.SlotType.RARE) {
                // 未中大獎，累積幸運值
                member.setLuckyValue(member.getLuckyValue() + 10);
            } else {
                // 中大獎，重置幸運值
                member.setLuckyValue(0);
            }
        }

        // 更新數據
        game.setTotalDraws(game.getTotalDraws() + 1);
        gameMapper.update(game);
        memberMapper.update(member);

        // 處理獎勵
        int shardsEarned = 0;
        boolean isFreeSpin = false;

        // 所有獎項現在均產出隨機積分 1~20
        shardsEarned = processGachaShards(memberId, "ROULETTE", gameId, "轉盤抽獎獲得");

        if (winningSlot.getSlotTypeEnum() == RouletteSlot.SlotType.FREE_SPIN) {
            isFreeSpin = true;
        }

        // 記錄抽獎
        saveRouletteRecord(memberId, gameId, winningSlot.getPrizeName(), shardsEarned,
                isGuarantee ? 0 : 10, isGuarantee,
                winningSlot.getPrizeValue() != null ? winningSlot.getPrizeValue() : java.math.BigDecimal.ZERO);

        return new SpinResult(winningSlot, isGuarantee, isFreeSpin, shardsEarned,
                member.getLuckyValue(), threshold);
    }

    /**
     * 取得會員幸運值 (向後兼容)
     */
    public int getMemberLuckyValue(Long memberId) {
        if (memberId == null)
            return 0;
        return memberMapper.findById(memberId)
                .map(Member::getLuckyValue)
                .orElse(0);
    }

    /**
     * 選擇大獎格子（用於保底）
     */
    private RouletteSlot selectJackpotSlot(List<RouletteSlot> slots) {
        // 優先選擇 JACKPOT，其次 RARE
        return slots.stream()
                .filter(s -> s.getSlotTypeEnum() == RouletteSlot.SlotType.JACKPOT)
                .findFirst()
                .orElse(slots.stream()
                        .filter(s -> s.getSlotTypeEnum() == RouletteSlot.SlotType.RARE)
                        .findFirst()
                        .orElse(slots.get(0)));
    }

    /**
     * 旋轉結果封裝類
     */
    public static class SpinResult {
        private final RouletteSlot slot;
        private final boolean guarantee;
        private final boolean freeSpin;
        private final int shardsEarned;
        private final int currentLuckyValue;
        private final int luckyThreshold;

        public SpinResult(RouletteSlot slot, boolean guarantee, boolean freeSpin,
                int shardsEarned, int currentLuckyValue, int luckyThreshold) {
            this.slot = slot;
            this.guarantee = guarantee;
            this.freeSpin = freeSpin;
            this.shardsEarned = shardsEarned;
            this.currentLuckyValue = currentLuckyValue;
            this.luckyThreshold = luckyThreshold;
        }

        public RouletteSlot getSlot() {
            return slot;
        }

        public boolean isGuarantee() {
            return guarantee;
        }

        public boolean isFreeSpin() {
            return freeSpin;
        }

        public int getShardsEarned() {
            return shardsEarned;
        }

        public int getCurrentLuckyValue() {
            return currentLuckyValue;
        }

        public int getLuckyThreshold() {
            return luckyThreshold;
        }

        public int getLuckyPercentage() {
            return luckyThreshold > 0 ? (currentLuckyValue * 100 / luckyThreshold) : 0;
        }
    }
}
