package com.toy.store.mapper;

import com.toy.store.model.MemberTagRelation;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 會員標籤關聯 MyBatis Mapper
 */
@Mapper
public interface MemberTagRelationMapper {

    @Select("SELECT * FROM member_tag_relations WHERE id = #{id}")
    Optional<MemberTagRelation> findById(Long id);

    @Select("SELECT * FROM member_tag_relations WHERE member_id = #{memberId}")
    List<MemberTagRelation> findByMemberId(Long memberId);

    @Select("SELECT * FROM member_tag_relations WHERE tag_id = #{tagId}")
    List<MemberTagRelation> findByTagId(Long tagId);

    @Select("SELECT COUNT(*) > 0 FROM member_tag_relations WHERE member_id = #{memberId} AND tag_id = #{tagId}")
    boolean existsByMemberIdAndTagId(@Param("memberId") Long memberId, @Param("tagId") Long tagId);

    @Delete("DELETE FROM member_tag_relations WHERE member_id = #{memberId} AND tag_id = #{tagId}")
    int deleteByMemberIdAndTagId(@Param("memberId") Long memberId, @Param("tagId") Long tagId);

    @Insert("INSERT INTO member_tag_relations (member_id, tag_id, created_at) " +
            "VALUES (#{memberId}, #{tagId}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MemberTagRelation relation);

    @Delete("DELETE FROM member_tag_relations WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM member_tag_relations")
    List<MemberTagRelation> findAll();
}
