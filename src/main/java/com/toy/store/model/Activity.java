package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "activities")
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 1000)
    private String description;

    private String imageUrl;

    private String type; // e.g. SALE, PREORDER

    private java.time.LocalDateTime startDate;
    private java.time.LocalDateTime expiryDate; // Using as End Date

    private boolean active = true;
}
