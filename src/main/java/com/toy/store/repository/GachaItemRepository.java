package com.toy.store.repository;

import com.toy.store.model.GachaItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GachaItemRepository extends JpaRepository<GachaItem, Long> {
}
