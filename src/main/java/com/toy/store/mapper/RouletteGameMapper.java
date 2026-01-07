package com.toy.store.mapper;

import com.toy.store.model.RouletteGame;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 轉盤遊戲 MyBatis Mapper
 */
@Mapper
public interface RouletteGameMapper {

        @Select("SELECT * FROM roulette_games WHERE id = #{id}")
        Optional<RouletteGame> findById(Long id);

        @Select("SELECT * FROM roulette_games")
        List<RouletteGame> findAll();

        @Select("SELECT * FROM roulette_games WHERE ip_id = #{ipId}")
        List<RouletteGame> findByIpId(Long ipId);

        @Select("SELECT * FROM roulette_games WHERE status = #{status}")
        List<RouletteGame> findByStatus(String status);

        @Select("SELECT * FROM roulette_games ORDER BY created_at DESC")
        List<RouletteGame> findAllByOrderByCreatedAtDesc();

        @Insert("INSERT INTO roulette_games (ip_id, name, description, image_url, price_per_spin, " +
                        "status, guarantee_spins, created_at) " +
                        "VALUES (#{ip.id}, #{name}, #{description}, #{imageUrl}, #{pricePerSpin}, " +
                        "#{status}, #{guaranteeSpins}, #{createdAt})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(RouletteGame game);

        @Update("UPDATE roulette_games SET ip_id = #{ip.id}, name = #{name}, description = #{description}, " +
                        "image_url = #{imageUrl}, price_per_spin = #{pricePerSpin}, status = #{status}, " +
                        "guarantee_spins = #{guaranteeSpins} WHERE id = #{id}")
        int update(RouletteGame game);

        @Delete("DELETE FROM roulette_games WHERE id = #{id}")
        int deleteById(Long id);

        @Select("SELECT * FROM roulette_games LIMIT #{limit} OFFSET #{offset}")
        List<RouletteGame> findAllPaged(@Param("offset") int offset, @Param("limit") int limit);

        @Select("SELECT COUNT(*) FROM roulette_games")
        long count();
}
