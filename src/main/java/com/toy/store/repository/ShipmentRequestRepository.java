package com.toy.store.repository;

import com.toy.store.model.ShipmentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentRequestRepository extends JpaRepository<ShipmentRequest, Long> {

    List<ShipmentRequest> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    List<ShipmentRequest> findByStatusOrderByCreatedAtAsc(ShipmentRequest.Status status);

    List<ShipmentRequest> findByStatusInOrderByCreatedAtAsc(List<ShipmentRequest.Status> statuses);
}
