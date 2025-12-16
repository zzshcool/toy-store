-- =====================================================
-- 抽獎系統初始化測試資料
-- 六大 IP：洛克人、獵人、七龍珠、進擊的巨人、鋼之鍊金術師、天竺鼠車車
-- =====================================================

-- 清除舊資料（僅開發環境使用）
-- DELETE FROM ichiban_slots;
-- DELETE FROM ichiban_prizes;
-- DELETE FROM ichiban_boxes;
-- DELETE FROM roulette_slots;
-- DELETE FROM roulette_games;
-- DELETE FROM bingo_cells;
-- DELETE FROM bingo_games;
-- DELETE FROM gacha_ips;

-- =====================================================
-- 1. IP 主題
-- =====================================================
INSERT INTO gacha_ips (name, description, image_url, status, created_at) VALUES 
('洛克人', 'Mega Man 經典遊戲系列，藍色機器人英雄的冒險故事', '/images/ip/rockman.jpg', 'ACTIVE', NOW()),
('獵人', 'Hunter × Hunter 獵人執照的考驗之旅', '/images/ip/hunter.jpg', 'ACTIVE', NOW()),
('七龍珠', 'Dragon Ball 集齊七顆龍珠實現願望', '/images/ip/dragonball.jpg', 'ACTIVE', NOW()),
('進擊的巨人', 'Attack on Titan 人類與巨人的生存戰鬥', '/images/ip/aot.jpg', 'ACTIVE', NOW()),
('鋼之鍊金術師', 'Fullmetal Alchemist 等價交換的煉金術世界', '/images/ip/fma.jpg', 'ACTIVE', NOW()),
('天竺鼠車車', 'PUI PUI Molcar 可愛的毛茸茸車車冒險', '/images/ip/molcar.jpg', 'ACTIVE', NOW()),
('神奇寶貝', 'Pokémon 與寶可夢一起成為最強訓練家', '/images/ip/pokemon.jpg', 'ACTIVE', NOW()),
('鏈鋸人', 'Chainsaw Man 電次與惡魔的血腥戰鬥', '/images/ip/chainsawman.jpg', 'ACTIVE', NOW()),
('BLEACH死神', 'BLEACH 黑崎一護與死神代理的覺醒', '/images/ip/bleach.jpg', 'ACTIVE', NOW()),
('美少女戰士', 'Sailor Moon 月野兔與水手服戰士的正義', '/images/ip/sailormoon.jpg', 'ACTIVE', NOW()),
('灌籃高手', 'SLAM DUNK 櫻木花道的籃球夢想', '/images/ip/slamdunk.jpg', 'ACTIVE', NOW()),
('鬼滅之刃', 'Demon Slayer 炭治郎與禰豆子的復仇之旅', '/images/ip/kimetsu.jpg', 'ACTIVE', NOW()),
('超人力霸王', 'Ultraman 光之巨人守護地球的使命', '/images/ip/ultraman.jpg', 'ACTIVE', NOW()),
('櫻桃小丸子', 'Chibi Maruko-chan 小丸子的日常生活故事', '/images/ip/maruko.jpg', 'ACTIVE', NOW()),
('死亡筆記本', 'Death Note 夜神月與L的智慧對決', '/images/ip/deathnote.jpg', 'ACTIVE', NOW()),
('銀魂', 'Gintama 坂田銀時的萬事屋搞笑日常', '/images/ip/gintama.jpg', 'ACTIVE', NOW()),
('新網球王子', 'The Prince of Tennis 越前龍馬的網球之路', '/images/ip/tenipuri.jpg', 'ACTIVE', NOW()),
('庫洛魔法使', 'Cardcaptor Sakura 小櫻收集庫洛牌的冒險', '/images/ip/cardcaptor.jpg', 'ACTIVE', NOW()),
('ONE PIECE航海王', 'ONE PIECE 魯夫與夥伴們的海賊王之夢', '/images/ip/onepiece.jpg', 'ACTIVE', NOW()),
('JoJo的奇妙冒險', 'JoJo''s Bizarre Adventure 喬斯達家族的替身之戰', '/images/ip/jojo.jpg', 'ACTIVE', NOW());

-- =====================================================
-- 2. 一番賞箱體 (每個 IP 一個)
-- =====================================================
INSERT INTO ichiban_boxes (ip_id, name, description, image_url, price_per_draw, max_slots, total_slots, status, created_at) VALUES 
(1, '洛克人一番賞 Vol.1', '經典藍色轟炸機系列首發！限量公仔與周邊商品', '/images/ichiban/rockman1.jpg', 680, 80, 20, 'ACTIVE', NOW()),
(2, '獵人一番賞 貪婪之島篇', '小傑與奇犽的冒險，稀有念能力系周邊', '/images/ichiban/hunter1.jpg', 750, 80, 25, 'ACTIVE', NOW()),
(3, '七龍珠一番賞 賽亞人篇', '悟空變身超級賽亞人經典場景復刻', '/images/ichiban/db1.jpg', 800, 80, 20, 'ACTIVE', NOW()),
(4, '進擊的巨人一番賞 最終季', '艾連與調查兵團最終決戰紀念', '/images/ichiban/aot1.jpg', 720, 80, 20, 'ACTIVE', NOW()),
(5, '鋼之鍊金術師一番賞', '艾力克兄弟的等價交換之旅', '/images/ichiban/fma1.jpg', 700, 80, 20, 'ACTIVE', NOW()),
(6, '天竺鼠車車一番賞 PUI PUI篇', '毛茸茸的車車大集合', '/images/ichiban/molcar1.jpg', 580, 80, 16, 'ACTIVE', NOW());

-- =====================================================
-- 3. 一番賞獎品 (洛克人 - ID=1)
-- =====================================================
INSERT INTO ichiban_prizes (box_id, rank, name, description, image_url, estimated_value, total_quantity, remaining_quantity, sort_order) VALUES 
(1, 'A', '洛克人 1/6 限量公仔', '限量精緻可動公仔，高度約25cm', '/images/prize/rm_a.jpg', 2500, 1, 1, 1),
(1, 'B', '萊特博士 Q版公仔', '可愛Q版造型，附研究室配件', '/images/prize/rm_b.jpg', 1200, 2, 2, 2),
(1, 'C', '洛克人 壓克力立牌組', '經典造型立牌三入組', '/images/prize/rm_c.jpg', 500, 3, 3, 3),
(1, 'D', '洛克人 馬克杯', 'E罐造型馬克杯', '/images/prize/rm_d.jpg', 300, 4, 4, 4),
(1, 'E', '洛克人 資料夾組', '文具周邊五件組', '/images/prize/rm_e.jpg', 150, 8, 8, 5),
(1, 'LAST', '洛克人 簽名板', '藤原得郎老師親筆簽名板', '/images/prize/rm_last.jpg', 3000, 1, 1, 99);

-- 獵人獎品 (ID=2)
INSERT INTO ichiban_prizes (box_id, rank, name, description, image_url, estimated_value, total_quantity, remaining_quantity, sort_order) VALUES 
(2, 'A', '小傑 念能力發動公仔', 'じゃじゃん拳場景重現', '/images/prize/hxh_a.jpg', 2800, 1, 1, 1),
(2, 'B', '奇犽 電擊造型公仔', '電光mode造型', '/images/prize/hxh_b.jpg', 1500, 2, 2, 2),
(2, 'C', '獵人執照 複製品', '1:1精緻複製品', '/images/prize/hxh_c.jpg', 800, 4, 4, 3),
(2, 'D', '揍敵客家族 掛畫', 'A3尺寸精美掛畫', '/images/prize/hxh_d.jpg', 400, 6, 6, 4),
(2, 'E', '貪婪之島卡片組', '稀有卡片10張組', '/images/prize/hxh_e.jpg', 200, 10, 10, 5),
(2, 'LAST', '團長 西索對決場景', '限定版雙人組公仔', '/images/prize/hxh_last.jpg', 4000, 1, 1, 99);

-- 七龍珠獎品 (ID=3)
INSERT INTO ichiban_prizes (box_id, rank, name, description, image_url, estimated_value, total_quantity, remaining_quantity, sort_order) VALUES 
(3, 'A', '悟空 超級賽亞人公仔', '經典變身場景', '/images/prize/db_a.jpg', 3000, 1, 1, 1),
(3, 'B', '達爾 王子公仔', '驕傲的賽亞人王子', '/images/prize/db_b.jpg', 1600, 2, 2, 2),
(3, 'C', '七顆龍珠 水晶球組', '透明水晶材質', '/images/prize/db_c.jpg', 1000, 3, 3, 3),
(3, 'D', '筋斗雲 造型燈', 'LED夜燈', '/images/prize/db_d.jpg', 500, 5, 5, 4),
(3, 'E', '龜仙人 周邊組', '墨鏡+龜仙流道服', '/images/prize/db_e.jpg', 250, 7, 7, 5),
(3, 'LAST', '神龍 限定公仔', '可發光神龍', '/images/prize/db_last.jpg', 5000, 1, 1, 99);

-- 進擊的巨人獎品 (ID=4)
INSERT INTO ichiban_prizes (box_id, rank, name, description, image_url, estimated_value, total_quantity, remaining_quantity, sort_order) VALUES 
(4, 'A', '艾連 始祖巨人公仔', '最終決戰造型', '/images/prize/aot_a.jpg', 2800, 1, 1, 1),
(4, 'B', '兵長 立體機動裝置', '1/6比例複製品', '/images/prize/aot_b.jpg', 1500, 2, 2, 2),
(4, 'C', '調查兵團披風', '可穿戴複製品', '/images/prize/aot_c.jpg', 800, 3, 3, 3),
(4, 'D', '三笠 壓克力掛畫', 'A2尺寸', '/images/prize/aot_d.jpg', 400, 5, 5, 4),
(4, 'E', '超大型巨人 徽章組', '精密金屬徽章', '/images/prize/aot_e.jpg', 200, 7, 7, 5),
(4, 'LAST', '始祖尤彌爾 限定公仔', '限量版', '/images/prize/aot_last.jpg', 4500, 1, 1, 99);

-- 鋼之鍊金術師獎品 (ID=5)
INSERT INTO ichiban_prizes (box_id, rank, name, description, image_url, estimated_value, total_quantity, remaining_quantity, sort_order) VALUES 
(5, 'A', '愛德華 鋼鍊手臂公仔', '可動機械手臂', '/images/prize/fma_a.jpg', 2600, 1, 1, 1),
(5, 'B', '阿爾馮斯 鎧甲公仔', '可開蓋', '/images/prize/fma_b.jpg', 1400, 2, 2, 2),
(5, 'C', '國家鍊金術師銀懷錶', '精密複製品', '/images/prize/fma_c.jpg', 900, 3, 3, 3),
(5, 'D', '鍊成陣 地毯', '圓形地毯', '/images/prize/fma_d.jpg', 500, 5, 5, 4),
(5, 'E', '火焰鍊金術士 周邊組', '馬斯坦主題周邊', '/images/prize/fma_e.jpg', 250, 7, 7, 5),
(5, 'LAST', '賢者之石 LED燈', '發光特效', '/images/prize/fma_last.jpg', 3500, 1, 1, 99);

-- 天竺鼠車車獎品 (ID=6)
INSERT INTO ichiban_prizes (box_id, rank, name, description, image_url, estimated_value, total_quantity, remaining_quantity, sort_order) VALUES 
(6, 'A', '馬鈴薯 大型絨毛娃娃', '40cm超大尺寸', '/images/prize/mc_a.jpg', 1800, 1, 1, 1),
(6, 'B', '西羅摩 絨毛娃娃', '30cm中型', '/images/prize/mc_b.jpg', 1000, 2, 2, 2),
(6, 'C', '車車 造型抱枕', '超柔軟材質', '/images/prize/mc_c.jpg', 500, 3, 3, 3),
(6, 'D', '車車 馬克杯組', '三入陶瓷杯', '/images/prize/mc_d.jpg', 350, 4, 4, 4),
(6, 'E', '車車 貼紙組', '50張精美貼紙', '/images/prize/mc_e.jpg', 150, 5, 5, 5),
(6, 'LAST', '車車樂園 場景組', '可停車場景', '/images/prize/mc_last.jpg', 2500, 1, 1, 99);

-- =====================================================
-- 3.5 一番賞格子 (根據獎品數量建立)
-- =====================================================
-- 洛克人箱體 (box_id=1, 共 19 格: 1+2+3+4+8+1)
INSERT INTO ichiban_slots (box_id, slot_number, prize_id, status) VALUES 
(1, 1, 1, 'AVAILABLE'), -- A賞
(1, 2, 2, 'AVAILABLE'), (1, 3, 2, 'AVAILABLE'), -- B賞 x2
(1, 4, 3, 'AVAILABLE'), (1, 5, 3, 'AVAILABLE'), (1, 6, 3, 'AVAILABLE'), -- C賞 x3
(1, 7, 4, 'AVAILABLE'), (1, 8, 4, 'AVAILABLE'), (1, 9, 4, 'AVAILABLE'), (1, 10, 4, 'AVAILABLE'), -- D賞 x4
(1, 11, 5, 'AVAILABLE'), (1, 12, 5, 'AVAILABLE'), (1, 13, 5, 'AVAILABLE'), (1, 14, 5, 'AVAILABLE'),
(1, 15, 5, 'AVAILABLE'), (1, 16, 5, 'AVAILABLE'), (1, 17, 5, 'AVAILABLE'), (1, 18, 5, 'AVAILABLE'), -- E賞 x8
(1, 19, 6, 'AVAILABLE'), -- LAST賞
(1, 20, 5, 'AVAILABLE'); -- 補充

-- 獵人箱體 (box_id=2, 共 24 格)
INSERT INTO ichiban_slots (box_id, slot_number, prize_id, status) VALUES 
(2, 1, 7, 'AVAILABLE'), -- A賞
(2, 2, 8, 'AVAILABLE'), (2, 3, 8, 'AVAILABLE'), -- B賞 x2
(2, 4, 9, 'AVAILABLE'), (2, 5, 9, 'AVAILABLE'), (2, 6, 9, 'AVAILABLE'), (2, 7, 9, 'AVAILABLE'), -- C賞 x4
(2, 8, 10, 'AVAILABLE'), (2, 9, 10, 'AVAILABLE'), (2, 10, 10, 'AVAILABLE'), (2, 11, 10, 'AVAILABLE'), (2, 12, 10, 'AVAILABLE'), (2, 13, 10, 'AVAILABLE'), -- D賞 x6
(2, 14, 11, 'AVAILABLE'), (2, 15, 11, 'AVAILABLE'), (2, 16, 11, 'AVAILABLE'), (2, 17, 11, 'AVAILABLE'), (2, 18, 11, 'AVAILABLE'),
(2, 19, 11, 'AVAILABLE'), (2, 20, 11, 'AVAILABLE'), (2, 21, 11, 'AVAILABLE'), (2, 22, 11, 'AVAILABLE'), (2, 23, 11, 'AVAILABLE'), -- E賞 x10
(2, 24, 12, 'AVAILABLE'), (2, 25, 11, 'AVAILABLE'); -- LAST賞 + 補充

-- 七龍珠箱體 (box_id=3, 共 19 格)
INSERT INTO ichiban_slots (box_id, slot_number, prize_id, status) VALUES 
(3, 1, 13, 'AVAILABLE'),
(3, 2, 14, 'AVAILABLE'), (3, 3, 14, 'AVAILABLE'),
(3, 4, 15, 'AVAILABLE'), (3, 5, 15, 'AVAILABLE'), (3, 6, 15, 'AVAILABLE'),
(3, 7, 16, 'AVAILABLE'), (3, 8, 16, 'AVAILABLE'), (3, 9, 16, 'AVAILABLE'), (3, 10, 16, 'AVAILABLE'), (3, 11, 16, 'AVAILABLE'),
(3, 12, 17, 'AVAILABLE'), (3, 13, 17, 'AVAILABLE'), (3, 14, 17, 'AVAILABLE'), (3, 15, 17, 'AVAILABLE'), (3, 16, 17, 'AVAILABLE'), (3, 17, 17, 'AVAILABLE'), (3, 18, 17, 'AVAILABLE'),
(3, 19, 18, 'AVAILABLE'), (3, 20, 17, 'AVAILABLE');

-- 進擊的巨人箱體 (box_id=4, 共 19 格)
INSERT INTO ichiban_slots (box_id, slot_number, prize_id, status) VALUES 
(4, 1, 19, 'AVAILABLE'),
(4, 2, 20, 'AVAILABLE'), (4, 3, 20, 'AVAILABLE'),
(4, 4, 21, 'AVAILABLE'), (4, 5, 21, 'AVAILABLE'), (4, 6, 21, 'AVAILABLE'),
(4, 7, 22, 'AVAILABLE'), (4, 8, 22, 'AVAILABLE'), (4, 9, 22, 'AVAILABLE'), (4, 10, 22, 'AVAILABLE'), (4, 11, 22, 'AVAILABLE'),
(4, 12, 23, 'AVAILABLE'), (4, 13, 23, 'AVAILABLE'), (4, 14, 23, 'AVAILABLE'), (4, 15, 23, 'AVAILABLE'), (4, 16, 23, 'AVAILABLE'), (4, 17, 23, 'AVAILABLE'), (4, 18, 23, 'AVAILABLE'),
(4, 19, 24, 'AVAILABLE'), (4, 20, 23, 'AVAILABLE');

-- 鋼之鍊金術師箱體 (box_id=5, 共 19 格)
INSERT INTO ichiban_slots (box_id, slot_number, prize_id, status) VALUES 
(5, 1, 25, 'AVAILABLE'),
(5, 2, 26, 'AVAILABLE'), (5, 3, 26, 'AVAILABLE'),
(5, 4, 27, 'AVAILABLE'), (5, 5, 27, 'AVAILABLE'), (5, 6, 27, 'AVAILABLE'),
(5, 7, 28, 'AVAILABLE'), (5, 8, 28, 'AVAILABLE'), (5, 9, 28, 'AVAILABLE'), (5, 10, 28, 'AVAILABLE'), (5, 11, 28, 'AVAILABLE'),
(5, 12, 29, 'AVAILABLE'), (5, 13, 29, 'AVAILABLE'), (5, 14, 29, 'AVAILABLE'), (5, 15, 29, 'AVAILABLE'), (5, 16, 29, 'AVAILABLE'), (5, 17, 29, 'AVAILABLE'), (5, 18, 29, 'AVAILABLE'),
(5, 19, 30, 'AVAILABLE'), (5, 20, 29, 'AVAILABLE');

-- 天竺鼠車車箱體 (box_id=6, 共 16 格)
INSERT INTO ichiban_slots (box_id, slot_number, prize_id, status) VALUES 
(6, 1, 31, 'AVAILABLE'),
(6, 2, 32, 'AVAILABLE'), (6, 3, 32, 'AVAILABLE'),
(6, 4, 33, 'AVAILABLE'), (6, 5, 33, 'AVAILABLE'), (6, 6, 33, 'AVAILABLE'),
(6, 7, 34, 'AVAILABLE'), (6, 8, 34, 'AVAILABLE'), (6, 9, 34, 'AVAILABLE'), (6, 10, 34, 'AVAILABLE'),
(6, 11, 35, 'AVAILABLE'), (6, 12, 35, 'AVAILABLE'), (6, 13, 35, 'AVAILABLE'), (6, 14, 35, 'AVAILABLE'), (6, 15, 35, 'AVAILABLE'),
(6, 16, 36, 'AVAILABLE');

-- =====================================================
-- 4. 轉盤遊戲 (每個 IP 一個)
-- =====================================================
INSERT INTO roulette_games (ip_id, name, description, image_url, price_per_spin, max_slots, total_slots, status, created_at) VALUES 
(1, '洛克人幸運轉盤', '轉動E罐，獲取能量！', '/images/roulette/rm.jpg', 100, 25, 8, 'ACTIVE', NOW()),
(2, '獵人念能力轉盤', '測試你的念能力系別！', '/images/roulette/hxh.jpg', 120, 25, 8, 'ACTIVE', NOW()),
(3, '七龍珠許願轉盤', '向神龍許願吧！', '/images/roulette/db.jpg', 150, 25, 10, 'ACTIVE', NOW()),
(4, '進擊巨人命運轉盤', '你是調查兵團還是...', '/images/roulette/aot.jpg', 100, 25, 8, 'ACTIVE', NOW()),
(5, '鍊金術轉盤', '等價交換的法則！', '/images/roulette/fma.jpg', 100, 25, 8, 'ACTIVE', NOW()),
(6, '車車幸運輪', 'PUI PUI 毛茸茸驚喜！', '/images/roulette/mc.jpg', 80, 25, 8, 'ACTIVE', NOW());

-- =====================================================
-- 5. 轉盤獎格 (洛克人 - game_id=1)
-- =====================================================
INSERT INTO roulette_slots (game_id, slot_order, slot_type, prize_name, prize_description, weight, shard_amount, color) VALUES 
-- 洛克人轉盤
(1, 1, 'JACKPOT', '洛克人公仔', '大獎！Q版公仔', 5, NULL, '#FF0000'),
(1, 2, 'RARE', '萊特博士周邊', '稀有周邊禮包', 8, NULL, '#FF6600'),
(1, 3, 'NORMAL', '洛克人徽章', '精美金屬徽章', 20, NULL, '#4ECDC4'),
(1, 4, 'SHARD', '100碎片', '碎片獎勵', 25, 100, '#AAAAAA'),
(1, 5, 'NORMAL', 'E罐貼紙', '經典貼紙組', 22, NULL, '#FFD93D'),
(1, 6, 'FREE_SPIN', '再來一次', '免費再轉', 10, NULL, '#00FF00'),
(1, 7, 'SHARD', '50碎片', '碎片獎勵', 30, 50, '#CCCCCC'),
(1, 8, 'NORMAL', '洛克人明信片', '精美明信片', 25, NULL, '#6C5CE7'),
-- 獵人轉盤 (game_id=2)
(2, 1, 'JACKPOT', '小傑公仔', '念能力發動版', 4, NULL, '#FF0000'),
(2, 2, 'RARE', '獵人執照複製', '限定複製品', 6, NULL, '#FF6600'),
(2, 3, 'NORMAL', '貪婪之島卡片', '稀有卡片', 18, NULL, '#4ECDC4'),
(2, 4, 'SHARD', '120碎片', '碎片獎勵', 22, 120, '#AAAAAA'),
(2, 5, 'NORMAL', '揍敵客徽章', '家族徽章', 20, NULL, '#FFD93D'),
(2, 6, 'FREE_SPIN', '再來一次', '免費再轉', 8, NULL, '#00FF00'),
(2, 7, 'SHARD', '60碎片', '碎片獎勵', 28, 60, '#CCCCCC'),
(2, 8, 'NORMAL', '幻影旅團明信片', '團員明信片', 22, NULL, '#6C5CE7'),
-- 七龍珠轉盤 (game_id=3) 10格
(3, 1, 'JACKPOT', '悟空公仔', '超級賽亞人', 3, NULL, '#FF0000'),
(3, 2, 'JACKPOT', '達爾公仔', '王子造型', 3, NULL, '#FF0000'),
(3, 3, 'RARE', '龍珠水晶球', '單顆龍珠', 8, NULL, '#FF6600'),
(3, 4, 'NORMAL', '筋斗雲小物', '造型周邊', 15, NULL, '#4ECDC4'),
(3, 5, 'SHARD', '150碎片', '碎片獎勵', 18, 150, '#AAAAAA'),
(3, 6, 'NORMAL', '龜仙人墨鏡', '造型墨鏡', 15, NULL, '#FFD93D'),
(3, 7, 'FREE_SPIN', '再來一次', '免費再轉', 8, NULL, '#00FF00'),
(3, 8, 'SHARD', '80碎片', '碎片獎勵', 22, 80, '#CCCCCC'),
(3, 9, 'NORMAL', '膠囊公司周邊', '布瑪科技', 18, NULL, '#6C5CE7'),
(3, 10, 'NORMAL', '仙豆造型糖', '恢復體力！', 20, NULL, '#27AE60'),
-- 進擊巨人轉盤 (game_id=4)
(4, 1, 'JACKPOT', '兵長公仔', '人類最強', 5, NULL, '#FF0000'),
(4, 2, 'RARE', '立體機動裝置', '迷你複製', 7, NULL, '#FF6600'),
(4, 3, 'NORMAL', '調查兵團徽章', '自由之翼', 20, NULL, '#4ECDC4'),
(4, 4, 'SHARD', '100碎片', '碎片獎勵', 24, 100, '#AAAAAA'),
(4, 5, 'NORMAL', '瑪利亞之壁模型', '迷你場景', 18, NULL, '#FFD93D'),
(4, 6, 'FREE_SPIN', '再來一次', '免費再轉', 10, NULL, '#00FF00'),
(4, 7, 'SHARD', '50碎片', '碎片獎勵', 28, 50, '#CCCCCC'),
(4, 8, 'NORMAL', '巨人明信片', '九大巨人', 22, NULL, '#6C5CE7'),
-- 鋼之鍊金術師轉盤 (game_id=5)
(5, 1, 'JACKPOT', '愛德華公仔', '鋼之鍊金術師', 5, NULL, '#FF0000'),
(5, 2, 'RARE', '國家鍊金術師懷錶', '迷你版', 8, NULL, '#FF6600'),
(5, 3, 'NORMAL', '鍊成陣杯墊', '可發動？', 20, NULL, '#4ECDC4'),
(5, 4, 'SHARD', '100碎片', '碎片獎勵', 25, 100, '#AAAAAA'),
(5, 5, 'NORMAL', '合成獸周邊', '妮娜...', 18, NULL, '#FFD93D'),
(5, 6, 'FREE_SPIN', '再來一次', '免費再轉', 10, NULL, '#00FF00'),
(5, 7, 'SHARD', '50碎片', '碎片獎勵', 30, 50, '#CCCCCC'),
(5, 8, 'NORMAL', '火焰明信片', '馬斯坦大佐', 22, NULL, '#6C5CE7'),
-- 天竺鼠車車轉盤 (game_id=6)
(6, 1, 'JACKPOT', '馬鈴薯絨毛', '大型娃娃', 6, NULL, '#FF0000'),
(6, 2, 'RARE', '西羅摩鑰匙圈', '可愛吊飾', 10, NULL, '#FF6600'),
(6, 3, 'NORMAL', '車車貼紙', '可愛貼紙', 22, NULL, '#4ECDC4'),
(6, 4, 'SHARD', '80碎片', '碎片獎勵', 25, 80, '#AAAAAA'),
(6, 5, 'NORMAL', '車車徽章', 'PUI PUI', 20, NULL, '#FFD93D'),
(6, 6, 'FREE_SPIN', '再來一次', '免費再轉', 12, NULL, '#00FF00'),
(6, 7, 'SHARD', '40碎片', '碎片獎勵', 28, 40, '#CCCCCC'),
(6, 8, 'NORMAL', '車車明信片', '全員集合', 22, NULL, '#6C5CE7');

-- =====================================================
-- 6. 九宮格遊戲 (每個 IP 一個)
-- =====================================================
INSERT INTO bingo_games (ip_id, name, description, image_url, price_per_dig, grid_size, status, bingo_reward_name, bingo_reward_image_url, bingo_reward_value, created_at) VALUES 
(1, '洛克人挖寶九宮格', '挖掘E罐找寶藏！', '/images/bingo/rm.jpg', 80, 3, 'ACTIVE', '洛克人海報套組', '/images/bingo/rm_bingo.jpg', 600, NOW()),
(2, '獵人念能力九宮格', '發掘你的念能力！', '/images/bingo/hxh.jpg', 100, 3, 'ACTIVE', '念能力系圖鑑', '/images/bingo/hxh_bingo.jpg', 800, NOW()),
(3, '七龍珠尋寶九宮格', '找出龍珠所在！', '/images/bingo/db.jpg', 120, 4, 'ACTIVE', '龍珠雷達複製品', '/images/bingo/db_bingo.jpg', 1200, NOW()),
(4, '進擊的巨人地下室', '發現真相！', '/images/bingo/aot.jpg', 100, 3, 'ACTIVE', '艾爾迪亞歷史書', '/images/bingo/aot_bingo.jpg', 900, NOW()),
(5, '鋼之鍊金術師真理之門', '等價交換！', '/images/bingo/fma.jpg', 100, 3, 'ACTIVE', '真理之門模型', '/images/bingo/fma_bingo.jpg', 1000, NOW()),
(6, '天竺鼠車車停車場', 'PUI PUI 找找看！', '/images/bingo/mc.jpg', 60, 3, 'ACTIVE', '車車停車場玩具', '/images/bingo/mc_bingo.jpg', 500, NOW());

-- =====================================================
-- 7. 九宮格格子 (洛克人 3x3 - game_id=1)
-- =====================================================
INSERT INTO bingo_cells (game_id, position, row_num, col_num, prize_name, prize_description, is_revealed) VALUES 
-- 洛克人 3x3
(1, 1, 0, 0, '洛克人貼紙', '經典貼紙', FALSE),
(1, 2, 0, 1, 'E罐能量', '回復道具', FALSE),
(1, 3, 0, 2, '洛克人徽章', '金屬徽章', FALSE),
(1, 4, 1, 0, '萊特博士卡片', '收藏卡', FALSE),
(1, 5, 1, 1, '洛克人鑰匙圈', '壓克力吊飾', FALSE),
(1, 6, 1, 2, '蓋乃博士書籤', '書籤套組', FALSE),
(1, 7, 2, 0, '萊姆馬克杯', 'Q版杯', FALSE),
(1, 8, 2, 1, '威利博士筆', '造型原子筆', FALSE),
(1, 9, 2, 2, '洛克人手機殼', '透明手機殼', FALSE),
-- 獵人 3x3 (game_id=2)
(2, 1, 0, 0, '小傑貼紙', '念能力', FALSE),
(2, 2, 0, 1, '奇犽徽章', '電光', FALSE),
(2, 3, 0, 2, '酷拉皮卡卡片', '收藏卡', FALSE),
(2, 4, 1, 0, '雷歐力書籤', '醫學書籤', FALSE),
(2, 5, 1, 1, '獵人執照吊飾', '迷你執照', FALSE),
(2, 6, 1, 2, '西索撲克牌', '魔術師', FALSE),
(2, 7, 2, 0, '伊爾謎針', '變裝道具', FALSE),
(2, 8, 2, 1, '團長十字架', '幻影旅團', FALSE),
(2, 9, 2, 2, '金鑰匙圈', '小傑的父親', FALSE),
-- 七龍珠 4x4 (game_id=3)
(3, 1, 0, 0, '悟空貼紙', '龜派氣功', FALSE),
(3, 2, 0, 1, '達爾徽章', '王子', FALSE),
(3, 3, 0, 2, '悟飯卡片', '學者', FALSE),
(3, 4, 0, 3, '特南克斯書籤', '未來戰士', FALSE),
(3, 5, 1, 0, '比克吊飾', '地球人', FALSE),
(3, 6, 1, 1, '布瑪周邊', '膠囊公司', FALSE),
(3, 7, 1, 2, '克林鑰匙圈', '地球最強', FALSE),
(3, 8, 1, 3, '龜仙人墨鏡', '武天老師', FALSE),
(3, 9, 2, 0, '弗利沙筆', '宇宙帝王', FALSE),
(3, 10, 2, 1, '賽魯杯墊', '完全體', FALSE),
(3, 11, 2, 2, '普烏貼紙', '魔人', FALSE),
(3, 12, 2, 3, '比魯斯周邊', '破壞神', FALSE),
(3, 13, 3, 0, '維斯吊飾', '天使', FALSE),
(3, 14, 3, 1, '全王徽章', '全能', FALSE),
(3, 15, 3, 2, '神龍願望卡', '許願', FALSE),
(3, 16, 3, 3, '仙豆保存袋', '回復', FALSE),
-- 進擊巨人 3x3 (game_id=4)
(4, 1, 0, 0, '艾連貼紙', '進擊的', FALSE),
(4, 2, 0, 1, '三笠徽章', '阿卡曼', FALSE),
(4, 3, 0, 2, '阿爾敏卡片', '超大型', FALSE),
(4, 4, 1, 0, '兵長書籤', '最強', FALSE),
(4, 5, 1, 1, '韓吉吊飾', '調查兵團', FALSE),
(4, 6, 1, 2, '艾爾文筆', '團長', FALSE),
(4, 7, 2, 0, '萊納杯墊', '鎧之巨人', FALSE),
(4, 8, 2, 1, '貝爾托特周邊', '超大型', FALSE),
(4, 9, 2, 2, '尤彌爾鑰匙圈', '始祖', FALSE),
-- 鋼之鍊金術師 3x3 (game_id=5)
(5, 1, 0, 0, '愛德華貼紙', '鋼之', FALSE),
(5, 2, 0, 1, '阿爾馮斯徽章', '鎧甲', FALSE),
(5, 3, 0, 2, '溫莉卡片', '機械師', FALSE),
(5, 4, 1, 0, '馬斯坦書籤', '火焰', FALSE),
(5, 5, 1, 1, '霍克愛吊飾', '鷹眼', FALSE),
(5, 6, 1, 2, '阿姆斯特朗筆', '肌肉', FALSE),
(5, 7, 2, 0, '恩維杯墊', '嫉妒', FALSE),
(5, 8, 2, 1, '拉絲特周邊', '色欲', FALSE),
(5, 9, 2, 2, '父親大人鑰匙圈', '燒瓶', FALSE),
-- 天竺鼠車車 3x3 (game_id=6)
(6, 1, 0, 0, '馬鈴薯貼紙', 'PUI PUI', FALSE),
(6, 2, 0, 1, '西羅摩徽章', '白色', FALSE),
(6, 3, 0, 2, '阿比卡片', '棕色', FALSE),
(6, 4, 1, 0, '泰迪書籤', '灰色', FALSE),
(6, 5, 1, 1, '救護車車吊飾', '嗶嗶', FALSE),
(6, 6, 1, 2, '警察車車筆', '逮捕', FALSE),
(6, 7, 2, 0, '殭屍車車杯墊', '可怕', FALSE),
(6, 8, 2, 1, '車車駕駛周邊', '人類', FALSE),
(6, 9, 2, 2, '紅蘿蔔鑰匙圈', '零食', FALSE);

-- =====================================================
-- 8. 兌換商店商品
-- =====================================================
INSERT INTO redeem_shop_items (name, description, image_url, shard_cost, estimated_value, stock, total_stock, item_type, status, sort_order, created_at) VALUES 
('洛克人 S賞 限量公仔', '超稀有！1/4比例精緻可動公仔', '/images/redeem/rm_s.jpg', 10000, 5000, 3, 3, 'S_RANK', 'ACTIVE', 1, NOW()),
('獵人 隱藏款 金色獵人執照', '24K鍍金限定版', '/images/redeem/hxh_hidden.jpg', 15000, 8000, 2, 2, 'HIDDEN', 'ACTIVE', 2, NOW()),
('七龍珠 特別款 發光神龍', 'LED發光版神龍模型', '/images/redeem/db_special.jpg', 20000, 12000, 1, 1, 'SPECIAL', 'ACTIVE', 3, NOW()),
('進擊的巨人 兵長組合包', '兵長主題周邊全套', '/images/redeem/aot_pack.jpg', 5000, 2000, 10, 10, 'PRIZE', 'ACTIVE', 10, NOW()),
('鋼之鍊金術師 懷錶複製品', '精密機械複製品', '/images/redeem/fma_watch.jpg', 8000, 3500, 5, 5, 'PRIZE', 'ACTIVE', 11, NOW()),
('天竺鼠車車 超大抱枕', '60cm馬鈴薯造型', '/images/redeem/mc_pillow.jpg', 6000, 2500, 8, 8, 'PRIZE', 'ACTIVE', 12, NOW());
