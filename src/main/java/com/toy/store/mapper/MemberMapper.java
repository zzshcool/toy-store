package com.toy.store.mapper;

import com.toy.store.model.Member;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 會員 MyBatis Mapper
 */
@Mapper
public interface MemberMapper {

        @Select("SELECT * FROM members WHERE id = #{id}")
        Optional<Member> findById(Long id);

        @Select("SELECT * FROM members")
        List<Member> findAll();

        @Select("SELECT * FROM members WHERE username = #{username}")
        Optional<Member> findByUsername(String username);

        @Select("SELECT COUNT(*) > 0 FROM members WHERE username = #{username}")
        Boolean existsByUsername(String username);

        @Select("SELECT COUNT(*) > 0 FROM members WHERE email = #{email}")
        Boolean existsByEmail(String email);

        List<Member> searchByKeyword(@Param("keyword") String keyword,
                        @Param("offset") int offset,
                        @Param("limit") int limit);

        @Select("SELECT * FROM members WHERE LOWER(username) LIKE #{keyword} OR LOWER(email) LIKE #{keyword} OR LOWER(nickname) LIKE #{keyword}")
        List<Member> searchByKeywordNoPage(@Param("keyword") String keyword);

        @Select("SELECT COUNT(*) FROM members WHERE LOWER(username) LIKE #{keyword} OR LOWER(email) LIKE #{keyword} OR LOWER(nickname) LIKE #{keyword}")
        int countByKeyword(@Param("keyword") String keyword);

        @Insert("INSERT INTO members (username, password, email, role, avatar_url, nickname, phone, " +
                        "platform_wallet_balance, created_at, enabled, member_level_id, monthly_recharge, " +
                        "real_name, address, gender, birthday, growth_value, points, bonus_points, lucky_value) " +
                        "VALUES (#{username}, #{password}, #{email}, #{role}, #{avatarUrl}, #{nickname}, #{phone}, " +
                        "#{platformWalletBalance}, #{createdAt}, #{enabled}, #{memberLevelId}, #{monthlyRecharge}, " +
                        "#{realName}, #{address}, #{gender}, #{birthday}, #{growthValue}, #{points}, #{bonusPoints}, #{luckyValue})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(Member member);

        int update(Member member);

        @Delete("DELETE FROM members WHERE id = #{id}")
        int deleteById(Long id);

        @Select("SELECT * FROM members LIMIT #{limit} OFFSET #{offset}")
        List<Member> findAllPaged(@Param("offset") int offset, @Param("limit") int limit);

        @Select("SELECT COUNT(*) FROM members")
        long count();

        @Select("SELECT COUNT(*) FROM members WHERE created_at >= #{startTime} AND created_at <= #{endTime}")
        long countByCreatedAtBetween(@Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);
}
