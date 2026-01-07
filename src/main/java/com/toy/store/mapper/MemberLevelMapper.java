package com.toy.store.mapper;

import com.toy.store.model.MemberLevel;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 會員等級 MyBatis Mapper
 */
@Mapper
public interface MemberLevelMapper {

        @Select("SELECT * FROM member_levels WHERE id = #{id}")
        Optional<MemberLevel> findById(Long id);

        @Select("SELECT * FROM member_levels ORDER BY min_growth_value ASC")
        List<MemberLevel> findAll();

        @Select("SELECT * FROM member_levels WHERE min_growth_value <= #{growthValue} ORDER BY min_growth_value DESC LIMIT 1")
        Optional<MemberLevel> findByGrowthValue(Long growthValue);

        @Insert("INSERT INTO member_levels (name, min_growth_value, discount_rate, points_multiplier, description, icon_url) "
                        +
                        "VALUES (#{name}, #{minGrowthValue}, #{discountRate}, #{pointsMultiplier}, #{description}, #{iconUrl})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(MemberLevel level);

        @Update("UPDATE member_levels SET name = #{name}, min_growth_value = #{minGrowthValue}, " +
                        "discount_rate = #{discountRate}, points_multiplier = #{pointsMultiplier}, " +
                        "description = #{description}, icon_url = #{iconUrl} WHERE id = #{id}")
        int update(MemberLevel level);

        @Delete("DELETE FROM member_levels WHERE id = #{id}")
        int deleteById(Long id);

        @Select("SELECT * FROM member_levels WHERE enabled = true ORDER BY sort_order ASC")
        List<MemberLevel> findByEnabledTrueOrderBySortOrderAsc();

        @Select("SELECT * FROM member_levels ORDER BY sort_order ASC")
        List<MemberLevel> findAllByOrderBySortOrderAsc();

        @Select("SELECT COUNT(*) FROM member_levels")
        long count();
}
