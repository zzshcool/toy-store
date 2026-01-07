package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 活動實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Activity {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status = "ACTIVE";
    private LocalDateTime createdAt = LocalDateTime.now();

    public String getTitle() {
        return name;
    }

    public void setTitle(String title) {
        this.name = title;
    }

    public String getType() {
        return "GENERAL";
    }

    public void setType(String type) {
        // Just for compatibility
    }

    public LocalDateTime getExpiryDate() {
        return endDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.endDate = expiryDate;
    }

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    public void setActive(boolean active) {
        this.status = active ? "ACTIVE" : "INACTIVE";
    }
}
