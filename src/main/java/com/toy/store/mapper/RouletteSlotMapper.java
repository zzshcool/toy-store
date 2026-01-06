package com.toy.store.mapper;

import com.toy.store.model.RouletteSlot;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 轉盤格子 MyBatis Mapper
 */
@Mapper
public interface RouletteSlotMapper {

    @Select("SELECT * FROM roulette_slots WHERE id = #{id}")
    Optional<RouletteSlot> findById(Long id);

    @Select("SELECT * FROM roulette_slots WHERE game_id = #{gameId} ORDER BY position ASC")
    List<RouletteSlot> findByGameIdOrderByPositionAsc(Long gameId);

    @Insert("INSERT INTO roulette_slots (game_id, position, prize_name, prize_description, " +
            "prize_image_url, prize_value, weight, tier, shards_reward, lucky_value_reward) " +
            "VALUES (#{game.id}, #{position}, #{prizeName}, #{prizeDescription}, " +
            "#{prizeImageUrl}, #{prizeValue}, #{weight}, #{tier}, #{shardsReward}, #{luckyValueReward})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RouletteSlot slot);

    @Update("UPDATE roulette_slots SET prize_name = #{prizeName}, prize_description = #{prizeDescription}, " +
            "prize_image_url = #{prizeImageUrl}, prize_value = #{prizeValue}, weight = #{weight}, " +
            "tier = #{tier}, shards_reward = #{shardsReward}, lucky_value_reward = #{luckyValueReward} " +
            "WHERE id = #{id}")
    int update(RouletteSlot slot);

    @Delete("DELETE FROM roulette_slots WHERE id = #{id}")
    int deleteById(Long id);

    @Delete("DELETE FROM roulette_slots WHERE game_id = #{gameId}")
    int deleteByGameId(Long gameId);

    @Select("SELECT * FROM roulette_slots")
    List<RouletteSlot> findAll();
}
