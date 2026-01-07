package com.toy.store.mapper;

import com.toy.store.model.BingoCell;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 九宮格格子 MyBatis Mapper
 */
@Mapper
public interface BingoCellMapper {

        @Select("SELECT * FROM bingo_cells WHERE id = #{id}")
        Optional<BingoCell> findById(Long id);

        @Select("SELECT * FROM bingo_cells WHERE game_id = #{gameId} ORDER BY position ASC")
        List<BingoCell> findByGameIdOrderByPositionAsc(Long gameId);

        @Select("SELECT * FROM bingo_cells WHERE game_id = #{gameId} AND is_revealed = false ORDER BY position ASC")
        List<BingoCell> findByGameIdAndNotRevealed(Long gameId);

        @Select("SELECT * FROM bingo_cells WHERE game_id = #{gameId} AND position = #{position}")
        Optional<BingoCell> findByGameIdAndPosition(@Param("gameId") Long gameId, @Param("position") Integer position);

        @Select("SELECT COUNT(*) FROM bingo_cells WHERE game_id = #{gameId} AND is_revealed = false")
        int countUnrevealedByGameId(Long gameId);

        @Insert("INSERT INTO bingo_cells (game_id, position, row_num, col_num, prize_name, " +
                        "prize_description, prize_image_url, prize_value, is_revealed, tier) " +
                        "VALUES (#{gameId}, #{position}, #{row}, #{col}, #{prizeName}, " +
                        "#{prizeDescription}, #{prizeImageUrl}, #{prizeValue}, #{isRevealed}, #{tier})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(BingoCell cell);

        @Update("UPDATE bingo_cells SET prize_name = #{prizeName}, prize_description = #{prizeDescription}, " +
                        "prize_image_url = #{prizeImageUrl}, prize_value = #{prizeValue}, is_revealed = #{isRevealed}, "
                        +
                        "tier = #{tier}, revealed_by_member_id = #{revealedByMemberId}, revealed_at = #{revealedAt} " +
                        "WHERE id = #{id}")
        int update(BingoCell cell);

        @Delete("DELETE FROM bingo_cells WHERE id = #{id}")
        int deleteById(Long id);

        @Delete("DELETE FROM bingo_cells WHERE game_id = #{gameId}")
        int deleteByGameId(Long gameId);

        @Select("SELECT * FROM bingo_cells")
        List<BingoCell> findAll();
}
