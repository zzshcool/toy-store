package com.toy.store.repository;

import com.toy.store.model.GachaIp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GachaIpRepository extends JpaRepository<GachaIp, Long> {

    List<GachaIp> findByStatus(GachaIp.Status status);

    List<GachaIp> findByStatusOrderByCreatedAtDesc(GachaIp.Status status);

    List<GachaIp> findAllByOrderByCreatedAtDesc();
}
