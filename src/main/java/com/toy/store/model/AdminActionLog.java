package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "admin_action_logs")
public class AdminActionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String adminUsername;
    private String action; // e.g., "CREATE_PRODUCT", "BLOCK_MEMBER"
    private String details;
    private LocalDateTime timestamp = LocalDateTime.now();

    public AdminActionLog(String adminUsername, String action, String details) {
        this.adminUsername = adminUsername;
        this.action = action;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }
}
