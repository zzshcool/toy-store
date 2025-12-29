package com.toy.store.dto.gacha;

import com.toy.store.model.IchibanSlot;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 一番賞格子 DTO
 */
@Data
public class IchibanSlotDto {
    private Long id;
    private Integer slotNumber;
    private String status;
    private boolean isLocked;
    private boolean isRevealed;
    private Long lockedByMemberId;
    private LocalDateTime lockTime;
    private Long revealedByMemberId;
    private LocalDateTime revealedAt;
    // 只有揭曉後才顯示獎品資訊
    private IchibanPrizeDto prize;

    public static IchibanSlotDto from(IchibanSlot slot) {
        IchibanSlotDto dto = new IchibanSlotDto();
        dto.setId(slot.getId());
        dto.setSlotNumber(slot.getSlotNumber());
        dto.setStatus(slot.getStatus() != null ? slot.getStatus().name() : null);
        dto.setLocked(slot.getStatus() == IchibanSlot.Status.LOCKED);
        dto.setRevealed(slot.getStatus() == IchibanSlot.Status.REVEALED);
        dto.setLockedByMemberId(slot.getLockedByMemberId());
        dto.setLockTime(slot.getLockTime());
        dto.setRevealedByMemberId(slot.getRevealedByMemberId());
        dto.setRevealedAt(slot.getRevealedAt());
        // 只有揭曉後才暴露獎品
        if (slot.getStatus() == IchibanSlot.Status.REVEALED && slot.getPrize() != null) {
            dto.setPrize(IchibanPrizeDto.from(slot.getPrize()));
        }
        return dto;
    }
}
