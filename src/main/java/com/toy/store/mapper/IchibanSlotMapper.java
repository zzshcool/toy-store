package com.toy.store.mapper;

import com.toy.store.model.IchibanSlot;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 一番賞格子 MyBatis Mapper
 */
@Mapper
public interface IchibanSlotMapper {

        @Select("SELECT * FROM ichiban_slots WHERE id = #{id}")
        Optional<IchibanSlot> findById(Long id);

        @Select("SELECT * FROM ichiban_slots WHERE box_id = #{boxId} ORDER BY slot_number ASC")
        List<IchibanSlot> findByBoxIdOrderBySlotNumberAsc(Long boxId);

        @Select("SELECT * FROM ichiban_slots WHERE box_id = #{boxId} AND slot_number = #{slotNumber}")
        Optional<IchibanSlot> findByBoxIdAndSlotNumber(@Param("boxId") Long boxId,
                        @Param("slotNumber") Integer slotNumber);

        @Select("SELECT * FROM ichiban_slots WHERE box_id = #{boxId} AND status = #{status}")
        List<IchibanSlot> findByBoxIdAndStatus(@Param("boxId") Long boxId, @Param("status") String status);

        @Select("SELECT * FROM ichiban_slots WHERE status = 'LOCKED' AND locked_at < #{expireTime}")
        List<IchibanSlot> findExpiredLocks(@Param("expireTime") LocalDateTime expireTime);

        @Select("SELECT COUNT(*) FROM ichiban_slots WHERE box_id = #{boxId} AND status = 'AVAILABLE'")
        int countAvailableSlots(@Param("boxId") Long boxId);

        @Update("UPDATE ichiban_slots SET status = 'AVAILABLE', locked_by_member_id = null, locked_at = null " +
                        "WHERE status = 'LOCKED' AND locked_at < #{expireTime}")
        int releaseExpiredLocks(@Param("expireTime") LocalDateTime expireTime);

        @Insert("INSERT INTO ichiban_slots (box_id, slot_number, status, prize_id) " +
                        "VALUES (#{boxId}, #{slotNumber}, #{status}, #{prizeId})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(IchibanSlot slot);

        @Update("UPDATE ichiban_slots SET status = #{status}, locked_by_member_id = #{lockedByMemberId}, " +
                        "locked_at = #{lockedAt}, revealed_by_member_id = #{revealedByMemberId}, " +
                        "revealed_at = #{revealedAt}, prize_id = #{prizeId} WHERE id = #{id}")
        int update(IchibanSlot slot);

        @Delete("DELETE FROM ichiban_slots WHERE id = #{id}")
        int deleteById(Long id);

        @Select("SELECT * FROM ichiban_slots")
        List<IchibanSlot> findAll();
}
