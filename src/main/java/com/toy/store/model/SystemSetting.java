package com.toy.store.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 系統設定實體 - 鍵值對存儲功能開關與配置
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "system_settings")
public class SystemSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String settingKey;

    @Column(nullable = false)
    private String settingValue;

    @Column(length = 500)
    private String description;

    public SystemSetting(String settingKey, String settingValue, String description) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.description = description;
    }

    // 常用設定鍵常量
    public static final String MODULE_SHOPPING_ENABLED = "module.shopping.enabled";
    public static final String MODULE_ICHIBAN_ENABLED = "module.ichiban.enabled";
    public static final String MODULE_ROULETTE_ENABLED = "module.roulette.enabled";
    public static final String MODULE_BINGO_ENABLED = "module.bingo.enabled";
    public static final String MODULE_REDEEM_ENABLED = "module.redeem.enabled";
    public static final String MODULE_GACHA_ENABLED = "module.gacha.enabled";
    public static final String GACHA_LUCKY_THRESHOLD = "gacha.lucky.threshold";
    public static final String GACHA_SHARD_MIN = "gacha.shard.min";
    public static final String GACHA_SHARD_MAX = "gacha.shard.max";
    public static final String GACHA_DUPLICATE_SHARD = "gacha.duplicate.shard";
    public static final String GACHA_REDEEM_COST = "gacha.redeem.cost";

    // 驗證碼設定
    public static final String CAPTCHA_ENABLED = "captcha.enabled";
    public static final String CAPTCHA_TYPE = "captcha.type"; // GRAPHIC 或 OTP
    public static final String OTP_ENABLED = "otp.enabled";
}
