package com.toy.store.service;

import com.toy.store.exception.ResourceNotFoundException;
import com.toy.store.model.*;
import com.toy.store.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 後台管理服務
 */
@Slf4j
@Service
public class AdminService {

        private final MemberMapper memberMapper;
        private final TransactionMapper transactionMapper;
        private final GachaRecordMapper recordMapper;
        private final AdminActionLogMapper adminActionLogMapper;
        private final MemberActionLogMapper memberActionLogMapper;
        private final GachaThemeMapper gachaThemeMapper;
        private final ProductService productService;
        private final CategoryMapper categoryMapper;
        private final MemberLevelMapper memberLevelMapper;
        private final CouponMapper couponMapper;
        private final CarouselSlideMapper carouselSlideMapper;
        private final FeaturedItemMapper featuredItemMapper;
        private final NotificationMapper notificationMapper;
        private final GachaIpMapper gachaIpMapper;
        private final IchibanBoxMapper ichibanBoxMapper;
        private final RouletteGameMapper rouletteGameMapper;
        private final BingoGameMapper bingoGameMapper;
        private final RedeemShopItemMapper redeemShopItemMapper;
        private final CouponService couponService;
        private final SubCategoryMapper subCategoryMapper;
        private final GachaItemMapper gachaItemMapper;
        private final ActivityMapper activityMapper;
        private final ProductMapper productMapper;

        public AdminService(
                        MemberMapper memberMapper,
                        TransactionMapper transactionMapper,
                        GachaRecordMapper recordMapper,
                        AdminActionLogMapper adminActionLogMapper,
                        MemberActionLogMapper memberActionLogMapper,
                        GachaThemeMapper gachaThemeMapper,
                        ProductService productService,
                        CategoryMapper categoryMapper,
                        MemberLevelMapper memberLevelMapper,
                        CouponMapper couponMapper,
                        CarouselSlideMapper carouselSlideMapper,
                        FeaturedItemMapper featuredItemMapper,
                        NotificationMapper notificationMapper,
                        GachaIpMapper gachaIpMapper,
                        IchibanBoxMapper ichibanBoxMapper,
                        RouletteGameMapper rouletteGameMapper,
                        BingoGameMapper bingoGameMapper,
                        RedeemShopItemMapper redeemShopItemMapper,
                        CouponService couponService,
                        SubCategoryMapper subCategoryMapper,
                        GachaItemMapper gachaItemMapper,
                        ActivityMapper activityMapper,
                        ProductMapper productMapper) {
                this.memberMapper = memberMapper;
                this.transactionMapper = transactionMapper;
                this.recordMapper = recordMapper;
                this.adminActionLogMapper = adminActionLogMapper;
                this.memberActionLogMapper = memberActionLogMapper;
                this.gachaThemeMapper = gachaThemeMapper;
                this.productService = productService;
                this.categoryMapper = categoryMapper;
                this.memberLevelMapper = memberLevelMapper;
                this.couponMapper = couponMapper;
                this.carouselSlideMapper = carouselSlideMapper;
                this.featuredItemMapper = featuredItemMapper;
                this.notificationMapper = notificationMapper;
                this.gachaIpMapper = gachaIpMapper;
                this.ichibanBoxMapper = ichibanBoxMapper;
                this.rouletteGameMapper = rouletteGameMapper;
                this.bingoGameMapper = bingoGameMapper;
                this.redeemShopItemMapper = redeemShopItemMapper;
                this.couponService = couponService;
                this.subCategoryMapper = subCategoryMapper;
                this.gachaItemMapper = gachaItemMapper;
                this.activityMapper = activityMapper;
                this.productMapper = productMapper;
        }

        /**
         * 獲取儀表板所需的匯總數據
         */
        public Map<String, Object> getDashboardData(
                        int memberPage, int memberSize, String memberSearch,
                        int txPage, int txSize,
                        int logPage, int logSize,
                        int gachaPage, int gachaSize) {

                Map<String, Object> data = new HashMap<>();

                // Members with optional search (簡化版，不分頁)
                if (memberSearch != null && !memberSearch.trim().isEmpty()) {
                        String search = "%" + memberSearch.trim().toLowerCase() + "%";
                        data.put("members", memberMapper.searchByKeywordNoPage(search));
                } else {
                        data.put("members", memberMapper.findAll());
                }

                // Transactions
                data.put("transactions", transactionMapper.findAll());

                // Logs
                data.put("logs", adminActionLogMapper.findAll());

                // Gacha Data
                data.put("ichibanBoxes", ichibanBoxMapper.findAll());
                data.put("rouletteGames", rouletteGameMapper.findAll());
                data.put("bingoGames", bingoGameMapper.findAll());

                // Static / Unpaginated Data
                data.put("products", productMapper.findAll());
                data.put("gachaThemes", gachaThemeMapper.findAll());
                data.put("categories", categoryMapper.findAll());
                data.put("memberLevels", memberLevelMapper.findAllByOrderBySortOrderAsc());
                data.put("coupons", couponMapper.findAll());
                data.put("carouselSlides", carouselSlideMapper.findAllByOrderBySortOrderAsc());
                data.put("newArrivals", featuredItemMapper
                                .findByItemTypeOrderBySortOrderAsc(FeaturedItem.Type.NEW_ARRIVAL.name()));
                data.put("bestSellers", featuredItemMapper
                                .findByItemTypeOrderBySortOrderAsc(FeaturedItem.Type.BEST_SELLER.name()));
                data.put("notifications", notificationMapper.findAllByOrderByCreatedAtDesc());
                data.put("gachaIps", gachaIpMapper.findAllByOrderByCreatedAtDesc());
                data.put("redeemItems", redeemShopItemMapper.findAllByOrderBySortOrderAsc());

                return data;
        }

        @Transactional
        public void toggleMemberStatus(Long id, String adminName) {
                Member member = memberMapper.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("會員", id));
                member.setEnabled(!member.isEnabled());
                memberMapper.update(member);

                logAdminAction(adminName, "TOGGLE_MEMBER_STATUS",
                                "Member: " + member.getUsername() + " (ID: " + id + "), New status: "
                                                + member.isEnabled());
        }

        @Transactional
        public void updateMember(Long id, String email, String nickname, boolean enabled, Long levelId,
                        String adminName) {
                Member member = memberMapper.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("會員", id));

                String oldEmail = member.getEmail();
                String oldNickname = member.getNickname();

                member.setEmail(email);
                member.setNickname(nickname);
                member.setEnabled(enabled);
                member.setMemberLevelId(levelId);
                memberMapper.update(member);

                logAdminAction(adminName, "UPDATE_MEMBER",
                                String.format("Updated member %s. Email: %s->%s, Nickname: %s->%s",
                                                member.getUsername(), oldEmail, email, oldNickname, nickname));
        }

        @Transactional
        public void saveProduct(Product product, String adminName) {
                productService.saveProduct(product);
                logAdminAction(adminName, "SAVE_PRODUCT",
                                "Product: " + product.getName() + " (Price: " + product.getPrice() + ")");
        }

        @Transactional
        public void deleteProduct(Long id, String adminName) {
                productService.deleteProduct(id);
                logAdminAction(adminName, "DELETE_PRODUCT", "Product ID: " + id);
        }

        @Transactional
        public void createCategory(Category category, String adminName) {
                categoryMapper.insert(category);
                logAdminAction(adminName, "CREATE_CATEGORY", "Category: " + category.getName());
        }

        @Transactional
        public void createSubCategory(Long categoryId, String name, String adminName) {
                Category cat = categoryMapper.findById(categoryId)
                                .orElseThrow(() -> new ResourceNotFoundException("主分類", categoryId));
                SubCategory sub = new SubCategory();
                sub.setName(name);
                sub.setCategoryId(categoryId);
                subCategoryMapper.insert(sub);

                logAdminAction(adminName, "CREATE_SUBCATEGORY",
                                "SubCategory: " + name + " under " + cat.getName());
        }

        @Transactional
        public void updateCategory(Long id, String name, String adminName) {
                Category cat = categoryMapper.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("分類", id));
                String oldName = cat.getName();
                cat.setName(name);
                categoryMapper.update(cat);
                logAdminAction(adminName, "UPDATE_CATEGORY",
                                String.format("Updated %s -> %s", oldName, name));
        }

        @Transactional
        public void deleteCategory(Long id, String adminName) {
                categoryMapper.deleteById(id);
                logAdminAction(adminName, "DELETE_CATEGORY", "Category ID: " + id);
        }

        @Transactional
        public void updateSubCategory(Long id, String name, String adminName) {
                SubCategory sub = subCategoryMapper.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("次分類", id));
                String oldName = sub.getName();
                sub.setName(name);
                subCategoryMapper.update(sub);
                logAdminAction(adminName, "UPDATE_SUBCATEGORY",
                                String.format("Updated %s -> %s", oldName, name));
        }

        @Transactional
        public void deleteSubCategory(Long id, String adminName) {
                subCategoryMapper.deleteById(id);
                logAdminAction(adminName, "DELETE_SUBCATEGORY", "SubCategory ID: " + id);
        }

        @Transactional
        public void createTheme(GachaTheme theme, String adminName) {
                gachaThemeMapper.insert(theme);
                logAdminAction(adminName, "CREATE_THEME", "Theme: " + theme.getName());
        }

        @Transactional
        public void updateTheme(Long id, String name, BigDecimal price, String adminName) {
                GachaTheme theme = gachaThemeMapper.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("主題", id));
                theme.setName(name);
                theme.setPrice(price);
                gachaThemeMapper.update(theme);

                logAdminAction(adminName, "UPDATE_THEME", "Theme: " + name + " (ID: " + id + ")");
        }

        @Transactional
        public void deleteTheme(Long id, String adminName) {
                gachaThemeMapper.deleteById(id);
                logAdminAction(adminName, "DELETE_THEME", "Theme ID: " + id);
        }

        @Transactional
        public void createGachaItem(Long themeId, GachaItem item, String adminName) {
                GachaTheme theme = gachaThemeMapper.findById(themeId)
                                .orElseThrow(() -> new ResourceNotFoundException("主題", themeId));
                item.setIpId(themeId);
                gachaItemMapper.insert(item);
                logAdminAction(adminName, "CREATE_GACHA_ITEM",
                                "Item: " + item.getName() + " in " + theme.getName());
        }

        @Transactional
        public void deleteGachaItem(Long id, String adminName) {
                gachaItemMapper.deleteById(id);
                logAdminAction(adminName, "DELETE_GACHA_ITEM", "Item ID: " + id);
        }

        @Transactional
        public void updateGachaItem(Long id, String name, BigDecimal estimatedValue, Integer weight, String adminName) {
                GachaItem item = gachaItemMapper.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("獎項", id));
                item.setName(name);
                item.setEstimatedValue(estimatedValue);
                item.setWeight(weight);
                gachaItemMapper.update(item);

                logAdminAction(adminName, "UPDATE_GACHA_ITEM", "Item: " + name + " (ID: " + id + ")");
        }

        @Transactional
        public void updateActivity(Long id, String title, String description, String type,
                        String startDate, String expiryDate, String adminName) {
                Activity activity = activityMapper.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("活動", id));
                activity.setTitle(title);
                activity.setDescription(description);
                activity.setType(type);
                if (startDate != null && !startDate.isEmpty()) {
                        activity.setStartDate(LocalDateTime.parse(startDate));
                }
                if (expiryDate != null && !expiryDate.isEmpty()) {
                        activity.setExpiryDate(LocalDateTime.parse(expiryDate));
                }
                activityMapper.update(activity);
                logAdminAction(adminName, "UPDATE_ACTIVITY", "Activity: " + title);
        }

        @Transactional
        public void toggleActivity(Long id, String adminName) {
                Activity activity = activityMapper.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("活動", id));
                activity.setActive(!activity.isActive());
                activityMapper.update(activity);
                logAdminAction(adminName, "TOGGLE_ACTIVITY",
                                "Activity: " + activity.getTitle() + ", Active: " + activity.isActive());
        }

        @Transactional
        public void deleteActivity(Long id, String adminName) {
                activityMapper.deleteById(id);
                logAdminAction(adminName, "DELETE_ACTIVITY", "Activity ID: " + id);
        }

        @Transactional
        public void saveMemberLevel(MemberLevel level, String adminName) {
                if (level.getId() == null) {
                        memberLevelMapper.insert(level);
                } else {
                        memberLevelMapper.update(level);
                }
                logAdminAction(adminName, "SAVE_MEMBER_LEVEL", "Level: " + level.getName());
        }

        @Transactional
        public void deleteMemberLevel(Long id, String adminName) {
                memberLevelMapper.deleteById(id);
                logAdminAction(adminName, "DELETE_MEMBER_LEVEL", "Level ID: " + id);
        }

        @Transactional
        public CarouselSlide createCarouselSlide(String imageUrl, String linkUrl, String adminName) {
                CarouselSlide slide = new CarouselSlide();
                slide.setImageUrl(imageUrl);
                slide.setLinkUrl(linkUrl);
                slide.setSortOrder(0);
                slide.setCreatedAt(LocalDateTime.now());
                carouselSlideMapper.insert(slide);
                logAdminAction(adminName, "CREATE_CAROUSEL", "Image: " + imageUrl);
                return slide;
        }

        @Transactional
        public void deleteCarouselSlide(Long id, String adminName) {
                carouselSlideMapper.deleteById(id);
                logAdminAction(adminName, "DELETE_CAROUSEL", "ID: " + id);
        }

        @Transactional
        public FeaturedItem addFeaturedItem(Long productId, FeaturedItem.Type type, Integer sortOrder,
                        String adminName) {
                Product product = productMapper.findById(productId)
                                .orElseThrow(() -> new ResourceNotFoundException("產品", productId));
                FeaturedItem item = new FeaturedItem();
                item.setItemId(productId);
                item.setItemType(type.name());
                item.setSortOrder(sortOrder);
                item.setCreatedAt(LocalDateTime.now());
                featuredItemMapper.insert(item);
                logAdminAction(adminName, "ADD_FEATURED",
                                "Product: " + product.getName() + " Type: " + type);
                return item;
        }

        @Transactional
        public void deleteFeaturedItem(Long id, String adminName) {
                featuredItemMapper.deleteById(id);
                logAdminAction(adminName, "DELETE_FEATURED", "ID: " + id);
        }

        @Transactional
        public Notification sendNotification(Notification notification, String adminName) {
                notification.setCreatedAt(LocalDateTime.now());
                notificationMapper.insert(notification);
                logAdminAction(adminName, "SEND_NOTIFICATION", "Title: " + notification.getTitle());
                return notification;
        }

        public Map<String, Object> getMemberHistory(Long memberId) {
                Map<String, Object> history = new HashMap<>();

                Member member = memberMapper.findById(memberId)
                                .orElseThrow(() -> new ResourceNotFoundException("會員", memberId));

                history.put("member", member);
                history.put("transactions", transactionMapper.findByMemberId(memberId));
                history.put("actionLogs", memberActionLogMapper.findByMemberIdOrderByTimestampDesc(memberId));

                return history;
        }

        @Transactional
        public void createCoupon(String name, String code, String type, java.math.BigDecimal value,
                        String description, String validFrom, String validUntil, String adminName) {
                LocalDateTime from = (validFrom != null && !validFrom.isEmpty()) ? LocalDateTime.parse(validFrom)
                                : null;
                LocalDateTime until = (validUntil != null && !validUntil.isEmpty()) ? LocalDateTime.parse(validUntil)
                                : null;
                Coupon coupon = couponService.createCoupon(name, code, type, value, description, from, until);
                logAdminAction(adminName, "CREATE_COUPON",
                                "Coupon: " + coupon.getName() + " (Code: " + coupon.getCode() + ")");
        }

        @Transactional
        public void distributeToLevel(Long couponId, Long levelId, String adminName) {
                couponService.distributeToLevel(couponId, levelId);
                logAdminAction(adminName, "DISTRIBUTE_COUPON_LEVEL",
                                "Coupon ID: " + couponId + " to Level ID: " + levelId);
        }

        @Transactional
        public void distributeToMember(Long couponId, Long memberId, String adminName) {
                couponService.distributeToMember(couponId, memberId);
                logAdminAction(adminName, "DISTRIBUTE_COUPON_MEMBER",
                                "Coupon ID: " + couponId + " to Member ID: " + memberId);
        }

        /**
         * 計算各機台的即時 RTP (Return to Player)
         */
        public List<Map<String, Object>> getRtpStats() {
                List<GachaRecord> allRecords = recordMapper.findAll();

                // 分組計算
                Map<String, List<GachaRecord>> grouped = allRecords.stream()
                                .collect(java.util.stream.Collectors
                                                .groupingBy(r -> r.getGachaType() + "_" + r.getGameId()));

                List<Map<String, Object>> stats = new ArrayList<>();

                grouped.forEach((key, records) -> {
                        String[] parts = key.split("_");
                        String type = parts[0];
                        Long gameId = Long.parseLong(parts[1]);

                        BigDecimal totalOutcome = records.stream()
                                        .map(GachaRecord::getPrizeValue)
                                        .filter(v -> v != null)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                        BigDecimal pricePerDraw = BigDecimal.ZERO;
                        String gameName = "Unknown";

                        // 根據類型獲取單價與名稱
                        if ("ICHIBAN".equals(type)) {
                                IchibanBox box = ichibanBoxMapper.findById(gameId).orElse(null);
                                if (box != null) {
                                        pricePerDraw = box.getPricePerDraw();
                                        gameName = box.getName();
                                }
                        } else if ("ROULETTE".equals(type)) {
                                RouletteGame game = rouletteGameMapper.findById(gameId).orElse(null);
                                if (game != null) {
                                        pricePerDraw = game.getPricePerSpin();
                                        gameName = game.getName();
                                }
                        } else if ("BINGO".equals(type)) {
                                BingoGame game = bingoGameMapper.findById(gameId).orElse(null);
                                if (game != null) {
                                        pricePerDraw = game.getPricePerDig();
                                        gameName = game.getName();
                                }
                        }

                        BigDecimal totalIncome = pricePerDraw.multiply(new BigDecimal(records.size()));
                        double rtp = 0;
                        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
                                rtp = totalOutcome.divide(totalIncome, 4, java.math.RoundingMode.HALF_UP).doubleValue()
                                                * 100;
                        }

                        Map<String, Object> stat = new HashMap<>();
                        stat.put("gameName", gameName);
                        stat.put("type", type);
                        stat.put("totalDraws", records.size());
                        stat.put("totalIncome", totalIncome);
                        stat.put("totalOutcome", totalOutcome);
                        stat.put("rtp", rtp);
                        stats.add(stat);
                });

                return stats;
        }

        /**
         * 記錄管理員操作日誌
         */
        private void logAdminAction(String adminName, String action, String details) {
                AdminActionLog log = new AdminActionLog();
                log.setAdminName(adminName);
                log.setAction(action);
                log.setDetails(details);
                log.setCreatedAt(LocalDateTime.now());
                adminActionLogMapper.insert(log);
        }
}
