package com.toy.store.config;

import com.toy.store.model.*;
import com.toy.store.mapper.*;
import com.toy.store.service.IchibanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final ProductMapper productMapper;
    private final GachaThemeMapper gachaThemeMapper;
    private final AdminUserMapper adminUserMapper;
    private final AdminPermissionMapper adminPermissionMapper;
    private final AdminRoleMapper adminRoleMapper;
    private final ActivityMapper activityMapper;
    private final CategoryMapper categoryMapper;
    private final SubCategoryMapper subCategoryMapper;
    private final MemberLevelMapper memberLevelMapper;
    private final GachaIpMapper ipMapper;
    private final IchibanBoxMapper ichibanBoxMapper;
    private final BingoGameMapper bingoGameMapper;
    private final IchibanService ichibanService;
    private final RouletteGameMapper rouletteGameMapper;
    private final RouletteSlotMapper rouletteSlotMapper;
    private final BlindBoxMapper blindBoxMapper;

    // 使用環境變數設定初始密碼，未設定則生成隨機密碼
    @Value("${ADMIN_INIT_PASSWORD:}")
    private String adminInitPassword;

    @Value("${USER_INIT_PASSWORD:}")
    private String userInitPassword;

    @Override
    public void run(String... args) throws Exception {
        if (adminUserMapper.findByUsername("admin").isEmpty()) {
            seedPermissions();
            AdminRole superAdminRole = seedSuperAdminRole();

            // 使用環境變數或生成隨機密碼
            String adminPassword = adminInitPassword.isEmpty()
                    ? UUID.randomUUID().toString().substring(0, 12)
                    : adminInitPassword;

            AdminUser admin = new AdminUser();
            admin.setUsername("admin");
            admin.setEmail("admin@toystore.com");
            admin.setPassword(passwordEncoder.encode(adminPassword));
            adminUserMapper.insert(admin);
            // 添加角色關聯
            adminUserMapper.addRoleToAdmin(admin.getId(), superAdminRole.getId());

            log.warn("═══════════════════════════════════════════════════════════");
            log.warn("⚠️  SECURITY WARNING: Admin account created");
            log.warn("    Username: admin");
            log.warn("    Password: {} (請立即更改！)", adminPassword);
            log.warn("    Set ADMIN_INIT_PASSWORD env var for custom password");
            log.warn("═══════════════════════════════════════════════════════════");
        }

        memberMapper.findByUsername("admin").ifPresent(member -> {
            memberMapper.deleteById(member.getId());
            log.info("Legacy admin removed from members table.");
        });

        if (!memberMapper.existsByUsername("user")) {
            // 使用環境變數或生成隨機密碼
            String userPassword = userInitPassword.isEmpty()
                    ? UUID.randomUUID().toString().substring(0, 12)
                    : userInitPassword;

            Member user = new Member();
            user.setUsername("user");
            user.setEmail("user@toystore.com");
            user.setPassword(passwordEncoder.encode(userPassword));
            user.setRole(Member.Role.USER);
            user.setPlatformWalletBalance(new BigDecimal("1000.00"));
            memberMapper.insert(user);

            log.warn("⚠️  Demo user created - Username: user, Password: {}", userPassword);
        }

        seedSeries("鋼彈系列",
                new String[] { "鋼彈W", "鋼彈G武鬥", "鋼彈Seed", "無敵鐵金剛", "鋼彈(夏亞逆襲)", "鋼彈X", "鋼蛋Z", "鋼彈ZZ", "鋼彈UC" });
        seedSeries("任天堂系列", new String[] { "超級瑪莉", "神奇寶貝" });
        seedSeries("Capcom系列", new String[] { "元祖洛克人", "洛克人X" });
        seedSeries("海賊王系列", new String[] { "草帽一伙", "和之國篇", "紅髮歌姬" });
        seedSeries("鬼滅之刃系列", new String[] { "竈門炭治郎立志篇", "無限列車篇", "遊郭篇" });
        seedSeries("咒術迴戰系列", new String[] { "特級咒物", "懷玉·玉折", "澀谷事變" });
        seedSeries("我的英雄學院系列", new String[] { "雄英高中", "職業英雄", "死穢八齋會" });
        seedSeries("間諜家家酒系列", new String[] { "佛傑家族", "伊甸學園", "秘密任務" });

        seedActivities();
        seedMemberLevels();
        seedBlindBoxes();
        seedGachaGames();
    }

    private void seedActivities() {
        createActivity("開站活動", "慶祝開站活動，所有商品9折起", "SALE");
        createActivity("首抽活動", "完成第一次抽獎，有機率獲得神秘禮物", "EVENT");
        createActivity("儲值活動", "儲值金超過1萬，即可獲得神秘獎品", "PROMOTION");
        createActivity("首購活動", "首次購買商品即可隨機抽取折扣，最高購物車內商品打五折", "DISCOUNT");
    }

    private void createActivity(String title, String description, String type) {
        if (activityMapper.findAll().stream().noneMatch(a -> a.getTitle().equals(title))) {
            Activity activity = new Activity();
            activity.setTitle(title);
            activity.setDescription(description);
            activity.setType(type);
            activity.setExpiryDate(LocalDateTime.of(2025, 12, 31, 23, 59, 59));
            activity.setActive(true);
            activityMapper.insert(activity);
        }
    }

    private void seedSeries(String seriesName, String[] subSeriesList) {
        Category categoryEntity = categoryMapper.findByName(seriesName).orElse(null);
        if (categoryEntity == null) {
            categoryEntity = new Category();
            categoryEntity.setName(seriesName);
            categoryMapper.insert(categoryEntity);
        }

        GachaTheme theme = gachaThemeMapper.findByName(seriesName);
        if (theme == null) {
            theme = new GachaTheme();
            theme.setName(seriesName);
            theme.setDescription(seriesName + " 專屬扭蛋，內含鑰匙圈、毛巾與稀有公仔！");
            theme.setPrice(new BigDecimal("100.00"));
            gachaThemeMapper.insert(theme);
        }

        for (String subSeries : subSeriesList) {
            if (subCategoryMapper.findByNameAndCategoryId(subSeries, categoryEntity.getId()) == null) {
                SubCategory sub = new SubCategory();
                sub.setName(subSeries);
                sub.setCategoryId(categoryEntity.getId());
                subCategoryMapper.insert(sub);
            }

            createProduct(subSeries + " 鑰匙圈", new BigDecimal("50.00"), 100, seriesName, subSeries);
            createProduct(subSeries + " 毛巾", new BigDecimal("50.00"), 100, seriesName, subSeries);
            createProduct(subSeries + " 系列公仔", new BigDecimal("350.00"), 5, seriesName, subSeries);

            createGachaItem(theme, subSeries + " 鑰匙圈", new BigDecimal("50.00"), 20);
            createGachaItem(theme, subSeries + " 毛巾", new BigDecimal("50.00"), 20);
            createGachaItem(theme, subSeries + " 系列公仔", new BigDecimal("350.00"), 1);
        }
    }

    private void createProduct(String name, BigDecimal price, int stock, String category, String subCategory) {
        if (productMapper.findByName(name).isEmpty()) {
            Product product = new Product();
            product.setName(name);
            product.setPrice(price);
            product.setStock(stock);
            product.setCategory(category);
            product.setSubCategory(subCategory);
            product.setDescription(name + " - " + category + " 正版授權商品");
            productMapper.insert(product);
        }
    }

    private void createGachaItem(GachaTheme theme, String name, BigDecimal value, int weight) {
        if (theme.getItems() == null) {
            theme.setItems(new ArrayList<>());
        }

        boolean exists = theme.getItems().stream().anyMatch(i -> i.getName().equals(name));
        if (!exists) {
            GachaItem item = new GachaItem();
            item.setThemeId(theme.getId());
            item.setName(name);
            item.setEstimatedValue(value);
            item.setWeight(weight);
            theme.getItems().add(item);
            gachaThemeMapper.update(theme);
        }
    }

    private void seedMemberLevels() {
        if (memberLevelMapper.count() == 0) {
            createLevel("VIP1 青銅", 1, 0, "基礎返利");
            createLevel("VIP2 白銀", 2, 5000, "消費返利 1%");
            createLevel("VIP3 黃金", 3, 25000, "消費返利 2%");
            createLevel("VIP4 鉑金", 4, 50000, "消費返利 3%");
            createLevel("VIP5 鑽石", 5, 80000, "消費返利 5%");
            createLevel("VIP6 大師", 6, 150000, "消費返利 7%");
            createLevel("VIP7 宗師", 7, 300000, "消費返利 10%");
            createLevel("VIP8 傳奇", 8, 600000, "消費返利 12%");
            createLevel("VIP9 神話", 9, 1200000, "消費返利 15%");
            createLevel("VIP10 至尊", 10, 2500000, "消費返利 20%");
        }
    }

    private void createLevel(String name, int sortOrder, int threshold, String monthlyReward) {
        MemberLevel level = new MemberLevel();
        level.setName(name);
        level.setSortOrder(sortOrder);
        level.setThreshold(new BigDecimal(threshold));
        level.setMonthlyReward(monthlyReward);
        level.setEnabled(true);
        memberLevelMapper.insert(level);
    }

    private void seedBlindBoxes() {
        if (blindBoxMapper.count() > 0)
            return;

        // 創建第一個盲盒
        BlindBox box1 = new BlindBox();
        box1.setName("海賊王角色盲盒");
        box1.setDescription("收集你喜愛的海賊王角色！內含6款精美公仔");
        box1.setIpName("海賊王系列");
        box1.setPricePerBox(new BigDecimal("150.00"));
        box1.setFullBoxPrice(new BigDecimal("800.00"));
        box1.setTotalBoxes(6);
        box1.setStatus(BlindBox.Status.ACTIVE);
        box1.setItems(new ArrayList<>());

        String[] op_prizes = { "路飛", "索隆", "娜美", "香吉士", "喬巴", "羅賓" };
        BlindBoxItem.Rarity[] op_rarities = {
                BlindBoxItem.Rarity.RARE, BlindBoxItem.Rarity.RARE,
                BlindBoxItem.Rarity.NORMAL, BlindBoxItem.Rarity.NORMAL,
                BlindBoxItem.Rarity.ULTRA_RARE, BlindBoxItem.Rarity.NORMAL
        };
        for (int i = 0; i < 6; i++) {
            BlindBoxItem item = new BlindBoxItem();
            item.setBlindBox(box1);
            item.setBoxNumber(i + 1);
            item.setPrizeName(op_prizes[i] + " 角色公仔");
            item.setRarity(op_rarities[i]);
            item.setEstimatedValue(new BigDecimal(op_rarities[i] == BlindBoxItem.Rarity.ULTRA_RARE ? "500" : "200"));
            item.setStatus(BlindBoxItem.Status.AVAILABLE);
            box1.getItems().add(item);
        }
        blindBoxMapper.insert(box1);

        // 創建第二個盲盒
        BlindBox box2 = new BlindBox();
        box2.setName("鬼滅之刃角色盲盒");
        box2.setDescription("鬼殺隊精英集結！含隱藏款禰豆子");
        box2.setIpName("鬼滅之刃系列");
        box2.setPricePerBox(new BigDecimal("180.00"));
        box2.setFullBoxPrice(new BigDecimal("1000.00"));
        box2.setTotalBoxes(8);
        box2.setStatus(BlindBox.Status.ACTIVE);
        box2.setItems(new ArrayList<>());

        String[] ds_prizes = { "炭治郎", "禰豆子", "善逸", "伊之助", "蝴蝶忍", "富岡義勇", "煉獄杏壽郎", "隱藏款禰豆子" };
        BlindBoxItem.Rarity[] ds_rarities = {
                BlindBoxItem.Rarity.RARE, BlindBoxItem.Rarity.RARE,
                BlindBoxItem.Rarity.NORMAL, BlindBoxItem.Rarity.NORMAL,
                BlindBoxItem.Rarity.RARE, BlindBoxItem.Rarity.RARE,
                BlindBoxItem.Rarity.ULTRA_RARE, BlindBoxItem.Rarity.SECRET
        };
        for (int i = 0; i < 8; i++) {
            BlindBoxItem item = new BlindBoxItem();
            item.setBlindBox(box2);
            item.setBoxNumber(i + 1);
            item.setPrizeName(ds_prizes[i] + " 角色公仔");
            item.setRarity(ds_rarities[i]);
            item.setEstimatedValue(new BigDecimal(ds_rarities[i] == BlindBoxItem.Rarity.SECRET ? "800"
                    : ds_rarities[i] == BlindBoxItem.Rarity.ULTRA_RARE ? "500" : "200"));
            item.setStatus(BlindBoxItem.Status.AVAILABLE);
            box2.getItems().add(item);
        }
        blindBoxMapper.insert(box2);

        // 創建第三個盲盒：鋼彈系列
        BlindBox box3 = new BlindBox();
        box3.setName("機動戰士鋼彈盲盒");
        box3.setDescription("經典鋼彈機體系列，SD比例可動模型");
        box3.setIpName("鋼彈系列");
        box3.setPricePerBox(new BigDecimal("200.00"));
        box3.setFullBoxPrice(new BigDecimal("1100.00"));
        box3.setTotalBoxes(6);
        box3.setStatus(BlindBox.Status.ACTIVE);
        box3.setItems(new ArrayList<>());
        String[] gd_prizes = { "RX-78-2 元祖鋼彈", "獨角獸鋼彈", "自由鋼彈", "攻擊自由", "00 Raiser", "飛翼零式" };
        BlindBoxItem.Rarity[] gd_rarities = {
                BlindBoxItem.Rarity.RARE, BlindBoxItem.Rarity.ULTRA_RARE,
                BlindBoxItem.Rarity.RARE, BlindBoxItem.Rarity.SECRET,
                BlindBoxItem.Rarity.RARE, BlindBoxItem.Rarity.NORMAL
        };
        for (int i = 0; i < 6; i++) {
            BlindBoxItem item = new BlindBoxItem();
            item.setBlindBox(box3);
            item.setBoxNumber(i + 1);
            item.setPrizeName(gd_prizes[i]);
            item.setRarity(gd_rarities[i]);
            item.setEstimatedValue(new BigDecimal(gd_rarities[i] == BlindBoxItem.Rarity.SECRET ? "1000"
                    : gd_rarities[i] == BlindBoxItem.Rarity.ULTRA_RARE ? "600" : "300"));
            item.setStatus(BlindBoxItem.Status.AVAILABLE);
            box3.getItems().add(item);
        }
        blindBoxMapper.insert(box3);

        // 創建第四個盲盒：間諜家家酒
        BlindBox box4 = new BlindBox();
        box4.setName("SPY×FAMILY 角色盲盒");
        box4.setDescription("佛傑家族全員集合！含隱藏款安妮亞");
        box4.setIpName("間諜家家酒");
        box4.setPricePerBox(new BigDecimal("160.00"));
        box4.setFullBoxPrice(new BigDecimal("900.00"));
        box4.setTotalBoxes(6);
        box4.setStatus(BlindBox.Status.ACTIVE);
        box4.setItems(new ArrayList<>());
        String[] spy_prizes = { "洛伊德", "約兒", "安妮亞", "乔德", "法蘭奇", "隱藏款安妮亞" };
        BlindBoxItem.Rarity[] spy_rarities = {
                BlindBoxItem.Rarity.RARE, BlindBoxItem.Rarity.RARE,
                BlindBoxItem.Rarity.ULTRA_RARE, BlindBoxItem.Rarity.NORMAL,
                BlindBoxItem.Rarity.NORMAL, BlindBoxItem.Rarity.SECRET
        };
        for (int i = 0; i < 6; i++) {
            BlindBoxItem item = new BlindBoxItem();
            item.setBlindBox(box4);
            item.setBoxNumber(i + 1);
            item.setPrizeName(spy_prizes[i] + " 公仔");
            item.setRarity(spy_rarities[i]);
            item.setEstimatedValue(new BigDecimal(spy_rarities[i] == BlindBoxItem.Rarity.SECRET ? "800"
                    : spy_rarities[i] == BlindBoxItem.Rarity.ULTRA_RARE ? "500" : "200"));
            item.setStatus(BlindBoxItem.Status.AVAILABLE);
            box4.getItems().add(item);
        }
        blindBoxMapper.insert(box4);

        // 創建第五個盲盒：進擊的巨人
        BlindBox box5 = new BlindBox();
        box5.setName("進擊的巨人角色盲盒");
        box5.setDescription("調查兵團精英集結！含隱藏款巨人化艾連");
        box5.setIpName("進擊的巨人系列");
        box5.setPricePerBox(new BigDecimal("180.00"));
        box5.setFullBoxPrice(new BigDecimal("1000.00"));
        box5.setTotalBoxes(6);
        box5.setStatus(BlindBox.Status.ACTIVE);
        box5.setItems(new ArrayList<>());
        String[] aot_prizes = { "艾連", "米卡莎", "阿爾敏", "里維", "韓吉", "巨人化艾連" };
        BlindBoxItem.Rarity[] aot_rarities = {
                BlindBoxItem.Rarity.RARE, BlindBoxItem.Rarity.RARE,
                BlindBoxItem.Rarity.NORMAL, BlindBoxItem.Rarity.ULTRA_RARE,
                BlindBoxItem.Rarity.NORMAL, BlindBoxItem.Rarity.SECRET
        };
        for (int i = 0; i < 6; i++) {
            BlindBoxItem item = new BlindBoxItem();
            item.setBlindBox(box5);
            item.setBoxNumber(i + 1);
            item.setPrizeName(aot_prizes[i] + " 公仔");
            item.setRarity(aot_rarities[i]);
            item.setEstimatedValue(new BigDecimal(aot_rarities[i] == BlindBoxItem.Rarity.SECRET ? "900"
                    : aot_rarities[i] == BlindBoxItem.Rarity.ULTRA_RARE ? "600" : "250"));
            item.setStatus(BlindBoxItem.Status.AVAILABLE);
            box5.getItems().add(item);
        }
        blindBoxMapper.insert(box5);

        System.out.println("Blind boxes created: 5 boxes with items");
    }

    private void seedGachaGames() {
        if (ichibanBoxMapper.count() > 0)
            return;

        List<GachaIp> ips = ipMapper.findAll();
        if (ips.isEmpty())
            return;
        GachaIp ip = ips.get(0);

        IchibanBox box = new IchibanBox();
        box.setIp(ip);
        box.setName("動漫大會串 一番賞");
        box.setDescription("超強一番賞，內含多款實體獎品！");
        box.setPricePerDraw(new BigDecimal("250.00"));
        box.setTotalSlots(80);
        box.setStatus(IchibanBox.Status.ACTIVE);

        List<IchibanPrize> prizes = new ArrayList<>();
        prizes.add(createPrize(box, IchibanPrize.Rank.A, "實體抱枕 - 炭治郎款", "精美抱枕", new BigDecimal("1200"), 2, 2, 0));
        prizes.add(createPrize(box, IchibanPrize.Rank.B, "實體滑鼠墊 - 禰豆子款", "大尺寸滑鼠墊", new BigDecimal("800"), 3, 3, 1));
        prizes.add(createPrize(box, IchibanPrize.Rank.C, "實體鉛筆盒 - 善逸款", "多功能鉛筆盒", new BigDecimal("500"), 5, 5, 2));
        prizes.add(createPrize(box, IchibanPrize.Rank.D, "實體筆記本 - 伊之助款", "B5 筆記本", new BigDecimal("300"), 10, 10, 3));
        prizes.add(createPrize(box, IchibanPrize.Rank.E, "角色精美徽章", "隨機角色款式", new BigDecimal("150"), 60, 60, 4));

        ichibanService.createBox(box, prizes);

        BingoGame bingo = new BingoGame();
        bingo.setIp(ip);
        bingo.setName("幸運九宮格");
        bingo.setDescription("連線即可獲得限量實體抱枕！");
        bingo.setPricePerDig(new BigDecimal("150.00"));
        bingo.setGridSize(3);
        bingo.setStatus(BingoGame.Status.ACTIVE);
        bingo.setBingoRewardName("限量實體抱枕 (黃金版)");
        bingo = bingoGameMapper.insert(bingo) > 0 ? bingo : bingo;

        for (int i = 1; i <= 9; i++) {
            BingoCell cell = new BingoCell();
            cell.setGame(bingo);
            cell.setPosition(i);
            cell.setRow((i - 1) / 3);
            cell.setCol((i - 1) % 3);

            if (i == 5) {
                cell.setPrizeName("限量實體抱枕 (黃金版)");
                cell.setPrizeValue(new BigDecimal("1500"));
            } else if (i % 3 == 0) {
                cell.setPrizeName("實體滑鼠墊");
                cell.setPrizeValue(new BigDecimal("800"));
            } else if (i % 3 == 1) {
                cell.setPrizeName("實體鉛筆盒");
                cell.setPrizeValue(new BigDecimal("500"));
            } else {
                cell.setPrizeName("實體筆記本");
                cell.setPrizeValue(new BigDecimal("300"));
            }
            cell.setIsRevealed(false);
            if (bingo.getCells() == null)
                bingo.setCells(new ArrayList<>());
            bingo.getCells().add(cell);
        }
        bingoGameMapper.update(bingo);

        RouletteGame roulette = new RouletteGame();
        roulette.setIp(ip);
        roulette.setName("狂熱轉盤");
        roulette.setDescription("轉動轉盤獲得徽章與水杯！");
        roulette.setPricePerSpin(new BigDecimal("200.00"));
        roulette.setTotalSlots(8);
        roulette.setStatus(RouletteGame.Status.ACTIVE);
        rouletteGameMapper.insert(roulette);

        String[] prizesRoulette = { "周邊商品徽章", "實體水杯", "精美杯墊", "鑰匙圈", "角色帆布袋", "再來一次", "限量黃金版抱枕", "實體滑鼠墊" };
        RouletteSlot.SlotType[] types = {
                RouletteSlot.SlotType.NORMAL,
                RouletteSlot.SlotType.RARE,
                RouletteSlot.SlotType.NORMAL,
                RouletteSlot.SlotType.NORMAL,
                RouletteSlot.SlotType.NORMAL,
                RouletteSlot.SlotType.FREE_SPIN,
                RouletteSlot.SlotType.JACKPOT,
                RouletteSlot.SlotType.RARE
        };

        for (int i = 0; i < 8; i++) {
            RouletteSlot slot = new RouletteSlot();
            slot.setGame(roulette);
            slot.setSlotOrder(i + 1);
            slot.setPrizeName(prizesRoulette[i]);
            slot.setSlotType(types[i]);
            slot.setWeight(types[i] == RouletteSlot.SlotType.JACKPOT ? 10 : 100);
            if (types[i] == RouletteSlot.SlotType.SHARD)
                slot.setShardAmount(100);
            rouletteSlotMapper.insert(slot);
        }
    }

    private void seedPermissions() {
        createPermission("DASHBOARD_VIEW", "查看儀錶板");
        createPermission("GACHA_MANAGE", "管理扭蛋與一番賞機台");
        createPermission("PRODUCT_MANAGE", "管理商品與分類");
        createPermission("MEMBER_MANAGE", "管理會員資料與餘額");
        createPermission("FINANCE_MANAGE", "財務與訂單審核");
        createPermission("ADMIN_MANAGE", "系統管理員與權限設定");
        createPermission("SYSTEM_SETTING", "系統參數設定");
    }

    private void createPermission(String code, String name) {
        if (adminPermissionMapper.findByCode(code).isEmpty()) {
            AdminPermission perm = new AdminPermission();
            perm.setCode(code);
            perm.setName(name);
            adminPermissionMapper.insert(perm);
        }
    }

    private AdminRole seedSuperAdminRole() {
        return adminRoleMapper.findByName("超級管理員")
                .orElseGet(() -> {
                    AdminRole role = new AdminRole();
                    role.setName("超級管理員");
                    adminRoleMapper.insert(role);
                    // 添加所有權限
                    for (AdminPermission perm : adminPermissionMapper.findAll()) {
                        adminRoleMapper.addPermissionToRole(role.getId(), perm.getId());
                    }
                    return role;
                });
    }

    private IchibanPrize createPrize(IchibanBox box, IchibanPrize.Rank rank, String name, String desc, BigDecimal val,
            int qty, int remain, int sort) {
        IchibanPrize p = new IchibanPrize();
        p.setBox(box);
        p.setRank(rank);
        p.setName(name);
        p.setDescription(desc);
        p.setEstimatedValue(val);
        p.setQuantity(qty);
        p.setRemainingQuantity(remain);
        p.setSortOrder(sort);
        return p;
    }
}
