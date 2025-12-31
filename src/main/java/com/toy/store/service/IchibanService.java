package com.toy.store.service;

import com.toy.store.exception.AppException;
import com.toy.store.model.*;
import com.toy.store.repository.*;
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

    private final IchibanBoxRepository boxRepository;
    private final IchibanSlotRepository slotRepository;
    private final IchibanPrizeRepository prizeRepository;
    private final MemberRepository memberRepository;
    private final SystemSettingService systemSettingService;

    public IchibanService(
            GachaRecordRepository recordRepository,
            TransactionService transactionService,
            ShardService shardService,
            MissionService missionService,
            IchibanBoxRepository boxRepository,
            IchibanSlotRepository slotRepository,
            IchibanPrizeRepository prizeRepository,
            MemberRepository memberRepository,
            SystemSettingService systemSettingService) {
        super(recordRepository, transactionService, shardService, missionService);
        this.boxRepository = boxRepository;
        this.slotRepository = slotRepository;
        this.prizeRepository = prizeRepository;
        this.memberRepository = memberRepository;
        this.systemSettingService = systemSettingService;
    }

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
        return slotRepository.findByBox_IdOrderBySlotNumberAsc(boxId);
    }

    /**
     * 鎖定格子（3分鐘）
     */
    @Transactional
    public IchibanSlot lockSlot(Long boxId, Integer slotNumber, Long memberId) {
        IchibanSlot slot = slotRepository.findByBox_IdAndSlotNumber(boxId, slotNumber)
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
        return slotRepository.save(slot);
    }

    /**
     * 揭曉格子（付款後）
     */
    @Transactional
    public IchibanSlot revealSlot(Long boxId, Integer slotNumber, Long memberId) {
        IchibanSlot slot = slotRepository.findByBox_IdAndSlotNumber(boxId, slotNumber)
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
        IchibanBox box = slot.getBox();
        deductWallet(memberId, box.getPricePerDraw(), Transaction.TransactionType.ICHIBAN_COST,
                "一番賞抽獎: 箱體ID " + boxId + ", 格子號碼 " + slotNumber);

        // 核心邏輯啟動
        int totalSlotsCount = box.getTotalSlots();
        int revealedCount = totalSlotsCount - slotRepository.countAvailableSlots(boxId);
        double progress = (double) revealedCount / totalSlotsCount;

        // 檢查幸運值保底或收益保護
        applyRevenueProtectionAndLuckyValue(memberId, slot, progress);

        // 揭曉格子
        slot.reveal(memberId);
        slotRepository.save(slot);

        // 減少獎品剩餘數量
        if (slot.getPrize() != null) {
            IchibanPrize prize = slot.getPrize();
            prize.decreaseQuantity();
            prizeRepository.save(prize);

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
     * 1. 驗證所有格子可用
     * 2. 計算總價並扣款
     * 3. 依序揭曉並收集結果
     */
    @Transactional
    public PurchaseResult purchaseMultipleSlots(Long boxId, List<Integer> slotNumbers, Long memberId) {
        if (slotNumbers == null || slotNumbers.isEmpty()) {
            throw new AppException("請選擇至少一個格子");
        }
        if (slotNumbers.size() > 10) {
            throw new AppException("單次最多選擇10格");
        }

        IchibanBox box = boxRepository.findById(boxId)
                .orElseThrow(() -> new AppException("箱體不存在"));

        if (box.getStatus() != IchibanBox.Status.ACTIVE) {
            throw new AppException("該箱體未開放");
        }

        // 1. 驗證並鎖定所有格子
        List<IchibanSlot> lockedSlots = new java.util.ArrayList<>();
        for (Integer slotNumber : slotNumbers) {
            IchibanSlot slot = slotRepository.findByBox_IdAndSlotNumber(boxId, slotNumber)
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
            lockedSlots.add(slotRepository.save(slot));
        }

        // 3. 扣款
        java.math.BigDecimal totalPrice = box.getPricePerDraw().multiply(new java.math.BigDecimal(slotNumbers.size()));
        deductWallet(memberId, totalPrice, Transaction.TransactionType.ICHIBAN_COST,
                "一番賞購買: 箱體ID " + boxId + ", 格數: " + slotNumbers.size());

        // 3. 依序揭曉
        List<SlotResult> results = new java.util.ArrayList<>();
        int totalShards = 0;
        int totalSlotsCount = box.getTotalSlots();

        for (IchibanSlot slot : lockedSlots) {
            int revealedCount = totalSlotsCount - slotRepository.countAvailableSlots(boxId);
            double progress = (double) revealedCount / totalSlotsCount;

            // 檢查幸運值保底或收益保護
            applyRevenueProtectionAndLuckyValue(memberId, slot, progress);

            slot.reveal(memberId);
            slotRepository.save(slot);

            // 減少獎品剩餘數量
            if (slot.getPrize() != null) {
                IchibanPrize prize = slot.getPrize();
                prize.decreaseQuantity();
                prizeRepository.save(prize);

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

        // 5. 更新大箱體狀態 (如有需要)
        boxRepository.save(box);

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
     * 更新會員幸運值
     */
    private void updateMemberLuckyValue(Long memberId, IchibanPrize.Rank rank) {
        if (memberId == null)
            return;
        memberRepository.findById(memberId).ifPresent(member -> {
            if (rank == IchibanPrize.Rank.A) {
                member.setLuckyValue(0); // 抽中大獎重置
            } else {
                member.setLuckyValue(member.getLuckyValue() + 10); // 否則增加 10
            }
            memberRepository.save(member);
        });
    }

    /**
     * 應用收益保護與幸運值保底邏輯
     */
    private void applyRevenueProtectionAndLuckyValue(Long memberId, IchibanSlot slot, double progress) {
        if (memberId == null)
            return;
        Member member = memberRepository.findById(memberId).orElse(null);
        if (member == null)
            return;

        boolean isBigPrize = slot.getPrize() != null && slot.getPrize().getRank() == IchibanPrize.Rank.A;
        boolean hasGuarantee = member.getLuckyValue() >= 100;
        double threshold = systemSettingService.getRevenueThreshold();

        // 1. 幸運值保底邏輯：如果幸運值滿 100 且目前沒中 A 賞，嘗試強制換成 A 賞
        if (hasGuarantee && !isBigPrize) {
            List<IchibanSlot> availableBigSlots = slotRepository
                    .findByBox_IdAndStatus(slot.getBox().getId(), IchibanSlot.Status.AVAILABLE).stream()
                    .filter(s -> s.getPrize() != null && s.getPrize().getRank() == IchibanPrize.Rank.A)
                    .collect(java.util.stream.Collectors.toList());

            if (!availableBigSlots.isEmpty()) {
                IchibanSlot targetSlot = availableBigSlots
                        .get(new java.util.Random().nextInt(availableBigSlots.size()));
                swapPrizes(slot, targetSlot);
                isBigPrize = true; // 現在它是 A 賞了
            }
        }

        // 2. 收益保護邏輯：如果進度不滿門檻 且抽中 A 賞，且「沒有」觸發保底，則強制換成非 A 賞
        if (progress < threshold && isBigPrize && !hasGuarantee) {
            List<IchibanSlot> availableNormalSlots = slotRepository
                    .findByBox_IdAndStatus(slot.getBox().getId(), IchibanSlot.Status.AVAILABLE).stream()
                    .filter(s -> s.getPrize() != null && s.getPrize().getRank() != IchibanPrize.Rank.A
                            && !s.getSlotNumber().equals(slot.getSlotNumber()))
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
        IchibanPrize p1 = s1.getPrize();
        s1.setPrize(s2.getPrize());
        s2.setPrize(p1);
        slotRepository.save(s2); // s1 會在外部 save
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
