package com.toy.store.repository;

import com.toy.store.model.IchibanPrize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IchibanPrizeRepository extends JpaRepository<IchibanPrize, Long> {

    List<IchibanPrize> findByBox_IdOrderBySortOrderAsc(Long boxId);

    List<IchibanPrize> findByBox_IdAndRemainingQuantityGreaterThan(Long boxId, Integer quantity);

    List<IchibanPrize> findByBox_IdAndRank(Long boxId, IchibanPrize.Rank rank);
}
