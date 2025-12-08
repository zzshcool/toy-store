package com.toy.store.repository;

import com.toy.store.model.Notification;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends CrudRepository<Notification, Long> {
    List<Notification> findAllByOrderByCreatedAtDesc();
}
