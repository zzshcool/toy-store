package com.toy.store.service;

import com.toy.store.model.SystemSetting;
import com.toy.store.repository.SystemSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * 系統設定服務
 * 管理功能開關與遊戲參數配置
 */
@Service
@RequiredArgsConstructor
public class SystemSettingService {

    private final SystemSettingRepository settingRepository;

    /**
     * 初始化預設設定（應用程式啟動時執行）
     */
    @PostConstruct
    @Transactional
    public void initDefaultSettings() {
        // 功能開關預設值
        createIfNotExists(SystemSetting.MODULE_SHOPPING_ENABLED, "false", "購物功能開關");
        createIfNotExists(SystemSetting.MODULE_ICHIBAN_ENABLED, "true", "一番賞功能開關");
        createIfNotExists(SystemSetting.MODULE_ROULETTE_ENABLED, "true", "轉盤功能開關");
        createIfNotExists(SystemSetting.MODULE_BINGO_ENABLED, "true", "九宮格功能開關");
        createIfNotExists(SystemSetting.MODULE_REDEEM_ENABLED, "true", "碎片兌換功能開關");
        createIfNotExists(SystemSetting.MODULE_GACHA_ENABLED, "true", "扭蛋功能開關");

        // 遊戲參數預設值
        createIfNotExists(SystemSetting.GACHA_LUCKY_THRESHOLD, "1000", "保底觸發門檻");
        createIfNotExists(SystemSetting.GACHA_SHARD_MIN, "10", "碎片最小掉落量");
        createIfNotExists(SystemSetting.GACHA_SHARD_MAX, "50", "碎片最大掉落量");
        createIfNotExists(SystemSetting.GACHA_DUPLICATE_SHARD, "300", "重複款轉換碎片數");
        createIfNotExists(SystemSetting.GACHA_REDEEM_COST, "10000", "S賞兌換所需碎片");
        createIfNotExists(SystemSetting.GACHA_REVENUE_THRESHOLD, "70", "機台收益保護門檻 (百分比，如 70 代表 70%)");

        // 驗證碼設定（預設關閉）
        createIfNotExists(SystemSetting.CAPTCHA_ENABLED, "false", "圖形驗證碼開關");
        createIfNotExists(SystemSetting.CAPTCHA_TYPE, "GRAPHIC", "驗證碼類型（GRAPHIC/OTP）");
        createIfNotExists(SystemSetting.OTP_ENABLED, "false", "OTP 簡訊驗證開關");
    }

    private void createIfNotExists(String key, String value, String description) {
        if (!settingRepository.existsBySettingKey(key)) {
            settingRepository.save(new SystemSetting(key, value, description));
        }
    }

    /**
     * 取得設定值
     */
    public String getSetting(String key) {
        return settingRepository.findBySettingKey(key)
                .map(SystemSetting::getSettingValue)
                .orElse(null);
    }

    /**
     * 取得布林設定值
     */
    public boolean getBooleanSetting(String key) {
        String value = getSetting(key);
        return "true".equalsIgnoreCase(value);
    }

    /**
     * 取得整數設定值
     */
    public int getIntSetting(String key, int defaultValue) {
        String value = getSetting(key);
        if (value == null)
            return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 更新設定值
     */
    @Transactional
    public void updateSetting(String key, String value) {
        settingRepository.findBySettingKey(key).ifPresentOrElse(setting -> {
            setting.setSettingValue(value);
            settingRepository.save(setting);
        }, () -> {
            settingRepository.save(new SystemSetting(key, value, null));
        });
    }

    /**
     * 取得所有設定
     */
    public List<SystemSetting> getAllSettings() {
        return settingRepository.findAll();
    }

    // 便捷方法：檢查模組是否啟用
    public boolean isShoppingEnabled() {
        return getBooleanSetting(SystemSetting.MODULE_SHOPPING_ENABLED);
    }

    public boolean isIchibanEnabled() {
        return getBooleanSetting(SystemSetting.MODULE_ICHIBAN_ENABLED);
    }

    public boolean isRouletteEnabled() {
        return getBooleanSetting(SystemSetting.MODULE_ROULETTE_ENABLED);
    }

    public boolean isBingoEnabled() {
        return getBooleanSetting(SystemSetting.MODULE_BINGO_ENABLED);
    }

    public boolean isRedeemEnabled() {
        return getBooleanSetting(SystemSetting.MODULE_REDEEM_ENABLED);
    }

    public boolean isGachaEnabled() {
        return getBooleanSetting(SystemSetting.MODULE_GACHA_ENABLED);
    }

    public int getLuckyThreshold() {
        return getIntSetting(SystemSetting.GACHA_LUCKY_THRESHOLD, 1000);
    }

    public double getRevenueThreshold() {
        return getIntSetting(SystemSetting.GACHA_REVENUE_THRESHOLD, 70) / 100.0;
    }
}
