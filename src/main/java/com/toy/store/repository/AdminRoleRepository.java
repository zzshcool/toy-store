package com.toy.store.repository;

import com.toy.store.model.AdminRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AdminRoleRepository extends JpaRepository<AdminRole, Long> {
    Optional<AdminRole> findByName(String name);
}
