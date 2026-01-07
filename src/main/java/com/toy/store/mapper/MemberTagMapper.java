package com.toy.store.mapper;

import com.toy.store.model.MemberTag;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 會員標籤 MyBatis Mapper
 */
@Mapper
public interface MemberTagMapper {

    @Select("SELECT * FROM member_tags WHERE id = #{id}")
    Optional<MemberTag> findById(Long id);

    @Select("SELECT * FROM member_tags WHERE name = #{name}")
    Optional<MemberTag> findByName(String name);

    @Select("SELECT * FROM member_tags WHERE type = #{type}")
    List<MemberTag> findByType(@Param("type") String type);

    @Insert("INSERT INTO member_tags (name, type, description, color, created_at) " +
            "VALUES (#{name}, #{type}, #{description}, #{color}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MemberTag tag);

    @Update("UPDATE member_tags SET name = #{name}, type = #{type}, " +
            "description = #{description}, color = #{color} WHERE id = #{id}")
    int update(MemberTag tag);

    @Delete("DELETE FROM member_tags WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM member_tags")
    List<MemberTag> findAll();
}
