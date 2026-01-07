package com.toy.store.mapper;

import com.toy.store.model.RedeemShopItem;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 兌換商店商品 MyBatis Mapper
 */
@Mapper
public interface RedeemShopItemMapper {

    @Select("SELECT * FROM redeem_shop_items WHERE id = #{id}")
    Optional<RedeemShopItem> findById(Long id);

    @Select("SELECT * FROM redeem_shop_items WHERE status = #{status} ORDER BY sort_order ASC")
    List<RedeemShopItem> findByStatusOrderBySortOrderAsc(@Param("status") String status);

    @Select("SELECT * FROM redeem_shop_items WHERE item_type = #{itemType}")
    List<RedeemShopItem> findByItemType(@Param("itemType") String itemType);

    @Select("SELECT * FROM redeem_shop_items ORDER BY sort_order ASC")
    List<RedeemShopItem> findAllByOrderBySortOrderAsc();

    @Select("SELECT * FROM redeem_shop_items WHERE stock > #{stock} ORDER BY sort_order ASC")
    List<RedeemShopItem> findByStockGreaterThanOrderBySortOrderAsc(@Param("stock") Integer stock);

    @Insert("INSERT INTO redeem_shop_items (name, description, image_url, item_type, required_shards, " +
            "stock, status, sort_order, created_at) VALUES (#{name}, #{description}, #{imageUrl}, " +
            "#{itemType}, #{requiredShards}, #{stock}, #{status}, #{sortOrder}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RedeemShopItem item);

    @Update("UPDATE redeem_shop_items SET name = #{name}, description = #{description}, " +
            "image_url = #{imageUrl}, item_type = #{itemType}, required_shards = #{requiredShards}, " +
            "stock = #{stock}, status = #{status}, sort_order = #{sortOrder} WHERE id = #{id}")
    int update(RedeemShopItem item);

    @Delete("DELETE FROM redeem_shop_items WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM redeem_shop_items")
    List<RedeemShopItem> findAll();
}
