package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 系統設定實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemSetting {
    private Long id;

    private String settingKey;

    private String settingValue;

    private String settingType;

    private String description;

    private LocalDateTime updatedAt = LocalDateTime.now();
}
