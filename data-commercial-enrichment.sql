-- 商務內容增強與 20 IP 獎項矩陣注入
-- 適用於 Phase 12 扭蛋體系升級

-- 1. 清理現有數據（可選，根據需求決定是否保留舊數據）
-- DELETE FROM ichiban_slots;
-- DELETE FROM ichiban_prizes;
-- DELETE FROM ichiban_boxes;
-- DELETE FROM gacha_ips;

-- 2. 注入 20 個核心 IP
INSERT INTO gacha_ips (name, description, image_url, status, created_at, updated_at) VALUES
('機動戰士鋼彈', '傳奇擬真機器人系列，包含 U.C. 與各作品公仔', '/images/ips/gundam.jpg', 'ACTIVE', NOW(), NOW()),
('海賊王 ONE PIECE', '大航海時代的熱血冒險，限定 POP 系列公仔', '/images/ips/onepiece.jpg', 'ACTIVE', NOW(), NOW()),
('七龍珠', '經典格鬥神作，傳說中的賽亞人雕像級獎項', '/images/ips/dragonball.jpg', 'ACTIVE', NOW(), NOW()),
('火影忍者', '忍者世界的牽絆，限定版簽名掛軸與忍具模型', '/images/ips/naruto.jpg', 'ACTIVE', NOW(), NOW()),
('鬼滅之刃', '呼吸法的極致，炭治郎與柱的精細雕刻作品', '/images/ips/demonslayer.jpg', 'ACTIVE', NOW(), NOW()),
('咒術迴戰', '詛咒與術式的交鋒，特級咒物複刻品', '/images/ips/jujutsu.jpg', 'ACTIVE', NOW(), NOW()),
('間諜家家酒', '安妮亞的可愛魅力，專屬表情收藏集', '/images/ips/spyfamily.jpg', 'ACTIVE', NOW(), NOW()),
('鏈鋸人', '闇黑奇幻作品，波奇塔布偶與震撼場景公仔', '/images/ips/chainsawman.jpg', 'ACTIVE', NOW(), NOW()),
('進擊的巨人', '人類與巨人的對抗，立體機動裝置限量模型', '/images/ips/titan.jpg', 'ACTIVE', NOW(), NOW()),
('精靈寶可夢', '皮卡丘與夥伴們，限定異色版公仔與卡牌', '/images/ips/pokemon.jpg', 'ACTIVE', NOW(), NOW()),
('數碼寶貝', '被選召的孩子們，戰鬥暴龍獸超進化雕像', '/images/ips/digimon.jpg', 'ACTIVE', NOW(), NOW()),
('聖鬥士星矢', '小宇宙的爆發，黃金魂版黃金聖衣', '/images/ips/saintseiya.jpg', 'ACTIVE', NOW(), NOW()),
('新世紀福音戰士', '人類補完計劃，初號機覺醒版比例模型', '/images/ips/eva.jpg', 'ACTIVE', NOW(), NOW()),
('美少女戰士', '代替月亮懲罰你，限定變身器粉盒與水手服套裝', '/images/ips/sailormoon.jpg', 'ACTIVE', NOW(), NOW()),
('庫洛魔法使', '封印解除，全套小櫻牌與夢之杖', '/images/ips/sakura.jpg', 'ACTIVE', NOW(), NOW()),
('我的英雄學院', '超越巔峰 Plus Ultra，歐爾麥特與綠谷經典場景', '/images/ips/hero.jpg', 'ACTIVE', NOW(), NOW()),
('死神 BLEACH', '卍解的力量，黑崎一護虛化形態公仔', '/images/ips/bleach.jpg', 'ACTIVE', NOW(), NOW()),
('獵人 HUNTER x HUNTER', '念能力者的考驗，蟻王與會長對戰場景組', '/images/ips/hunter.jpg', 'ACTIVE', NOW(), NOW()),
('灌籃高手', '安西教練我想打球，湘北五虎經典復刻球鞋與公仔', '/images/ips/slamdunk.jpg', 'ACTIVE', NOW(), NOW()),
('遊戲王', '決鬥者之魂，三幻神限量卡片與青眼白龍雕塑', '/images/ips/yugioh.jpg', 'ACTIVE', NOW(), NOW());

-- 3. 為「海賊王」注入專業一番賞
-- 假設 One Piece IP ID 為 2 (實際需根據 DB 序列)
-- INSERT INTO ichiban_boxes (ip_id, name, description, price_per_draw, total_slots, status, created_at) 
-- VALUES (2, '海賊王 和之國篇 一番賞', '包含霸氣四溢的凱多與魯夫模型！', 250.00, 80, 'ACTIVE', NOW());

-- 4. 注入商城特化商品 (Redeem Shop)
INSERT INTO redeem_shop_items (name, description, shard_cost, total_stock, remaining_stock, category, image_url) VALUES
('【限定】1:4 超大魯夫 尼卡形態公仔', '神話級收藏品，稀有度極高', 50000, 5, 5, 'FIGURE', '/images/redeem/nika.jpg'),
('【珍藏】PG 級 獨角獸鋼彈 最終決戰版', '精細組裝模型，包含金屬套件', 35000, 10, 10, 'GUNPLA', '/images/redeem/unicorn.jpg'),
('【復刻】三幻神 金屬卡片套裝', '全球限量的收藏卡片，含精美展示框', 80000, 3, 3, 'CARD', '/images/redeem/godcards.jpg');

-- 5. 更新扭蛋主題與商務文案
INSERT INTO gacha_themes (name, description, price, image_url) VALUES
('精品動漫大匯聚', '綜合各大 IP 稀有獎項，高回報率保證', 150.00, '/images/gacha/premium.jpg'),
('童年回憶殺專場', '包含數碼寶貝、神奇寶貝等限量週邊', 80.00, '/images/gacha/childhood.jpg');
