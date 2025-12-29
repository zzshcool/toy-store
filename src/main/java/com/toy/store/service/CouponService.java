package com.toy.store.service;

import com.toy.store.exception.ResourceNotFoundException;
import com.toy.store.model.Coupon;
import com.toy.store.model.Member;
import com.toy.store.model.MemberCoupon;
import com.toy.store.repository.CouponRepository;
import com.toy.store.repository.MemberCouponRepository;
import com.toy.store.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 優惠券服務
 */
@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final MemberCouponRepository memberCouponRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Coupon createCoupon(String name, String code, Coupon.CouponType type, BigDecimal value,
            String description, LocalDateTime validFrom, LocalDateTime validUntil) {
        Coupon coupon = new Coupon();
        coupon.setName(name);
        coupon.setCode(code);
        coupon.setType(type);
        coupon.setValue(value);
        coupon.setDescription(description);
        coupon.setValidFrom(validFrom);
        coupon.setValidUntil(validUntil);
        return couponRepository.save(coupon);
    }

    @Transactional
    public void distributeToMember(Long couponId, Long memberId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("優惠券", couponId));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("會員", memberId));

        if (!coupon.isActive()) {
            throw new RuntimeException("優惠券尚未啟用或已過期");
        }

        MemberCoupon memberCoupon = new MemberCoupon();
        memberCoupon.setCoupon(coupon);
        memberCoupon.setMember(member);
        memberCoupon.setStatus(MemberCoupon.Status.UNUSED);
        memberCouponRepository.save(memberCoupon);
    }

    @Transactional
    public void distributeToLevel(Long couponId, Long levelId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("優惠券", couponId));

        // 找尋該等級的所有會員
        List<Member> members = memberRepository.findAll().stream()
                .filter(m -> m.getLevel() != null && m.getLevel().getId().equals(levelId))
                .toList();

        for (Member member : members) {
            MemberCoupon memberCoupon = new MemberCoupon();
            memberCoupon.setCoupon(coupon);
            memberCoupon.setMember(member);
            memberCoupon.setStatus(MemberCoupon.Status.UNUSED);
            memberCouponRepository.save(memberCoupon);
        }
    }

    public List<MemberCoupon> getMemberCoupons(Long memberId) {
        return memberRepository.findById(memberId)
                .map(member -> memberCouponRepository.findByMemberAndStatus(member, MemberCoupon.Status.UNUSED))
                .orElseGet(List::of);
    }
}
