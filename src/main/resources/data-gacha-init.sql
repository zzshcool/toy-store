-- =============================================================================
-- 【資深後端工程師：最終重構 - 顯式 ID 與 確定性資料種子】
-- 1. 確定性 (Determinism)：硬編碼 Primary Key ID，確保開發環境數據完全可預測且易於除錯。
-- 2. 邏輯耦合：依賴顯式 ID 進行父子關聯，徹底解決自增 ID 偏移與函式相容性問題。
-- 3. 工程實務：符合「不要強制依賴物理外鍵自增」的手法，方便數據跨環境遷移。
-- 4. 模組化結構：依 IP 系列分組展示資料。
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

-- ########## 1. IP 主題 (ID 1-20) ##########
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

-- ########## 2. 一番賞箱體 (ID 1-20，與 IP ID 對應) ##########
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

-- ########## 3. 一番賞獎品 (ID 顯式定址: 10 * (box_id-1) + offset) ##########
-- 為熱門 IP 提供特化獎項名稱
INSERT INTO ichiban_prizes (id, box_id, rank, name, description, image_url, estimated_value, total_quantity, remaining_quantity, sort_order) VALUES
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

-- 其餘 IP 使用迴圈生成高品質通用名稱 (略)
-- 為節省空間，其餘獎項將根據 IP 名稱 + 特化字尾生成
(11, 2, 'A', '獵人 奇犽 神速形態限定模型', '雷光特效環繞', '/images/prize/hunter_a.jpg', 2600, 1, 1, 1),
(21, 3, 'A', '七龍珠 孫悟空 自在極意功 雕塑', '銀髮閃耀，氣場爆裂', '/images/prize/db_a.jpg', 3200, 1, 1, 1),
(111, 12, 'A', '鬼滅之刃 煉獄杏壽郎 炎之呼吸公仔', '大哥沒有輸！華麗火焰效果', '/images/prize/kimetsu_a.jpg', 3000, 1, 1, 1);

-- 補全其餘 150+ 獎項 (使用更具商業感的生成方式)
INSERT INTO ichiban_prizes (id, box_id, rank, name, description, image_url, estimated_value, total_quantity, remaining_quantity, sort_order)
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
-- 避免與上面手動插入的 ID 衝突
WHERE NOT EXISTS (SELECT 1 FROM ichiban_prizes p2 WHERE p2.id = (b.id - 1) * 10 + r.idx);

-- ########## 4. 一番賞格子 (80 格) ##########
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
CROSS JOIN SYSTEM_RANGE(1, 80) s;

-- ########## 5. 轉盤與九宮格特化獎項 ##########
INSERT INTO roulette_games (id, ip_id, name, description, image_url, price_per_spin, max_slots, total_slots, total_draws, status, created_at)
SELECT id, id, CONCAT(name, ' 極速轉盤'), '最高有機率獲得 1000 碎片！', '/images/roulette/generic.jpg', 150, 25, 8, 0, 'ACTIVE', NOW() FROM gacha_ips;

INSERT INTO roulette_slots (game_id, slot_order, slot_type, prize_name, prize_description, weight, shard_amount, color)
SELECT g.id, s.idx, 
    CASE WHEN s.idx = 1 THEN 'RARE' ELSE 'NORMAL' END,
    CASE WHEN s.idx = 1 THEN CONCAT(i.name, ' 官方限量徽章') ELSE '10 碎片' END,
    '精美小物', 
    CASE WHEN s.idx = 1 THEN 5 ELSE 30 END,
    CASE WHEN s.idx = 1 THEN 100 ELSE 10 END,
    CASE WHEN s.idx = 1 THEN '#FFD700' ELSE '#4ECDC4' END
FROM roulette_games g
JOIN gacha_ips i ON i.id = g.ip_id
CROSS JOIN (SELECT 1 as idx UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8) s;

INSERT INTO bingo_games (id, ip_id, name, description, image_url, price_per_dig, grid_size, status, bingo_reward_name, bingo_reward_image_url, bingo_reward_value, created_at)
SELECT id, id, CONCAT(name, ' 驚喜九宮格'), '挖開格子，收集相同角色連線！', '/images/bingo/generic.jpg', 120, 3, 'ACTIVE', '聖杯級神祕大禮包', '/images/reward/bingo.jpg', 1200, NOW() FROM gacha_ips;

INSERT INTO bingo_cells (game_id, position, row_num, col_num, prize_name, prize_description, is_revealed)
SELECT g.id, s.x, (s.x-1)/3, (s.x-1)%3, '隱藏碎片', '20 碎片', FALSE FROM bingo_games g CROSS JOIN SYSTEM_RANGE(1, 9) s;

-- ########## 6. 兌換商店階梯級商品 (ID 1-10) ##########
INSERT INTO redeem_shop_items (id, name, description, image_url, shard_cost, estimated_value, stock, total_stock, item_type, status, sort_order, created_at) VALUES 
(1, '【神話級】1:4 超大魯夫 尼卡形態公仔', '全球限量 5 件，附防偽證書', '/images/redeem/nika.jpg', 50000, 25000, 5, 5, 'S_RANK', 'ACTIVE', 1, NOW()),
(2, '【傳說級】PG 級 獨角獸鋼彈 最終決戰版', '精細組裝模型，包含全金屬套件', '/images/redeem/unicorn.jpg', 35000, 15000, 10, 10, 'A_RANK', 'ACTIVE', 2, NOW()),
(3, '【史詩級】三幻神 金屬卡片套裝', '高質感純金屬材質，情懷滿分', '/images/redeem/godcards.jpg', 20000, 8000, 20, 20, 'B_RANK', 'ACTIVE', 3, NOW()),
(4, '【稀有級】精靈寶可夢 大師球級 抱枕', '軟綿質感，巨大的 80cm 尺寸', '/images/redeem/ball.jpg', 8000, 2500, 50, 50, 'C_RANK', 'ACTIVE', 4, NOW()),
(5, '【普通級】全 IP 角色 隨機色紙一張', '入門級收藏，有機率抽中限量版', '/images/redeem/paper.jpg', 500, 150, 1000, 1000, 'D_RANK', 'ACTIVE', 5, NOW());

-- ########## 7. 序列重置 ##########
ALTER TABLE gacha_ips ALTER COLUMN id RESTART WITH 100;
ALTER TABLE ichiban_boxes ALTER COLUMN id RESTART WITH 100;
ALTER TABLE ichiban_prizes ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE ichiban_slots ALTER COLUMN id RESTART WITH 5000;
ALTER TABLE roulette_games ALTER COLUMN id RESTART WITH 100;
ALTER TABLE roulette_slots ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE bingo_games ALTER COLUMN id RESTART WITH 100;
ALTER TABLE bingo_cells ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE redeem_shop_items ALTER COLUMN id RESTART WITH 100;
