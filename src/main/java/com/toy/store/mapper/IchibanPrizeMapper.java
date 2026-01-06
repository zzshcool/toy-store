package com.toy.store.mapper;

import com.toy.store.model.IchibanPrize;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 一番賞獎品 MyBatis Mapper
 */
@Mapper
public interface IchibanPrizeMapper {

    @Select("SELECT * FROM ichiban_prizes WHERE id = #{id}")
    Optional<IchibanPrize> findById(Long id);

    @Select("SELECT * FROM ichiban_prizes WHERE box_id = #{boxId}")
    List<IchibanPrize> findByBoxId(Long boxId);

    @Select("SELECT * FROM ichiban_prizes WHERE box_id = #{boxId} AND `rank` = #{rank}")
    List<IchibanPrize> findByBoxIdAndRank(@Param("boxId") Long boxId, @Param("rank") String rank);

    @Insert("INSERT INTO ichiban_prizes (box_id, `rank`, name, description, image_url, " +
            "quantity, remaining_quantity, estimated_value, shards_value) " +
            "VALUES (#{box.id}, #{rank}, #{name}, #{description}, #{imageUrl}, " +
            "#{quantity}, #{remainingQuantity}, #{estimatedValue}, #{shardsValue})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(IchibanPrize prize);

    @Update("UPDATE ichiban_prizes SET `rank` = #{rank}, name = #{name}, description = #{description}, " +
            "image_url = #{imageUrl}, quantity = #{quantity}, remaining_quantity = #{remainingQuantity}, " +
            "estimated_value = #{estimatedValue}, shards_value = #{shardsValue} WHERE id = #{id}")
    int update(IchibanPrize prize);

    @Delete("DELETE FROM ichiban_prizes WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM ichiban_prizes")
    List<IchibanPrize> findAll();
}
