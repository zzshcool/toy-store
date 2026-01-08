-- =============================================================================
-- TiDB Cloud Consolidated Schema for Toy Store
-- 此檔案整合 schema.sql + schema-amendments.sql，並與 Java Model 對齊
-- Generated: 2026-01-08
-- =============================================================================

-- =====================================================
-- 會員相關資料表
-- =====================================================

-- 會員資料表
CREATE TABLE IF NOT EXISTS members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL,
    role VARCHAR(20) DEFAULT 'USER',
    avatar_url VARCHAR(500),
    nickname VARCHAR(50),
    phone VARCHAR(20),
    platform_wallet_balance DECIMAL(15,2) DEFAULT 0.00,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    enabled BOOLEAN DEFAULT TRUE,
    last_login_time DATETIME,
    member_level_id BIGINT,
    monthly_recharge DECIMAL(15,2) DEFAULT 0.00,
    real_name VARCHAR(50),
    address VARCHAR(500),
    gender VARCHAR(10),
    birthday DATE,
    growth_value BIGINT DEFAULT 0,
    points INT DEFAULT 0,
    bonus_points INT DEFAULT 0,
    lucky_value INT DEFAULT 0,
    last_level_review_date DATE,
    INDEX idx_username (username),
    INDEX idx_email (email)
);

-- 會員等級資料表
CREATE TABLE IF NOT EXISTS member_levels (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    min_growth_value BIGINT DEFAULT 0,
    discount_rate DECIMAL(5,2) DEFAULT 100.00,
    points_multiplier DECIMAL(5,2) DEFAULT 1.00,
    description VARCHAR(500),
    icon_url VARCHAR(500),
    sort_order INT DEFAULT 0
);

-- 會員簽到表
CREATE TABLE IF NOT EXISTS member_sign_ins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    sign_in_date DATE NOT NULL,
    consecutive_days INT DEFAULT 1,
    reward_points INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_member_date (member_id, sign_in_date)
);

-- 會員標籤表
CREATE TABLE IF NOT EXISTS member_tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    color VARCHAR(20),
    description VARCHAR(200)
);

-- 會員標籤關聯表
CREATE TABLE IF NOT EXISTS member_tag_relations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_member_tag (member_id, tag_id)
);

-- 會員操作日誌表
CREATE TABLE IF NOT EXISTS member_action_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    action VARCHAR(100),
    details TEXT,
    ip_address VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 會員幸運值表
CREATE TABLE IF NOT EXISTS member_lucky_values (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    game_type VARCHAR(50),
    game_id BIGINT,
    lucky_value INT DEFAULT 0,
    last_updated DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_member_game (member_id, game_type, game_id)
);

-- 會員訊息表
CREATE TABLE IF NOT EXISTS member_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    title VARCHAR(200),
    content TEXT,
    type VARCHAR(50),
    is_read BOOLEAN DEFAULT FALSE,
    reference_id VARCHAR(100),
    action_url VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_member_id (member_id)
);

-- 會員任務表
CREATE TABLE IF NOT EXISTS member_missions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    mission_type VARCHAR(50),
    mission_name VARCHAR(200),
    mission_date DATE,
    progress INT DEFAULT 0,
    current_progress INT DEFAULT 0,
    target INT,
    target_value INT,
    reward_type VARCHAR(50),
    reward_amount INT,
    reward_bonus_points INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'IN_PROGRESS',
    is_completed BOOLEAN DEFAULT FALSE,
    reward_claimed BOOLEAN DEFAULT FALSE,
    completed_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 會員優惠券表
CREATE TABLE IF NOT EXISTS member_coupons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    coupon_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'UNUSED',
    used_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 管理員相關資料表
-- =====================================================

-- 管理員使用者表
CREATE TABLE IF NOT EXISTS admin_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100)
);

-- 管理員角色表
CREATE TABLE IF NOT EXISTS admin_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200)
);

-- 管理員權限表
CREATE TABLE IF NOT EXISTS admin_permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100),
    description VARCHAR(200)
);

-- 管理員-角色關聯表
CREATE TABLE IF NOT EXISTS admin_user_roles (
    admin_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (admin_id, role_id)
);

-- 角色-權限關聯表
CREATE TABLE IF NOT EXISTS admin_role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id)
);

-- 管理員操作日誌
CREATE TABLE IF NOT EXISTS admin_action_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id BIGINT NOT NULL,
    admin_name VARCHAR(100),
    action VARCHAR(100),
    target_type VARCHAR(50),
    target_id BIGINT,
    details TEXT,
    ip_address VARCHAR(50),
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 產品與分類資料表
-- =====================================================

-- 產品資料表
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(15,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    category VARCHAR(100),
    sub_category VARCHAR(100),
    image_url VARCHAR(500),
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_status (status)
);

-- 分類表
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    image_url VARCHAR(500)
);

-- 子分類表
CREATE TABLE IF NOT EXISTS sub_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 購物車與訂單資料表
-- =====================================================

-- 購物車表
CREATE TABLE IF NOT EXISTS carts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL UNIQUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 購物車項目表
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_cart_id (cart_id)
);

-- 訂單表
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    total_price DECIMAL(15,2) NOT NULL,
    discount_amount DECIMAL(15,2) DEFAULT 0.00,
    coupon_name VARCHAR(100),
    status VARCHAR(20) DEFAULT 'PENDING',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_member_id (member_id),
    INDEX idx_status (status)
);

-- 訂單項目表
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT,
    product_name VARCHAR(200),
    price DECIMAL(15,2),
    quantity INT,
    INDEX idx_order_id (order_id)
);

-- =====================================================
-- 優惠券與促銷碼資料表
-- =====================================================

-- 優惠券表
CREATE TABLE IF NOT EXISTS coupons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) UNIQUE,
    type VARCHAR(50),
    discount_type VARCHAR(20),
    discount_value DECIMAL(15,2),
    min_purchase DECIMAL(15,2) DEFAULT 0.00,
    max_discount DECIMAL(15,2),
    start_date DATETIME,
    end_date DATETIME,
    valid_from DATETIME,
    valid_until DATETIME,
    usage_limit INT,
    used_count INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE'
);

-- 促銷碼表
CREATE TABLE IF NOT EXISTS promo_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100),
    description TEXT,
    type VARCHAR(50) DEFAULT 'DISCOUNT',
    discount_type VARCHAR(20),
    discount_value DECIMAL(15,2),
    reward_type VARCHAR(50),
    reward_value DECIMAL(15,2),
    min_purchase DECIMAL(15,2) DEFAULT 0.00,
    max_discount DECIMAL(15,2),
    start_date DATETIME,
    end_date DATETIME,
    valid_until DATETIME,
    usage_limit INT,
    max_uses INT DEFAULT 0,
    per_user_limit INT DEFAULT 1,
    used_count INT DEFAULT 0,
    creator_member_id BIGINT,
    enabled BOOLEAN DEFAULT TRUE,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 促銷碼使用紀錄表
CREATE TABLE IF NOT EXISTS promo_code_usages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    promo_code_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    used_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 抽獎系統 - IP 主題
-- =====================================================

-- IP主題表
CREATE TABLE IF NOT EXISTS gacha_ips (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    image_url VARCHAR(500),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_status (status)
);

-- =====================================================
-- 抽獎系統 - 一番賞
-- =====================================================

-- 一番賞箱體表
CREATE TABLE IF NOT EXISTS ichiban_boxes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ip_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    price_per_draw DECIMAL(15,2) NOT NULL,
    max_slots INT DEFAULT 80,
    total_slots INT NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    start_time DATETIME,
    end_time DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ip_id (ip_id),
    INDEX idx_status (status)
);

-- 一番賞獎品表
CREATE TABLE IF NOT EXISTS ichiban_prizes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    box_id BIGINT NOT NULL,
    `rank` VARCHAR(10) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    quantity INT NOT NULL,
    remaining_quantity INT,
    estimated_value DECIMAL(15,2),
    shards_value INT DEFAULT 0,
    sort_order INT DEFAULT 0,
    INDEX idx_box_id (box_id)
);

-- 一番賞格子表
CREATE TABLE IF NOT EXISTS ichiban_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    box_id BIGINT NOT NULL,
    slot_number INT NOT NULL,
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    locked_by_member_id BIGINT,
    locked_at DATETIME,
    revealed_by_member_id BIGINT,
    revealed_at DATETIME,
    prize_id BIGINT,
    lock_time DATETIME,
    INDEX idx_box_id (box_id),
    INDEX idx_status (status)
);

-- =====================================================
-- 抽獎系統 - 轉盤
-- =====================================================

-- 轉盤遊戲表
CREATE TABLE IF NOT EXISTS roulette_games (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ip_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    price_per_spin DECIMAL(15,2) NOT NULL,
    max_slots INT DEFAULT 25,
    total_slots INT DEFAULT 8,
    total_draws INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    guarantee_spins INT DEFAULT 10,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ip_id (ip_id)
);

-- 轉盤格子表
CREATE TABLE IF NOT EXISTS roulette_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    game_id BIGINT NOT NULL,
    position INT NOT NULL,
    prize_name VARCHAR(200) NOT NULL,
    prize_description TEXT,
    prize_image_url VARCHAR(500),
    prize_value DECIMAL(15,2),
    weight INT DEFAULT 1,
    tier VARCHAR(20) DEFAULT 'NORMAL',
    shards_reward INT DEFAULT 0,
    lucky_value_reward INT DEFAULT 0,
    slot_order INT DEFAULT 0,
    slot_type VARCHAR(50) DEFAULT 'NORMAL',
    shard_amount INT DEFAULT 0,
    color VARCHAR(20),
    INDEX idx_game_id (game_id)
);

-- =====================================================
-- 抽獎系統 - 九宮格
-- =====================================================

-- 九宮格遊戲表
CREATE TABLE IF NOT EXISTS bingo_games (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ip_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    price_per_dig DECIMAL(15,2) NOT NULL,
    grid_size INT DEFAULT 3,
    total_cells INT DEFAULT 9,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    bingo_reward_name VARCHAR(200),
    bingo_reward_image_url VARCHAR(500),
    bingo_reward_value DECIMAL(15,2),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ip_id (ip_id)
);

-- 九宮格格子表 (row/col 對齊 Java Model)
CREATE TABLE IF NOT EXISTS bingo_cells (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    game_id BIGINT NOT NULL,
    position INT NOT NULL,
    row_num INT NOT NULL,
    col_num INT NOT NULL,
    prize_name VARCHAR(200) NOT NULL,
    prize_description TEXT,
    prize_image_url VARCHAR(500),
    prize_value DECIMAL(15,2),
    is_revealed BOOLEAN DEFAULT FALSE,
    tier VARCHAR(20) DEFAULT 'NORMAL',
    revealed_by_member_id BIGINT,
    revealed_at DATETIME,
    INDEX idx_game_id (game_id)
);

-- =====================================================
-- 抽獎系統 - 盲盒
-- =====================================================

-- 盲盒表
CREATE TABLE IF NOT EXISTS blind_boxes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    ip_name VARCHAR(100) NOT NULL,
    price_per_box DECIMAL(15,2) NOT NULL,
    full_box_price DECIMAL(15,2) NOT NULL,
    total_boxes INT DEFAULT 12,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 盲盒物品表
CREATE TABLE IF NOT EXISTS blind_box_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    blind_box_id BIGINT NOT NULL,
    box_number INT NOT NULL,
    prize_name VARCHAR(200) NOT NULL,
    prize_description TEXT,
    prize_image_url VARCHAR(500),
    estimated_value DECIMAL(15,2),
    rarity VARCHAR(20) DEFAULT 'NORMAL',
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    locked_by_member_id BIGINT,
    locked_at DATETIME,
    purchased_by_member_id BIGINT,
    purchased_at DATETIME,
    INDEX idx_blind_box_id (blind_box_id)
);

-- =====================================================
-- 抽獎系統 - 扭蛋
-- =====================================================

-- 扭蛋主題表
CREATE TABLE IF NOT EXISTS gacha_themes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ip_id BIGINT,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    price_per_gacha DECIMAL(15,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 扭蛋物品表
CREATE TABLE IF NOT EXISTS gacha_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    theme_id BIGINT NOT NULL,
    ip_id BIGINT,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    rarity VARCHAR(20) DEFAULT 'NORMAL',
    weight INT DEFAULT 1,
    estimated_value DECIMAL(15,2),
    INDEX idx_theme_id (theme_id)
);

-- =====================================================
-- 抽獎紀錄與驗證
-- =====================================================

-- 抽獎紀錄表
CREATE TABLE IF NOT EXISTS gacha_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    gacha_type VARCHAR(20) NOT NULL,
    game_id BIGINT,
    prize_name VARCHAR(200) NOT NULL,
    prize_rank VARCHAR(10),
    shards_earned INT DEFAULT 0,
    lucky_value_earned INT DEFAULT 0,
    is_guarantee BOOLEAN DEFAULT FALSE,
    is_duplicate BOOLEAN DEFAULT FALSE,
    prize_value DECIMAL(15,2) DEFAULT 0.00,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_member_id (member_id),
    INDEX idx_gacha_type (gacha_type),
    INDEX idx_created_at (created_at)
);

-- 抽獎驗證表 (完整重建，對齊 Java Model)
CREATE TABLE IF NOT EXISTS draw_verifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    game_type VARCHAR(50) NOT NULL,
    game_id BIGINT NOT NULL,
    game_name VARCHAR(200),
    random_seed VARCHAR(100),
    hash_value VARCHAR(100),
    result_json TEXT,
    completed BOOLEAN DEFAULT FALSE,
    completed_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_game_type_id (game_type, game_id),
    INDEX idx_completed (completed)
);

-- =====================================================
-- 交易與支付
-- =====================================================

-- 交易紀錄表
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    balance_after DECIMAL(15,2),
    description VARCHAR(500),
    reference_id BIGINT,
    reference_type VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_member_id (member_id),
    INDEX idx_type (type)
);

-- 碎片交易紀錄表
CREATE TABLE IF NOT EXISTS shard_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    amount INT NOT NULL,
    balance_after INT,
    description VARCHAR(500),
    reference_id BIGINT,
    reference_type VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_member_id (member_id)
);

-- 支付訂單表
CREATE TABLE IF NOT EXISTS payment_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    amount DECIMAL(15,2) NOT NULL,
    payment_method VARCHAR(50),
    status VARCHAR(20) DEFAULT 'PENDING',
    third_party_trade_no VARCHAR(100),
    paid_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_member_id (member_id),
    INDEX idx_order_no (order_no)
);

-- 發票表
CREATE TABLE IF NOT EXISTS invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    payment_order_id BIGINT,
    invoice_no VARCHAR(50),
    invoice_type VARCHAR(20),
    carrier_type VARCHAR(20),
    carrier_no VARCHAR(100),
    buyer_name VARCHAR(100),
    buyer_tax_id VARCHAR(20),
    amount DECIMAL(15,2),
    tax_amount DECIMAL(15,2),
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 物流與出貨
-- =====================================================

-- 物流紀錄表
CREATE TABLE IF NOT EXISTS logistics_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    shipment_request_id BIGINT,
    tracking_no VARCHAR(100),
    carrier VARCHAR(50),
    provider VARCHAR(50),
    status VARCHAR(20) DEFAULT 'PENDING',
    shipped_at DATETIME,
    delivered_at DATETIME,
    last_update DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 出貨申請表
CREATE TABLE IF NOT EXISTS shipment_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    request_no VARCHAR(50) NOT NULL UNIQUE,
    item_count INT DEFAULT 0,
    shipping_fee DECIMAL(15,2) DEFAULT 0.00,
    is_free_shipping BOOLEAN DEFAULT FALSE,
    recipient_name VARCHAR(100),
    recipient_phone VARCHAR(20),
    recipient_address VARCHAR(500),
    postal_code VARCHAR(20),
    status VARCHAR(20) DEFAULT 'PENDING',
    admin_note TEXT,
    tracking_number VARCHAR(100),
    shipping_company VARCHAR(100),
    shipped_at DATETIME,
    delivered_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_member_id (member_id)
);

-- 置物櫃物品表 (保持 item_* 命名，對齊 Java Model)
CREATE TABLE IF NOT EXISTS cabinet_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    source_type VARCHAR(50) NOT NULL,
    source_id BIGINT,
    item_name VARCHAR(200) NOT NULL,
    item_description TEXT,
    item_image_url VARCHAR(500),
    item_rank VARCHAR(20),
    item_value DECIMAL(15,2),
    status VARCHAR(20) DEFAULT 'IN_CABINET',
    shipment_request_id BIGINT,
    requested_at DATETIME,
    obtained_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    shipped_at DATETIME,
    INDEX idx_member_id (member_id),
    INDEX idx_status (status)
);

-- =====================================================
-- 活動與展示
-- =====================================================

-- 活動表
CREATE TABLE IF NOT EXISTS activities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    title VARCHAR(200),
    description TEXT,
    type VARCHAR(50),
    start_date DATETIME,
    end_date DATETIME,
    expiry_date DATETIME,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 輪播圖表
CREATE TABLE IF NOT EXISTS carousel_slides (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200),
    image_url VARCHAR(500) NOT NULL,
    link_url VARCHAR(500),
    sort_order INT DEFAULT 0,
    active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 精選商品表
CREATE TABLE IF NOT EXISTS featured_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_type VARCHAR(50) NOT NULL,
    item_id BIGINT NOT NULL,
    product_id BIGINT,
    title VARCHAR(200),
    image_url VARCHAR(500),
    sort_order INT DEFAULT 0,
    active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 通知表
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    title VARCHAR(200),
    content TEXT,
    `read` BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 道具與兌換商店
-- =====================================================

-- 道具卡表
CREATE TABLE IF NOT EXISTS prop_cards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    card_type VARCHAR(50) NOT NULL,
    card_name VARCHAR(100),
    description TEXT,
    effect_value DECIMAL(15,2),
    expire_at DATETIME,
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    used_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_member_id (member_id)
);

-- 兌換商店商品表 (對齊 Java Model: shardCost, estimatedValue, totalStock)
CREATE TABLE IF NOT EXISTS redeem_shop_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    item_type VARCHAR(50),
    point_cost INT,
    shard_cost INT,
    estimated_value DECIMAL(15,2),
    stock INT DEFAULT 0,
    total_stock INT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    sort_order INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 系統設定
-- =====================================================

-- 系統設定表
CREATE TABLE IF NOT EXISTS system_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value TEXT,
    setting_type VARCHAR(50),
    description VARCHAR(500),
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
