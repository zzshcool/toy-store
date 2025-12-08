package com.toy.store.repository;

import com.toy.store.model.Member;
import com.toy.store.model.MemberCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Long> {
    List<MemberCoupon> findByMemberAndStatus(Member member, MemberCoupon.Status status);

    List<MemberCoupon> findByMember(Member member);
}
