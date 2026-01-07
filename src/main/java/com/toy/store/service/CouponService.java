package com.toy.store.service;

import com.toy.store.exception.ResourceNotFoundException;
import com.toy.store.model.Coupon;
import com.toy.store.model.Member;
import com.toy.store.model.MemberCoupon;
import com.toy.store.mapper.CouponMapper;
import com.toy.store.mapper.MemberCouponMapper;
import com.toy.store.mapper.MemberMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 優惠券服務
 */
@Service
public class CouponService {

    private final CouponMapper couponMapper;
    private final MemberCouponMapper memberCouponMapper;
    private final MemberMapper memberMapper;

    public CouponService(
            CouponMapper couponMapper,
            MemberCouponMapper memberCouponMapper,
            MemberMapper memberMapper) {
        this.couponMapper = couponMapper;
        this.memberCouponMapper = memberCouponMapper;
        this.memberMapper = memberMapper;
    }

    @Transactional
    public Coupon createCoupon(String name, String code, String type, BigDecimal value,
            String description, LocalDateTime validFrom, LocalDateTime validUntil) {
        Coupon coupon = new Coupon();
        coupon.setName(name);
        coupon.setCode(code);
        coupon.setType(type);
        coupon.setValue(value);
        coupon.setDescription(description);
        coupon.setValidFrom(validFrom);
        coupon.setValidUntil(validUntil);
        coupon.setCreatedAt(LocalDateTime.now());
        couponMapper.insert(coupon);
        return coupon;
    }

    @Transactional
    public void distributeToMember(Long couponId, Long memberId) {
        Coupon coupon = couponMapper.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("優惠券", couponId));
        Member member = memberMapper.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("會員", memberId));

        if (!coupon.isActive()) {
            throw new RuntimeException("優惠券尚未啟用或已過期");
        }

        MemberCoupon memberCoupon = new MemberCoupon();
        memberCoupon.setCouponId(couponId);
        memberCoupon.setMemberId(memberId);
        memberCoupon.setStatus("UNUSED");
        memberCoupon.setObtainedAt(LocalDateTime.now());
        memberCouponMapper.insert(memberCoupon);
    }

    @Transactional
    public void distributeToLevel(Long couponId, Long levelId) {
        Coupon coupon = couponMapper.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("優惠券", couponId));

        // 找尋該等級的所有會員
        List<Member> members = memberMapper.findAll().stream()
                .filter(m -> m.getMemberLevelId() != null && m.getMemberLevelId().equals(levelId))
                .toList();

        for (Member member : members) {
            MemberCoupon memberCoupon = new MemberCoupon();
            memberCoupon.setCouponId(couponId);
            memberCoupon.setMemberId(member.getId());
            memberCoupon.setStatus("UNUSED");
            memberCoupon.setObtainedAt(LocalDateTime.now());
            memberCouponMapper.insert(memberCoupon);
        }
    }

    public List<MemberCoupon> getMemberCoupons(Long memberId) {
        return memberCouponMapper.findByMemberIdAndStatus(memberId, "UNUSED");
    }
}
