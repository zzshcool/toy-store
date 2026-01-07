package com.toy.store.mapper;

import com.toy.store.model.SubCategory;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 子分類 MyBatis Mapper
 */
@Mapper
public interface SubCategoryMapper {

    @Select("SELECT * FROM sub_categories WHERE id = #{id}")
    Optional<SubCategory> findById(Long id);

    @Select("SELECT * FROM sub_categories WHERE name = #{name} AND category_id = #{categoryId}")
    SubCategory findByNameAndCategoryId(@Param("name") String name, @Param("categoryId") Long categoryId);

    @Select("SELECT * FROM sub_categories WHERE category_id = #{categoryId}")
    List<SubCategory> findByCategoryId(Long categoryId);

    @Insert("INSERT INTO sub_categories (name, category_id, description, created_at) " +
            "VALUES (#{name}, #{categoryId}, #{description}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SubCategory subCategory);

    @Update("UPDATE sub_categories SET name = #{name}, category_id = #{categoryId}, " +
            "description = #{description} WHERE id = #{id}")
    int update(SubCategory subCategory);

    @Delete("DELETE FROM sub_categories WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM sub_categories")
    List<SubCategory> findAll();
}
