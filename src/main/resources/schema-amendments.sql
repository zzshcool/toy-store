-- TiDB Cloud Schema Amendments for Toy Store
-- 此檔案包含對 schema.sql 的修正和補充

-- =====================================================
-- 1. 修正 draw_verifications 表結構
-- =====================================================
DROP TABLE IF EXISTS draw_verifications;
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
-- 2. 修正 member_levels 表（新增 sort_order）
-- =====================================================
ALTER TABLE member_levels ADD COLUMN IF NOT EXISTS sort_order INT DEFAULT 0;

-- =====================================================
-- 3. 修正 promo_codes 表結構
-- =====================================================
ALTER TABLE promo_codes ADD COLUMN IF NOT EXISTS type VARCHAR(50) DEFAULT 'DISCOUNT';
ALTER TABLE promo_codes ADD COLUMN IF NOT EXISTS reward_type VARCHAR(50);
ALTER TABLE promo_codes ADD COLUMN IF NOT EXISTS reward_value DECIMAL(15,2);
ALTER TABLE promo_codes ADD COLUMN IF NOT EXISTS creator_member_id BIGINT;
ALTER TABLE promo_codes ADD COLUMN IF NOT EXISTS enabled BOOLEAN DEFAULT TRUE;
ALTER TABLE promo_codes ADD COLUMN IF NOT EXISTS valid_until DATETIME;
ALTER TABLE promo_codes ADD COLUMN IF NOT EXISTS max_uses INT DEFAULT 0;
ALTER TABLE promo_codes ADD COLUMN IF NOT EXISTS created_at DATETIME DEFAULT CURRENT_TIMESTAMP;

-- =====================================================
-- 4. 修正 cabinet_items 表欄位名稱
-- =====================================================
ALTER TABLE cabinet_items CHANGE COLUMN item_name prize_name VARCHAR(200) NOT NULL;
ALTER TABLE cabinet_items CHANGE COLUMN item_description prize_description TEXT;
ALTER TABLE cabinet_items CHANGE COLUMN item_image_url prize_image_url VARCHAR(500);
ALTER TABLE cabinet_items CHANGE COLUMN item_value prize_value DECIMAL(15,2);
ALTER TABLE cabinet_items ADD COLUMN IF NOT EXISTS prize_rank VARCHAR(20);
ALTER TABLE cabinet_items ADD COLUMN IF NOT EXISTS shipment_request_id BIGINT;
ALTER TABLE cabinet_items ADD COLUMN IF NOT EXISTS requested_at DATETIME;

-- =====================================================
-- 5. 修正 roulette_games 表（新增 total_draws）
-- =====================================================
ALTER TABLE roulette_games ADD COLUMN IF NOT EXISTS total_draws INT DEFAULT 0;

-- =====================================================
-- 6. 修正 shipment_requests 表結構
-- =====================================================
ALTER TABLE shipment_requests ADD COLUMN IF NOT EXISTS is_free_shipping BOOLEAN DEFAULT FALSE;
ALTER TABLE shipment_requests ADD COLUMN IF NOT EXISTS postal_code VARCHAR(20);
ALTER TABLE shipment_requests ADD COLUMN IF NOT EXISTS admin_note TEXT;
ALTER TABLE shipment_requests ADD COLUMN IF NOT EXISTS shipped_at DATETIME;
ALTER TABLE shipment_requests ADD COLUMN IF NOT EXISTS delivered_at DATETIME;
ALTER TABLE shipment_requests ADD COLUMN IF NOT EXISTS tracking_number VARCHAR(100);
ALTER TABLE shipment_requests ADD COLUMN IF NOT EXISTS shipping_company VARCHAR(100);

-- =====================================================
-- 7. 修正 member_messages 表（新增欄位）
-- =====================================================
ALTER TABLE member_messages ADD COLUMN IF NOT EXISTS reference_id VARCHAR(100);
ALTER TABLE member_messages ADD COLUMN IF NOT EXISTS action_url VARCHAR(500);

-- =====================================================
-- 8. 修正 member_missions 表結構
-- =====================================================
ALTER TABLE member_missions ADD COLUMN IF NOT EXISTS mission_date DATE;
ALTER TABLE member_missions ADD COLUMN IF NOT EXISTS current_progress INT DEFAULT 0;
ALTER TABLE member_missions ADD COLUMN IF NOT EXISTS target_value INT;
ALTER TABLE member_missions ADD COLUMN IF NOT EXISTS reward_bonus_points INT DEFAULT 0;
ALTER TABLE member_missions ADD COLUMN IF NOT EXISTS is_completed BOOLEAN DEFAULT FALSE;
ALTER TABLE member_missions ADD COLUMN IF NOT EXISTS reward_claimed BOOLEAN DEFAULT FALSE;

-- =====================================================
-- 9. 修正 roulette_slots 表結構（新增 slot_order, slot_type）
-- =====================================================
ALTER TABLE roulette_slots ADD COLUMN IF NOT EXISTS slot_order INT DEFAULT 0;
ALTER TABLE roulette_slots ADD COLUMN IF NOT EXISTS slot_type VARCHAR(50) DEFAULT 'NORMAL';

-- =====================================================
-- 10. 修正 bingo_games 表（新增 total_cells）
-- =====================================================
ALTER TABLE bingo_games ADD COLUMN IF NOT EXISTS total_cells INT DEFAULT 9;

-- =====================================================
-- 11. 修正 gacha_items 表結構（新增 ip_id）
-- =====================================================
ALTER TABLE gacha_items ADD COLUMN IF NOT EXISTS ip_id BIGINT;
ALTER TABLE gacha_items ADD COLUMN IF NOT EXISTS estimated_value DECIMAL(15,2);

-- =====================================================
-- 12. 修正 payment_orders 表結構
-- =====================================================
ALTER TABLE payment_orders ADD COLUMN IF NOT EXISTS third_party_trade_no VARCHAR(100);
ALTER TABLE payment_orders ADD COLUMN IF NOT EXISTS updated_at DATETIME DEFAULT CURRENT_TIMESTAMP;

-- =====================================================
-- 13. 修正 invoices 表結構
-- =====================================================
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS tax_amount DECIMAL(15,2);

-- =====================================================
-- 14. 修正 logistics_records 表結構
-- =====================================================
ALTER TABLE logistics_records ADD COLUMN IF NOT EXISTS provider VARCHAR(50);
ALTER TABLE logistics_records ADD COLUMN IF NOT EXISTS last_update DATETIME;

-- =====================================================
-- 15. 修正 admin_action_logs 表結構（使用 admin_name）
-- =====================================================
ALTER TABLE admin_action_logs ADD COLUMN IF NOT EXISTS admin_name VARCHAR(100);
ALTER TABLE admin_action_logs ADD COLUMN IF NOT EXISTS timestamp DATETIME DEFAULT CURRENT_TIMESTAMP;

-- =====================================================
-- 16. 新增 cart_items 表（如果不存在）
-- =====================================================
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_cart_id (cart_id)
);

-- =====================================================
-- 17. 修正 coupons 表結構（使用 type 代替 discount_type）
-- =====================================================
ALTER TABLE coupons ADD COLUMN IF NOT EXISTS type VARCHAR(50);
ALTER TABLE coupons ADD COLUMN IF NOT EXISTS valid_from DATETIME;
ALTER TABLE coupons ADD COLUMN IF NOT EXISTS valid_until DATETIME;

-- =====================================================
-- 18. 修正 activities 表結構
-- =====================================================
ALTER TABLE activities ADD COLUMN IF NOT EXISTS type VARCHAR(50);
ALTER TABLE activities ADD COLUMN IF NOT EXISTS title VARCHAR(200);
ALTER TABLE activities ADD COLUMN IF NOT EXISTS active BOOLEAN DEFAULT TRUE;
ALTER TABLE activities ADD COLUMN IF NOT EXISTS expiry_date DATETIME;
ALTER TABLE activities ADD COLUMN IF NOT EXISTS created_at DATETIME DEFAULT CURRENT_TIMESTAMP;

-- =====================================================
-- 19. 修正 featured_items 表結構
-- =====================================================
ALTER TABLE featured_items ADD COLUMN IF NOT EXISTS product_id BIGINT;
ALTER TABLE featured_items ADD COLUMN IF NOT EXISTS created_at DATETIME DEFAULT CURRENT_TIMESTAMP;

-- =====================================================
-- 20. 修正 carousel_slides 表結構
-- =====================================================
ALTER TABLE carousel_slides ADD COLUMN IF NOT EXISTS created_at DATETIME DEFAULT CURRENT_TIMESTAMP;

-- =====================================================
-- 21. 修正 ichiban_prizes 表欄位名稱與結構
ALTER TABLE ichiban_prizes ADD COLUMN IF NOT EXISTS sort_order INT DEFAULT 0;
-- ALTER TABLE ichiban_prizes CHANGE COLUMN quantity total_quantity INT NOT NULL; -- Reverted to match Code/Mapper
