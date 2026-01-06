package com.toy.store.mapper;

import com.toy.store.model.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 分類 MyBatis Mapper
 */
@Mapper
public interface CategoryMapper {

    @Select("SELECT * FROM categories WHERE id = #{id}")
    Optional<Category> findById(Long id);

    @Select("SELECT * FROM categories WHERE name = #{name}")
    Optional<Category> findByName(String name);

    @Select("SELECT * FROM categories")
    List<Category> findAll();

    @Insert("INSERT INTO categories (name, description, image_url) VALUES (#{name}, #{description}, #{imageUrl})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);

    @Update("UPDATE categories SET name = #{name}, description = #{description}, image_url = #{imageUrl} WHERE id = #{id}")
    int update(Category category);

    @Delete("DELETE FROM categories WHERE id = #{id}")
    int deleteById(Long id);
}
