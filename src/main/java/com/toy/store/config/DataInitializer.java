package com.toy.store.config;

import com.toy.store.model.*;
import com.toy.store.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 使用者初始化器
 * 僅處理需要密碼加密的使用者建立
 * 其他資料初始化已移至 data-init.sql
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final AdminUserMapper adminUserMapper;
    private final AdminRoleMapper adminRoleMapper;

    // 使用環境變數設定初始密碼，未設定則生成隨機密碼
    @Value("${ADMIN_INIT_PASSWORD:}")
    private String adminInitPassword;

    @Value("${USER_INIT_PASSWORD:}")
    private String userInitPassword;

    @Override
    public void run(String... args) throws Exception {
        initAdminUser();
        initDemoUser();
        cleanupLegacyData();
    }

    /**
     * 初始化管理員帳號
     */
    private void initAdminUser() {
        if (adminUserMapper.findByUsername("admin").isPresent()) {
            return;
        }

        // 使用環境變數或生成隨機密碼
        String adminPassword = adminInitPassword.isEmpty()
                ? UUID.randomUUID().toString().substring(0, 12)
                : adminInitPassword;

        AdminUser admin = new AdminUser();
        admin.setUsername("admin");
        admin.setEmail("admin@toystore.com");
        admin.setPassword(passwordEncoder.encode(adminPassword));
        adminUserMapper.insert(admin);

        // 關聯超級管理員角色 (ID=1，由 data-init.sql 建立)
        adminRoleMapper.findById(1L).ifPresent(role -> adminUserMapper.addRoleToAdmin(admin.getId(), role.getId()));

        log.warn("═══════════════════════════════════════════════════════════");
        log.warn("⚠️  SECURITY WARNING: Admin account created");
        log.warn("    Username: admin");
        log.warn("    Password: {} (請立即更改！)", adminPassword);
        log.warn("    Set ADMIN_INIT_PASSWORD env var for custom password");
        log.warn("═══════════════════════════════════════════════════════════");
    }

    /**
     * 初始化示範使用者帳號
     */
    private void initDemoUser() {
        if (memberMapper.existsByUsername("user")) {
            return;
        }

        // 使用環境變數或生成隨機密碼
        String userPassword = userInitPassword.isEmpty()
                ? UUID.randomUUID().toString().substring(0, 12)
                : userInitPassword;

        Member user = new Member();
        user.setUsername("user");
        user.setEmail("user@toystore.com");
        user.setPassword(passwordEncoder.encode(userPassword));
        user.setRole(Member.Role.USER);
        user.setPlatformWalletBalance(new BigDecimal("1000.00"));
        memberMapper.insert(user);

        log.warn("⚠️  Demo user created - Username: user, Password: {}", userPassword);
    }

    /**
     * 清理舊版資料
     */
    private void cleanupLegacyData() {
        // 移除 members 表中的舊版 admin 帳號（如果存在）
        memberMapper.findByUsername("admin").ifPresent(member -> {
            memberMapper.deleteById(member.getId());
            log.info("Legacy admin removed from members table.");
        });
    }
}
