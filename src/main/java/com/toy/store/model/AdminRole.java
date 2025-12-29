package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Set;
import java.util.HashSet;
import com.toy.store.model.AdminPermission;

/**
 * 管理員角色，包含多個權限
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "admin_roles")
public class AdminRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // 角色名稱，如 超級管理員

    @ManyToMany(fetch = FetchType.EAGER, targetEntity = AdminPermission.class)
    @JoinTable(name = "admin_role_permissions", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<AdminPermission> permissions = new HashSet<>();

    public AdminRole(String name) {
        this.name = name;
    }
}
