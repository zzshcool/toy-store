package com.toy.store.service;

import com.toy.store.model.Member;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

@Service
public class TokenService {

    private static final Map<String, TokenInfo> memberTokenStore = new ConcurrentHashMap<>();
    private static final Map<String, TokenInfo> adminTokenStore = new ConcurrentHashMap<>();

    // Roles
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    public String createToken(String username, String role, Set<String> permissions) {
        String token = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry;
        LocalDateTime maxLife = null;

        if (ROLE_ADMIN.equals(role)) {
            // Admin: 8 hours max life, 5 min idle timeout
            expiry = now.plusMinutes(5);
            maxLife = now.plusHours(8);
            adminTokenStore.put(token, new TokenInfo(username, role, expiry, now, maxLife, permissions));
        } else {
            // User: 1 hour initial
            expiry = now.plusHours(1);
            memberTokenStore.put(token, new TokenInfo(username, role, expiry, now, null, Collections.emptySet()));
        }

        return token;
    }

    @Deprecated
    public String createToken(String username, String role) {
        return createToken(username, role, Collections.emptySet());
    }

    public TokenInfo validateMemberToken(String token) {
        return validateTokenFromStore(token, memberTokenStore);
    }

    public TokenInfo validateAdminToken(String token) {
        return validateTokenFromStore(token, adminTokenStore);
    }

    private TokenInfo validateTokenFromStore(String token, Map<String, TokenInfo> store) {
        TokenInfo info = store.get(token);
        if (info == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();

        // Check Hard Absolute Expiry
        if (now.isAfter(info.getExpiryTime())) {
            store.remove(token);
            return null;
        }

        // Check Admin Max Life (8 hours)
        if (ROLE_ADMIN.equals(info.getRole()) && info.getMaxLifeTime() != null && now.isAfter(info.getMaxLifeTime())) {
            store.remove(token);
            return null;
        }

        // Logic for updates
        if (ROLE_ADMIN.equals(info.getRole())) {
            info.setExpiryTime(now.plusMinutes(5));
            info.setLastAccessTime(now);
        } else {
            if (java.time.Duration.between(info.getLastAccessTime(), now).toMinutes() >= 1) {
                info.setExpiryTime(now.plusMinutes(30));
                info.setLastAccessTime(now);
            }
        }

        return info;
    }

    public void invalidateMemberToken(String token) {
        if (token != null)
            memberTokenStore.remove(token);
    }

    public void invalidateAdminToken(String token) {
        if (token != null)
            adminTokenStore.remove(token);
    }

    // 保留舊方法以維持相容性，但標記為過渡
    @Deprecated
    public void invalidateToken(String token) {
        if (token != null) {
            memberTokenStore.remove(token);
            adminTokenStore.remove(token);
        }
    }

    @Deprecated
    public TokenInfo validateToken(String token) {
        TokenInfo info = memberTokenStore.get(token);
        if (info != null)
            return validateMemberToken(token);
        return validateAdminToken(token);
    }

    public static class TokenInfo {
        private String username;
        private String role;
        private LocalDateTime expiryTime;
        private LocalDateTime lastAccessTime;
        private LocalDateTime maxLifeTime; // For Admin 8hr limit
        private Set<String> permissions;

        public TokenInfo(String username, String role, LocalDateTime expiryTime, LocalDateTime lastAccessTime,
                LocalDateTime maxLifeTime, Set<String> permissions) {
            this.username = username;
            this.role = role;
            this.expiryTime = expiryTime;
            this.lastAccessTime = lastAccessTime;
            this.maxLifeTime = maxLifeTime;
            this.permissions = permissions != null ? permissions : new HashSet<>();
        }

        public String getUsername() {
            return username;
        }

        public String getRole() {
            return role;
        }

        public Set<String> getPermissions() {
            return permissions;
        }

        public boolean hasPermission(String code) {
            return permissions.contains(code);
        }

        public LocalDateTime getExpiryTime() {
            return expiryTime;
        }

        public void setExpiryTime(LocalDateTime expiryTime) {
            this.expiryTime = expiryTime;
        }

        public LocalDateTime getLastAccessTime() {
            return lastAccessTime;
        }

        public void setLastAccessTime(LocalDateTime lastAccessTime) {
            this.lastAccessTime = lastAccessTime;
        }

        public LocalDateTime getMaxLifeTime() {
            return maxLifeTime;
        }
    }
}
