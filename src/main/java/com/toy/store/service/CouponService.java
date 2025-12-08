package com.toy.store.service;

import com.toy.store.model.Coupon;
import com.toy.store.model.Member;
import com.toy.store.model.MemberCoupon;
import com.toy.store.model.MemberLevel;
import com.toy.store.repository.CouponRepository;
import com.toy.store.repository.MemberCouponRepository;
import com.toy.store.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CouponService {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private MemberCouponRepository memberCouponRepository;

    @Autowired
    private MemberRepository memberRepository;

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
                .orElseThrow(() -> new RuntimeException("Coupon not found"));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        if (!coupon.isActive()) {
            throw new RuntimeException("Coupon is not active");
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
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        // Find all members of this level (assuming MemberRepository has findByLevelId
        // or we iterate)
        // Since Member has @ManyToOne MemberLevel, we can query by level.
        // Assuming memberRepository.findByLevelId(levelId) exists or we use findAll and
        // filter.
        // For efficiency, we should have a query method. I'll rely on findAll for now
        // or filter.

        List<Member> members = memberRepository.findAll().stream()
                .filter(m -> m.getLevel().getId().equals(levelId))
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
        Member member = memberRepository.findById(memberId).orElse(null);
        if (member == null)
            return List.of();
        return memberCouponRepository.findByMemberAndStatus(member, MemberCoupon.Status.UNUSED);
    }
}
