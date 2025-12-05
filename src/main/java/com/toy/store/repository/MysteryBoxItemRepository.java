package com.toy.store.repository;

import com.toy.store.model.MysteryBoxItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MysteryBoxItemRepository extends JpaRepository<MysteryBoxItem, Long> {
    List<MysteryBoxItem> findByThemeId(Long themeId);
}
