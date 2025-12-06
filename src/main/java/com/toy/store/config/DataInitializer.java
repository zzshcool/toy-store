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
    private com.toy.store.repository.MysteryBoxThemeRepository mysteryBoxThemeRepository;

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

        // Seed Activities
        seedActivities();
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

        // 2. Create Mystery Box Theme
        com.toy.store.model.MysteryBoxTheme theme = mysteryBoxThemeRepository.findByName(seriesName);
        if (theme == null) {
            theme = new com.toy.store.model.MysteryBoxTheme();
            theme.setName(seriesName);
            theme.setDescription(seriesName + " 專屬盲盒，內含鑰匙圈、毛巾與稀有公仔！");
            theme.setPrice(new BigDecimal("100.00"));
            theme = mysteryBoxThemeRepository.save(theme);
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

            // 4. Create Mystery Box Items (Lottery)
            createMysteryBoxItem(theme, subSeries + " 鑰匙圈", new BigDecimal("50.00"), 20);
            createMysteryBoxItem(theme, subSeries + " 毛巾", new BigDecimal("50.00"), 20);
            createMysteryBoxItem(theme, subSeries + " 系列公仔", new BigDecimal("350.00"), 1);
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

    private void createMysteryBoxItem(com.toy.store.model.MysteryBoxTheme theme, String name, BigDecimal value,
            int weight) {
        if (theme.getItems() == null) {
            theme.setItems(new java.util.ArrayList<>());
        }

        // Check if item already exists
        boolean exists = theme.getItems().stream().anyMatch(i -> i.getName().equals(name));
        if (!exists) {
            com.toy.store.model.MysteryBoxItem item = new com.toy.store.model.MysteryBoxItem();
            item.setTheme(theme);
            item.setName(name);
            item.setEstimatedValue(value);
            item.setWeight(weight);
            theme.getItems().add(item);
            mysteryBoxThemeRepository.save(theme);
        }
    }
}
