package com.toy.store.mapper;

import com.toy.store.model.PromoCode;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 優惠碼 MyBatis Mapper
 */
@Mapper
public interface PromoCodeMapper {

        @Select("SELECT * FROM promo_codes WHERE id = #{id}")
        Optional<PromoCode> findById(Long id);

        @Select("SELECT * FROM promo_codes WHERE code = #{code}")
        Optional<PromoCode> findByCode(String code);

        @Select("SELECT * FROM promo_codes WHERE creator_member_id = #{memberId}")
        List<PromoCode> findByCreatorMemberId(Long memberId);

        @Select("SELECT * FROM promo_codes WHERE type = #{type} AND enabled = true")
        List<PromoCode> findByTypeAndEnabledTrue(@Param("type") String type);

        @Select("SELECT COUNT(*) > 0 FROM promo_codes WHERE code = #{code}")
        boolean existsByCode(String code);

        @Insert("INSERT INTO promo_codes (code, name, description, type, reward_type, reward_value, " +
                        "min_purchase, max_discount, valid_until, max_uses, per_user_limit, used_count, " +
                        "creator_member_id, enabled, created_at) " +
                        "VALUES (#{code}, #{name}, #{description}, #{type}, #{rewardType}, #{rewardValue}, " +
                        "#{minPurchase}, #{maxDiscount}, #{validUntil}, #{maxUses}, #{perUserLimit}, #{usedCount}, " +
                        "#{creatorMemberId}, #{enabled}, #{createdAt})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(PromoCode promoCode);

        @Update("UPDATE promo_codes SET code = #{code}, name = #{name}, description = #{description}, " +
                        "type = #{type}, reward_type = #{rewardType}, reward_value = #{rewardValue}, " +
                        "min_purchase = #{minPurchase}, max_discount = #{maxDiscount}, valid_until = #{validUntil}, " +
                        "max_uses = #{maxUses}, per_user_limit = #{perUserLimit}, used_count = #{usedCount}, " +
                        "creator_member_id = #{creatorMemberId}, enabled = #{enabled} WHERE id = #{id}")
        int update(PromoCode promoCode);

        @Delete("DELETE FROM promo_codes WHERE id = #{id}")
        int deleteById(Long id);

        @Select("SELECT * FROM promo_codes")
        List<PromoCode> findAll();
}
