package com.toy.store.mapper;

import com.toy.store.model.BlindBox;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 盲盒 MyBatis Mapper
 */
@Mapper
public interface BlindBoxMapper {

    @Select("SELECT * FROM blind_boxes WHERE id = #{id}")
    Optional<BlindBox> findById(Long id);

    @Select("SELECT * FROM blind_boxes")
    List<BlindBox> findAll();

    @Select("SELECT * FROM blind_boxes WHERE status = #{status}")
    List<BlindBox> findByStatus(String status);

    @Insert("INSERT INTO blind_boxes (name, description, image_url, ip_name, price_per_box, " +
            "full_box_price, total_boxes, status, created_at, updated_at) " +
            "VALUES (#{name}, #{description}, #{imageUrl}, #{ipName}, #{pricePerBox}, " +
            "#{fullBoxPrice}, #{totalBoxes}, #{status}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BlindBox blindBox);

    @Update("UPDATE blind_boxes SET name = #{name}, description = #{description}, " +
            "image_url = #{imageUrl}, ip_name = #{ipName}, price_per_box = #{pricePerBox}, " +
            "full_box_price = #{fullBoxPrice}, total_boxes = #{totalBoxes}, status = #{status}, " +
            "updated_at = #{updatedAt} WHERE id = #{id}")
    int update(BlindBox blindBox);

    @Delete("DELETE FROM blind_boxes WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM blind_boxes LIMIT #{limit} OFFSET #{offset}")
    List<BlindBox> findAllPaged(@Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM blind_boxes")
    long count();
}
