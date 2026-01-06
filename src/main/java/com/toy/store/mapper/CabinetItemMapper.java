package com.toy.store.mapper;

import com.toy.store.model.CabinetItem;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 置物櫃物品 MyBatis Mapper
 */
@Mapper
public interface CabinetItemMapper {

    @Select("SELECT * FROM cabinet_items WHERE id = #{id}")
    Optional<CabinetItem> findById(Long id);

    @Select("SELECT * FROM cabinet_items WHERE member_id = #{memberId} ORDER BY obtained_at DESC")
    List<CabinetItem> findByMemberId(Long memberId);

    @Select("SELECT * FROM cabinet_items WHERE member_id = #{memberId} AND status = #{status} ORDER BY obtained_at DESC")
    List<CabinetItem> findByMemberIdAndStatus(@Param("memberId") Long memberId, @Param("status") String status);

    @Select("SELECT * FROM cabinet_items WHERE member_id = #{memberId} AND status = 'IN_CABINET' ORDER BY obtained_at DESC")
    List<CabinetItem> findInCabinetByMemberId(Long memberId);

    @Select("SELECT COUNT(*) FROM cabinet_items WHERE member_id = #{memberId} AND status = 'IN_CABINET'")
    int countInCabinetByMemberId(Long memberId);

    @Insert("INSERT INTO cabinet_items (member_id, source_type, source_id, item_name, item_description, " +
            "item_image_url, item_value, status, obtained_at) " +
            "VALUES (#{memberId}, #{sourceType}, #{sourceId}, #{itemName}, #{itemDescription}, " +
            "#{itemImageUrl}, #{itemValue}, #{status}, #{obtainedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CabinetItem item);

    @Update("UPDATE cabinet_items SET status = #{status}, shipped_at = #{shippedAt} WHERE id = #{id}")
    int update(CabinetItem item);

    @Delete("DELETE FROM cabinet_items WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM cabinet_items")
    List<CabinetItem> findAll();
}
