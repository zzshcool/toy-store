package com.toy.store.repository;

import com.toy.store.model.RouletteSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RouletteSlotRepository extends JpaRepository<RouletteSlot, Long> {

    List<RouletteSlot> findByGame_IdOrderBySlotOrderAsc(Long gameId);

    List<RouletteSlot> findByGame_IdAndSlotType(Long gameId, RouletteSlot.SlotType slotType);
}
