package com.toy.store.repository;

import com.toy.store.model.IchibanBox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IchibanBoxRepository extends JpaRepository<IchibanBox, Long> {

    List<IchibanBox> findByIpId(Long ipId);

    List<IchibanBox> findByStatus(IchibanBox.Status status);

    List<IchibanBox> findByIpIdAndStatus(Long ipId, IchibanBox.Status status);

    List<IchibanBox> findAllByOrderByCreatedAtDesc();
}
