package com.toy.store.repository;

import com.toy.store.model.CabinetItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CabinetItemRepository extends JpaRepository<CabinetItem, Long> {

    List<CabinetItem> findByMemberIdAndStatusOrderByObtainedAtDesc(Long memberId, CabinetItem.Status status);

    List<CabinetItem> findByMemberIdOrderByObtainedAtDesc(Long memberId);

    List<CabinetItem> findByMemberIdAndStatusIn(Long memberId, List<CabinetItem.Status> statuses);

    @Query("SELECT COUNT(c) FROM CabinetItem c WHERE c.memberId = :memberId AND c.status = 'IN_CABINET'")
    int countItemsInCabinet(@Param("memberId") Long memberId);

    List<CabinetItem> findByShipmentRequestId(Long shipmentRequestId);
}
