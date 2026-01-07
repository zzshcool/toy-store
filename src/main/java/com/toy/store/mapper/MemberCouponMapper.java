package com.toy.store.mapper;

import com.toy.store.model.MemberCoupon;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 會員優惠券 MyBatis Mapper
 */
@Mapper
public interface MemberCouponMapper {

    @Select("SELECT * FROM member_coupons WHERE id = #{id}")
    Optional<MemberCoupon> findById(Long id);

    @Select("SELECT * FROM member_coupons WHERE member_id = #{memberId} AND status = #{status}")
    List<MemberCoupon> findByMemberIdAndStatus(@Param("memberId") Long memberId, @Param("status") String status);

    @Select("SELECT * FROM member_coupons WHERE member_id = #{memberId}")
    List<MemberCoupon> findByMemberId(Long memberId);

    @Insert("INSERT INTO member_coupons (member_id, coupon_id, status, obtained_at, used_at, expires_at) " +
            "VALUES (#{memberId}, #{couponId}, #{status}, #{obtainedAt}, #{usedAt}, #{expiresAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MemberCoupon memberCoupon);

    @Update("UPDATE member_coupons SET member_id = #{memberId}, coupon_id = #{couponId}, " +
            "status = #{status}, obtained_at = #{obtainedAt}, used_at = #{usedAt}, " +
            "expires_at = #{expiresAt} WHERE id = #{id}")
    int update(MemberCoupon memberCoupon);

    @Delete("DELETE FROM member_coupons WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM member_coupons")
    List<MemberCoupon> findAll();
}
