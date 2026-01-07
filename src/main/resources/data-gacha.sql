-- 抽獎系統測試資料
-- 用於開發與測試環境

-- 1. 系統設定初始化（由 SystemSettingService @PostConstruct 自動處理）

-- 2. 測試 IP 主題
INSERT INTO gacha_ips (name, description, image_url, status, created_at) VALUES 
('洛克人系列', 'Mega Man 經典遊戲系列收藏', '/images/ip/rockman.jpg', 'ACTIVE', NOW()),
('航海王', 'One Piece 海賊王系列', '/images/ip/onepiece.jpg', 'ACTIVE', NOW()),
('鬼滅之刃', '鬼滅之刃限定系列', '/images/ip/kimetsu.jpg', 'COMING_SOON', NOW());

-- 3. 測試一番賞箱體
INSERT INTO ichiban_boxes (ip_id, name, description, image_url, price_per_draw, max_slots, total_slots, status, created_at) VALUES 
(1, '洛克人一番賞 第一彈', '經典洛克人系列首發！包含限量公仔與精美周邊', '/images/ichiban/rockman1.jpg', 680, 80, 20, 'ACTIVE', NOW()),
(2, '航海王 新世界篇', '草帽海賊團全員集結！', '/images/ichiban/onepiece1.jpg', 780, 80, 30, 'ACTIVE', NOW());

-- 4. 測試一番賞獎品
INSERT INTO ichiban_prizes (box_id, `rank`, name, description, image_url, estimated_value, quantity, remaining_quantity, sort_order) VALUES 
-- 洛克人箱體獎品
(1, 'A', '洛克人 1/6 限量公仔', '限量版精緻公仔', '/images/prize/rockman_a.jpg', 2500, 1, 1, 1),
(1, 'B', '萊特博士 Q版公仔', '可愛Q版造型', '/images/prize/rockman_b.jpg', 1200, 2, 2, 2),
(1, 'C', '洛克人 壓克力立牌', '精美壓克力立牌', '/images/prize/rockman_c.jpg', 500, 3, 3, 3),
(1, 'D', '洛克人 馬克杯', '日常實用好物', '/images/prize/rockman_d.jpg', 300, 4, 4, 4),
(1, 'E', '洛克人 資料夾組', '文具周邊三件組', '/images/prize/rockman_e.jpg', 150, 5, 5, 5),
(1, 'LAST', '洛克人 簽名板', '限定版簽名板', '/images/prize/rockman_last.jpg', 3000, 1, 1, 99),
-- 航海王箱體獎品
(2, 'A', '魯夫 齒輪五 公仔', '尼卡形態限定公仔', '/images/prize/op_a.jpg', 3000, 1, 1, 1),
(2, 'B', '索隆 三刀流公仔', '閻魔配色版', '/images/prize/op_b.jpg', 1500, 2, 2, 2),
(2, 'C', '草帽海賊團 掛畫', '全員集結掛畫', '/images/prize/op_c.jpg', 800, 4, 4, 3),
(2, 'D', '惡魔果實 造型杯', '橡膠果實造型', '/images/prize/op_d.jpg', 400, 6, 6, 4),
(2, 'E', '航海王 徽章組', '10入精美徽章', '/images/prize/op_e.jpg', 200, 8, 8, 5),
(2, 'LAST', '黃金梅利號 模型', '限定版船模', '/images/prize/op_last.jpg', 5000, 1, 1, 99);

-- 5. 一番賞格子（根據獎品數量建立）
-- 洛克人箱體 20 格
INSERT INTO ichiban_slots (box_id, slot_number, status, prize_id) VALUES 
(1, 1, 'AVAILABLE', 1), (1, 2, 'AVAILABLE', 2), (1, 3, 'AVAILABLE', 2),
(1, 4, 'AVAILABLE', 3), (1, 5, 'AVAILABLE', 3), (1, 6, 'AVAILABLE', 3),
(1, 7, 'AVAILABLE', 4), (1, 8, 'AVAILABLE', 4), (1, 9, 'AVAILABLE', 4), (1, 10, 'AVAILABLE', 4),
(1, 11, 'AVAILABLE', 5), (1, 12, 'AVAILABLE', 5), (1, 13, 'AVAILABLE', 5), (1, 14, 'AVAILABLE', 5), (1, 15, 'AVAILABLE', 5),
(1, 16, 'AVAILABLE', 5), (1, 17, 'AVAILABLE', 5), (1, 18, 'AVAILABLE', 5), (1, 19, 'AVAILABLE', 5),
(1, 20, 'AVAILABLE', 6);

-- 6. 測試轉盤遊戲
INSERT INTO roulette_games (ip_id, name, description, image_url, price_per_spin, max_slots, total_slots, total_draws, status, created_at) VALUES 
(1, '洛克人幸運轉盤', '轉動命運，贏取豪禮！', '/images/roulette/rockman.jpg', 100, 25, 8, 0, 'ACTIVE', NOW()),
(2, '航海王尋寶輪盤', '尋找屬於你的寶藏！', '/images/roulette/onepiece.jpg', 150, 25, 10, 0, 'ACTIVE', NOW());

-- 7. 轉盤獎格
INSERT INTO roulette_slots (game_id, slot_order, slot_type, prize_name, prize_description, weight, shard_amount, color) VALUES 
-- 洛克人轉盤
(1, 1, 'JACKPOT', '洛克人公仔', '大獎！限量公仔', 5, NULL, '#FF0000'),
(1, 2, 'NORMAL', '徽章', '精美徽章', 20, NULL, '#4ECDC4'),
(1, 3, 'SHARD', '100碎片', '碎片獎勵', 25, 100, '#AAAAAA'),
(1, 4, 'NORMAL', '貼紙組', '精美貼紙', 20, NULL, '#FFD93D'),
(1, 5, 'FREE_SPIN', '再來一次', '免費再轉', 10, NULL, '#00FF00'),
(1, 6, 'RARE', '壓克力立牌', '稀有獎品', 8, NULL, '#FF6600'),
(1, 7, 'SHARD', '50碎片', '碎片獎勵', 30, 50, '#CCCCCC'),
(1, 8, 'NORMAL', '明信片', '精美明信片', 25, NULL, '#FFD700'),
-- 航海王轉盤
(2, 1, 'JACKPOT', '魯夫公仔', '大獎！齒輪五公仔', 3, NULL, '#FF0000'),
(2, 2, 'RARE', '索隆模型', '稀有三刀流模型', 5, NULL, '#FF6600'),
(2, 3, 'NORMAL', '海賊旗', '草帽海賊團旗幟', 15, NULL, '#4ECDC4'),
(2, 4, 'SHARD', '150碎片', '碎片獎勵', 20, 150, '#AAAAAA'),
(2, 5, 'FREE_SPIN', '再來一次', '免費再轉', 8, NULL, '#00FF00'),
(2, 6, 'NORMAL', '懸賞令', '魯夫懸賞令複製品', 18, NULL, '#FFD93D'),
(2, 7, 'SHARD', '80碎片', '碎片獎勵', 25, 80, '#CCCCCC'),
(2, 8, 'NORMAL', '惡魔果實鑰匙圈', '橡膠果實造型', 20, NULL, '#6C5CE7'),
(2, 9, 'RARE', '喬巴帽', '喬巴造型帽子', 6, NULL, '#FF8B94'),
(2, 10, 'NORMAL', '航海日誌', '精裝筆記本', 15, NULL, '#FFD700');

-- 8. 測試九宮格遊戲
INSERT INTO bingo_games (ip_id, name, description, image_url, price_per_dig, grid_size, status, bingo_reward_name, bingo_reward_image_url, bingo_reward_value, created_at) VALUES 
(1, '洛克人挖寶九宮格', '挖掘寶藏，連線獲得額外獎勵！', '/images/bingo/rockman.jpg', 80, 3, 'ACTIVE', '洛克人海報套組', '/images/bingo/rockman_bingo.jpg', 600, NOW()),
(2, '航海王藏寶圖', '發現One Piece的秘密！', '/images/bingo/onepiece.jpg', 120, 4, 'ACTIVE', '草帽海賊團全員公仔組', '/images/bingo/op_bingo.jpg', 2000, NOW());

-- 9. 九宮格格子
INSERT INTO bingo_cells (game_id, position, row, col, prize_name, prize_description, is_revealed) VALUES 
-- 洛克人 3x3
(1, 1, 0, 0, '洛克人貼紙', '精美貼紙', FALSE),
(1, 2, 0, 1, 'E罐能量罐', '經典造型周邊', FALSE),
(1, 3, 0, 2, '洛克人徽章', '金屬徽章', FALSE),
(1, 4, 1, 0, '萊特博士明信片', '精美明信片', FALSE),
(1, 5, 1, 1, '洛克人鑰匙圈', '壓克力鑰匙圈', FALSE),
(1, 6, 1, 2, '蓋乃博士書籤', '書籤套組', FALSE),
(1, 7, 2, 0, '萊姆馬克杯', '可愛Q版杯', FALSE),
(1, 8, 2, 1, '威利博士筆', '造型原子筆', FALSE),
(1, 9, 2, 2, '洛克人手機殼', '透明手機殼', FALSE),
-- 航海王 4x4
(2, 1, 0, 0, '魯夫貼紙', '齒輪五貼紙', FALSE),
(2, 2, 0, 1, '索隆徽章', '三刀流徽章', FALSE),
(2, 3, 0, 2, '娜美明信片', '天氣棒明信片', FALSE),
(2, 4, 0, 3, '騙人布鑰匙圈', '狙擊王鑰匙圈', FALSE),
(2, 5, 1, 0, '香吉士書籤', '黑足書籤', FALSE),
(2, 6, 1, 1, '喬巴杯墊', '可愛杯墊', FALSE),
(2, 7, 1, 2, '羅賓筆', '花花果實筆', FALSE),
(2, 8, 1, 3, '佛朗乃手環', '可樂手環', FALSE),
(2, 9, 2, 0, '布魯克面紙盒', '靈魂之王面紙盒', FALSE),
(2, 10, 2, 1, '甚平毛巾', '魚人空手道毛巾', FALSE),
(2, 11, 2, 2, '千陽號模型', '迷你船模', FALSE),
(2, 12, 2, 3, '惡魔果實糖', '造型糖果', FALSE),
(2, 13, 3, 0, '懸賞令組', '草帽團懸賞令', FALSE),
(2, 14, 3, 1, '海賊旗扇子', '骷髏旗扇子', FALSE),
(2, 15, 3, 2, '航海日誌', '航海筆記本', FALSE),
(2, 16, 3, 3, '電話蟲存錢筒', '造型存錢筒', FALSE);

-- 10. 兌換商店商品
INSERT INTO redeem_shop_items (name, description, image_url, shard_cost, estimated_value, stock, total_stock, item_type, status, sort_order, created_at) VALUES 
('洛克人 S賞 限量公仔', '超稀有！1/4比例精緻公仔', '/images/redeem/rockman_s.jpg', 10000, 5000, 3, 3, 'S_RANK', 'ACTIVE', 1, NOW()),
('航海王 隱藏款 黃金城娜美', '隱藏款限定！金色禮服娜美', '/images/redeem/op_hidden.jpg', 15000, 8000, 1, 1, 'HIDDEN', 'ACTIVE', 2, NOW()),
('鬼滅之刃 特別款 炭治郎日輪刀複製品', '1:1比例日輪刀', '/images/redeem/kimetsu_special.jpg', 20000, 12000, 2, 2, 'SPECIAL', 'ACTIVE', 3, NOW()),
('洛克人 壓克力立牌組', '四款經典角色立牌', '/images/redeem/rockman_stand.jpg', 3000, 800, 10, 10, 'PRIZE', 'ACTIVE', 10, NOW()),
('航海王 徽章全套', '草帽團全員徽章20入', '/images/redeem/op_badge.jpg', 5000, 1500, 8, 8, 'PRIZE', 'ACTIVE', 11, NOW());
