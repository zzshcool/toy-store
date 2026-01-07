package com.toy.store.mapper;

import com.toy.store.model.MemberActionLog;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 會員行為日誌 MyBatis Mapper
 */
@Mapper
public interface MemberActionLogMapper {

        @Select("SELECT * FROM member_action_logs WHERE id = #{id}")
        Optional<MemberActionLog> findById(Long id);

        @Select("SELECT * FROM member_action_logs WHERE member_id = #{memberId} ORDER BY created_at DESC")
        List<MemberActionLog> findByMemberIdOrderByTimestampDesc(Long memberId);

        @Insert("INSERT INTO member_action_logs (member_id, action, details, ip_address, created_at) " +
                        "VALUES (#{memberId}, #{action}, #{details}, #{ipAddress}, #{createdAt})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(MemberActionLog log);

        @Update("UPDATE member_action_logs SET member_id = #{memberId}, action = #{action}, " +
                        "details = #{details}, ip_address = #{ipAddress}, created_at = #{createdAt} WHERE id = #{id}")
        int update(MemberActionLog log);

        @Delete("DELETE FROM member_action_logs WHERE id = #{id}")
        int deleteById(Long id);

        @Select("SELECT * FROM member_action_logs")
        List<MemberActionLog> findAll();
}
