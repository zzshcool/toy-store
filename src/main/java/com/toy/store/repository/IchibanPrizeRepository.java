package com.toy.store.repository;

import com.toy.store.model.IchibanPrize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IchibanPrizeRepository extends JpaRepository<IchibanPrize, Long> {

    List<IchibanPrize> findByBoxIdOrderBySortOrderAsc(Long boxId);

    List<IchibanPrize> findByBoxIdAndRemainingQuantityGreaterThan(Long boxId, Integer quantity);

    List<IchibanPrize> findByBoxIdAndRank(Long boxId, IchibanPrize.Rank rank);
}
