package com.toy.store.repository;

import com.toy.store.model.LogisticsRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LogisticsRecordRepository extends JpaRepository<LogisticsRecord, Long> {

    Optional<LogisticsRecord> findByTrackingNo(String trackingNo);

    Optional<LogisticsRecord> findByShipmentId(Long shipmentId);

    List<LogisticsRecord> findByStatus(LogisticsRecord.LogisticsStatus status);
}
