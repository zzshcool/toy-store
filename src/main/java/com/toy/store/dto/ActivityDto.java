package com.toy.store.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ActivityDto {
    private String title;
    private String type; // PREORDER, SALE, LIMITED
    private String description;
    private LocalDateTime validUntil;
    private String imageUrl;
}
