package com.toy.store.repository;

import com.toy.store.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByMemberIdOrderByCreateTimeDesc(Long memberId);
}
