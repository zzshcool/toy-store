package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mystery_box_themes")
public class MysteryBoxTheme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private BigDecimal price; // Cost to draw once

    private String imageUrl;

    @OneToMany(mappedBy = "theme", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MysteryBoxItem> items;
}
