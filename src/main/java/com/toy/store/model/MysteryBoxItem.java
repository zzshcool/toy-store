package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mystery_box_items")
public class MysteryBoxItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id")
    @JsonIgnore
    private MysteryBoxTheme theme;

    @Column(nullable = false)
    private String name;

    private BigDecimal estimatedValue;

    // Weight for random selection algorithm. Higher weight = higher chance.
    @Column(nullable = false)
    private Integer weight;

    private String imageUrl;
}
