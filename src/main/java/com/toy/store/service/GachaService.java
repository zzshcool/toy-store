package com.toy.store.service;

import com.toy.store.exception.AppException;
import com.toy.store.model.*;
import com.toy.store.repository.GachaRecordRepository;
import com.toy.store.repository.GachaThemeRepository;
import com.toy.store.repository.MemberRepository;
import com.toy.store.repository.OrderRepository;
import com.toy.store.repository.MemberCouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.math.BigDecimal;

@Service
public class GachaService {

    @Autowired
    private GachaThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private MemberCouponRepository memberCouponRepository;

    @Autowired
    private GachaRecordRepository gachaRecordRepository;

    private final Random random = new Random();

    /**
     * Draws a gacha box.
     */
    @Transactional
    public GachaItem drawBox(Long memberId, Long themeId, Long couponId) {
        GachaTheme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new AppException("找不到扭蛋主題"));

        boolean useCoupon = false;

        if (couponId != null) {
            MemberCoupon memberCoupon = memberCouponRepository.findById(couponId)
                    .orElseThrow(() -> new AppException("找不到優惠券"));

            if (!memberCoupon.getMember().getId().equals(memberId)) {
                throw new AppException("優惠券不屬於此會員");
            }
            if (memberCoupon.getStatus() != MemberCoupon.Status.UNUSED) {
                throw new AppException("優惠券已使用");
            }
            if (memberCoupon.getCoupon().getType() != Coupon.CouponType.MYSTERY_BOX_FREE) {
                throw new AppException("非扭蛋免費券類型");
            }

            memberCoupon.setStatus(MemberCoupon.Status.USED);
            memberCoupon.setUsedAt(LocalDateTime.now());
            memberCouponRepository.save(memberCoupon);
            useCoupon = true;
        }

        if (!useCoupon) {
            transactionService.updateWalletBalance(memberId, theme.getPrice().negate(),
                    Transaction.TransactionType.GACHA_COST, "扭蛋消費: " + theme.getName());
        }

        List<GachaItem> items = theme.getItems();
        if (items == null || items.isEmpty()) {
            throw new AppException("此扭蛋主題沒有獎品");
        }

        int totalWeight = items.stream().mapToInt(GachaItem::getWeight).sum();
        int randomValue = random.nextInt(totalWeight);
        int currentWeight = 0;

        GachaItem selectedItem = null;
        for (GachaItem item : items) {
            currentWeight += item.getWeight();
            if (randomValue < currentWeight) {
                selectedItem = item;
                break;
            }
        }

        // 記錄抽獎紀錄
        GachaRecord record = new GachaRecord();
        record.setMemberId(memberId);
        record.setGachaType(GachaRecord.GachaType.GACHA);
        record.setGameId(themeId);
        record.setPrizeName(selectedItem.getName());
        record.setCreatedAt(LocalDateTime.now());
        gachaRecordRepository.save(record);

        // Create Order Record for the Prize
        Order prizeOrder = new Order();
        prizeOrder.setMember(memberRepository.findById(memberId).orElseThrow());
        prizeOrder.setTotalPrice(BigDecimal.ZERO);
        prizeOrder.setStatus(Order.OrderStatus.COMPLETED);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(prizeOrder);
        orderItem.setProductName("獎品: " + selectedItem.getName());
        orderItem.setPriceAtPurchase(selectedItem.getEstimatedValue());
        orderItem.setQuantity(1);

        prizeOrder.addItem(orderItem);
        orderRepository.save(prizeOrder);

        return selectedItem;
    }

    public List<GachaTheme> getAllThemes() {
        return themeRepository.findAll();
    }

    /**
     * 扭蛋試抽
     */
    public GachaItem drawTrial(Long themeId) {
        if (themeId == null)
            throw new AppException("請選擇主題");

        GachaTheme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new AppException("主題不存在"));

        List<GachaItem> items = theme.getItems();
        if (items == null || items.isEmpty()) {
            throw new AppException("該主題暫無獎品");
        }

        int totalWeight = items.stream().mapToInt(GachaItem::getWeight).sum();
        int randomValue = random.nextInt(totalWeight);

        int current = 0;
        for (GachaItem item : items) {
            current += item.getWeight();
            if (randomValue < current) {
                return item;
            }
        }
        return items.get(0);
    }
}
