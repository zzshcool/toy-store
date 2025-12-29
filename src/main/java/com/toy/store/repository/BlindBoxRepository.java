package com.toy.store.repository;

import com.toy.store.model.BlindBox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlindBoxRepository extends JpaRepository<BlindBox, Long> {

    List<BlindBox> findByStatusOrderByCreatedAtDesc(BlindBox.Status status);

    List<BlindBox> findByStatusInOrderByCreatedAtDesc(List<BlindBox.Status> statuses);

    List<BlindBox> findByIpNameContainingIgnoreCaseAndStatus(String ipName, BlindBox.Status status);
}
