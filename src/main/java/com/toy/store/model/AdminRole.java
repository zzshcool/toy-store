package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Set;
import java.util.HashSet;

/**
 * 管理員角色實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminRole {
    private Long id;

    private String name;

    private String description;

    // 權限（非持久化，由 Service 層填充）
    private transient Set<AdminPermission> permissions = new HashSet<>();

    public AdminRole(String name) {
        this.name = name;
    }
}
