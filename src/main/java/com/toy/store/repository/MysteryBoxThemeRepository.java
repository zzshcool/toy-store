package com.toy.store.repository;

import com.toy.store.model.MysteryBoxTheme;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MysteryBoxThemeRepository extends JpaRepository<MysteryBoxTheme, Long> {
    MysteryBoxTheme findByName(String name);
}
