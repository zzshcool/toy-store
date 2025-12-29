package com.toy.store.service;

import com.toy.store.exception.AppException;
import com.toy.store.model.*;
import com.toy.store.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 轉盤服務
 * 處理旋轉、保底機制等核心邏輯
 */
@Service
public class RouletteService extends BaseGachaService {

    @Autowired
    private RouletteGameRepository gameRepository;

    @Autowired
    private RouletteSlotRepository slotRepository;

    @Autowired
    private GachaProbabilityEngine probabilityEngine;

    @Autowired
    private MemberLuckyValueRepository luckyValueRepository;

    @Autowired
    private SystemSettingService settingService;

    /**
     * 取得所有進行中的轉盤遊戲
     */
    public List<RouletteGame> getActiveGames() {
        return gameRepository.findByStatus(RouletteGame.Status.ACTIVE);
    }

    /**
     * 取得轉盤詳情（含獎格）
     */
    public RouletteGame getGameWithSlots(Long gameId) {
        if (gameId == null)
            return null;
        return gameRepository.findById(gameId).orElse(null);
    }

    /**
     * 取得轉盤的獎格列表
     */
    public List<RouletteSlot> getSlots(Long gameId) {
        return slotRepository.findByGameIdOrderBySlotOrderAsc(gameId);
    }

    /**
     * 旋轉轉盤
     * 
     * @return SpinResult 包含中獎格子、是否保底觸發、獲得碎片等資訊
     */
    @Transactional
    public SpinResult spin(Long gameId, Long memberId) {
        RouletteGame game = gameRepository.findById(gameId)
                .orElseThrow(() -> new AppException("轉盤遊戲不存在"));

        // 扣款
        deductWallet(memberId, game.getPricePerSpin(), Transaction.TransactionType.ROULETTE_COST,
                "轉盤消費: " + game.getName());

        // 取得會員幸運值
        MemberLuckyValue luckyValue = getOrCreateLuckyValue(memberId);
        int threshold = settingService.getLuckyThreshold();
        boolean isGuarantee = luckyValue.hasReachedGuarantee(threshold);

        // 取得獎格列表
        List<RouletteSlot> slots = slotRepository.findByGameIdOrderBySlotOrderAsc(gameId);
        if (slots.isEmpty()) {
            throw new AppException("轉盤沒有獎格");
        }

        // 選擇中獎格子
        RouletteSlot winningSlot;
        if (isGuarantee) {
            // 保底：必定中大獎
            winningSlot = selectJackpotSlot(slots);
            luckyValue.resetLuckyValue();
        } else {
            // 收益保護核心邏輯：計算進度 (此處模擬 100 次為一個收益週期)
            double revenueThreshold = 0.7; // 70% 門檻
            double progress = (game.getTotalDraws() % 100) / 100.0;

            winningSlot = probabilityEngine.draw(slots, progress, revenueThreshold);

            if (!winningSlot.isJackpot()) {
                // 未中大獎，累積幸運值
                luckyValue.addLuckyValue(10);
            } else {
                // 中大獎，重置幸運值
                luckyValue.resetLuckyValue();
            }
        }

        // 更新累計抽獎次數
        game.setTotalDraws(game.getTotalDraws() + 1);
        gameRepository.save(game);
        luckyValueRepository.save(luckyValue);

        // 處理獎勵
        int shardsEarned = 0;
        boolean isFreeSpin = false;

        // 所有獎項現在均產出隨機積分 1~20
        shardsEarned = processGachaShards(memberId, "ROULETTE", gameId, "轉盤抽獎獲得");

        if (winningSlot.getSlotType() == RouletteSlot.SlotType.FREE_SPIN) {
            isFreeSpin = true;
        }

        // 記錄抽獎
        saveRouletteRecord(memberId, gameId, winningSlot.getPrizeName(), shardsEarned,
                isGuarantee ? 0 : 10, isGuarantee);

        return new SpinResult(winningSlot, isGuarantee, isFreeSpin, shardsEarned,
                luckyValue.getLuckyValue(), threshold);
    }

    /**
     * 取得會員幸運值資訊
     */
    public MemberLuckyValue getMemberLuckyValue(Long memberId) {
        return luckyValueRepository.findByMemberId(memberId)
                .orElse(new MemberLuckyValue(memberId));
    }

    /**
     * 選擇大獎格子（用於保底）
     */
    private RouletteSlot selectJackpotSlot(List<RouletteSlot> slots) {
        // 優先選擇 JACKPOT，其次 RARE
        return slots.stream()
                .filter(s -> s.getSlotType() == RouletteSlot.SlotType.JACKPOT)
                .findFirst()
                .orElse(slots.stream()
                        .filter(s -> s.getSlotType() == RouletteSlot.SlotType.RARE)
                        .findFirst()
                        .orElse(slots.get(0)));
    }

    private MemberLuckyValue getOrCreateLuckyValue(Long memberId) {
        return luckyValueRepository.findByMemberId(memberId)
                .orElse(new MemberLuckyValue(memberId));
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
