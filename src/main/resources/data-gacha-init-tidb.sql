-- =============================================================================
-- TiDB Cloud 相容版資料初始化腳本
-- 修正所有 H2 特有語法，確保與 TiDB Cloud 相容
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

-- ########## 2. 一番賞箱體 (ID 1-20) ##########
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

-- ########## 3. 一番賞獎品 - 範例獎項 ##########
INSERT INTO ichiban_prizes (id, box_id, `rank`, name, description, image_url, estimated_value, quantity, remaining_quantity, sort_order) VALUES
-- Box 1: 洛克人
(1, 1, 'A', '洛克人 X 1/12 比例可動公仔', '精細塗裝，含多種手口更換件', '/images/prize/rockman_a.jpg', 2800, 1, 1, 1),
(2, 1, 'B', '洛克人 Zero 經典紅色裝甲模型', '動漫忠實還原', '/images/prize/rockman_b.jpg', 2200, 2, 2, 2),
(3, 1, 'C', '洛克人系列 亞克力立牌', '精美透明亞克力', '/images/prize/rockman_c.jpg', 450, 5, 5, 3),
(4, 1, 'D', '洛克人 馬克杯', '經典造型', '/images/prize/rockman_d.jpg', 350, 10, 10, 4),
(5, 1, 'E', '洛克人 貼紙組', '多款設計', '/images/prize/rockman_e.jpg', 150, 20, 20, 5),
(6, 1, 'LAST', '洛克人 35週年紀念版公仔', '限定金色', '/images/prize/rockman_last.jpg', 5000, 1, 1, 6),
-- Box 12: 鬼滅之刃
(71, 12, 'A', '煉獄杏壽郎 炎之呼吸公仔', '華麗火焰效果', '/images/prize/kimetsu_a.jpg', 3000, 1, 1),
(72, 12, 'B', '炭治郎 水之呼吸模型', '動態姿態', '/images/prize/kimetsu_b.jpg', 2500, 2, 2),
(73, 12, 'C', '禰豆子 睡眠狀態公仔', '可愛造型', '/images/prize/kimetsu_c.jpg', 1800, 3, 3),
(74, 12, 'D', '柱系列 亞克力立牌全套', '9柱齊聚', '/images/prize/kimetsu_d.jpg', 800, 8, 8),
(75, 12, 'E', '鬼滅之刃 收藏色紙', '隨機角色', '/images/prize/kimetsu_e.jpg', 200, 25, 25),
(76, 12, 'LAST', '竈門兄妹 無限列車場景組', '劇場版名場面', '/images/prize/kimetsu_last.jpg', 6000, 1, 1),
-- Box 19: 航海王
(111, 19, 'A', '魯夫 尼卡形態公仔', '太陽神姿態', '/images/prize/op_a.jpg', 3500, 1, 1),
(112, 19, 'B', '索隆 三刀流模型', '閻魔版', '/images/prize/op_b.jpg', 3000, 2, 2),
(113, 19, 'C', '香吉士 魔神風腳公仔', '發光特效', '/images/prize/op_c.jpg', 2500, 3, 3),
(114, 19, 'D', '草帽團 全員亞克力立牌', '和之國版', '/images/prize/op_d.jpg', 1200, 5, 5),
(115, 19, 'E', '航海王 隨機色紙', '眾多角色', '/images/prize/op_e.jpg', 250, 30, 30),
(116, 19, 'LAST', '羅傑與白鬍子 世紀對決景品', '傳奇名場面', '/images/prize/op_last.jpg', 8000, 1, 1);

-- ########## 4. 轉盤遊戲 ##########
INSERT INTO roulette_games (id, ip_id, name, description, image_url, price_per_spin, status, guarantee_spins, created_at) VALUES 
(1, 1, '洛克人轉盤', '極速轉盤抽獎', '/images/roulette/rockman.jpg', 150, 'ACTIVE', 10, NOW()),
(2, 12, '鬼滅之刃轉盤', '鬼殺隊專屬轉盤', '/images/roulette/kimetsu.jpg', 180, 'ACTIVE', 10, NOW()),
(3, 19, '航海王轉盤', '大航海時代轉盤', '/images/roulette/onepiece.jpg', 200, 'ACTIVE', 10, NOW());

-- ########## 5. 轉盤格子 ##########
INSERT INTO roulette_slots (game_id, position, prize_name, prize_description, weight, tier) VALUES
-- 洛克人轉盤
(1, 1, '50碎片', '基礎獎勵', 30, 'NORMAL'),
(1, 2, '100碎片', '中等獎勵', 20, 'NORMAL'),
(1, 3, '洛克人徽章', '限定徽章', 10, 'RARE'),
(1, 4, '200碎片', '豐厚獎勵', 10, 'NORMAL'),
(1, 5, '20碎片', '小獎', 35, 'NORMAL'),
(1, 6, '洛克人貼紙', '稀有貼紙', 15, 'RARE'),
(1, 7, '500碎片', '大獎', 5, 'EPIC'),
(1, 8, '洛克人迷你公仔', '超級大獎', 2, 'LEGENDARY'),
-- 鬼滅之刃轉盤
(2, 1, '80碎片', '基礎獎勵', 30, 'NORMAL'),
(2, 2, '150碎片', '中等獎勵', 20, 'NORMAL'),
(2, 3, '鬼滅徽章', '限定徽章', 10, 'RARE'),
(2, 4, '300碎片', '豐厚獎勵', 10, 'NORMAL'),
(2, 5, '30碎片', '小獎', 35, 'NORMAL'),
(2, 6, '禰豆子貼紙', '稀有貼紙', 15, 'RARE'),
(2, 7, '700碎片', '大獎', 5, 'EPIC'),
(2, 8, '炭治郎迷你公仔', '超級大獎', 2, 'LEGENDARY');

-- ########## 6. 九宮格遊戲 ##########
INSERT INTO bingo_games (id, ip_id, name, description, image_url, price_per_dig, grid_size, status, bingo_reward_name, created_at) VALUES 
(1, 7, '神奇寶貝九宮格', '寶可夢驚喜挖寶', '/images/bingo/pokemon.jpg', 120, 3, 'ACTIVE', '皮卡丘大獎', NOW()),
(2, 12, '鬼滅之刃九宮格', '鬼殺隊探索', '/images/bingo/kimetsu.jpg', 150, 3, 'ACTIVE', '禰豆子禮盒', NOW());

-- ########## 7. 九宮格格子 ##########
INSERT INTO bingo_cells (game_id, position, row_num, col_num, prize_name, is_revealed) VALUES
-- 神奇寶貝九宮格
(1, 1, 0, 0, '30碎片', FALSE),
(1, 2, 0, 1, '50碎片', FALSE),
(1, 3, 0, 2, '皮卡丘貼紙', FALSE),
(1, 4, 1, 0, '20碎片', FALSE),
(1, 5, 1, 1, '100碎片', FALSE),
(1, 6, 1, 2, '伊布徽章', FALSE),
(1, 7, 2, 0, '40碎片', FALSE),
(1, 8, 2, 1, '精靈球掛飾', FALSE),
(1, 9, 2, 2, '200碎片', FALSE),
-- 鬼滅九宮格
(2, 1, 0, 0, '50碎片', FALSE),
(2, 2, 0, 1, '80碎片', FALSE),
(2, 3, 0, 2, '炭治郎貼紙', FALSE),
(2, 4, 1, 0, '30碎片', FALSE),
(2, 5, 1, 1, '150碎片', FALSE),
(2, 6, 1, 2, '禰豆子徽章', FALSE),
(2, 7, 2, 0, '60碎片', FALSE),
(2, 8, 2, 1, '柱系列色紙', FALSE),
(2, 9, 2, 2, '300碎片', FALSE);

-- ########## 8. 兌換商店商品 ##########
INSERT INTO redeem_shop_items (id, name, description, image_url, point_cost, stock, item_type, status, created_at) VALUES 
(1, '【神話級】1:4 超大魯夫 尼卡形態公仔', '全球限量 5 件，附防偽證書', '/images/redeem/nika.jpg', 50000, 5, 'FIGURE', 'ACTIVE', NOW()),
(2, '【傳說級】PG 級 獨角獸鋼彈 最終決戰版', '精細組裝模型，包含全金屬套件', '/images/redeem/unicorn.jpg', 35000, 10, 'GUNPLA', 'ACTIVE', NOW()),
(3, '【史詩級】三幻神 金屬卡片套裝', '高質感純金屬材質，情懷滿分', '/images/redeem/godcards.jpg', 20000, 20, 'CARD', 'ACTIVE', NOW()),
(4, '【稀有級】精靈寶可夢 大師球級 抱枕', '軟綿質感，巨大的 80cm 尺寸', '/images/redeem/ball.jpg', 8000, 50, 'PILLOW', 'ACTIVE', NOW()),
(5, '【普通級】全 IP 角色 隨機色紙一張', '入門級收藏，有機率抽中限量版', '/images/redeem/paper.jpg', 500, 1000, 'PAPER', 'ACTIVE', NOW());

-- ########## 9. 重置自增序列（TiDB 語法）##########
ALTER TABLE gacha_ips AUTO_INCREMENT = 100;
ALTER TABLE ichiban_boxes AUTO_INCREMENT = 100;
ALTER TABLE ichiban_prizes AUTO_INCREMENT = 1000;
ALTER TABLE roulette_games AUTO_INCREMENT = 100;
ALTER TABLE bingo_games AUTO_INCREMENT = 100;
ALTER TABLE redeem_shop_items AUTO_INCREMENT = 100;
