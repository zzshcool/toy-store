package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import com.toy.store.model.AdminRole;
import com.toy.store.model.AdminPermission;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "admin_users")
public class AdminUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private String email;

    @ManyToMany(fetch = FetchType.EAGER, targetEntity = AdminRole.class)
    @JoinTable(name = "admin_user_roles", joinColumns = @JoinColumn(name = "admin_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<AdminRole> roles = new HashSet<>();

    /**
     * 獲取管理員擁有的所有權限代碼
     */
    public Set<String> getPermissions() {
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(AdminPermission::getCode)
                .collect(Collectors.toSet());
    }

    /**
     * 檢查是否擁有特定權限
     */
    public boolean hasPermission(String permissionCode) {
        return getPermissions().contains(permissionCode);
    }
}
