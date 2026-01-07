package com.toy.store.mapper;

import com.toy.store.model.GachaRecord;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 抽獎紀錄 MyBatis Mapper
 */
@Mapper
public interface GachaRecordMapper {

        @Select("SELECT * FROM gacha_records WHERE id = #{id}")
        Optional<GachaRecord> findById(Long id);

        @Select("SELECT * FROM gacha_records WHERE member_id = #{memberId} ORDER BY created_at DESC")
        List<GachaRecord> findByMemberId(Long memberId);

        @Select("SELECT * FROM gacha_records WHERE member_id = #{memberId} ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
        List<GachaRecord> findByMemberIdPaged(@Param("memberId") Long memberId,
                        @Param("offset") int offset,
                        @Param("limit") int limit);

        @Select("SELECT * FROM gacha_records WHERE gacha_type = #{gachaType} ORDER BY created_at DESC")
        List<GachaRecord> findByGachaType(String gachaType);

        @Select("SELECT * FROM gacha_records WHERE member_id = #{memberId} AND gacha_type = #{gachaType} ORDER BY created_at DESC")
        List<GachaRecord> findByMemberIdAndGachaType(@Param("memberId") Long memberId,
                        @Param("gachaType") String gachaType);

        @Select("SELECT * FROM gacha_records WHERE game_id = #{gameId} AND gacha_type = #{gachaType} ORDER BY created_at DESC")
        List<GachaRecord> findByGameIdAndGachaType(@Param("gameId") Long gameId,
                        @Param("gachaType") String gachaType);

        @Select("SELECT * FROM gacha_records WHERE created_at >= #{startTime} AND created_at <= #{endTime}")
        List<GachaRecord> findByCreatedAtBetween(@Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        @Insert("INSERT INTO gacha_records (member_id, gacha_type, game_id, prize_name, prize_rank, " +
                        "shards_earned, lucky_value_earned, is_guarantee, is_duplicate, prize_value, created_at) " +
                        "VALUES (#{memberId}, #{gachaType}, #{gameId}, #{prizeName}, #{prizeRank}, " +
                        "#{shardsEarned}, #{luckyValueEarned}, #{isGuarantee}, #{isDuplicate}, #{prizeValue}, #{createdAt})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(GachaRecord record);

        @Select("SELECT COUNT(*) FROM gacha_records WHERE member_id = #{memberId}")
        long countByMemberId(Long memberId);

        @Select("SELECT * FROM gacha_records ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
        List<GachaRecord> findAllPaged(@Param("offset") int offset, @Param("limit") int limit);

        @Select("SELECT COUNT(*) FROM gacha_records")
        long count();

        @Select("SELECT * FROM gacha_records")
        List<GachaRecord> findAll();

        @Select("SELECT * FROM gacha_records ORDER BY created_at DESC LIMIT 20")
        List<GachaRecord> findTop20ByOrderByCreatedAtDesc();

        @Select("SELECT COUNT(*) FROM gacha_records WHERE created_at >= #{startTime} AND created_at <= #{endTime}")
        long countByCreatedAtBetween(@Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        // 兼容別名方法
        @Select("SELECT * FROM gacha_records WHERE member_id = #{memberId} ORDER BY created_at DESC")
        List<GachaRecord> findByMemberIdOrderByCreatedAtDesc(Long memberId);

        // 按會員ID和獎品名稱查詢
        @Select("SELECT * FROM gacha_records WHERE member_id = #{memberId} AND prize_name = #{prizeName} ORDER BY created_at DESC")
        List<GachaRecord> findByMemberIdAndPrizeName(@Param("memberId") Long memberId,
                        @Param("prizeName") String prizeName);
}
