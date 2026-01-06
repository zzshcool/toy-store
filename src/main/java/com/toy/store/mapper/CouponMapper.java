package com.toy.store.mapper;

import com.toy.store.model.Coupon;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 優惠券 MyBatis Mapper
 */
@Mapper
public interface CouponMapper {

    @Select("SELECT * FROM coupons WHERE id = #{id}")
    Optional<Coupon> findById(Long id);

    @Select("SELECT * FROM coupons WHERE code = #{code}")
    Optional<Coupon> findByCode(String code);

    @Select("SELECT * FROM coupons WHERE status = #{status}")
    List<Coupon> findByStatus(String status);

    @Select("SELECT * FROM coupons")
    List<Coupon> findAll();

    @Insert("INSERT INTO coupons (name, code, discount_type, discount_value, min_purchase, max_discount, " +
            "start_date, end_date, usage_limit, used_count, status) " +
            "VALUES (#{name}, #{code}, #{discountType}, #{discountValue}, #{minPurchase}, #{maxDiscount}, " +
            "#{startDate}, #{endDate}, #{usageLimit}, #{usedCount}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Coupon coupon);

    @Update("UPDATE coupons SET name = #{name}, discount_type = #{discountType}, discount_value = #{discountValue}, " +
            "min_purchase = #{minPurchase}, max_discount = #{maxDiscount}, start_date = #{startDate}, " +
            "end_date = #{endDate}, usage_limit = #{usageLimit}, used_count = #{usedCount}, status = #{status} " +
            "WHERE id = #{id}")
    int update(Coupon coupon);

    @Delete("DELETE FROM coupons WHERE id = #{id}")
    int deleteById(Long id);
}
