package com.toy.store.service;

import com.toy.store.exception.AppException;
import com.toy.store.model.*;
import com.toy.store.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.math.BigDecimal;

@Service
public class GachaService extends BaseGachaService {

    private final GachaThemeMapper themeMapper;
    private final GachaItemMapper itemMapper;
    private final MemberMapper memberMapper;
    private final OrderMapper orderMapper;
    private final MemberCouponMapper memberCouponMapper;
    private final CouponMapper couponMapper;
    private final Random random = new Random();

    public GachaService(
            GachaRecordMapper recordMapper,
            TransactionService transactionService,
            ShardService shardService,
            MissionService missionService,
            GachaThemeMapper themeMapper,
            GachaItemMapper itemMapper,
            MemberMapper memberMapper,
            OrderMapper orderMapper,
            MemberCouponMapper memberCouponMapper,
            CouponMapper couponMapper) {
        super(recordMapper, transactionService, shardService, missionService);
        this.themeMapper = themeMapper;
        this.itemMapper = itemMapper;
        this.memberMapper = memberMapper;
        this.orderMapper = orderMapper;
        this.memberCouponMapper = memberCouponMapper;
        this.couponMapper = couponMapper;
    }

    /**
     * Draws a gacha box.
     */
    @Transactional
    public GachaItem drawBox(Long memberId, Long themeId, Long couponId) {
        GachaTheme theme = themeMapper.findById(themeId)
                .orElseThrow(() -> new AppException("找不到扭蛋主題"));

        boolean useCoupon = false;

        if (couponId != null) {
            MemberCoupon memberCoupon = memberCouponMapper.findById(couponId)
                    .orElseThrow(() -> new AppException("找不到優惠券"));

            if (!memberCoupon.getMemberId().equals(memberId)) {
                throw new AppException("優惠券不屬於此會員");
            }
            if (!"UNUSED".equals(memberCoupon.getStatus())) {
                throw new AppException("優惠券已使用");
            }

            // 檢查優惠券類型
            if (memberCoupon.getCouponId() != null) {
                Coupon coupon = couponMapper.findById(memberCoupon.getCouponId()).orElse(null);
                if (coupon == null || !"MYSTERY_BOX_FREE".equals(coupon.getType())) {
                    throw new AppException("非扭蛋免費券類型");
                }
            }

            memberCoupon.setStatus("USED");
            memberCoupon.setUsedAt(LocalDateTime.now());
            memberCouponMapper.update(memberCoupon);
            useCoupon = true;
        }

        if (!useCoupon) {
            transactionService.updateWalletBalance(memberId, theme.getPrice().negate(),
                    Transaction.TransactionType.GACHA_COST, "扭蛋消費: " + theme.getName());
        }

        List<GachaItem> items = itemMapper.findByIpId(themeId);
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

        // 產出隨機碎片 (1~20)
        int shards = processGachaShards(memberId, "GACHA", themeId, "扭蛋抽獎獲得");

        // 記錄抽獎紀錄
        saveGachaRecord(memberId, themeId, selectedItem.getName(), shards);

        // Create Order Record for the Prize
        Member member = memberMapper.findById(memberId).orElseThrow();
        Order prizeOrder = new Order();
        prizeOrder.setMemberId(memberId);
        prizeOrder.setTotalPrice(BigDecimal.ZERO);
        prizeOrder.setStatus(Order.OrderStatus.COMPLETED.name());
        prizeOrder.setCreateTime(LocalDateTime.now());
        orderMapper.insert(prizeOrder);

        return selectedItem;
    }

    private void saveGachaRecord(Long memberId, Long themeId, String prizeName, int shards) {
        GachaRecord record = new GachaRecord();
        record.setMemberId(memberId);
        record.setGachaType(GachaRecord.GachaType.GACHA);
        record.setGameId(themeId);
        record.setPrizeName(prizeName);
        record.setShardsEarned(shards);
        record.setCreatedAt(LocalDateTime.now());
        recordMapper.insert(record);
    }

    public List<GachaTheme> getAllThemes() {
        return themeMapper.findAll();
    }

    /**
     * 扭蛋試抽
     */
    public GachaItem drawTrial(Long themeId) {
        if (themeId == null)
            throw new AppException("請選擇主題");

        GachaTheme theme = themeMapper.findById(themeId)
                .orElseThrow(() -> new AppException("主題不存在"));

        List<GachaItem> items = itemMapper.findByIpId(themeId);
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
