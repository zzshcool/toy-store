package com.toy.store.mapper;

import com.toy.store.model.CarouselSlide;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 輪播圖 MyBatis Mapper
 */
@Mapper
public interface CarouselSlideMapper {

        @Select("SELECT * FROM carousel_slides WHERE id = #{id}")
        Optional<CarouselSlide> findById(Long id);

        @Select("SELECT * FROM carousel_slides WHERE active = true ORDER BY sort_order ASC")
        List<CarouselSlide> findActiveSlides();

        @Select("SELECT * FROM carousel_slides ORDER BY sort_order ASC")
        List<CarouselSlide> findAll();

        @Insert("INSERT INTO carousel_slides (title, image_url, link_url, sort_order, active) " +
                        "VALUES (#{title}, #{imageUrl}, #{linkUrl}, #{sortOrder}, #{active})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(CarouselSlide slide);

        @Update("UPDATE carousel_slides SET title = #{title}, image_url = #{imageUrl}, " +
                        "link_url = #{linkUrl}, sort_order = #{sortOrder}, active = #{active} WHERE id = #{id}")
        int update(CarouselSlide slide);

        @Delete("DELETE FROM carousel_slides WHERE id = #{id}")
        int deleteById(Long id);

        @Select("SELECT * FROM carousel_slides ORDER BY sort_order ASC")
        List<CarouselSlide> findAllByOrderBySortOrderAsc();
}
