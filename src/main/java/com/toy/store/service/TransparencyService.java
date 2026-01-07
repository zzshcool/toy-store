package com.toy.store.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toy.store.model.DrawVerification;
import com.toy.store.mapper.DrawVerificationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 抽獎透明化驗證服務
 * 使用 SHA256 確保抽獎結果可驗證
 */
@Slf4j
@Service
public class TransparencyService {

    private final DrawVerificationMapper verificationMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SecureRandom secureRandom = new SecureRandom();

    public TransparencyService(DrawVerificationMapper verificationMapper) {
        this.verificationMapper = verificationMapper;
    }

    /**
     * 初始化遊戲驗證記錄（遊戲開始時調用）
     */
    @Transactional
    public DrawVerification initializeVerification(
            DrawVerification.GameType gameType,
            Long gameId,
            String gameName) {

        // 檢查是否已存在
        Optional<DrawVerification> existing = verificationMapper.findByGameTypeAndGameId(gameType.name(), gameId);
        if (existing.isPresent()) {
            return existing.get();
        }

        // 生成隨機種子
        String seed = generateRandomSeed();

        // 計算初始哈希值
        String initialHash = computeSHA256(seed + gameType + gameId + gameName);

        DrawVerification verification = new DrawVerification();
        verification.setGameType(gameType);
        verification.setGameId(gameId);
        verification.setGameName(gameName);
        verification.setRandomSeed(seed);
        verification.setHashValue(initialHash);
        verification.setCompleted(false);
        verification.setCreatedAt(LocalDateTime.now());

        verificationMapper.insert(verification);
        return verification;
    }

    /**
     * 記錄抽獎結果（每次抽獎後調用）
     */
    @Transactional
    public void recordDrawResult(
            DrawVerification.GameType gameType,
            Long gameId,
            Long memberId,
            String memberName,
            String prizeName,
            int slot) {

        verificationMapper.findByGameTypeAndGameId(gameType.name(), gameId).ifPresent(verification -> {
            if (verification.isCompleted()) {
                return;
            }

            // 構建新的哈希（鏈式哈希）
            String newData = memberId + ":" + memberName + ":" + prizeName + ":" + slot + ":"
                    + System.currentTimeMillis();
            String newHash = computeSHA256(verification.getHashValue() + newData);
            verification.setHashValue(newHash);

            verificationMapper.update(verification);
        });
    }

    /**
     * 完成遊戲驗證（完售時調用）
     */
    @Transactional
    public void finalizeVerification(
            DrawVerification.GameType gameType,
            Long gameId,
            List<Map<String, Object>> results) {

        verificationMapper.findByGameTypeAndGameId(gameType.name(), gameId).ifPresent(verification -> {
            try {
                // 記錄完整結果 JSON
                String resultJson = objectMapper.writeValueAsString(results);
                verification.setResultJson(resultJson);

                // 計算最終哈希
                String finalHash = computeSHA256(verification.getHashValue() + resultJson);
                verification.setHashValue(finalHash);

                verification.setCompleted(true);
                verification.setCompletedAt(LocalDateTime.now());

                verificationMapper.update(verification);
                log.info("遊戲 {} ({}) 驗證記錄已完成，哈希值: {}",
                        verification.getGameName(), gameId, finalHash);
            } catch (JsonProcessingException e) {
                log.error("結果 JSON 序列化失敗", e);
            }
        });
    }

    /**
     * 驗證哈希值是否正確
     */
    public boolean verifyHash(Long verificationId, String providedHash) {
        return verificationMapper.findById(verificationId)
                .map(v -> v.getHashValue().equals(providedHash))
                .orElse(false);
    }

    /**
     * 獲取已完售遊戲的驗證記錄
     */
    public List<DrawVerification> getCompletedVerifications() {
        return verificationMapper.findByCompletedTrueOrderByCompletedAtDesc();
    }

    /**
     * 獲取特定遊戲的驗證記錄
     */
    public Optional<DrawVerification> getVerification(
            DrawVerification.GameType gameType, Long gameId) {
        return verificationMapper.findByGameTypeAndGameId(gameType.name(), gameId);
    }

    /**
     * 生成隨機種子
     */
    private String generateRandomSeed() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return bytesToHex(bytes);
    }

    /**
     * 計算 SHA256 哈希
     */
    public String computeSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * 字節數組轉十六進制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
