package com.toy.store.service;

import com.toy.store.exception.AppException;
import com.toy.store.model.*;
import com.toy.store.repository.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

/**
 * 盲盒（動漫周邊）服務
 * 對應規格書 §4.D 動漫周邊系統
 */
@Service
@RequiredArgsConstructor
public class BlindBoxService {

    private final BlindBoxRepository boxRepository;
    private final BlindBoxItemRepository itemRepository;
    private final PropCardRepository propCardRepository;
    private final MemberRepository memberRepository;
    private final TransactionService transactionService;

    /**
     * 取得所有進行中的盲盒
     */
    public List<BlindBox> getActiveBoxes() {
        return boxRepository.findByStatusOrderByCreatedAtDesc(BlindBox.Status.ACTIVE);
    }

    /**
     * 取得所有盲盒（包含售完）
     */
    public List<BlindBox> getAllBoxes() {
        return boxRepository.findAll();
    }

    /**
     * 取得盲盒詳情
     */
    public BlindBox getBoxWithItems(Long boxId) {
        return boxRepository.findById(boxId).orElse(null);
    }

    /**
     * 取得盲盒內的所有單品
     */
    public List<BlindBoxItem> getItems(Long boxId) {
        return itemRepository.findByBlindBox_IdOrderByBoxNumberAsc(boxId);
    }

    /**
     * 鎖定盒子（開始 180 秒倒數確認期）
     */
    @Transactional
    public BlindBoxItem lockItem(Long boxId, Integer boxNumber, Long memberId) {
        BlindBoxItem item = itemRepository.findByBlindBox_IdAndBoxNumber(boxId, boxNumber)
                .orElseThrow(() -> new AppException("盒子不存在"));

        if (item.getStatus() == BlindBoxItem.Status.SOLD) {
            throw new AppException("此盒子已售出");
        }

        if (item.getStatus() == BlindBoxItem.Status.LOCKED && !item.isLockExpired()) {
            if (!memberId.equals(item.getLockedByMemberId())) {
                throw new AppException("此盒子已被其他玩家鎖定，剩餘 " + item.getRemainingLockSeconds() + " 秒");
            }
            // 自己鎖定的，直接返回
            return item;
        }

        // 如果鎖定已過期，先釋放
        if (item.isLockExpired()) {
            item.releaseLock();
        }

        item.lock(memberId);
        return itemRepository.save(item);
    }

    /**
     * 確認購買（使用代幣）
     */
    @Transactional
    public PurchaseResult purchaseItem(Long boxId, Integer boxNumber, Long memberId) {
        BlindBoxItem item = itemRepository.findByBlindBox_IdAndBoxNumber(boxId, boxNumber)
                .orElseThrow(() -> new AppException("盒子不存在"));

        // 驗證狀態
        if (item.getStatus() != BlindBoxItem.Status.LOCKED) {
            throw new AppException("請先選擇並鎖定盒子");
        }

        if (!memberId.equals(item.getLockedByMemberId())) {
            throw new AppException("此盒子不是由您鎖定的");
        }

        if (item.isLockExpired()) {
            item.releaseLock();
            itemRepository.save(item);
            throw new AppException("鎖定已過期，請重新選擇");
        }

        BlindBox box = item.getBlindBox();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AppException("會員不存在"));

        // 扣除代幣
        BigDecimal price = box.getPricePerBox();
        transactionService.deductBalance(memberId, price, "盲盒購買: " + box.getName());

        // 完成購買
        item.purchase(memberId);
        itemRepository.save(item);

        // 發放碎片獎勵
        int shards = calculateShards(item);
        if (shards > 0) {
            member.setPoints(member.getPoints() + shards);
            memberRepository.save(member);
        }

        return new PurchaseResult(item, price, shards);
    }

    /**
     * 全包購買（整中盒）
     */
    @Transactional
    public FullBoxResult purchaseFullBox(Long boxId, Long memberId) {
        BlindBox box = boxRepository.findById(boxId)
                .orElseThrow(() -> new AppException("盲盒不存在"));

        List<BlindBoxItem> availableItems = itemRepository.findByBlindBox_IdAndStatus(boxId,
                BlindBoxItem.Status.AVAILABLE);

        if (availableItems.isEmpty()) {
            throw new AppException("此盲盒已售罄");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AppException("會員不存在"));

        // 計算全包價格（使用優惠價）
        BigDecimal fullPrice = box.getFullBoxPrice();
        transactionService.deductBalance(memberId, fullPrice, "盲盒全包: " + box.getName());

        // 購買所有可用盒子
        int totalShards = 0;
        for (BlindBoxItem item : availableItems) {
            item.purchase(memberId);
            totalShards += calculateShards(item);
        }
        itemRepository.saveAll(availableItems);

        // 發放碎片
        if (totalShards > 0) {
            member.setPoints(member.getPoints() + totalShards);
            memberRepository.save(member);
        }

        // 更新盲盒狀態
        box.setStatus(BlindBox.Status.SOLD_OUT);
        boxRepository.save(box);

        return new FullBoxResult(availableItems, fullPrice, totalShards);
    }

    /**
     * 天選抽（電腦隨機選號）
     */
    @Transactional
    public PurchaseResult randomPurchase(Long boxId, Long memberId) {
        List<BlindBoxItem> availableItems = itemRepository.findByBlindBox_IdAndStatus(boxId,
                BlindBoxItem.Status.AVAILABLE);

        if (availableItems.isEmpty()) {
            throw new AppException("此盲盒已售罄");
        }

        // 隨機選擇一個
        Random random = new Random();
        BlindBoxItem selectedItem = availableItems.get(random.nextInt(availableItems.size()));

        // 鎖定並購買
        selectedItem.lock(memberId);
        itemRepository.save(selectedItem);

        return purchaseItem(boxId, selectedItem.getBoxNumber(), memberId);
    }

    /**
     * 使用道具卡 - 提示卡
     */
    public List<BlindBoxItem> useHintCard(Long boxId, Long memberId) {
        PropCard card = propCardRepository.findByMemberIdAndCardType(memberId, PropCard.CardType.HINT)
                .orElseThrow(() -> new AppException("您沒有提示卡"));

        if (!card.use()) {
            throw new AppException("提示卡已用完或已過期");
        }
        propCardRepository.save(card);

        // 返回過濾後的盒子列表（排除某個稀有度）
        List<BlindBoxItem> items = itemRepository.findByBlindBox_IdAndStatus(boxId, BlindBoxItem.Status.AVAILABLE);

        // 隨機排除一個稀有度等級的提示
        BlindBoxItem.Rarity[] rarities = BlindBoxItem.Rarity.values();
        BlindBoxItem.Rarity excludedRarity = rarities[new Random().nextInt(rarities.length)];

        return items.stream()
                .filter(i -> i.getRarity() != excludedRarity)
                .toList();
    }

    /**
     * 使用道具卡 - 透視卡
     */
    public BlindBoxItem usePeekCard(Long boxId, Integer boxNumber, Long memberId) {
        PropCard card = propCardRepository.findByMemberIdAndCardType(memberId, PropCard.CardType.PEEK)
                .orElseThrow(() -> new AppException("您沒有透視卡"));

        if (!card.use()) {
            throw new AppException("透視卡已用完或已過期");
        }
        propCardRepository.save(card);

        // 返回盒子內容詳情
        return itemRepository.findByBlindBox_IdAndBoxNumber(boxId, boxNumber)
                .orElseThrow(() -> new AppException("盒子不存在"));
    }

    /**
     * 使用道具卡 - 換一盒
     */
    @Transactional
    public BlindBoxItem useSwapCard(Long boxId, Integer currentBoxNumber, Long memberId) {
        PropCard card = propCardRepository.findByMemberIdAndCardType(memberId, PropCard.CardType.SWAP)
                .orElseThrow(() -> new AppException("您沒有換一盒道具"));

        if (!card.use()) {
            throw new AppException("換一盒道具已用完或已過期");
        }
        propCardRepository.save(card);

        // 釋放當前鎖定
        BlindBoxItem currentItem = itemRepository.findByBlindBox_IdAndBoxNumber(boxId, currentBoxNumber)
                .orElseThrow(() -> new AppException("盒子不存在"));

        if (currentItem.getStatus() == BlindBoxItem.Status.LOCKED
                && memberId.equals(currentItem.getLockedByMemberId())) {
            currentItem.releaseLock();
            itemRepository.save(currentItem);
        }

        // 隨機選擇新盒子
        List<BlindBoxItem> availableItems = itemRepository.findByBlindBox_IdAndStatus(boxId,
                BlindBoxItem.Status.AVAILABLE);
        if (availableItems.isEmpty()) {
            throw new AppException("沒有其他可選盒子");
        }

        Random random = new Random();
        BlindBoxItem newItem = availableItems.get(random.nextInt(availableItems.size()));
        newItem.lock(memberId);
        return itemRepository.save(newItem);
    }

    /**
     * 試抽（模擬）
     */
    public TrialResult trial(Long boxId, int count) {
        BlindBox box = boxRepository.findById(boxId)
                .orElseThrow(() -> new AppException("盲盒不存在"));

        List<BlindBoxItem> items = itemRepository.findByBlindBox_IdAndStatus(boxId, BlindBoxItem.Status.AVAILABLE);
        if (items.isEmpty()) {
            throw new AppException("此盲盒已售罄，無法試抽");
        }

        Random random = new Random();
        java.util.List<BlindBoxItem> results = new java.util.ArrayList<>();

        for (int i = 0; i < count && i < items.size(); i++) {
            results.add(items.get(random.nextInt(items.size())));
        }

        return new TrialResult(box, results);
    }

    private int calculateShards(BlindBoxItem item) {
        return switch (item.getRarity()) {
            case SECRET -> 50;
            case ULTRA_RARE -> 30;
            case RARE -> 20;
            case NORMAL -> 10;
        };
    }

    // ============== 結果類別 ==============

    @Data
    public static class PurchaseResult {
        private final BlindBoxItem item;
        private final BigDecimal cost;
        private final int shards;
    }

    @Data
    public static class FullBoxResult {
        private final List<BlindBoxItem> items;
        private final BigDecimal cost;
        private final int totalShards;
    }

    @Data
    public static class TrialResult {
        private final BlindBox box;
        private final List<BlindBoxItem> results;
    }
}
