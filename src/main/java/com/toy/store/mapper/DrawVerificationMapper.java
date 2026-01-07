package com.toy.store.mapper;

import com.toy.store.model.DrawVerification;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 抽獎驗證 MyBatis Mapper
 */
@Mapper
public interface DrawVerificationMapper {

    @Select("SELECT * FROM draw_verifications WHERE id = #{id}")
    Optional<DrawVerification> findById(Long id);

    @Select("SELECT * FROM draw_verifications WHERE game_type = #{gameType} AND game_id = #{gameId}")
    Optional<DrawVerification> findByGameTypeAndGameId(@Param("gameType") String gameType,
            @Param("gameId") Long gameId);

    @Select("SELECT * FROM draw_verifications WHERE status = 'VERIFIED' ORDER BY verified_at DESC")
    List<DrawVerification> findByCompletedTrueOrderByCompletedAtDesc();

    @Select("SELECT * FROM draw_verifications WHERE status = 'VERIFIED' ORDER BY verified_at DESC LIMIT 10")
    List<DrawVerification> findTop10ByCompletedTrueOrderByCompletedAtDesc();

    @Insert("INSERT INTO draw_verifications (member_id, verification_code, game_type, game_id, slot_id, status, verified_at, expires_at, created_at) "
            +
            "VALUES (#{memberId}, #{verificationCode}, #{gameType}, #{gameId}, #{slotId}, #{status}, #{verifiedAt}, #{expiresAt}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DrawVerification verification);

    @Update("UPDATE draw_verifications SET member_id = #{memberId}, verification_code = #{verificationCode}, " +
            "game_type = #{gameType}, game_id = #{gameId}, slot_id = #{slotId}, status = #{status}, " +
            "verified_at = #{verifiedAt}, expires_at = #{expiresAt} WHERE id = #{id}")
    int update(DrawVerification verification);

    @Delete("DELETE FROM draw_verifications WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM draw_verifications")
    List<DrawVerification> findAll();
}
