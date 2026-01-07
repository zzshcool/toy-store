package com.toy.store.mapper;

import com.toy.store.model.FeaturedItem;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 精選商品 MyBatis Mapper
 */
@Mapper
public interface FeaturedItemMapper {

    @Select("SELECT * FROM featured_items WHERE id = #{id}")
    Optional<FeaturedItem> findById(Long id);

    @Select("SELECT * FROM featured_items WHERE item_type = #{itemType} ORDER BY sort_order ASC")
    List<FeaturedItem> findByItemTypeOrderBySortOrderAsc(@Param("itemType") String itemType);

    @Insert("INSERT INTO featured_items (item_type, item_id, title, description, image_url, sort_order, created_at) " +
            "VALUES (#{itemType}, #{itemId}, #{title}, #{description}, #{imageUrl}, #{sortOrder}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(FeaturedItem item);

    @Update("UPDATE featured_items SET item_type = #{itemType}, item_id = #{itemId}, " +
            "title = #{title}, description = #{description}, image_url = #{imageUrl}, " +
            "sort_order = #{sortOrder} WHERE id = #{id}")
    int update(FeaturedItem item);

    @Delete("DELETE FROM featured_items WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM featured_items")
    List<FeaturedItem> findAll();
}
