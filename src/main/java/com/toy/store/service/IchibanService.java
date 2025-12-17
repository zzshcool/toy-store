package com.toy.store.service;

import com.toy.store.model.*;
import com.toy.store.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 一番賞服務
 * 處理選號、鎖定、揭曉等核心邏輯
 */
@Service
public class IchibanService {

    @Autowired
    private IchibanBoxRepository boxRepository;

    @Autowired
    private IchibanSlotRepository slotRepository;

    @Autowired
    private IchibanPrizeRepository prizeRepository;

    @Autowired
    private GachaRecordRepository recordRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ShardService shardService;

    /**
     * 取得所有進行中的一番賞箱體
     */
    public List<IchibanBox> getActiveBoxes() {
        return boxRepository.findByStatus(IchibanBox.Status.ACTIVE);
    }

    /**
     * 取得箱體詳情（含格子狀態）
     */
    public IchibanBox getBoxWithSlots(Long boxId) {
        return boxRepository.findById(boxId).orElse(null);
    }

    /**
     * 取得箱體的格子狀態
     */
    public List<IchibanSlot> getSlots(Long boxId) {
        return slotRepository.findByBoxIdOrderBySlotNumberAsc(boxId);
    }

    /**
     * 鎖定格子（3分鐘）
     */
    @Transactional
    public IchibanSlot lockSlot(Long boxId, Integer slotNumber, Long memberId) {
        IchibanSlot slot = slotRepository.findByBoxIdAndSlotNumber(boxId, slotNumber)
                .orElseThrow(() -> new RuntimeException("格子不存在"));

        if (slot.getStatus() == IchibanSlot.Status.REVEALED) {
            throw new RuntimeException("該格子已被揭曉");
        }

        if (slot.getStatus() == IchibanSlot.Status.LOCKED) {
            if (slot.isLockExpired()) {
                // 鎖定已過期，可以重新鎖定
                slot.releaseLock();
            } else if (!slot.getLockedByMemberId().equals(memberId)) {
                throw new RuntimeException("該格子已被其他玩家鎖定");
            }
            // 如果是同一玩家，刷新鎖定時間
        }

        slot.lock(memberId);
        return slotRepository.save(slot);
    }

    /**
     * 揭曉格子（付款後）
     */
    @Transactional
    public IchibanSlot revealSlot(Long boxId, Integer slotNumber, Long memberId) {
        IchibanSlot slot = slotRepository.findByBoxIdAndSlotNumber(boxId, slotNumber)
                .orElseThrow(() -> new RuntimeException("格子不存在"));

        // 驗證狀態
        if (slot.getStatus() == IchibanSlot.Status.REVEALED) {
            throw new RuntimeException("該格子已被揭曉");
        }

        if (slot.getStatus() == IchibanSlot.Status.LOCKED) {
            if (!slot.getLockedByMemberId().equals(memberId)) {
                throw new RuntimeException("該格子被其他玩家鎖定");
            }
            if (slot.isLockExpired()) {
                throw new RuntimeException("鎖定已過期，請重新選擇");
            }
        }

        // 取得箱體並扣款
        IchibanBox box = slot.getBox();
        transactionService.updateWalletBalance(memberId, box.getPricePerDraw().negate(),
                Transaction.TransactionType.MYSTERY_BOX_COST, "ICHIBAN-" + boxId + "-" + slotNumber);

        // 揭曉格子
        slot.reveal(memberId);
        slotRepository.save(slot);

        // 減少獎品剩餘數量
        if (slot.getPrize() != null) {
            IchibanPrize prize = slot.getPrize();
            prize.decreaseQuantity();
            prizeRepository.save(prize);
        }

        // 產出碎片
        int shards = shardService.generateRandomShards();
        shardService.addGachaShards(memberId, shards, "ICHIBAN", boxId, "一番賞抽獎獲得");

        // 記錄抽獎
        String prizeName = slot.getPrize() != null ? slot.getPrize().getName() : "未知獎品";
        String prizeRank = slot.getPrize() != null ? slot.getPrize().getRank().name() : "";
        GachaRecord record = GachaRecord.createIchibanRecord(memberId, boxId, prizeName, prizeRank, shards);
        recordRepository.save(record);

        // 檢查箱體是否售罄
        checkBoxSoldOut(boxId);

        return slot;
    }

    /**
     * 多格購買（主要 API）
     * 1. 驗證所有格子可用
     * 2. 計算總價並扣款
     * 3. 依序揭曉並收集結果
     */
    @Transactional
    public PurchaseResult purchaseMultipleSlots(Long boxId, List<Integer> slotNumbers, Long memberId) {
        if (slotNumbers == null || slotNumbers.isEmpty()) {
            throw new RuntimeException("請選擇至少一個格子");
        }
        if (slotNumbers.size() > 10) {
            throw new RuntimeException("單次最多選擇10格");
        }

        IchibanBox box = boxRepository.findById(boxId)
                .orElseThrow(() -> new RuntimeException("箱體不存在"));

        if (box.getStatus() != IchibanBox.Status.ACTIVE) {
            throw new RuntimeException("該箱體未開放");
        }

        // 1. 驗證並鎖定所有格子
        List<IchibanSlot> lockedSlots = new java.util.ArrayList<>();
        for (Integer slotNumber : slotNumbers) {
            IchibanSlot slot = slotRepository.findByBoxIdAndSlotNumber(boxId, slotNumber)
                    .orElseThrow(() -> new RuntimeException("格子 " + slotNumber + " 不存在"));

            if (slot.getStatus() == IchibanSlot.Status.REVEALED) {
                throw new RuntimeException("格子 " + slotNumber + " 已被揭曉");
            }
            if (slot.getStatus() == IchibanSlot.Status.LOCKED && !slot.isLockExpired()) {
                if (!slot.getLockedByMemberId().equals(memberId)) {
                    throw new RuntimeException("格子 " + slotNumber + " 被其他玩家鎖定");
                }
            }
            slot.lock(memberId);
            lockedSlots.add(slotRepository.save(slot));
        }

        // 2. 計算總價並扣款
        java.math.BigDecimal totalCost = box.getPricePerDraw()
                .multiply(new java.math.BigDecimal(slotNumbers.size()));
        transactionService.updateWalletBalance(memberId, totalCost.negate(),
                Transaction.TransactionType.MYSTERY_BOX_COST, "ICHIBAN-" + boxId + "-MULTI-" + slotNumbers.size());

        // 3. 依序揭曉
        List<SlotResult> results = new java.util.ArrayList<>();
        int totalShards = 0;

        for (IchibanSlot slot : lockedSlots) {
            slot.reveal(memberId);
            slotRepository.save(slot);

            // 減少獎品剩餘數量
            if (slot.getPrize() != null) {
                IchibanPrize prize = slot.getPrize();
                prize.decreaseQuantity();
                prizeRepository.save(prize);
            }

            // 產出碎片
            int shards = shardService.generateRandomShards();
            totalShards += shards;

            // 記錄
            String prizeName = slot.getPrize() != null ? slot.getPrize().getName() : "未知獎品";
            String prizeRank = slot.getPrize() != null ? slot.getPrize().getRank().name() : "";
            GachaRecord record = GachaRecord.createIchibanRecord(memberId, boxId, prizeName, prizeRank, shards);
            recordRepository.save(record);

            results.add(new SlotResult(slot.getSlotNumber(), slot.getPrize(), shards));
        }

        // 添加碎片給會員
        shardService.addGachaShards(memberId, totalShards, "ICHIBAN", boxId, "一番賞抽獎獲得");

        // 檢查箱體是否售罄
        checkBoxSoldOut(boxId);

        return new PurchaseResult(results, totalCost, totalShards);
    }

    /**
     * 購買結果 DTO
     */
    public static class PurchaseResult {
        private final List<SlotResult> slots;
        private final java.math.BigDecimal totalCost;
        private final int totalShards;

        public PurchaseResult(List<SlotResult> slots, java.math.BigDecimal totalCost, int totalShards) {
            this.slots = slots;
            this.totalCost = totalCost;
            this.totalShards = totalShards;
        }

        public List<SlotResult> getSlots() {
            return slots;
        }

        public java.math.BigDecimal getTotalCost() {
            return totalCost;
        }

        public int getTotalShards() {
            return totalShards;
        }
    }

    /**
     * 單格結果 DTO
     */
    public static class SlotResult {
        private final Integer slotNumber;
        private final IchibanPrize prize;
        private final int shards;

        public SlotResult(Integer slotNumber, IchibanPrize prize, int shards) {
            this.slotNumber = slotNumber;
            this.prize = prize;
            this.shards = shards;
        }

        public Integer getSlotNumber() {
            return slotNumber;
        }

        public IchibanPrize getPrize() {
            return prize;
        }

        public int getShards() {
            return shards;
        }
    }

    /**
     * 釋放過期的鎖定（由排程器呼叫）
     */
    @Transactional
    public int releaseExpiredLocks() {
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(3);
        return slotRepository.releaseExpiredLocks(expireTime);
    }

    /**
     * 建立一番賞箱體（後台用）
     */
    @Transactional
    public IchibanBox createBox(IchibanBox box, List<IchibanPrize> prizes) {
        // 儲存箱體
        box = boxRepository.save(box);

        // 儲存獎品
        for (IchibanPrize prize : prizes) {
            prize.setBox(box);
            prizeRepository.save(prize);
        }

        // 根據獎品建立格子
        createSlotsFromPrizes(box, prizes);

        return box;
    }

    /**
     * 根據獎品建立格子
     */
    private void createSlotsFromPrizes(IchibanBox box, List<IchibanPrize> prizes) {
        int slotNumber = 1;
        for (IchibanPrize prize : prizes) {
            for (int i = 0; i < prize.getTotalQuantity() && slotNumber <= box.getTotalSlots(); i++) {
                IchibanSlot slot = new IchibanSlot();
                slot.setBox(box);
                slot.setSlotNumber(slotNumber++);
                slot.setPrize(prize);
                slot.setStatus(IchibanSlot.Status.AVAILABLE);
                slotRepository.save(slot);
            }
        }
        // 更新實際格數
        box.setTotalSlots(slotNumber - 1);
        boxRepository.save(box);
    }

    /**
     * 檢查箱體是否售罄
     */
    private void checkBoxSoldOut(Long boxId) {
        int available = slotRepository.countAvailableSlots(boxId);
        if (available == 0) {
            boxRepository.findById(boxId).ifPresent(box -> {
                box.setStatus(IchibanBox.Status.SOLD_OUT);
                boxRepository.save(box);
            });
        }
    }
}
