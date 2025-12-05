package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "member_logs")
public class MemberActionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;
    private String username;
    private String uri;
    private String method;
    private LocalDateTime timestamp;

    private String action;
    private String details;

    // Legacy or URI logging
    public MemberActionLog(Long memberId, String username, String uri, String method) {
        this.memberId = memberId;
        this.username = username;
        this.uri = uri;
        this.method = method;
        this.timestamp = LocalDateTime.now();
    }

    // New Action logging
    public MemberActionLog(Long memberId, String username, String action, String details, boolean isAction) {
        this.memberId = memberId;
        this.username = username;
        this.action = action;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }
}
