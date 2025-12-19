package com.toy.store.service;

import com.toy.store.exception.AppException;
import com.toy.store.model.*;
import com.toy.store.repository.GachaRecordRepository;
import com.toy.store.repository.MysteryBoxThemeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.math.BigDecimal;

@Service
public class MysteryBoxService {

    @Autowired
    private MysteryBoxThemeRepository themeRepository;

    @Autowired
    private com.toy.store.repository.MemberRepository memberRepository;

    @Autowired
    private com.toy.store.repository.OrderRepository orderRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private com.toy.store.repository.MemberCouponRepository memberCouponRepository;

    @Autowired
    private GachaRecordRepository gachaRecordRepository;

    private final Random random = new Random();

    /**
     * Draws a mystery box.
     * 1. Check/Deduct Balance (OR Use Coupon)
     * 2. Weighted Random Selection
     * 3. Return Item
     */
    @Transactional
    public MysteryBoxItem drawBox(Long memberId, Long themeId, Long couponId) {
        // 1. Get Theme
        MysteryBoxTheme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new AppException("找不到盲盒主題"));

        boolean useCoupon = false;

        if (couponId != null) {
            com.toy.store.model.MemberCoupon memberCoupon = memberCouponRepository.findById(couponId)
                    .orElseThrow(() -> new AppException("找不到優惠券"));

            if (!memberCoupon.getMember().getId().equals(memberId)) {
                throw new AppException("優惠券不屬於此會員");
            }
            if (memberCoupon.getStatus() != com.toy.store.model.MemberCoupon.Status.UNUSED) {
                throw new AppException("優惠券已使用");
            }
            if (memberCoupon.getCoupon().getType() != com.toy.store.model.Coupon.CouponType.MYSTERY_BOX_FREE) {
                throw new AppException("非盲盒免費券類型");
            }

            // Mark used
            memberCoupon.setStatus(com.toy.store.model.MemberCoupon.Status.USED);
            memberCoupon.setUsedAt(java.time.LocalDateTime.now());
            memberCouponRepository.save(memberCoupon);
            useCoupon = true;
            memberCoupon.setUsedAt(java.time.LocalDateTime.now());
            memberCouponRepository.save(memberCoupon);
            useCoupon = true;
        }

        if (!useCoupon) {
            // 2. Deduct Cost
            transactionService.updateWalletBalance(memberId, theme.getPrice().negate(),
                    Transaction.TransactionType.MYSTERY_BOX_COST, "盲盒消費: " + theme.getName());
        } else {
            // Log free spin usage?
            // Maybe via TransactionService with 0 amount or just skipping it.
            // We can record a 0 amount transaction or just rely on MemberCoupon history.
            // Let's skip transaction deduction.
        }

        // 3. Weighted Random Selection
        List<MysteryBoxItem> items = theme.getItems();
        if (items == null || items.isEmpty()) {
            throw new AppException("此盲盒主題沒有獎品");
        }

        int totalWeight = items.stream().mapToInt(MysteryBoxItem::getWeight).sum();
        int randomValue = random.nextInt(totalWeight);
        int currentWeight = 0;

        MysteryBoxItem selectedItem = null;
        for (MysteryBoxItem item : items) {
            currentWeight += item.getWeight();
            if (randomValue < currentWeight) {
                selectedItem = item;
                break;
            }
        }

        // 4. 記錄抽獎紀錄
        GachaRecord record = new GachaRecord();
        record.setMemberId(memberId);
        record.setGachaType(GachaRecord.GachaType.MYSTERY_BOX);
        record.setGameId(themeId);
        record.setPrizeName(selectedItem.getName());
        record.setCreatedAt(LocalDateTime.now());
        gachaRecordRepository.save(record);

        // 5. Create Order Record for the Prize
        Order prizeOrder = new Order();
        prizeOrder.setMember(memberRepository.findById(memberId).orElseThrow());
        prizeOrder.setTotalPrice(BigDecimal.ZERO); // It's a prize
        prizeOrder.setStatus(Order.OrderStatus.COMPLETED); // Instant win

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(prizeOrder);
        orderItem.setProductName("獎品: " + selectedItem.getName());
        orderItem.setPriceAtPurchase(selectedItem.getEstimatedValue());
        orderItem.setQuantity(1);

        prizeOrder.addItem(orderItem);
        orderRepository.save(prizeOrder);

        return selectedItem;
    }

    public List<MysteryBoxTheme> getAllThemes() {
        return themeRepository.findAll();
    }

    /**
     * 盲盒試抽（不扣款，不檢查權限）
     */
    public MysteryBoxItem drawTrial(Long themeId) {
        if (themeId == null)
            throw new AppException("請選擇主題");

        MysteryBoxTheme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new AppException("主題不存在"));

        List<MysteryBoxItem> items = theme.getItems();
        if (items == null || items.isEmpty()) {
            throw new AppException("該主題暫無獎品");
        }

        // 簡單的隨機抽選
        int totalWeight = items.stream().mapToInt(MysteryBoxItem::getWeight).sum();
        int randomValue = new java.util.Random().nextInt(totalWeight);

        int current = 0;
        for (MysteryBoxItem item : items) {
            current += item.getWeight();
            if (randomValue < current) {
                return item;
            }
        }
        return items.get(0);
    }
}
