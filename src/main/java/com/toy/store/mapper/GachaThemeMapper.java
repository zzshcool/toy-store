package com.toy.store.mapper;

import com.toy.store.model.GachaTheme;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 扭蛋主題 MyBatis Mapper
 */
@Mapper
public interface GachaThemeMapper {

    @Select("SELECT * FROM gacha_themes WHERE id = #{id}")
    Optional<GachaTheme> findById(Long id);

    @Select("SELECT * FROM gacha_themes WHERE name = #{name}")
    GachaTheme findByName(String name);

    @Insert("INSERT INTO gacha_themes (name, description, image_url, created_at) " +
            "VALUES (#{name}, #{description}, #{imageUrl}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(GachaTheme theme);

    @Update("UPDATE gacha_themes SET name = #{name}, description = #{description}, " +
            "image_url = #{imageUrl} WHERE id = #{id}")
    int update(GachaTheme theme);

    @Delete("DELETE FROM gacha_themes WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM gacha_themes")
    List<GachaTheme> findAll();
}
