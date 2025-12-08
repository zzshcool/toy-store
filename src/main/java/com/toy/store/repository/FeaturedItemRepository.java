package com.toy.store.repository;

import com.toy.store.model.FeaturedItem;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public interface FeaturedItemRepository extends CrudRepository<FeaturedItem, Long> {
    List<FeaturedItem> findByItemTypeOrderBySortOrderAsc(FeaturedItem.Type itemType);
}
