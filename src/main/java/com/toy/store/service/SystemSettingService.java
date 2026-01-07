package com.toy.store.service;

import com.toy.store.model.SystemSetting;
import com.toy.store.mapper.SystemSettingMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * 系統設定服務
 * 管理功能開關與遊戲參數配置
 */
@Service
public class SystemSettingService {

    private final SystemSettingMapper settingMapper;

    public SystemSettingService(SystemSettingMapper settingMapper) {
        this.settingMapper = settingMapper;
    }

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

        // 導航管理設定
        createIfNotExists(SystemSetting.MODULE_BLINDBOX_ENABLED, "true", "動漫周邊功能開關");
        createIfNotExists(SystemSetting.NAV_ITEM_ORDER, "ichiban,roulette,bingo,blindbox,gacha", "導航列項目順序");

        // 簽到獎勵設定
        createIfNotExists(SystemSetting.SIGNIN_DAILY_REWARD, "10", "每日簽到獎勵紅利點數");
        createIfNotExists(SystemSetting.SIGNIN_WEEKLY_BONUS, "50", "連續7天簽到額外獎勵");

        // 任務獎勵設定
        createIfNotExists(SystemSetting.MISSION_DAILY_LOGIN_REWARD, "10", "每日登入任務獎勵");
        createIfNotExists(SystemSetting.MISSION_SPEND_REWARD, "20", "消費任務獎勵");
        createIfNotExists(SystemSetting.MISSION_DRAW_REWARD, "30", "抽獎任務獎勵");
        createIfNotExists(SystemSetting.MISSION_SPEND_TARGET, "500", "消費任務目標金額");
        createIfNotExists(SystemSetting.MISSION_DRAW_TARGET, "10", "抽獎任務目標次數");
    }

    private void createIfNotExists(String key, String value, String description) {
        if (!settingMapper.existsBySettingKey(key)) {
            SystemSetting setting = new SystemSetting();
            setting.setSettingKey(key);
            setting.setSettingValue(value);
            setting.setDescription(description);
            settingMapper.insert(setting);
        }
    }

    /**
     * 取得設定值
     */
    public String getSetting(String key) {
        return settingMapper.findBySettingKey(key)
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
        settingMapper.findBySettingKey(key).ifPresentOrElse(setting -> {
            setting.setSettingValue(value);
            settingMapper.update(setting);
        }, () -> {
            SystemSetting newSetting = new SystemSetting();
            newSetting.setSettingKey(key);
            newSetting.setSettingValue(value);
            settingMapper.insert(newSetting);
        });
    }

    /**
     * 取得所有設定
     */
    public List<SystemSetting> getAllSettings() {
        return settingMapper.findAll();
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

    public boolean isBlindboxEnabled() {
        return getBooleanSetting(SystemSetting.MODULE_BLINDBOX_ENABLED);
    }

    /**
     * 取得導航項目順序
     * 
     * @return 逗號分隔的導航項目 key 列表
     */
    public String getNavItemOrder() {
        String order = getSetting(SystemSetting.NAV_ITEM_ORDER);
        return order != null ? order : "ichiban,roulette,bingo,blindbox,gacha";
    }

    /**
     * 取得排序後且啟用的導航項目
     * 
     * @return 導航項目 DTO 列表
     */
    public java.util.List<java.util.Map<String, Object>> getSortedNavItems() {
        java.util.List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();
        String order = getNavItemOrder();
        String[] keys = order.split(",");

        java.util.Map<String, Object> navMeta = new java.util.LinkedHashMap<>();
        navMeta.put("ichiban", new Object[] { "🎯 一番賞", "/ichiban", isIchibanEnabled() });
        navMeta.put("roulette", new Object[] { "🎡 轉盤", "/roulette", isRouletteEnabled() });
        navMeta.put("bingo", new Object[] { "🎲 九宮格", "/bingo", isBingoEnabled() });
        navMeta.put("blindbox", new Object[] { "📦 動漫周邊", "/blindbox", isBlindboxEnabled() });
        navMeta.put("gacha", new Object[] { "🎁 扭蛋", "/gacha", isGachaEnabled() });

        for (String key : keys) {
            key = key.trim();
            Object[] meta = (Object[]) navMeta.get(key);
            if (meta != null && (Boolean) meta[2]) {
                java.util.Map<String, Object> item = new java.util.LinkedHashMap<>();
                item.put("key", key);
                item.put("label", meta[0]);
                item.put("url", meta[1]);
                items.add(item);
            }
        }
        return items;
    }

    // ==================== 簽到獎勵設定 ====================
    public int getSignInDailyReward() {
        return getIntSetting(SystemSetting.SIGNIN_DAILY_REWARD, 10);
    }

    public int getSignInWeeklyBonus() {
        return getIntSetting(SystemSetting.SIGNIN_WEEKLY_BONUS, 50);
    }

    // ==================== 任務獎勵設定 ====================
    public int getMissionDailyLoginReward() {
        return getIntSetting(SystemSetting.MISSION_DAILY_LOGIN_REWARD, 10);
    }

    public int getMissionSpendReward() {
        return getIntSetting(SystemSetting.MISSION_SPEND_REWARD, 20);
    }

    public int getMissionDrawReward() {
        return getIntSetting(SystemSetting.MISSION_DRAW_REWARD, 30);
    }

    public int getMissionSpendTarget() {
        return getIntSetting(SystemSetting.MISSION_SPEND_TARGET, 500);
    }

    public int getMissionDrawTarget() {
        return getIntSetting(SystemSetting.MISSION_DRAW_TARGET, 10);
    }
}
