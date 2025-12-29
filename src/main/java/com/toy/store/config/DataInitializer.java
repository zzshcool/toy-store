package com.toy.store.config;

import com.toy.store.model.Member;
import com.toy.store.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private com.toy.store.repository.ProductRepository productRepository;

    @Autowired
    private com.toy.store.repository.GachaThemeRepository gachaThemeRepository;

    @Autowired
    private com.toy.store.repository.AdminUserRepository adminUserRepository;

    @Override
    public void run(String... args) throws Exception {
        // Create Admin Account (Backstage)
        if (adminUserRepository.findByUsername("admin").isEmpty()) {
            com.toy.store.model.AdminUser admin = new com.toy.store.model.AdminUser();
            admin.setUsername("admin");
            admin.setEmail("admin@toystore.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            adminUserRepository.save(admin);
            System.out.println("Admin account created: admin / admin123");
        }

        // Cleanup legacy Admin in Member table
        memberRepository.findByUsername("admin").ifPresent(member -> {
            memberRepository.delete(member);
            System.out.println("Legacy admin removed from members table.");
        });

        // Create User Account
        if (!memberRepository.existsByUsername("user")) {
            Member user = new Member();
            user.setUsername("user");
            user.setEmail("user@toystore.com");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRole(Member.Role.USER);
            user.setPlatformWalletBalance(new BigDecimal("1000.00"));
            memberRepository.save(user);
            System.out.println("User account created: user / user123");
        }

        // Seed Products and Mystery Boxes
        // Seed Products and Mystery Boxes
        seedSeries("鋼彈系列",
                new String[] { "鋼彈W", "鋼彈G武鬥", "鋼彈Seed", "無敵鐵金剛", "鋼彈(夏亞逆襲)", "鋼彈X", "鋼蛋Z", "鋼彈ZZ", "鋼彈UC" });
        seedSeries("任天堂系列", new String[] { "超級瑪莉", "神奇寶貝" });
        seedSeries("Capcom系列", new String[] { "元祖洛克人", "洛克人X" });
        seedSeries("海賊王系列", new String[] { "草帽一伙", "和之國篇", "紅髮歌姬" });
        seedSeries("鬼滅之刃系列", new String[] { "竈門炭治郎立志篇", "無限列車篇", "遊郭篇" });
        seedSeries("咒術迴戰系列", new String[] { "特級咒物", "懷玉·玉折", "澀谷事變" });
        seedSeries("我的英雄學院系列", new String[] { "雄英高中", "職業英雄", "死穢八齋會" });
        seedSeries("間諜家家酒系列", new String[] { "佛傑家族", "伊甸學園", "秘密任務" });

        // Seed Activities
        seedActivities();

        // Seed Member Levels
        seedMemberLevels();

        // Seed Gacha Games (Ichiban, Bingo, Roulette)
        seedGachaGames();
    }

    @Autowired
    private com.toy.store.repository.ActivityRepository activityRepository;

    private void seedActivities() {
        createActivity("開站活動", "慶祝開站活動，所有商品9折起", "SALE");
        createActivity("首抽活動", "完成第一次抽獎，有機率獲得神秘禮物", "EVENT");
        createActivity("儲值活動", "儲值金超過1萬，即可獲得神秘獎品", "PROMOTION");
        createActivity("首購活動", "首次購買商品即可隨機抽取折扣，最高購物車內商品打五折", "DISCOUNT");
    }

    private void createActivity(String title, String description, String type) {
        if (activityRepository.findAll().stream().noneMatch(a -> a.getTitle().equals(title))) {
            com.toy.store.model.Activity activity = new com.toy.store.model.Activity();
            activity.setTitle(title);
            activity.setDescription(description);
            activity.setType(type);
            activity.setExpiryDate(java.time.LocalDateTime.of(2025, 12, 31, 23, 59, 59));
            activity.setActive(true);
            activityRepository.save(activity);
        }
    }

    @Autowired
    private com.toy.store.repository.CategoryRepository categoryRepository;

    @Autowired
    private com.toy.store.repository.SubCategoryRepository subCategoryRepository;

    private void seedSeries(String seriesName, String[] subSeriesList) {
        // 1. Create Category Entity (for Dropdowns)
        com.toy.store.model.Category categoryEntity = categoryRepository.findByName(seriesName);
        if (categoryEntity == null) {
            categoryEntity = new com.toy.store.model.Category();
            categoryEntity.setName(seriesName);
            categoryEntity = categoryRepository.save(categoryEntity);
        }

        // 2. Create Gacha Theme
        com.toy.store.model.GachaTheme theme = gachaThemeRepository.findByName(seriesName);
        if (theme == null) {
            theme = new com.toy.store.model.GachaTheme();
            theme.setName(seriesName);
            theme.setDescription(seriesName + " 專屬扭蛋，內含鑰匙圈、毛巾與稀有公仔！");
            theme.setPrice(new BigDecimal("100.00"));
            theme = gachaThemeRepository.save(theme);
        }

        for (String subSeries : subSeriesList) {
            // Create SubCategory Entity
            if (subCategoryRepository.findByNameAndCategory(subSeries, categoryEntity) == null) {
                com.toy.store.model.SubCategory sub = new com.toy.store.model.SubCategory();
                sub.setName(subSeries);
                sub.setCategory(categoryEntity);
                subCategoryRepository.save(sub);
            }

            // 3. Create Products (Direct Buy)
            createProduct(subSeries + " 鑰匙圈", new BigDecimal("50.00"), 100, seriesName, subSeries);
            createProduct(subSeries + " 毛巾", new BigDecimal("50.00"), 100, seriesName, subSeries);
            createProduct(subSeries + " 系列公仔", new BigDecimal("350.00"), 5, seriesName, subSeries);

            // 4. Create Gacha Items (Lottery)
            createGachaItem(theme, subSeries + " 鑰匙圈", new BigDecimal("50.00"), 20);
            createGachaItem(theme, subSeries + " 毛巾", new BigDecimal("50.00"), 20);
            createGachaItem(theme, subSeries + " 系列公仔", new BigDecimal("350.00"), 1);
        }
    }

    private void createProduct(String name, BigDecimal price, int stock, String category, String subCategory) {
        if (productRepository.findByName(name).isEmpty()) {
            com.toy.store.model.Product product = new com.toy.store.model.Product();
            product.setName(name);
            product.setPrice(price);
            product.setStock(stock);
            product.setCategory(category);
            product.setSubCategory(subCategory);
            product.setDescription(name + " - " + category + " 正版授權商品");
            productRepository.save(product);
        }
    }

    private void createGachaItem(com.toy.store.model.GachaTheme theme, String name, BigDecimal value,
            int weight) {
        if (theme.getItems() == null) {
            theme.setItems(new java.util.ArrayList<>());
        }

        // Check if item already exists
        boolean exists = theme.getItems().stream().anyMatch(i -> i.getName().equals(name));
        if (!exists) {
            com.toy.store.model.GachaItem item = new com.toy.store.model.GachaItem();
            item.setTheme(theme);
            item.setName(name);
            item.setEstimatedValue(value);
            item.setWeight(weight);
            theme.getItems().add(item);
            gachaThemeRepository.save(theme);
        }
    }

    @Autowired
    private com.toy.store.repository.MemberLevelRepository memberLevelRepository;

    private void seedMemberLevels() {
        if (memberLevelRepository.count() == 0) {
            createLevel("平民", 1, 0, "無");
            createLevel("良民", 2, 10000, "送抽獎3次");
            createLevel("青銅", 3, 50000, "送抽獎5次");
            createLevel("黃銅", 4, 100000, "送抽獎10次");
            createLevel("金銅", 5, 150000, "送抽獎15次");
            createLevel("白銀", 6, 300000, "送抽獎20次");
            createLevel("白金", 7, 400000, "送抽獎30次");
            createLevel("黃金", 8, 500000, "送抽獎35次");
        }
    }

    private void createLevel(String name, int sortOrder, int threshold, String monthlyReward) {
        com.toy.store.model.MemberLevel level = new com.toy.store.model.MemberLevel();
        level.setName(name);
        level.setSortOrder(sortOrder);
        level.setThreshold(new BigDecimal(threshold));
        level.setMonthlyReward(monthlyReward);
        level.setEnabled(true);
        memberLevelRepository.save(level);
    }

    @Autowired
    private com.toy.store.repository.GachaIpRepository ipRepository;

    @Autowired
    private com.toy.store.repository.IchibanBoxRepository ichibanBoxRepository;

    @Autowired
    private com.toy.store.repository.BingoGameRepository bingoGameRepository;

    @Autowired
    private com.toy.store.service.IchibanService ichibanService;

    @Autowired
    private com.toy.store.repository.RouletteGameRepository rouletteGameRepository;

    @Autowired
    private com.toy.store.repository.RouletteSlotRepository rouletteSlotRepository;

    private void seedGachaGames() {
        if (ichibanBoxRepository.count() > 0)
            return;

        com.toy.store.model.GachaIp ip = ipRepository.findAll().get(0);

        // 1. One Ichiban Box
        com.toy.store.model.IchibanBox box = new com.toy.store.model.IchibanBox();
        box.setIp(ip);
        box.setName("動漫大會串 一番賞");
        box.setDescription("超強一番賞，內含多款實體獎品！");
        box.setPricePerDraw(new BigDecimal("250.00"));
        box.setTotalSlots(80);
        box.setStatus(com.toy.store.model.IchibanBox.Status.ACTIVE);

        java.util.List<com.toy.store.model.IchibanPrize> prizes = new java.util.ArrayList<>();
        prizes.add(new com.toy.store.model.IchibanPrize(null, box, com.toy.store.model.IchibanPrize.Rank.A,
                "實體抱枕 - 炭治郎款", "精美抱枕", null, new BigDecimal("1200"), 2, 2, 0));
        prizes.add(new com.toy.store.model.IchibanPrize(null, box, com.toy.store.model.IchibanPrize.Rank.B,
                "實體滑鼠墊 - 禰豆子款", "大尺寸滑鼠墊", null, new BigDecimal("800"), 3, 3, 1));
        prizes.add(new com.toy.store.model.IchibanPrize(null, box, com.toy.store.model.IchibanPrize.Rank.C,
                "實體鉛筆盒 - 善逸款", "多功能鉛筆盒", null, new BigDecimal("500"), 5, 5, 2));
        prizes.add(new com.toy.store.model.IchibanPrize(null, box, com.toy.store.model.IchibanPrize.Rank.D,
                "實體筆記本 - 伊之助款", "B5 筆記本", null, new BigDecimal("300"), 10, 10, 3));
        prizes.add(new com.toy.store.model.IchibanPrize(null, box, com.toy.store.model.IchibanPrize.Rank.E, "角色精美徽章",
                "隨機角色款式", null, new BigDecimal("150"), 60, 60, 4));

        ichibanService.createBox(box, prizes);

        // 2. One Bingo Game
        com.toy.store.model.BingoGame bingo = new com.toy.store.model.BingoGame();
        bingo.setIp(ip);
        bingo.setName("幸運九宮格");
        bingo.setDescription("連線即可獲得限量實體抱枕！");
        bingo.setPricePerDig(new BigDecimal("150.00"));
        bingo.setGridSize(3);
        bingo.setStatus(com.toy.store.model.BingoGame.Status.ACTIVE);
        bingo.setBingoRewardName("限量實體抱枕 (黃金版)");
        bingo = bingoGameRepository.save(bingo);

        for (int i = 1; i <= 9; i++) {
            com.toy.store.model.BingoCell cell = new com.toy.store.model.BingoCell();
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
                bingo.setCells(new java.util.ArrayList<>());
            bingo.getCells().add(cell);
        }
        bingoGameRepository.save(bingo);

        // 3. One Roulette Game
        com.toy.store.model.RouletteGame roulette = new com.toy.store.model.RouletteGame();
        roulette.setIp(ip);
        roulette.setName("狂熱轉盤");
        roulette.setDescription("轉動轉盤獲得徽章與水杯！");
        roulette.setPricePerSpin(new BigDecimal("200.00"));
        roulette.setTotalSlots(8);
        roulette.setStatus(com.toy.store.model.RouletteGame.Status.ACTIVE);
        roulette = rouletteGameRepository.save(roulette);

        String[] prizesRoulette = { "周邊商品徽章", "實體水杯", "精美杯墊", "鑰匙圈", "角色帆布袋", "再來一次", "限量黃金版抱枕", "實體滑鼠墊" };
        com.toy.store.model.RouletteSlot.SlotType[] types = {
                com.toy.store.model.RouletteSlot.SlotType.NORMAL,
                com.toy.store.model.RouletteSlot.SlotType.RARE,
                com.toy.store.model.RouletteSlot.SlotType.NORMAL,
                com.toy.store.model.RouletteSlot.SlotType.NORMAL,
                com.toy.store.model.RouletteSlot.SlotType.NORMAL,
                com.toy.store.model.RouletteSlot.SlotType.FREE_SPIN,
                com.toy.store.model.RouletteSlot.SlotType.JACKPOT,
                com.toy.store.model.RouletteSlot.SlotType.RARE
        };

        for (int i = 0; i < 8; i++) {
            com.toy.store.model.RouletteSlot slot = new com.toy.store.model.RouletteSlot();
            slot.setGame(roulette);
            slot.setSlotOrder(i + 1);
            slot.setPrizeName(prizesRoulette[i]);
            slot.setSlotType(types[i]);
            slot.setWeight(types[i] == com.toy.store.model.RouletteSlot.SlotType.JACKPOT ? 10 : 100);
            if (types[i] == com.toy.store.model.RouletteSlot.SlotType.SHARD)
                slot.setShardAmount(100);
            rouletteSlotRepository.save(slot);
        }
    }
}
