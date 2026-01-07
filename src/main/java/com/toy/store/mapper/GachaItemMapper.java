package com.toy.store.mapper;

import com.toy.store.model.GachaItem;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 扭蛋物品 MyBatis Mapper
 */
@Mapper
public interface GachaItemMapper {

    @Select("SELECT * FROM gacha_items WHERE id = #{id}")
    Optional<GachaItem> findById(Long id);

    @Insert("INSERT INTO gacha_items (ip_id, name, description, image_url, rarity, weight) " +
            "VALUES (#{ipId}, #{name}, #{description}, #{imageUrl}, #{rarity}, #{weight})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(GachaItem item);

    @Update("UPDATE gacha_items SET ip_id = #{ipId}, name = #{name}, description = #{description}, " +
            "image_url = #{imageUrl}, rarity = #{rarity}, weight = #{weight} WHERE id = #{id}")
    int update(GachaItem item);

    @Delete("DELETE FROM gacha_items WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM gacha_items")
    List<GachaItem> findAll();

    @Select("SELECT * FROM gacha_items WHERE ip_id = #{ipId}")
    List<GachaItem> findByIpId(Long ipId);
}
