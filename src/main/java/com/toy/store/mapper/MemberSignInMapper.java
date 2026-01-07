package com.toy.store.mapper;

import com.toy.store.model.MemberSignIn;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 會員簽到 MyBatis Mapper
 */
@Mapper
public interface MemberSignInMapper {

    @Select("SELECT * FROM member_sign_ins WHERE id = #{id}")
    Optional<MemberSignIn> findById(Long id);

    @Select("SELECT * FROM member_sign_ins WHERE member_id = #{memberId} AND sign_in_date = #{signInDate}")
    Optional<MemberSignIn> findByMemberIdAndSignInDate(@Param("memberId") Long memberId,
            @Param("signInDate") LocalDate signInDate);

    @Select("SELECT * FROM member_sign_ins WHERE member_id = #{memberId} ORDER BY sign_in_date DESC")
    List<MemberSignIn> findByMemberIdOrderBySignInDateDesc(Long memberId);

    @Insert("INSERT INTO member_sign_ins (member_id, sign_in_date, consecutive_days, reward_points, created_at) " +
            "VALUES (#{memberId}, #{signInDate}, #{consecutiveDays}, #{rewardPoints}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MemberSignIn signIn);

    @Update("UPDATE member_sign_ins SET member_id = #{memberId}, sign_in_date = #{signInDate}, " +
            "consecutive_days = #{consecutiveDays}, reward_points = #{rewardPoints} WHERE id = #{id}")
    int update(MemberSignIn signIn);

    @Delete("DELETE FROM member_sign_ins WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM member_sign_ins")
    List<MemberSignIn> findAll();
}
