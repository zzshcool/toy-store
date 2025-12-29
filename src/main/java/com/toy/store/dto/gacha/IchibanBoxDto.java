package com.toy.store.dto.gacha;

import com.toy.store.model.IchibanBox;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 一番賞箱體 DTO
 */
@Data
public class IchibanBoxDto {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private String bannerUrl;
    private BigDecimal pricePerDraw;
    private int totalSlots;
    private int availableSlots;
    private String status;
    private String ipName;
    private LocalDateTime createdAt;

    public static IchibanBoxDto from(IchibanBox box) {
        IchibanBoxDto dto = new IchibanBoxDto();
        dto.setId(box.getId());
        dto.setName(box.getName());
        dto.setDescription(box.getDescription());
        dto.setImageUrl(box.getThumbnailUrl());
        dto.setBannerUrl(box.getBannerUrl());
        dto.setPricePerDraw(box.getPricePerDraw());
        dto.setTotalSlots(box.getTotalSlots());
        dto.setAvailableSlots(box.getAvailableSlots());
        dto.setStatus(box.getStatus() != null ? box.getStatus().name() : null);
        dto.setIpName(box.getIp() != null ? box.getIp().getName() : null);
        dto.setCreatedAt(box.getCreatedAt());
        return dto;
    }
}
