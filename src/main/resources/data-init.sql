-- =============================================================================
-- TiDB Cloud Consolidated Data Initialization for Toy Store
-- 合併 data-gacha-init.sql (完整版) + 額外優化
-- 已轉換為 TiDB 相容語法 (row/col 對齊 Java Model, AUTO_INCREMENT 語法)
-- Generated: 2026-01-08
-- =============================================================================

-- ########## 0. 環境重置 ##########
DELETE FROM ichiban_slots;
DELETE FROM ichiban_prizes;
DELETE FROM ichiban_boxes;
DELETE FROM roulette_slots;
DELETE FROM roulette_games;
DELETE FROM bingo_cells;
DELETE FROM bingo_games;
DELETE FROM gacha_ips;
DELETE FROM redeem_shop_items;
DELETE FROM member_levels;
DELETE FROM gacha_items;
DELETE FROM gacha_themes;
DELETE FROM products;
DELETE FROM sub_categories;
DELETE FROM categories;
DELETE FROM activities;
DELETE FROM admin_role_permissions;
DELETE FROM admin_user_roles;
DELETE FROM admin_permissions;
DELETE FROM admin_roles;

-- ########## 1. 會員等級初始化 ##########
INSERT INTO member_levels (id, name, min_growth_value, discount_rate, points_multiplier, description, icon_url, sort_order) VALUES
(1, '普通會員', 0, 100.00, 1.00, '新註冊會員', '/images/level/normal.png', 1),
(2, '銀卡會員', 1000, 98.00, 1.20, '累積成長值達1000', '/images/level/silver.png', 2),
(3, '金卡會員', 5000, 95.00, 1.50, '累積成長值達5000', '/images/level/gold.png', 3),
(4, '鑽石會員', 20000, 90.00, 2.00, '累積成長值達20000', '/images/level/diamond.png', 4),
(5, 'VIP會員', 50000, 85.00, 3.00, '累積成長值達50000', '/images/level/vip.png', 5);

-- ########## 2. IP 主題 (ID 1-20) ##########
INSERT INTO gacha_ips (id, name, description, image_url, status, created_at) VALUES 
(1, '洛克人', 'Mega Man 經典英雄系列', '/images/ip/rockman.jpg', 'ACTIVE', NOW()),
(2, '獵人', 'Hunter × Hunter 念能力冒險', '/images/ip/hunter.jpg', 'ACTIVE', NOW()),
(3, '七龍珠', 'Dragon Ball 傳奇戰鬥', '/images/ip/dragonball.jpg', 'ACTIVE', NOW()),
(4, '進擊的巨人', 'Attack on Titan 調查兵團', '/images/ip/aot.jpg', 'ACTIVE', NOW()),
(5, '鋼之鍊金術師', 'Fullmetal Alchemist 等價交換', '/images/ip/fma.jpg', 'ACTIVE', NOW()),
(6, '天竺鼠車車', 'PUI PUI Molcar 療癒小車', '/images/ip/molcar.jpg', 'ACTIVE', NOW()),
(7, '神奇寶貝', 'Pokémon 成為大師', '/images/ip/pokemon.jpg', 'ACTIVE', NOW()),
(8, '鏈鋸人', 'Chainsaw Man 惡魔獵人', '/images/ip/chainsawman.jpg', 'ACTIVE', NOW()),
(9, 'BLEACH死神', 'BLEACH 卍解釋放', '/images/ip/bleach.jpg', 'ACTIVE', NOW()),
(10, '美少女戰士', 'Sailor Moon 正義化身', '/images/ip/sailormoon.jpg', 'ACTIVE', NOW()),
(11, '灌籃高手', 'SLAM DUNK 全國大賽', '/images/ip/slamdunk.jpg', 'ACTIVE', NOW()),
(12, '鬼滅之刃', 'Demon Slayer 復仇之旅', '/images/ip/kimetsu.jpg', 'ACTIVE', NOW()),
(13, '超人力霸王', 'Ultraman 守護地球', '/images/ip/ultra.jpg', 'ACTIVE', NOW()),
(14, '櫻桃小丸子', 'Chibi Maruko-chan', '/images/ip/maruko.jpg', 'ACTIVE', NOW()),
(15, '死亡筆記本', 'Death Note 智慧對決', '/images/ip/deathnote.jpg', 'ACTIVE', NOW()),
(16, '銀魂', 'Gintama 萬事屋日常', '/images/ip/gintama.jpg', 'ACTIVE', NOW()),
(17, '新網球王子', 'Prince of Tennis', '/images/ip/tenipuri.jpg', 'ACTIVE', NOW()),
(18, '庫洛魔法使', 'Cardcaptor Sakura', '/images/ip/cardcaptor.jpg', 'ACTIVE', NOW()),
(19, 'ONE PIECE航海王', '和之國大章節', '/images/ip/onepiece.jpg', 'ACTIVE', NOW()),
(20, 'JoJo的奇妙冒險', '黃金之風', '/images/ip/jojo.jpg', 'ACTIVE', NOW());

-- ########## 3. 一番賞箱體 (ID 1-20，與 IP ID 對應) ##########
INSERT INTO ichiban_boxes (id, ip_id, name, description, image_url, price_per_draw, max_slots, total_slots, status, created_at) VALUES 
(1, 1, '洛克人一番賞', '經典藍色轟炸機', '/images/ichiban/rockman1.jpg', 680, 80, 80, 'ACTIVE', NOW()),
(2, 2, '獵人一番賞', '貪婪之島限定', '/images/ichiban/hunter1.jpg', 750, 80, 80, 'ACTIVE', NOW()),
(3, 3, '七龍珠一番賞', '賽亞人傳奇', '/images/ichiban/db1.jpg', 800, 80, 80, 'ACTIVE', NOW()),
(4, 4, '進擊的巨人一番賞', '最終季紀念', '/images/ichiban/aot1.jpg', 720, 80, 80, 'ACTIVE', NOW()),
(5, 5, '鋼之鍊金術師一番賞', '鍊成陣大賞', '/images/ichiban/fma1.jpg', 700, 80, 80, 'ACTIVE', NOW()),
(6, 6, '天竺鼠車車一番賞', 'PUI PUI篇', '/images/ichiban/molcar1.jpg', 580, 80, 80, 'ACTIVE', NOW()),
(7, 7, '神奇寶貝一番賞', '寶可夢大師', '/images/ichiban/pokemon1.jpg', 750, 80, 80, 'ACTIVE', NOW()),
(8, 8, '鏈鋸人一番賞', '惡魔獵人大賞', '/images/ichiban/csm1.jpg', 780, 80, 80, 'ACTIVE', NOW()),
(9, 9, 'BLEACH死神一番賞', '卍解篇收藏', '/images/ichiban/bleach1.jpg', 720, 80, 80, 'ACTIVE', NOW()),
(10, 10, '美少女戰士一番賞', '水晶篇限定', '/images/ichiban/sailor1.jpg', 700, 80, 80, 'ACTIVE', NOW()),
(11, 11, '灌籃高手一番賞', '全國大賽熱血', '/images/ichiban/slam1.jpg', 750, 80, 80, 'ACTIVE', NOW()),
(12, 12, '鬼滅之刃一番賞', '無限列車紀念', '/images/ichiban/kimetsu1.jpg', 800, 80, 80, 'ACTIVE', NOW()),
(13, 13, '超人力霸王一番賞', '光之巨人再現', '/images/ichiban/ultra1.jpg', 680, 80, 80, 'ACTIVE', NOW()),
(14, 14, '櫻桃小丸子一番賞', '日常溫馨系列', '/images/ichiban/maruko1.jpg', 550, 80, 80, 'ACTIVE', NOW()),
(15, 15, '死亡筆記本一番賞', '對決篇收藏', '/images/ichiban/dn1.jpg', 720, 80, 80, 'ACTIVE', NOW()),
(16, 16, '銀魂一番賞', '萬事屋精選', '/images/ichiban/gintama1.jpg', 700, 80, 80, 'ACTIVE', NOW()),
(17, 17, '新網球王子一番賞', '王子之路', '/images/ichiban/tenipuri1.jpg', 680, 80, 80, 'ACTIVE', NOW()),
(18, 18, '庫洛魔法使一番賞', '庫洛牌典藏', '/images/ichiban/ccs1.jpg', 700, 80, 80, 'ACTIVE', NOW()),
(19, 19, 'ONE PIECE航海王一番賞', '和之國篇大賞', '/images/ichiban/op1.jpg', 850, 80, 80, 'ACTIVE', NOW()),
(20, 20, 'JoJo的奇妙冒險一番賞', '黃金之風紀念', '/images/ichiban/jojo1.jpg', 780, 80, 80, 'ACTIVE', NOW());

-- ########## 4. 一番賞獎品 - 特化獎項 (手動精選) ##########
INSERT INTO ichiban_prizes (id, box_id, `rank`, name, description, image_url, estimated_value, quantity, remaining_quantity, sort_order) VALUES
-- IP 1: 洛克人 (Rockman)
(1, 1, 'A', '洛克人 X 1/12 比例可動公仔 (艾克斯)', '精細塗裝，含多種手口更換件', '/images/prize/rockman_a.jpg', 2800, 1, 1, 1),
(2, 1, 'B', '洛克人 Zero 經典紅色裝甲模型', '動漫忠實還原，Z噴氣配件', '/images/prize/rockman_b.jpg', 2200, 2, 2, 2),
(3, 1, 'C', '洛克人系列 亞克力立牌 (全6種)', '精美透明亞克力，可組合展示', '/images/prize/rockman_c.jpg', 450, 5, 5, 3),
(9, 1, 'I', '洛克人經典 E 罐 造型馬克杯', '極具辨識度的回血道具造型', '/images/prize/rockman_i.jpg', 350, 15, 15, 9),
(10, 1, 'LAST', 'LAST賞 洛克人 35 週年金色紀念公仔', '全金屬質感噴塗，收藏首選', '/images/prize/rockman_last.jpg', 5000, 1, 1, 10),
-- IP 19: 航海王 (One Piece)
(181, 19, 'A', '魯夫 尼卡形態 「解放之鼓」公仔', '震撼的太陽神姿態，帶有透明雲霧特效', '/images/prize/op_a.jpg', 3500, 1, 1, 1),
(182, 19, 'B', '索隆 三刀流奧義 「九山八海」場景模型', '極限動態雕刻，氣勢驚人', '/images/prize/op_b.jpg', 3000, 2, 2, 2),
(183, 19, 'C', '香吉士 魔神風腳 模型', '腿部透明發光特件', '/images/prize/op_c.jpg', 2500, 3, 3, 3),
(190, 19, 'LAST', 'LAST賞 羅傑與白鬍子 世紀之對戰景品', '兩大傳奇決鬥場景復刻', '/images/prize/op_last.jpg', 8000, 1, 1, 10),
-- 其餘 IP 提供代表性獎項
(11, 2, 'A', '獵人 奇犽 神速形態限定模型', '雷光特效環繞', '/images/prize/hunter_a.jpg', 2600, 1, 1, 1),
(21, 3, 'A', '七龍珠 孫悟空 自在極意功 雕塑', '銀髮閃耀，氣場爆裂', '/images/prize/db_a.jpg', 3200, 1, 1, 1),
(111, 12, 'A', '鬼滅之刃 煉獄杏壽郎 炎之呼吸公仔', '大哥沒有輸！華麗火焰效果', '/images/prize/kimetsu_a.jpg', 3000, 1, 1, 1);

-- ########## 5. 一番賞獎品 - 動態補全其餘獎項 (TiDB 相容) ##########
INSERT INTO ichiban_prizes (id, box_id, `rank`, name, description, image_url, estimated_value, quantity, remaining_quantity, sort_order)
SELECT 
    (b.id - 1) * 10 + r.idx, 
    b.id, r.rnk, 
    CASE 
        WHEN r.idx=1 THEN CONCAT(gi.name, ' 官方限定比例模型 (A賞)')
        WHEN r.idx=2 THEN CONCAT(gi.name, ' 角色經典場景公仔 (B賞)')
        WHEN r.idx=3 THEN CONCAT(gi.name, ' 限量精選版模型 (C賞)')
        WHEN r.rnk='LAST' THEN CONCAT('LAST賞 ', gi.name, ' 終極限量隱藏版')
        ELSE CONCAT(gi.name, ' ', r.disp)
    END,
    '精選角色週邊，官方正版授權', 
    CONCAT('/images/prize/generic_', r.idx, '.jpg'), 
    CASE WHEN r.idx=1 THEN 2500 WHEN r.idx=10 THEN 4500 ELSE 150 END,
    CASE WHEN r.idx=1 OR r.idx=10 THEN 1 ELSE 15 END,
    CASE WHEN r.idx=1 OR r.idx=10 THEN 1 ELSE 15 END,
    r.idx
FROM ichiban_boxes b
JOIN gacha_ips gi ON gi.id = b.ip_id
CROSS JOIN (
    SELECT 1 as idx, 'A' as rnk, 'A賞 特大公仔' as disp UNION ALL
    SELECT 2, 'B', 'B賞 限量模型' UNION ALL
    SELECT 3, 'C', 'C賞 造型立牌' UNION ALL
    SELECT 4, 'D', 'D賞 實用馬克杯' UNION ALL
    SELECT 5, 'E', 'E賞 限定貼紙組' UNION ALL
    SELECT 6, 'F', 'F賞 收藏色紙' UNION ALL
    SELECT 7, 'G', 'G賞 造型吊飾' UNION ALL
    SELECT 8, 'H', 'H賞 迷你手帕' UNION ALL
    SELECT 9, 'I', 'I賞 紀念磁鐵' UNION ALL
    SELECT 10, 'LAST', 'LAST賞 隱藏紀念品'
) r
WHERE NOT EXISTS (SELECT 1 FROM ichiban_prizes p2 WHERE p2.id = (b.id - 1) * 10 + r.idx);

-- ########## 6. 一番賞格子 (80 格/箱，TiDB 相容 - 使用純 UNION ALL 替代 RECURSIVE) ##########
INSERT INTO ichiban_slots (box_id, slot_number, prize_id, status)
SELECT 
    b.id, s.x, 
    (b.id - 1) * 10 + 
    CASE 
        WHEN s.x = 1 THEN 1 -- A賞 (1)
        WHEN s.x BETWEEN 2 AND 3 THEN 2 -- B賞 (2)
        WHEN s.x BETWEEN 4 AND 6 THEN 3 -- C賞 (3)
        WHEN s.x BETWEEN 7 AND 12 THEN 4 -- D賞 (6)
        WHEN s.x BETWEEN 13 AND 20 THEN 5 -- E賞 (8)
        WHEN s.x BETWEEN 21 AND 35 THEN 6 -- F賞 (15)
        WHEN s.x BETWEEN 36 AND 50 THEN 7 -- G賞 (15)
        WHEN s.x BETWEEN 51 AND 65 THEN 8 -- H賞 (15)
        WHEN s.x BETWEEN 66 AND 79 THEN 9 -- I賞 (14)
        ELSE 10 -- LAST (1)
    END, 
    'AVAILABLE'
FROM ichiban_boxes b
CROSS JOIN (
    SELECT 1 as x UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL
    SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15 UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20 UNION ALL
    SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24 UNION ALL SELECT 25 UNION ALL SELECT 26 UNION ALL SELECT 27 UNION ALL SELECT 28 UNION ALL SELECT 29 UNION ALL SELECT 30 UNION ALL
    SELECT 31 UNION ALL SELECT 32 UNION ALL SELECT 33 UNION ALL SELECT 34 UNION ALL SELECT 35 UNION ALL SELECT 36 UNION ALL SELECT 37 UNION ALL SELECT 38 UNION ALL SELECT 39 UNION ALL SELECT 40 UNION ALL
    SELECT 41 UNION ALL SELECT 42 UNION ALL SELECT 43 UNION ALL SELECT 44 UNION ALL SELECT 45 UNION ALL SELECT 46 UNION ALL SELECT 47 UNION ALL SELECT 48 UNION ALL SELECT 49 UNION ALL SELECT 50 UNION ALL
    SELECT 51 UNION ALL SELECT 52 UNION ALL SELECT 53 UNION ALL SELECT 54 UNION ALL SELECT 55 UNION ALL SELECT 56 UNION ALL SELECT 57 UNION ALL SELECT 58 UNION ALL SELECT 59 UNION ALL SELECT 60 UNION ALL
    SELECT 61 UNION ALL SELECT 62 UNION ALL SELECT 63 UNION ALL SELECT 64 UNION ALL SELECT 65 UNION ALL SELECT 66 UNION ALL SELECT 67 UNION ALL SELECT 68 UNION ALL SELECT 69 UNION ALL SELECT 70 UNION ALL
    SELECT 71 UNION ALL SELECT 72 UNION ALL SELECT 73 UNION ALL SELECT 74 UNION ALL SELECT 75 UNION ALL SELECT 76 UNION ALL SELECT 77 UNION ALL SELECT 78 UNION ALL SELECT 79 UNION ALL SELECT 80
) s;

-- ########## 7. 轉盤遊戲 (動態生成 20 個) ##########
INSERT INTO roulette_games (id, ip_id, name, description, image_url, price_per_spin, max_slots, total_slots, total_draws, status, created_at)
SELECT id, id, CONCAT(name, ' 極速轉盤'), '最高有機率獲得 1000 碎片！', '/images/roulette/generic.jpg', 150, 25, 8, 0, 'ACTIVE', NOW() FROM gacha_ips;

-- ########## 8. 轉盤格子 (每個轉盤 8 格) ##########
INSERT INTO roulette_slots (game_id, position, slot_order, slot_type, prize_name, prize_description, weight, shard_amount, color)
SELECT g.id, s.idx, s.idx, 
    CASE WHEN s.idx = 1 THEN 'RARE' ELSE 'NORMAL' END,
    CASE WHEN s.idx = 1 THEN CONCAT(i.name, ' 官方限量徽章') ELSE '10 碎片' END,
    '精美小物', 
    CASE WHEN s.idx = 1 THEN 5 ELSE 30 END,
    CASE WHEN s.idx = 1 THEN 100 ELSE 10 END,
    CASE WHEN s.idx = 1 THEN '#FFD700' ELSE '#4ECDC4' END
FROM roulette_games g
JOIN gacha_ips i ON i.id = g.ip_id
CROSS JOIN (SELECT 1 as idx UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8) s;

-- ########## 9. 九宮格遊戲 (動態生成 20 個) ##########
INSERT INTO bingo_games (id, ip_id, name, description, image_url, price_per_dig, grid_size, status, bingo_reward_name, bingo_reward_image_url, bingo_reward_value, created_at)
SELECT id, id, CONCAT(name, ' 驚喜九宮格'), '挖開格子，收集相同角色連線！', '/images/bingo/generic.jpg', 120, 3, 'ACTIVE', '聖杯級神祕大禮包', '/images/reward/bingo.jpg', 1200, NOW() FROM gacha_ips;

-- ########## 10. 九宮格格子 (使用 row_num/col_num 避免保留字，TiDB 相容) ##########
INSERT INTO bingo_cells (game_id, position, row_num, col_num, prize_name, prize_description, is_revealed)
SELECT g.id, s.x, (s.x-1) DIV 3, (s.x-1) MOD 3, '隱藏碎片', '20 碎片', FALSE FROM bingo_games g 
CROSS JOIN (SELECT 1 as x UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) s;

-- ########## 11. 盲盒 (Blind Boxes) ##########
DELETE FROM blind_box_items;
DELETE FROM blind_boxes;

INSERT INTO blind_boxes (id, name, description, image_url, ip_name, price_per_box, full_box_price, total_boxes, status, created_at, updated_at) VALUES
(1, '航海王 和之國篇 盲盒', '和之國大冒險系列，集結魯夫與夥伴', '/images/blindbox/op_wano.jpg', 'ONE PIECE航海王', 350, 3500, 12, 'ACTIVE', NOW(), NOW()),
(2, '鬼滅之刃 無限列車 盲盒', '無限列車篇精選角色', '/images/blindbox/kimetsu_train.jpg', '鬼滅之刃', 380, 3800, 12, 'ACTIVE', NOW(), NOW()),
(3, '神奇寶貝 初代御三家 盲盒', '經典初代寶可夢收藏', '/images/blindbox/pokemon_starter.jpg', '神奇寶貝', 320, 3200, 12, 'ACTIVE', NOW(), NOW()),
(4, '鏈鋸人 惡魔篇 盲盒', '電次與惡魔夥伴們', '/images/blindbox/csm_devils.jpg', '鏈鋸人', 400, 4000, 12, 'ACTIVE', NOW(), NOW()),
(5, '獵人 貪婪之島 盲盒', '貪婪之島篇角色收藏', '/images/blindbox/hunter_gi.jpg', '獵人', 360, 3600, 12, 'ACTIVE', NOW(), NOW()),
(6, '七龍珠 賽亞人 盲盒', '傳說中的賽亞人戰士', '/images/blindbox/db_saiyan.jpg', '七龍珠', 380, 3800, 12, 'ACTIVE', NOW(), NOW());

-- ########## 12. 盲盒物品 (Blind Box Items) ##########
-- Box 1: 航海王 和之國篇
INSERT INTO blind_box_items (blind_box_id, box_number, prize_name, prize_description, prize_image_url, estimated_value, rarity, status) VALUES
(1, 1, '魯夫 尼卡形態', '太陽神尼卡覺醒姿態', '/images/blindbox/op/nika.jpg', 1500, 'SECRET', 'AVAILABLE'),
(1, 2, '索隆 閻魔', '持閻魔刀的索隆', '/images/blindbox/op/zoro.jpg', 800, 'ULTRA_RARE', 'AVAILABLE'),
(1, 3, '香吉士 魔神風腳', '惡魔風腳發動', '/images/blindbox/op/sanji.jpg', 700, 'ULTRA_RARE', 'AVAILABLE'),
(1, 4, '娜美 天候棒', '使用天候棒的娜美', '/images/blindbox/op/nami.jpg', 500, 'RARE', 'AVAILABLE'),
(1, 5, '羅賓 花花果實', '花花果實能力展現', '/images/blindbox/op/robin.jpg', 500, 'RARE', 'AVAILABLE'),
(1, 6, '喬巴 怪物型態', '怪物強化型態', '/images/blindbox/op/chopper.jpg', 400, 'RARE', 'AVAILABLE'),
(1, 7, '佛朗乾 鋼鐵海俠', '改造人佛朗乾', '/images/blindbox/op/franky.jpg', 350, 'NORMAL', 'AVAILABLE'),
(1, 8, '布魯克 靈魂之王', '靈魂之王姿態', '/images/blindbox/op/brook.jpg', 350, 'NORMAL', 'AVAILABLE'),
(1, 9, '神槍惡靈 烏索普', '狙擊手姿態', '/images/blindbox/op/usopp.jpg', 350, 'NORMAL', 'AVAILABLE'),
(1, 10, '甚平 魚人空手道', '魚人空手道達人', '/images/blindbox/op/jinbe.jpg', 400, 'RARE', 'AVAILABLE'),
(1, 11, '大和 雷鳴八卦', '凱多之女大和', '/images/blindbox/op/yamato.jpg', 600, 'ULTRA_RARE', 'AVAILABLE'),
(1, 12, '桃之助 龍形態', '變身青龍的桃之助', '/images/blindbox/op/momo.jpg', 350, 'NORMAL', 'AVAILABLE'),

-- Box 2: 鬼滅之刃 無限列車
(2, 1, '煉獄杏壽郎 炎之呼吸', '炎柱完全燃燒', '/images/blindbox/kimetsu/rengoku.jpg', 1500, 'SECRET', 'AVAILABLE'),
(2, 2, '竈門炭治郎 火神神樂', '日之呼吸覺醒', '/images/blindbox/kimetsu/tanjiro.jpg', 800, 'ULTRA_RARE', 'AVAILABLE'),
(2, 3, '竈門禰豆子 爆血', '血鬼術發動', '/images/blindbox/kimetsu/nezuko.jpg', 700, 'ULTRA_RARE', 'AVAILABLE'),
(2, 4, '我妻善逸 霹靂一閃', '雷之呼吸壹之型', '/images/blindbox/kimetsu/zenitsu.jpg', 500, 'RARE', 'AVAILABLE'),
(2, 5, '嘴平伊之助 獸之呼吸', '野獸般的戰士', '/images/blindbox/kimetsu/inosuke.jpg', 500, 'RARE', 'AVAILABLE'),
(2, 6, '魘夢 下弦之壹', '無限列車的惡夢', '/images/blindbox/kimetsu/enmu.jpg', 400, 'RARE', 'AVAILABLE'),
(2, 7, '炭治郎 水之呼吸', '水之呼吸拾之型', '/images/blindbox/kimetsu/tanjiro_water.jpg', 350, 'NORMAL', 'AVAILABLE'),
(2, 8, '禰豆子 睡眠狀態', '木箱中的禰豆子', '/images/blindbox/kimetsu/nezuko_sleep.jpg', 350, 'NORMAL', 'AVAILABLE'),
(2, 9, '善逸 睡眠戰鬥', '無意識戰鬥狀態', '/images/blindbox/kimetsu/zenitsu_sleep.jpg', 350, 'NORMAL', 'AVAILABLE'),
(2, 10, '伊之助 憤怒模式', '暴怒的伊之助', '/images/blindbox/kimetsu/inosuke_rage.jpg', 350, 'NORMAL', 'AVAILABLE'),
(2, 11, '煉獄 奧義', '炎之呼吸奧義', '/images/blindbox/kimetsu/rengoku_ogi.jpg', 600, 'ULTRA_RARE', 'AVAILABLE'),
(2, 12, '無限列車場景', '列車戰鬥場景', '/images/blindbox/kimetsu/train_scene.jpg', 400, 'RARE', 'AVAILABLE'),

-- Box 3: 神奇寶貝 初代御三家
(3, 1, '噴火龍 極巨化', '極巨化噴火龍', '/images/blindbox/pokemon/charizard_gmax.jpg', 1500, 'SECRET', 'AVAILABLE'),
(3, 2, '皮卡丘 肥肥版', '復古肥皮卡丘', '/images/blindbox/pokemon/pikachu_fat.jpg', 800, 'ULTRA_RARE', 'AVAILABLE'),
(3, 3, '水箭龜 極巨化', '極巨化水箭龜', '/images/blindbox/pokemon/blastoise_gmax.jpg', 700, 'ULTRA_RARE', 'AVAILABLE'),
(3, 4, '妙蛙花 極巨化', '極巨化妙蛙花', '/images/blindbox/pokemon/venusaur_gmax.jpg', 700, 'ULTRA_RARE', 'AVAILABLE'),
(3, 5, '小火龍', '可愛的火系御三家', '/images/blindbox/pokemon/charmander.jpg', 400, 'RARE', 'AVAILABLE'),
(3, 6, '傑尼龜', '可愛的水系御三家', '/images/blindbox/pokemon/squirtle.jpg', 400, 'RARE', 'AVAILABLE'),
(3, 7, '妙蛙種子', '可愛的草系御三家', '/images/blindbox/pokemon/bulbasaur.jpg', 400, 'RARE', 'AVAILABLE'),
(3, 8, '伊布', '多種進化可能', '/images/blindbox/pokemon/eevee.jpg', 350, 'NORMAL', 'AVAILABLE'),
(3, 9, '喵喵', '火箭隊的喵喵', '/images/blindbox/pokemon/meowth.jpg', 350, 'NORMAL', 'AVAILABLE'),
(3, 10, '卡比獸', '愛睡覺的卡比獸', '/images/blindbox/pokemon/snorlax.jpg', 350, 'NORMAL', 'AVAILABLE'),
(3, 11, '超夢', '傳說中的基因寶可夢', '/images/blindbox/pokemon/mewtwo.jpg', 600, 'ULTRA_RARE', 'AVAILABLE'),
(3, 12, '夢幻', '稀有的幻之寶可夢', '/images/blindbox/pokemon/mew.jpg', 500, 'RARE', 'AVAILABLE'),

-- Box 4: 鏈鋸人 惡魔篇
(4, 1, '電次 完全變身', '完全鏈鋸人型態', '/images/blindbox/csm/denji_full.jpg', 1500, 'SECRET', 'AVAILABLE'),
(4, 2, '瑪奇瑪 支配', '支配惡魔姿態', '/images/blindbox/csm/makima.jpg', 800, 'ULTRA_RARE', 'AVAILABLE'),
(4, 3, '帕瓦 血之惡魔', '血之惡魔能力發動', '/images/blindbox/csm/power.jpg', 700, 'ULTRA_RARE', 'AVAILABLE'),
(4, 4, '早川秋 狐狸惡魔', '狐狸惡魔契約者', '/images/blindbox/csm/aki.jpg', 500, 'RARE', 'AVAILABLE'),
(4, 5, '電次 變身中', '變身過程中的電次', '/images/blindbox/csm/denji_transform.jpg', 500, 'RARE', 'AVAILABLE'),
(4, 6, '蕾塞 炸彈惡魔', '炸彈惡魔少女', '/images/blindbox/csm/reze.jpg', 500, 'RARE', 'AVAILABLE'),
(4, 7, '波奇塔', '可愛的鏈鋸惡魔', '/images/blindbox/csm/pochita.jpg', 400, 'RARE', 'AVAILABLE'),
(4, 8, '姬野 幽靈惡魔', '公安惡魔獵人', '/images/blindbox/csm/himeno.jpg', 350, 'NORMAL', 'AVAILABLE'),
(4, 9, '拷問之惡魔 暴力', '暴力鬼怪', '/images/blindbox/csm/violence.jpg', 350, 'NORMAL', 'AVAILABLE'),
(4, 10, '蜘蛛惡魔', '蜘蛛惡魔姿態', '/images/blindbox/csm/spider.jpg', 350, 'NORMAL', 'AVAILABLE'),
(4, 11, '黑暗惡魔', '地獄歸來的惡魔', '/images/blindbox/csm/darkness.jpg', 600, 'ULTRA_RARE', 'AVAILABLE'),
(4, 12, '肌肉惡魔', '公安第4課成員', '/images/blindbox/csm/muscle.jpg', 350, 'NORMAL', 'AVAILABLE'),

-- Box 5: 獵人 貪婪之島
(5, 1, '奇犽 神速', '神速發動狀態', '/images/blindbox/hunter/killua_godspeed.jpg', 1500, 'SECRET', 'AVAILABLE'),
(5, 2, '小傑 猜拳', '猜拳必殺技', '/images/blindbox/hunter/gon_jajanken.jpg', 800, 'ULTRA_RARE', 'AVAILABLE'),
(5, 3, '酷拉皮卡 緋紅之眼', '絕對時間發動', '/images/blindbox/hunter/kurapika.jpg', 700, 'ULTRA_RARE', 'AVAILABLE'),
(5, 4, '雷歐力 念能力', '超音波拳', '/images/blindbox/hunter/leorio.jpg', 500, 'RARE', 'AVAILABLE'),
(5, 5, '西索 紋理驚奇', '奇術師姿態', '/images/blindbox/hunter/hisoka.jpg', 600, 'ULTRA_RARE', 'AVAILABLE'),
(5, 6, '比司吉 真實姿態', '修練師傅', '/images/blindbox/hunter/bisky.jpg', 400, 'RARE', 'AVAILABLE'),
(5, 7, '小傑 念能力覺醒', '第一次使用念', '/images/blindbox/hunter/gon_nen.jpg', 400, 'RARE', 'AVAILABLE'),
(5, 8, '奇犽 電擊', '電擊狀態', '/images/blindbox/hunter/killua_electric.jpg', 400, 'RARE', 'AVAILABLE'),
(5, 9, '彈球機制', '貪婪之島卡片', '/images/blindbox/hunter/gi_card.jpg', 350, 'NORMAL', 'AVAILABLE'),
(5, 10, '拳王 炸彈魔', '炸彈魔姿態', '/images/blindbox/hunter/bomber.jpg', 350, 'NORMAL', 'AVAILABLE'),
(5, 11, '雲古', '貪婪之島 NPC', '/images/blindbox/hunter/wing.jpg', 350, 'NORMAL', 'AVAILABLE'),
(5, 12, '揍敵客家族', '暗殺家族', '/images/blindbox/hunter/zoldyck.jpg', 350, 'NORMAL', 'AVAILABLE'),

-- Box 6: 七龍珠 賽亞人
(6, 1, '孫悟空 自在極意功', '銀髮完美形態', '/images/blindbox/db/goku_ui.jpg', 1500, 'SECRET', 'AVAILABLE'),
(6, 2, '貝吉塔 自我極意', '破壞神力量', '/images/blindbox/db/vegeta_ue.jpg', 800, 'ULTRA_RARE', 'AVAILABLE'),
(6, 3, '悟吉塔 超級賽亞人藍', '合體戰士', '/images/blindbox/db/gogeta_blue.jpg', 700, 'ULTRA_RARE', 'AVAILABLE'),
(6, 4, '悟飯 終極形態', '潛力全開', '/images/blindbox/db/gohan_ultimate.jpg', 500, 'RARE', 'AVAILABLE'),
(6, 5, '布羅利 傳說超級賽亞人', '狂暴形態', '/images/blindbox/db/broly_lss.jpg', 600, 'ULTRA_RARE', 'AVAILABLE'),
(6, 6, '特南克斯 超級賽亞人', '未來戰士', '/images/blindbox/db/trunks_ss.jpg', 400, 'RARE', 'AVAILABLE'),
(6, 7, '悟天 超級賽亞人', '小悟空的兒子', '/images/blindbox/db/goten_ss.jpg', 400, 'RARE', 'AVAILABLE'),
(6, 8, '克林 氣圓斬', '地球人最強', '/images/blindbox/db/krillin.jpg', 350, 'NORMAL', 'AVAILABLE'),
(6, 9, '比克 魔貫光殺砲', '那美克星人', '/images/blindbox/db/piccolo.jpg', 400, 'RARE', 'AVAILABLE'),
(6, 10, '弗利沙 黃金形態', '宇宙帝王', '/images/blindbox/db/frieza_gold.jpg', 500, 'RARE', 'AVAILABLE'),
(6, 11, '貝羅斯 破壞神', '第7宇宙破壞神', '/images/blindbox/db/beerus.jpg', 500, 'RARE', 'AVAILABLE'),
(6, 12, '全王', '全宇宙之王', '/images/blindbox/db/zeno.jpg', 350, 'NORMAL', 'AVAILABLE');

-- ########## 12.5 扭蛋主題 (Gacha Themes) ##########
INSERT INTO gacha_themes (id, ip_id, name, description, image_url, price_per_gacha, status, created_at) VALUES
(1, 1, '洛克人 元祖扭蛋', '經典機器人角色合集', '/images/gacha/rockman.jpg', 150, 'ACTIVE', NOW()),
(2, 3, '七龍珠 戰鬥扭蛋', '賽亞人戰士系列', '/images/gacha/dragonball.jpg', 180, 'ACTIVE', NOW()),
(3, 7, '神奇寶貝 精靈扭蛋', '初代寶可夢收藏', '/images/gacha/pokemon.jpg', 120, 'ACTIVE', NOW()),
(4, 12, '鬼滅之刃 鬼殺隊扭蛋', '呼吸法使者們', '/images/gacha/kimetsu.jpg', 180, 'ACTIVE', NOW()),
(5, 19, 'ONE PIECE 草帽扭蛋', '草帽一伙收藏', '/images/gacha/onepiece.jpg', 200, 'ACTIVE', NOW()),
(6, 8, '鏈鋸人 惡魔扭蛋', '電次與夥伴們', '/images/gacha/chainsawman.jpg', 180, 'ACTIVE', NOW()),
(7, 11, '灌籃高手 球員扭蛋', '湘北籃球隊', '/images/gacha/slamdunk.jpg', 150, 'ACTIVE', NOW()),
(8, 2, '獵人 念能力者扭蛋', '獵人協會成員', '/images/gacha/hunter.jpg', 180, 'ACTIVE', NOW()),
(9, 20, 'JoJo 替身扭蛋', '黃金之風篇', '/images/gacha/jojo.jpg', 200, 'ACTIVE', NOW()),
(10, 9, 'BLEACH 死神扭蛋', '護廷十三隊', '/images/gacha/bleach.jpg', 180, 'ACTIVE', NOW());

-- ########## 13. 兌換商店階梯級商品 (ID 1-5) ##########
INSERT INTO redeem_shop_items (id, name, description, image_url, shard_cost, estimated_value, stock, total_stock, item_type, status, sort_order, created_at) VALUES 
(1, '【神話級】1:4 超大魯夫 尼卡形態公仔', '全球限量 5 件，附防偽證書', '/images/redeem/nika.jpg', 50000, 25000, 5, 5, 'S_RANK', 'ACTIVE', 1, NOW()),
(2, '【傳說級】PG 級 獨角獸鋼彈 最終決戰版', '精細組裝模型，包含全金屬套件', '/images/redeem/unicorn.jpg', 35000, 15000, 10, 10, 'A_RANK', 'ACTIVE', 2, NOW()),
(3, '【史詩級】三幻神 金屬卡片套裝', '高質感純金屬材質，情懷滿分', '/images/redeem/godcards.jpg', 20000, 8000, 20, 20, 'B_RANK', 'ACTIVE', 3, NOW()),
(4, '【稀有級】精靈寶可夢 大師球級 抱枕', '軟綿質感，巨大的 80cm 尺寸', '/images/redeem/ball.jpg', 8000, 2500, 50, 50, 'C_RANK', 'ACTIVE', 4, NOW()),
(5, '【普通級】全 IP 角色 隨機色紙一張', '入門級收藏，有機率抽中限量版', '/images/redeem/paper.jpg', 500, 150, 1000, 1000, 'D_RANK', 'ACTIVE', 5, NOW());

-- ########## 14. 活動初始化 ##########
INSERT INTO activities (id, name, title, description, type, expiry_date, active, created_at) VALUES
(1, '開站活動', '開站活動', '慶祝開站活動，所有商品9折起', 'SALE', '2027-12-31 23:59:59', TRUE, NOW()),
(2, '首抽活動', '首抽活動', '完成第一次抽獎，有機率獲得神秘禮物', 'EVENT', '2027-12-31 23:59:59', TRUE, NOW()),
(3, '儲值活動', '儲值活動', '儲值金超過1萬，即可獲得神秘獎品', 'PROMOTION', '2027-12-31 23:59:59', TRUE, NOW()),
(4, '首購活動', '首購活動', '首次購買商品即可隨機抽取折扣，最高購物車內商品打五折', 'DISCOUNT', '2027-12-31 23:59:59', TRUE, NOW());

-- ########## 15. 分類與子分類 ##########
INSERT INTO categories (id, name, description) VALUES
(1, '鋼彈系列', '機動戰士鋼彈相關商品'),
(2, '任天堂系列', '任天堂遊戲相關商品'),
(3, 'Capcom系列', 'Capcom 遊戲相關商品'),
(4, '海賊王系列', 'ONE PIECE 海賊王相關商品'),
(5, '鬼滅之刃系列', '鬼滅之刃相關商品'),
(6, '咒術迴戰系列', '咒術迴戰相關商品'),
(7, '我的英雄學院系列', '我的英雄學院相關商品'),
(8, '間諜家家酒系列', 'SPY×FAMILY 相關商品');

INSERT INTO sub_categories (id, category_id, name, created_at) VALUES
-- 鋼彈系列
(1, 1, '鋼彈W', NOW()), (2, 1, '鋼彈G武鬥', NOW()), (3, 1, '鋼彈Seed', NOW()),
(4, 1, '無敵鐵金剛', NOW()), (5, 1, '鋼彈(夏亞逆襲)', NOW()), (6, 1, '鋼彈X', NOW()),
(7, 1, '鋼蛋Z', NOW()), (8, 1, '鋼彈ZZ', NOW()), (9, 1, '鋼彈UC', NOW()),
-- 任天堂系列
(10, 2, '超級瑪莉', NOW()), (11, 2, '神奇寶貝', NOW()),
-- Capcom系列
(12, 3, '元祖洛克人', NOW()), (13, 3, '洛克人X', NOW()),
-- 海賊王系列
(14, 4, '草帽一伙', NOW()), (15, 4, '和之國篇', NOW()), (16, 4, '紅髮歌姬', NOW()),
-- 鬼滅之刃系列
(17, 5, '竈門炭治郎立志篇', NOW()), (18, 5, '無限列車篇', NOW()), (19, 5, '遊郭篇', NOW()),
-- 咒術迴戰系列
(20, 6, '特級咒物', NOW()), (21, 6, '懷玉·玉折', NOW()), (22, 6, '澀谷事變', NOW()),
-- 我的英雄學院系列
(23, 7, '雄英高中', NOW()), (24, 7, '職業英雄', NOW()), (25, 7, '死穢八齋會', NOW()),
-- 間諜家家酒系列
(26, 8, '佛傑家族', NOW()), (27, 8, '伊甸學園', NOW()), (28, 8, '秘密任務', NOW());

-- ########## 17. 管理員權限 ##########
INSERT INTO admin_permissions (id, code, name, description) VALUES
(1, 'DASHBOARD_VIEW', '查看儀錶板', '可查看後台儀錶板'),
(2, 'GACHA_MANAGE', '管理扭蛋與一番賞機台', '可管理所有抽獎系統'),
(3, 'PRODUCT_MANAGE', '管理商品與分類', '可管理商品與分類'),
(4, 'MEMBER_MANAGE', '管理會員資料與餘額', '可管理會員相關資料'),
(5, 'FINANCE_MANAGE', '財務與訂單審核', '可處理財務與訂單'),
(6, 'ADMIN_MANAGE', '系統管理員與權限設定', '可管理管理員帳號'),
(7, 'SYSTEM_SETTING', '系統參數設定', '可設定系統參數');

-- ########## 18. 管理員角色 ##########
INSERT INTO admin_roles (id, name, description) VALUES
(1, '超級管理員', '擁有所有權限的管理員');

-- 超級管理員擁有所有權限
INSERT INTO admin_role_permissions (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7);

-- ########## 19. 序列重置 (TiDB 語法) ##########
ALTER TABLE gacha_ips AUTO_INCREMENT = 100;
ALTER TABLE ichiban_boxes AUTO_INCREMENT = 100;
ALTER TABLE ichiban_prizes AUTO_INCREMENT = 1000;
ALTER TABLE ichiban_slots AUTO_INCREMENT = 5000;
ALTER TABLE roulette_games AUTO_INCREMENT = 100;
ALTER TABLE roulette_slots AUTO_INCREMENT = 1000;
ALTER TABLE bingo_games AUTO_INCREMENT = 100;
ALTER TABLE bingo_cells AUTO_INCREMENT = 1000;
ALTER TABLE blind_boxes AUTO_INCREMENT = 100;
ALTER TABLE blind_box_items AUTO_INCREMENT = 1000;
ALTER TABLE redeem_shop_items AUTO_INCREMENT = 100;
ALTER TABLE member_levels AUTO_INCREMENT = 100;
ALTER TABLE activities AUTO_INCREMENT = 100;
ALTER TABLE categories AUTO_INCREMENT = 100;
ALTER TABLE sub_categories AUTO_INCREMENT = 100;
ALTER TABLE gacha_themes AUTO_INCREMENT = 100;
ALTER TABLE admin_permissions AUTO_INCREMENT = 100;
ALTER TABLE admin_roles AUTO_INCREMENT = 100;
