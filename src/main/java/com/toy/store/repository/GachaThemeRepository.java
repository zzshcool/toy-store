package com.toy.store.repository;

import com.toy.store.model.GachaTheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GachaThemeRepository extends JpaRepository<GachaTheme, Long> {
    GachaTheme findByName(String name);
}
