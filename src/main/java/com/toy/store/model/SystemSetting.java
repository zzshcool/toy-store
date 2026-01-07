package com.toy.store.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 系統設定實體 - 純 POJO (MyBatis)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemSetting {
    private Long id;

    private String settingKey;

    private String settingValue;

    private String settingType;

    private String description;

    private LocalDateTime updatedAt = LocalDateTime.now();

    // 模組開關常量
    public static final String MODULE_SHOPPING_ENABLED = "module.shopping.enabled";
    public static final String MODULE_ICHIBAN_ENABLED = "module.ichiban.enabled";
    public static final String MODULE_ROULETTE_ENABLED = "module.roulette.enabled";
    public static final String MODULE_BINGO_ENABLED = "module.bingo.enabled";
    public static final String MODULE_REDEEM_ENABLED = "module.redeem.enabled";
    public static final String MODULE_GACHA_ENABLED = "module.gacha.enabled";
    public static final String MODULE_BLINDBOX_ENABLED = "module.blindbox.enabled";

    // 扭蛋相關常量
    public static final String GACHA_LUCKY_THRESHOLD = "gacha.lucky.threshold";
    public static final String GACHA_SHARD_MIN = "gacha.shard.min";
    public static final String GACHA_SHARD_MAX = "gacha.shard.max";
    public static final String GACHA_DUPLICATE_SHARD = "gacha.duplicate.shard";
    public static final String GACHA_REDEEM_COST = "gacha.redeem.cost";
    public static final String GACHA_REVENUE_THRESHOLD = "gacha.revenue.threshold";

    // 驗證相關常量
    public static final String CAPTCHA_ENABLED = "captcha.enabled";
    public static final String CAPTCHA_TYPE = "captcha.type";
    public static final String OTP_ENABLED = "otp.enabled";

    // 導航相關常量
    public static final String NAV_ITEM_ORDER = "nav.item.order";

    // 簽到相關常量
    public static final String SIGNIN_DAILY_REWARD = "signin.daily.reward";
    public static final String SIGNIN_WEEKLY_BONUS = "signin.weekly.bonus";

    // 任務相關常量
    public static final String MISSION_DAILY_LOGIN_REWARD = "mission.daily.login.reward";
    public static final String MISSION_SPEND_REWARD = "mission.spend.reward";
    public static final String MISSION_DRAW_REWARD = "mission.draw.reward";
    public static final String MISSION_SPEND_TARGET = "mission.spend.target";
    public static final String MISSION_DRAW_TARGET = "mission.draw.target";
}
