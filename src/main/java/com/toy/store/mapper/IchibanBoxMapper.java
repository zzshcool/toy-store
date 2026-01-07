package com.toy.store.mapper;

import com.toy.store.model.IchibanBox;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 一番賞箱體 MyBatis Mapper
 */
@Mapper
public interface IchibanBoxMapper {

        @Select("SELECT * FROM ichiban_boxes WHERE id = #{id}")
        Optional<IchibanBox> findById(Long id);

        @Select("SELECT * FROM ichiban_boxes")
        List<IchibanBox> findAll();

        @Select("SELECT * FROM ichiban_boxes WHERE ip_id = #{ipId}")
        List<IchibanBox> findByIpId(Long ipId);

        @Select("SELECT * FROM ichiban_boxes WHERE status = #{status}")
        List<IchibanBox> findByStatus(String status);

        @Select("SELECT * FROM ichiban_boxes ORDER BY created_at DESC")
        List<IchibanBox> findAllByOrderByCreatedAtDesc();

        @Insert("INSERT INTO ichiban_boxes (ip_id, name, description, image_url, price_per_draw, " +
                        "max_slots, total_slots, status, start_time, end_time, created_at) " +
                        "VALUES (#{ipId}, #{name}, #{description}, #{imageUrl}, #{pricePerDraw}, " +
                        "#{maxSlots}, #{totalSlots}, #{status}, #{startTime}, #{endTime}, #{createdAt})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(IchibanBox box);

        @Update("UPDATE ichiban_boxes SET ip_id = #{ipId}, name = #{name}, description = #{description}, " +
                        "image_url = #{imageUrl}, price_per_draw = #{pricePerDraw}, max_slots = #{maxSlots}, " +
                        "total_slots = #{totalSlots}, status = #{status}, start_time = #{startTime}, " +
                        "end_time = #{endTime} WHERE id = #{id}")
        int update(IchibanBox box);

        @Delete("DELETE FROM ichiban_boxes WHERE id = #{id}")
        int deleteById(Long id);

        @Select("SELECT * FROM ichiban_boxes LIMIT #{limit} OFFSET #{offset}")
        List<IchibanBox> findAllPaged(@Param("offset") int offset, @Param("limit") int limit);

        @Select("SELECT COUNT(*) FROM ichiban_boxes")
        long count();
}
