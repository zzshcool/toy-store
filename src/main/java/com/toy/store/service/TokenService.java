package com.toy.store.service;

import com.toy.store.model.Member;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

@Service
public class TokenService {

    private static final Map<String, TokenInfo> tokenStore = new ConcurrentHashMap<>();

    // Roles
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    public String createToken(String username, String role) {
        String token = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry;
        LocalDateTime maxLife = null;

        if (ROLE_ADMIN.equals(role)) {
            // Admin: 8 hours max life, 5 min idle timeout (handled in validation)
            expiry = now.plusMinutes(5); // Initial idle timeout
            maxLife = now.plusHours(8);
        } else {
            // User: 1 hour initial
            expiry = now.plusHours(1);
        }

        TokenInfo info = new TokenInfo(username, role, expiry, now, maxLife);
        tokenStore.put(token, info);
        return token;
    }

    public TokenInfo validateToken(String token) {
        TokenInfo info = tokenStore.get(token);
        if (info == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();

        // Check Hard Absolute Expiry (for Admin max life, or User absolute expiry if
        // not extended)
        if (now.isAfter(info.getExpiryTime())) {
            tokenStore.remove(token);
            return null;
        }

        // Check Admin Max Life (8 hours)
        if (ROLE_ADMIN.equals(info.getRole()) && info.getMaxLifeTime() != null && now.isAfter(info.getMaxLifeTime())) {
            tokenStore.remove(token);
            return null;
        }

        // Logic for updates
        if (ROLE_ADMIN.equals(info.getRole())) {
            // Admin: Update idle timeout
            // If we are here, it hasn't expired yet (so < 5 mins since last access)
            // Reset idle timeout to 5 mins from NOW
            info.setExpiryTime(now.plusMinutes(5));
            info.setLastAccessTime(now);
        } else {
            // User: "If online, every 1 min extend 30 mins"
            // Throttle: Only extend if > 1 min since last access
            if (java.time.Duration.between(info.getLastAccessTime(), now).toMinutes() >= 1) {
                info.setExpiryTime(now.plusMinutes(30));
                info.setLastAccessTime(now);
            }
            // If < 1 min, do nothing (keep existing expiry), effectively keeping it valid.
        }

        return info;
    }

    public void invalidateToken(String token) {
        if (token != null) {
            tokenStore.remove(token);
        }
    }

    public static class TokenInfo {
        private String username;
        private String role;
        private LocalDateTime expiryTime;
        private LocalDateTime lastAccessTime;
        private LocalDateTime maxLifeTime; // For Admin 8hr limit

        public TokenInfo(String username, String role, LocalDateTime expiryTime, LocalDateTime lastAccessTime,
                LocalDateTime maxLifeTime) {
            this.username = username;
            this.role = role;
            this.expiryTime = expiryTime;
            this.lastAccessTime = lastAccessTime;
            this.maxLifeTime = maxLifeTime;
        }

        public String getUsername() {
            return username;
        }

        public String getRole() {
            return role;
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
