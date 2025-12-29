package com.toy.store.repository;

import com.toy.store.model.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {

    Optional<PaymentOrder> findByOrderNo(String orderNo);

    List<PaymentOrder> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    List<PaymentOrder> findByStatus(PaymentOrder.PaymentStatus status);
}
