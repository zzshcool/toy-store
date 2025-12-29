package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 代表管理員權限細項 (例如: DASHBOARD_VIEW, GACHA_MANAGE)
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "admin_permissions")
public class AdminPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code; // 權限代碼，如 CUSTOMER_VIEW

    @Column(nullable = false)
    private String name; // 權限名稱，如 查看用戶資料

    private String description;

    public AdminPermission(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
