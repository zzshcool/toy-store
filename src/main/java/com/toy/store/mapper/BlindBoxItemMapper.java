package com.toy.store.mapper;

import com.toy.store.model.BlindBoxItem;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 盲盒物品 MyBatis Mapper
 */
@Mapper
public interface BlindBoxItemMapper {

    @Select("SELECT * FROM blind_box_items WHERE id = #{id}")
    Optional<BlindBoxItem> findById(Long id);

    @Select("SELECT * FROM blind_box_items WHERE blind_box_id = #{blindBoxId} ORDER BY box_number ASC")
    List<BlindBoxItem> findByBlindBoxId(Long blindBoxId);

    @Select("SELECT * FROM blind_box_items WHERE blind_box_id = #{blindBoxId} AND status = #{status}")
    List<BlindBoxItem> findByBlindBoxIdAndStatus(@Param("blindBoxId") Long blindBoxId, @Param("status") String status);

    @Select("SELECT * FROM blind_box_items WHERE blind_box_id = #{blindBoxId} AND box_number = #{boxNumber}")
    Optional<BlindBoxItem> findByBlindBoxIdAndBoxNumber(@Param("blindBoxId") Long blindBoxId,
            @Param("boxNumber") Integer boxNumber);

    @Select("SELECT * FROM blind_box_items WHERE status = 'LOCKED' AND locked_at < #{expireTime}")
    List<BlindBoxItem> findExpiredLocks(@Param("expireTime") LocalDateTime expireTime);

    @Update("UPDATE blind_box_items SET status = 'AVAILABLE', locked_by_member_id = null, locked_at = null " +
            "WHERE status = 'LOCKED' AND locked_at < #{expireTime}")
    int releaseExpiredLocks(@Param("expireTime") LocalDateTime expireTime);

    @Insert("INSERT INTO blind_box_items (blind_box_id, box_number, prize_name, prize_description, " +
            "prize_image_url, estimated_value, rarity, status) " +
            "VALUES (#{blindBox.id}, #{boxNumber}, #{prizeName}, #{prizeDescription}, " +
            "#{prizeImageUrl}, #{estimatedValue}, #{rarity}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BlindBoxItem item);

    @Update("UPDATE blind_box_items SET status = #{status}, locked_by_member_id = #{lockedByMemberId}, " +
            "locked_at = #{lockedAt}, purchased_by_member_id = #{purchasedByMemberId}, " +
            "purchased_at = #{purchasedAt} WHERE id = #{id}")
    int update(BlindBoxItem item);

    @Delete("DELETE FROM blind_box_items WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM blind_box_items")
    List<BlindBoxItem> findAll();
}
