package com.toy.store.repository;

import com.toy.store.model.AdminPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AdminPermissionRepository extends JpaRepository<AdminPermission, Long> {
    Optional<AdminPermission> findByCode(String code);
}
