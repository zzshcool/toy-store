package com.toy.store.mapper;

import com.toy.store.model.MemberMessage;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 會員訊息 MyBatis Mapper
 */
@Mapper
public interface MemberMessageMapper {

    @Select("SELECT * FROM member_messages WHERE id = #{id}")
    Optional<MemberMessage> findById(Long id);

    @Select("SELECT * FROM member_messages WHERE member_id = #{memberId} ORDER BY created_at DESC")
    List<MemberMessage> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    @Select("SELECT * FROM member_messages WHERE member_id = #{memberId} AND is_read = false ORDER BY created_at DESC")
    List<MemberMessage> findByMemberIdAndReadFalseOrderByCreatedAtDesc(Long memberId);

    @Select("SELECT COUNT(*) FROM member_messages WHERE member_id = #{memberId} AND is_read = false")
    long countByMemberIdAndReadFalse(Long memberId);

    @Insert("INSERT INTO member_messages (member_id, title, content, type, is_read, created_at) " +
            "VALUES (#{memberId}, #{title}, #{content}, #{type}, #{isRead}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MemberMessage message);

    @Update("UPDATE member_messages SET member_id = #{memberId}, title = #{title}, " +
            "content = #{content}, type = #{type}, is_read = #{isRead} WHERE id = #{id}")
    int update(MemberMessage message);

    @Delete("DELETE FROM member_messages WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM member_messages")
    List<MemberMessage> findAll();
}
