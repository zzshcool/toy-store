package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * 管理員使用者實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUser {
    private Long id;

    private String username;

    private String password;

    private String email;

    // 角色（非持久化，由 Service 層填充）
    private transient Set<AdminRole> roles = new HashSet<>();

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
