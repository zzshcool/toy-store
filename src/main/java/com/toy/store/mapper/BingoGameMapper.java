package com.toy.store.mapper;

import com.toy.store.model.BingoGame;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 九宮格遊戲 MyBatis Mapper
 */
@Mapper
public interface BingoGameMapper {

    @Select("SELECT * FROM bingo_games WHERE id = #{id}")
    Optional<BingoGame> findById(Long id);

    @Select("SELECT * FROM bingo_games")
    List<BingoGame> findAll();

    @Select("SELECT * FROM bingo_games WHERE ip_id = #{ipId}")
    List<BingoGame> findByIpId(Long ipId);

    @Select("SELECT * FROM bingo_games WHERE status = #{status}")
    List<BingoGame> findByStatus(String status);

    @Insert("INSERT INTO bingo_games (ip_id, name, description, image_url, price_per_dig, " +
            "grid_size, status, bingo_reward_name, bingo_reward_image_url, bingo_reward_value, created_at) " +
            "VALUES (#{ip.id}, #{name}, #{description}, #{imageUrl}, #{pricePerDig}, " +
            "#{gridSize}, #{status}, #{bingoRewardName}, #{bingoRewardImageUrl}, #{bingoRewardValue}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BingoGame game);

    @Update("UPDATE bingo_games SET ip_id = #{ip.id}, name = #{name}, description = #{description}, " +
            "image_url = #{imageUrl}, price_per_dig = #{pricePerDig}, grid_size = #{gridSize}, " +
            "status = #{status}, bingo_reward_name = #{bingoRewardName}, " +
            "bingo_reward_image_url = #{bingoRewardImageUrl}, bingo_reward_value = #{bingoRewardValue} " +
            "WHERE id = #{id}")
    int update(BingoGame game);

    @Delete("DELETE FROM bingo_games WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM bingo_games LIMIT #{limit} OFFSET #{offset}")
    List<BingoGame> findAllPaged(@Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM bingo_games")
    long count();
}
