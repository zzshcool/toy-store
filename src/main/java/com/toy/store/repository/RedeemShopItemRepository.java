package com.toy.store.repository;

import com.toy.store.model.RedeemShopItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RedeemShopItemRepository extends JpaRepository<RedeemShopItem, Long> {

    List<RedeemShopItem> findByStatusOrderBySortOrderAsc(RedeemShopItem.Status status);

    List<RedeemShopItem> findByItemType(RedeemShopItem.ItemType itemType);

    List<RedeemShopItem> findAllByOrderBySortOrderAsc();

    List<RedeemShopItem> findByStockGreaterThanOrderBySortOrderAsc(Integer stock);
}
