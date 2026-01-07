package com.toy.store.mapper;

import com.toy.store.model.PromoCodeUsage;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 優惠碼使用紀錄 MyBatis Mapper
 */
@Mapper
public interface PromoCodeUsageMapper {

        @Select("SELECT * FROM promo_code_usages WHERE id = #{id}")
        Optional<PromoCodeUsage> findById(Long id);

        @Select("SELECT * FROM promo_code_usages WHERE member_id = #{memberId}")
        List<PromoCodeUsage> findByMemberId(Long memberId);

        @Select("SELECT * FROM promo_code_usages WHERE promo_code_id = #{promoCodeId}")
        List<PromoCodeUsage> findByPromoCodeId(Long promoCodeId);

        @Select("SELECT COUNT(*) FROM promo_code_usages WHERE promo_code_id = #{codeId} AND member_id = #{memberId}")
        long countByPromoCodeIdAndMemberId(@Param("codeId") Long codeId, @Param("memberId") Long memberId);

        @Select("SELECT COUNT(*) FROM promo_code_usages WHERE promo_code_id = #{codeId}")
        long countByPromoCodeId(@Param("codeId") Long codeId);

        @Insert("INSERT INTO promo_code_usages (promo_code_id, member_id, used_at) " +
                        "VALUES (#{promoCodeId}, #{memberId}, #{usedAt})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(PromoCodeUsage usage);

        @Update("UPDATE promo_code_usages SET promo_code_id = #{promoCodeId}, member_id = #{memberId}, " +
                        "used_at = #{usedAt} WHERE id = #{id}")
        int update(PromoCodeUsage usage);

        @Delete("DELETE FROM promo_code_usages WHERE id = #{id}")
        int deleteById(Long id);

        @Select("SELECT * FROM promo_code_usages")
        List<PromoCodeUsage> findAll();
}
