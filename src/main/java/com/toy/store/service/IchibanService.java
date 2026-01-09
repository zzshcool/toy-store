package com.toy.store.service;

import com.toy.store.exception.AppException;
import com.toy.store.model.*;
import com.toy.store.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 一番賞服務
 * 處理選號、鎖定、揭曉等核心邏輯
 */
@Service
public class IchibanService extends BaseGachaService {

    private final IchibanBoxMapper boxMapper;
    private final IchibanSlotMapper slotMapper;
    private final IchibanPrizeMapper prizeMapper;
    private final MemberMapper memberMapper;
    private final SystemSettingService systemSettingService;

    public IchibanService(
            GachaRecordMapper recordMapper,
            TransactionService transactionService,
            ShardService shardService,
            MissionService missionService,
            IchibanBoxMapper boxMapper,
            IchibanSlotMapper slotMapper,
            IchibanPrizeMapper prizeMapper,
            MemberMapper memberMapper,
            SystemSettingService systemSettingService) {
        super(recordMapper, transactionService, shardService, missionService);
        this.boxMapper = boxMapper;
        this.slotMapper = slotMapper;
        this.prizeMapper = prizeMapper;
        this.memberMapper = memberMapper;
        this.systemSettingService = systemSettingService;
    }

    /**
     * 取得所有進行中的一番賞箱體
     */
    public List<IchibanBox> getActiveBoxes() {
        return boxMapper.findByStatus(IchibanBox.Status.ACTIVE.name());
    }

    /**
     * 取得箱體詳情（含格子狀態與獎品）
     */
    public IchibanBox getBoxWithSlots(Long boxId) {
        IchibanBox box = boxMapper.findById(boxId).orElse(null);
        if (box != null) {
            box.setSlots(slotMapper.findByBoxIdOrderBySlotNumberAsc(boxId));
            box.setPrizes(prizeMapper.findByBoxId(boxId));
        }
        return box;
    }

    /**
     * 取得箱體的格子狀態
     */
    public List<IchibanSlot> getSlots(Long boxId) {
        return slotMapper.findByBoxIdOrderBySlotNumberAsc(boxId);
    }

    /**
     * 鎖定格子（3分鐘）
     */
    @Transactional
    public IchibanSlot lockSlot(Long boxId, Integer slotNumber, Long memberId) {
        IchibanSlot slot = slotMapper.findByBoxIdAndSlotNumber(boxId, slotNumber)
                .orElseThrow(() -> new AppException("格子不存在"));

        if (slot.getStatus() == IchibanSlot.Status.REVEALED) {
            throw new AppException("該格子已被揭曉");
        }

        if (slot.getStatus() == IchibanSlot.Status.LOCKED) {
            if (slot.isLockExpired()) {
                // 鎖定已過期，可以重新鎖定
                slot.releaseLock();
            } else if (!slot.getLockedByMemberId().equals(memberId)) {
                throw new AppException("該格子已被其他玩家鎖定");
            }
            // 如果是同一玩家，刷新鎖定時間
        }

        slot.lock(memberId);
        slotMapper.update(slot);
        return slot;
    }

    /**
     * 揭曉格子（付款後）
     */
    @Transactional
    public IchibanSlot revealSlot(Long boxId, Integer slotNumber, Long memberId) {
        IchibanSlot slot = slotMapper.findByBoxIdAndSlotNumber(boxId, slotNumber)
                .orElseThrow(() -> new AppException("格子不存在"));

        // 驗證狀態
        if (slot.getStatus() == IchibanSlot.Status.REVEALED) {
            throw new AppException("該格子已被揭曉");
        }

        if (slot.getStatus() == IchibanSlot.Status.LOCKED) {
            if (!slot.getLockedByMemberId().equals(memberId)) {
                throw new AppException("該格子被其他玩家鎖定");
            }
            if (slot.isLockExpired()) {
                throw new AppException("鎖定已過期，請重新選擇");
            }
        }

        // 取得箱體並扣款
        IchibanBox box = boxMapper.findById(boxId)
                .orElseThrow(() -> new AppException("箱體不存在"));
        deductWallet(memberId, box.getPricePerDraw(), Transaction.TransactionType.ICHIBAN_COST,
                "一番賞抽獎: 箱體ID " + boxId + ", 格子號碼 " + slotNumber);

        // 核心邏輯啟動
        int totalSlotsCount = box.getTotalSlots();
        int revealedCount = totalSlotsCount - slotMapper.countAvailableSlots(boxId);
        double progress = (double) revealedCount / totalSlotsCount;

        // 載入獎品資訊
        if (slot.getPrizeId() != null) {
            IchibanPrize prize = prizeMapper.findById(slot.getPrizeId()).orElse(null);
            slot.setPrize(prize);
        }

        // 檢查幸運值保底或收益保護
        applyRevenueProtectionAndLuckyValue(memberId, slot, progress, boxId);

        // 揭曉格子
        slot.reveal(memberId);
        slotMapper.update(slot);

        // 減少獎品剩餘數量
        if (slot.getPrize() != null) {
            IchibanPrize prize = slot.getPrize();
            prize.decreaseQuantity();
            prizeMapper.update(prize);

            // 幸運值邏輯
            updateMemberLuckyValue(memberId, prize.getRank());
        }

        // 產出碎片 (積分)
        int shards = processGachaShards(memberId, "ICHIBAN", boxId, "一番賞抽獎獲得");

        // 記錄抽獎
        String prizeName = slot.getPrize() != null ? slot.getPrize().getName() : "未知獎品";
        String prizeRank = slot.getPrize() != null ? slot.getPrize().getRank().name() : "";
        java.math.BigDecimal prizeValue = slot.getPrize() != null ? slot.getPrize().getEstimatedValue()
                : java.math.BigDecimal.ZERO;
        saveIchibanRecord(memberId, boxId, prizeName, prizeRank, shards, prizeValue);

        // 檢查箱體是否售罄
        checkBoxSoldOut(boxId);

        return slot;
    }

    /**
     * 多格購買（主要 API）
     */
    @Transactional
    public PurchaseResult purchaseMultipleSlots(Long boxId, List<Integer> slotNumbers, Long memberId) {
        if (slotNumbers == null || slotNumbers.isEmpty()) {
            throw new AppException("請選擇至少一個格子");
        }
        if (slotNumbers.size() > 10) {
            throw new AppException("單次最多選擇10格");
        }

        IchibanBox box = boxMapper.findById(boxId)
                .orElseThrow(() -> new AppException("箱體不存在"));

        if (box.getStatus() != IchibanBox.Status.ACTIVE) {
            throw new AppException("該箱體未開放");
        }

        // 1. 驗證並鎖定所有格子
        List<IchibanSlot> lockedSlots = new java.util.ArrayList<>();
        for (Integer slotNumber : slotNumbers) {
            IchibanSlot slot = slotMapper.findByBoxIdAndSlotNumber(boxId, slotNumber)
                    .orElseThrow(() -> new AppException("格子 " + slotNumber + " 不存在"));

            if (slot.getStatus() == IchibanSlot.Status.REVEALED) {
                throw new AppException("格子 " + slotNumber + " 已被揭曉");
            }
            if (slot.getStatus() == IchibanSlot.Status.LOCKED && !slot.isLockExpired()) {
                if (!slot.getLockedByMemberId().equals(memberId)) {
                    throw new AppException("格子 " + slotNumber + " 被其他玩家鎖定");
                }
            }
            slot.lock(memberId);
            slotMapper.update(slot);
            lockedSlots.add(slot);
        }

        // 2. 扣款
        java.math.BigDecimal totalPrice = box.getPricePerDraw().multiply(new java.math.BigDecimal(slotNumbers.size()));
        deductWallet(memberId, totalPrice, Transaction.TransactionType.ICHIBAN_COST,
                "一番賞購買: 箱體ID " + boxId + ", 格數: " + slotNumbers.size());

        // 3. 依序揭曉
        List<SlotResult> results = new java.util.ArrayList<>();
        int totalShards = 0;
        int totalSlotsCount = box.getTotalSlots();

        for (IchibanSlot slot : lockedSlots) {
            int revealedCount = totalSlotsCount - slotMapper.countAvailableSlots(boxId);
            double progress = (double) revealedCount / totalSlotsCount;

            // 載入獎品資訊
            if (slot.getPrizeId() != null) {
                IchibanPrize prize = prizeMapper.findById(slot.getPrizeId()).orElse(null);
                slot.setPrize(prize);
            }

            // 檢查幸運值保底或收益保護
            applyRevenueProtectionAndLuckyValue(memberId, slot, progress, boxId);

            slot.reveal(memberId);
            slotMapper.update(slot);

            // 減少獎品剩餘數量
            if (slot.getPrize() != null) {
                IchibanPrize prize = slot.getPrize();
                prize.decreaseQuantity();
                prizeMapper.update(prize);

                // 幸運值邏輯
                updateMemberLuckyValue(memberId, prize.getRank());
            }

            // 產出碎片 (積分)
            int shards = processGachaShards(memberId, "ICHIBAN", boxId, "一番賞抽獎獲得");
            totalShards += shards;

            // 記錄
            String prizeName = slot.getPrize() != null ? slot.getPrize().getName() : "未知獎品";
            String prizeRank = slot.getPrize() != null ? slot.getPrize().getRank().name() : "";
            java.math.BigDecimal prizeValue = slot.getPrize() != null ? slot.getPrize().getEstimatedValue()
                    : java.math.BigDecimal.ZERO;
            saveIchibanRecord(memberId, boxId, prizeName, prizeRank, shards, prizeValue);

            results.add(new SlotResult(slot.getSlotNumber(), slot.getPrize(), shards));
        }

        // 4. 更新箱體狀態
        boxMapper.update(box);

        // 檢查箱體是否售罄
        checkBoxSoldOut(boxId);

        return new PurchaseResult(results, totalPrice, totalShards);
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
        return slotMapper.releaseExpiredLocks(expireTime);
    }

    /**
     * 建立一番賞箱體（後台用）
     */
    @Transactional
    public IchibanBox createBox(IchibanBox box, List<IchibanPrize> prizes) {
        // 儲存箱體
        boxMapper.insert(box);

        // 儲存獎品
        for (IchibanPrize prize : prizes) {
            prize.setBoxId(box.getId());
            prizeMapper.insert(prize);
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
                slot.setBoxId(box.getId());
                slot.setSlotNumber(slotNumber++);
                slot.setPrizeId(prize.getId());
                slot.setStatus(IchibanSlot.Status.AVAILABLE);
                slotMapper.insert(slot);
            }
        }
        // 更新實際格數
        box.setTotalSlots(slotNumber - 1);
        boxMapper.update(box);
    }

    /**
     * 更新會員幸運值
     */
    private void updateMemberLuckyValue(Long memberId, IchibanPrize.Rank rank) {
        if (memberId == null)
            return;
        memberMapper.findById(memberId).ifPresent(member -> {
            if (rank == IchibanPrize.Rank.A) {
                member.setLuckyValue(0); // 抽中大獎重置
            } else {
                member.setLuckyValue(member.getLuckyValue() + 10); // 否則增加 10
            }
            memberMapper.update(member);
        });
    }

    /**
     * 應用收益保護與幸運值保底邏輯
     */
    private void applyRevenueProtectionAndLuckyValue(Long memberId, IchibanSlot slot, double progress, Long boxId) {
        if (memberId == null)
            return;
        Member member = memberMapper.findById(memberId).orElse(null);
        if (member == null)
            return;

        boolean isBigPrize = slot.getPrize() != null && slot.getPrize().getRank() == IchibanPrize.Rank.A;
        boolean hasGuarantee = member.getLuckyValue() >= 100;
        double threshold = systemSettingService.getRevenueThreshold();

        // 1. 幸運值保底邏輯
        if (hasGuarantee && !isBigPrize) {
            List<IchibanSlot> availableBigSlots = slotMapper
                    .findByBoxIdAndStatus(boxId, IchibanSlot.Status.AVAILABLE.name())
                    .stream()
                    .filter(s -> {
                        if (s.getPrizeId() != null) {
                            IchibanPrize p = prizeMapper.findById(s.getPrizeId()).orElse(null);
                            return p != null && p.getRank() == IchibanPrize.Rank.A;
                        }
                        return false;
                    })
                    .collect(java.util.stream.Collectors.toList());

            if (!availableBigSlots.isEmpty()) {
                IchibanSlot targetSlot = availableBigSlots
                        .get(new java.util.Random().nextInt(availableBigSlots.size()));
                swapPrizes(slot, targetSlot);
                isBigPrize = true;
            }
        }

        // 2. 收益保護邏輯
        if (progress < threshold && isBigPrize && !hasGuarantee) {
            List<IchibanSlot> availableNormalSlots = slotMapper
                    .findByBoxIdAndStatus(boxId, IchibanSlot.Status.AVAILABLE.name())
                    .stream()
                    .filter(s -> {
                        if (s.getPrizeId() != null) {
                            IchibanPrize p = prizeMapper.findById(s.getPrizeId()).orElse(null);
                            return p != null && p.getRank() != IchibanPrize.Rank.A
                                    && !s.getSlotNumber().equals(slot.getSlotNumber());
                        }
                        return false;
                    })
                    .collect(java.util.stream.Collectors.toList());

            if (!availableNormalSlots.isEmpty()) {
                IchibanSlot targetSlot = availableNormalSlots
                        .get(new java.util.Random().nextInt(availableNormalSlots.size()));
                swapPrizes(slot, targetSlot);
            }
        }
    }

    /**
     * 交換兩個格子的獎品
     */
    private void swapPrizes(IchibanSlot s1, IchibanSlot s2) {
        Long p1Id = s1.getPrizeId();
        IchibanPrize p1 = s1.getPrize();

        s1.setPrizeId(s2.getPrizeId());
        s1.setPrize(s2.getPrize());

        s2.setPrizeId(p1Id);
        s2.setPrize(p1);

        slotMapper.update(s2);
    }

    /**
     * 檢查箱體是否售罄
     */
    private void checkBoxSoldOut(Long boxId) {
        int available = slotMapper.countAvailableSlots(boxId);
        if (available == 0) {
            boxMapper.findById(boxId).ifPresent(box -> {
                box.setStatus(IchibanBox.Status.SOLD_OUT);
                boxMapper.update(box);
            });
        }
    }
}
