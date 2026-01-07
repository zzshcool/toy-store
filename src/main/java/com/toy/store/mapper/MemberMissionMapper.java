package com.toy.store.mapper;

import com.toy.store.model.MemberMission;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 會員任務 MyBatis Mapper
 */
@Mapper
public interface MemberMissionMapper {

        @Select("SELECT * FROM member_missions WHERE id = #{id}")
        Optional<MemberMission> findById(Long id);

        @Select("SELECT * FROM member_missions WHERE member_id = #{memberId} AND mission_date = #{missionDate}")
        List<MemberMission> findByMemberIdAndMissionDate(@Param("memberId") Long memberId,
                        @Param("missionDate") LocalDate missionDate);

        @Select("SELECT * FROM member_missions WHERE member_id = #{memberId} AND mission_date = #{missionDate} AND type = #{type}")
        Optional<MemberMission> findByMemberIdAndMissionDateAndType(@Param("memberId") Long memberId,
                        @Param("missionDate") LocalDate missionDate, @Param("type") String type);

        @Insert("INSERT INTO member_missions (member_id, mission_date, type, current_progress, target_value, " +
                        "reward_bonus_points, is_completed, reward_claimed, completed_at, created_at) " +
                        "VALUES (#{memberId}, #{missionDate}, #{type}, #{currentProgress}, #{targetValue}, " +
                        "#{rewardBonusPoints}, #{isCompleted}, #{rewardClaimed}, #{completedAt}, #{createdAt})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(MemberMission mission);

        @Update("UPDATE member_missions SET current_progress = #{currentProgress}, is_completed = #{isCompleted}, " +
                        "reward_claimed = #{rewardClaimed}, completed_at = #{completedAt} WHERE id = #{id}")
        int update(MemberMission mission);

        @Delete("DELETE FROM member_missions WHERE id = #{id}")
        int deleteById(Long id);

        @Select("SELECT * FROM member_missions")
        List<MemberMission> findAll();
}
