package com.toy.store.mapper;

import com.toy.store.model.MemberLuckyValue;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 會員幸運值 MyBatis Mapper
 */
@Mapper
public interface MemberLuckyValueMapper {

    @Select("SELECT * FROM member_lucky_values WHERE id = #{id}")
    Optional<MemberLuckyValue> findById(Long id);

    @Select("SELECT * FROM member_lucky_values WHERE member_id = #{memberId}")
    Optional<MemberLuckyValue> findByMemberId(Long memberId);

    @Select("SELECT COUNT(*) > 0 FROM member_lucky_values WHERE member_id = #{memberId}")
    boolean existsByMemberId(Long memberId);

    @Insert("INSERT INTO member_lucky_values (member_id, lucky_value, updated_at) " +
            "VALUES (#{memberId}, #{luckyValue}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MemberLuckyValue luckyValue);

    @Update("UPDATE member_lucky_values SET member_id = #{memberId}, lucky_value = #{luckyValue}, " +
            "updated_at = #{updatedAt} WHERE id = #{id}")
    int update(MemberLuckyValue luckyValue);

    @Delete("DELETE FROM member_lucky_values WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM member_lucky_values")
    List<MemberLuckyValue> findAll();
}
