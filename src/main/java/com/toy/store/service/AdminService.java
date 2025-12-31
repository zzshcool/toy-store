package com.toy.store.service;

import com.toy.store.exception.AppException;
import com.toy.store.exception.ResourceNotFoundException;
import com.toy.store.model.*;
import com.toy.store.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
@Service
@RequiredArgsConstructor
public class AdminService {

        private final MemberRepository memberRepository;
        private final TransactionRepository transactionRepository;
        private final GachaRecordRepository recordRepository;
        private final AdminActionLogRepository adminActionLogRepository;
        private final MemberActionLogRepository memberActionLogRepository;
        private final GachaThemeRepository gachaThemeRepository;
        private final ProductService productService;
        private final CategoryRepository categoryRepository;
        private final MemberLevelRepository memberLevelRepository;
        private final CouponRepository couponRepository;
        private final CarouselSlideRepository carouselSlideRepository;
        private final FeaturedItemRepository featuredItemRepository;
        private final NotificationRepository notificationRepository;
        private final GachaIpRepository gachaIpRepository;
        private final IchibanBoxRepository ichibanBoxRepository;
        private final RouletteGameRepository rouletteGameRepository;
        private final BingoGameRepository bingoGameRepository;
        private final RedeemShopItemRepository redeemShopItemRepository;
        private final CouponService couponService;
        private final SubCategoryRepository subCategoryRepository;
        private final GachaItemRepository gachaItemRepository;
        private final ActivityRepository activityRepository;
        private final ProductRepository productRepository;

        /**
         * 獲取儀表板所需的匯總數據
         */
        public Map<String, Object> getDashboardData(
                        int memberPage, int memberSize,
                        int txPage, int txSize,
                        int logPage, int logSize,
                        int gachaPage, int gachaSize) {

                Map<String, Object> data = new HashMap<>();

                // Paginated members
                Pageable memberPageable = PageRequest.of(memberPage, memberSize, Sort.by(Sort.Direction.DESC, "id"));
                data.put("membersPage", memberRepository.findAll(memberPageable));

                // Paginated transactions
                Pageable txPageable = PageRequest.of(txPage, txSize, Sort.by(Sort.Direction.DESC, "timestamp"));
                data.put("transactionsPage", transactionRepository.findAll(txPageable));

                // Paginated logs
                Pageable logPageable = PageRequest.of(logPage, logSize, Sort.by(Sort.Direction.DESC, "timestamp"));
                data.put("logsPage", adminActionLogRepository.findAll(logPageable));

                // Gacha Data (Paginated)
                Pageable gachaPageable = PageRequest.of(gachaPage, gachaSize,
                                Sort.by(Sort.Direction.DESC, "createdAt"));
                data.put("ichibanBoxes", ichibanBoxRepository.findAll(gachaPageable).getContent());
                data.put("rouletteGames", rouletteGameRepository.findAll(gachaPageable).getContent());
                data.put("bingoGames", bingoGameRepository.findAll(gachaPageable).getContent());

                // Static / Unpaginated Data
                data.put("products", productService.findAll(Pageable.unpaged()).getContent());
                data.put("gachaThemes", gachaThemeRepository.findAll());
                data.put("categories", categoryRepository.findAll());
                data.put("memberLevels", memberLevelRepository.findAllByOrderBySortOrderAsc());
                data.put("coupons", couponRepository.findAll());
                data.put("carouselSlides", carouselSlideRepository.findAllByOrderBySortOrderAsc());
                data.put("newArrivals",
                                featuredItemRepository
                                                .findByItemTypeOrderBySortOrderAsc(FeaturedItem.Type.NEW_ARRIVAL));
                data.put("bestSellers",
                                featuredItemRepository
                                                .findByItemTypeOrderBySortOrderAsc(FeaturedItem.Type.BEST_SELLER));
                data.put("notifications", notificationRepository.findAllByOrderByCreatedAtDesc());
                data.put("gachaIps", gachaIpRepository.findAllByOrderByCreatedAtDesc());
                data.put("redeemItems", redeemShopItemRepository.findAllByOrderBySortOrderAsc());

                return data;
        }

        @Transactional
        public void toggleMemberStatus(Long id, String adminName) {
                Member member = memberRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("會員", id));
                member.setEnabled(!member.isEnabled());
                memberRepository.save(member);

                adminActionLogRepository.save(new AdminActionLog(adminName, "TOGGLE_MEMBER_STATUS",
                                "Member: " + member.getUsername() + " (ID: " + id + "), New status: "
                                                + member.isEnabled(),
                                ""));
        }

        @Transactional
        public void updateMember(Long id, String email, String nickname, boolean enabled, MemberLevel level,
                        String adminName) {
                Member member = memberRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("會員", id));

                String oldEmail = member.getEmail();
                String oldNickname = member.getNickname();

                member.setEmail(email);
                member.setNickname(nickname);
                member.setEnabled(enabled);
                member.setLevel(level);
                memberRepository.save(member);

                adminActionLogRepository.save(new AdminActionLog(adminName, "UPDATE_MEMBER",
                                String.format("Updated member %s. Email: %s->%s, Nickname: %s->%s",
                                                member.getUsername(), oldEmail, email, oldNickname, nickname),
                                ""));
        }

        @Transactional
        public void saveProduct(Product product, String adminName) {
                productService.saveProduct(product);
                adminActionLogRepository.save(new AdminActionLog(adminName, "SAVE_PRODUCT",
                                "Product: " + product.getName() + " (Price: " + product.getPrice() + ")", ""));
        }

        @Transactional
        public void deleteProduct(Long id, String adminName) {
                productService.deleteProduct(id);
                adminActionLogRepository.save(new AdminActionLog(adminName, "DELETE_PRODUCT",
                                "Product ID: " + id, ""));
        }

        @Transactional
        public void createCategory(Category category, String adminName) {
                categoryRepository.save(category);
                adminActionLogRepository.save(new AdminActionLog(adminName, "CREATE_CATEGORY",
                                "Category: " + category.getName(), ""));
        }

        @Transactional
        public void createSubCategory(Long categoryId, String name, String adminName) {
                Category cat = categoryRepository.findById(categoryId)
                                .orElseThrow(() -> new ResourceNotFoundException("主分類", categoryId));
                SubCategory sub = new SubCategory();
                sub.setName(name);
                sub.setCategory(cat);
                subCategoryRepository.save(sub);

                adminActionLogRepository.save(new AdminActionLog(adminName, "CREATE_SUBCATEGORY",
                                "SubCategory: " + name + " under " + cat.getName(), ""));
        }

        @Transactional
        public void updateCategory(Long id, String name, String adminName) {
                Category cat = categoryRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("分類", id));
                String oldName = cat.getName();
                cat.setName(name);
                categoryRepository.save(cat);
                adminActionLogRepository.save(new AdminActionLog(adminName, "UPDATE_CATEGORY",
                                String.format("Updated %s -> %s", oldName, name), ""));
        }

        @Transactional
        public void deleteCategory(Long id, String adminName) {
                categoryRepository.deleteById(id);
                adminActionLogRepository.save(new AdminActionLog(adminName, "DELETE_CATEGORY",
                                "Category ID: " + id, ""));
        }

        @Transactional
        public void updateSubCategory(Long id, String name, String adminName) {
                SubCategory sub = subCategoryRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("次分類", id));
                String oldName = sub.getName();
                sub.setName(name);
                subCategoryRepository.save(sub);
                adminActionLogRepository.save(new AdminActionLog(adminName, "UPDATE_SUBCATEGORY",
                                String.format("Updated %s -> %s", oldName, name), ""));
        }

        @Transactional
        public void deleteSubCategory(Long id, String adminName) {
                subCategoryRepository.deleteById(id);
                adminActionLogRepository.save(new AdminActionLog(adminName, "DELETE_SUBCATEGORY",
                                "SubCategory ID: " + id, ""));
        }

        @Transactional
        public void createTheme(GachaTheme theme, String adminName) {
                gachaThemeRepository.save(theme);
                adminActionLogRepository.save(new AdminActionLog(adminName, "CREATE_THEME",
                                "Theme: " + theme.getName(), ""));
        }

        @Transactional
        public void updateTheme(Long id, String name, BigDecimal price, String adminName) {
                GachaTheme theme = gachaThemeRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("主題", id));
                theme.setName(name);
                theme.setPrice(price);
                gachaThemeRepository.save(theme);

                adminActionLogRepository.save(new AdminActionLog(adminName, "UPDATE_THEME",
                                "Theme: " + name + " (ID: " + id + ")", ""));
        }

        @Transactional
        public void deleteTheme(Long id, String adminName) {
                gachaThemeRepository.deleteById(id);
                adminActionLogRepository.save(new AdminActionLog(adminName, "DELETE_THEME",
                                "Theme ID: " + id, ""));
        }

        @Transactional
        public void createGachaItem(Long themeId, GachaItem item, String adminName) {
                GachaTheme theme = gachaThemeRepository.findById(themeId)
                                .orElseThrow(() -> new ResourceNotFoundException("主題", themeId));
                item.setTheme(theme);
                gachaItemRepository.save(item);
                adminActionLogRepository.save(new AdminActionLog(adminName, "CREATE_GACHA_ITEM",
                                "Item: " + item.getName() + " in " + theme.getName(), ""));
        }

        @Transactional
        public void deleteGachaItem(Long id, String adminName) {
                gachaItemRepository.deleteById(id);
                adminActionLogRepository.save(new AdminActionLog(adminName, "DELETE_GACHA_ITEM",
                                "Item ID: " + id, ""));
        }

        @Transactional
        public void updateGachaItem(Long id, String name, BigDecimal estimatedValue, Integer weight, String adminName) {
                GachaItem item = gachaItemRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("獎項", id));
                item.setName(name);
                item.setEstimatedValue(estimatedValue);
                item.setWeight(weight);
                gachaItemRepository.save(item);

                adminActionLogRepository.save(new AdminActionLog(adminName, "UPDATE_GACHA_ITEM",
                                "Item: " + name + " (ID: " + id + ")", ""));
        }

        @Transactional
        public void updateActivity(Long id, String title, String description, String type,
                        String startDate, String expiryDate, String adminName) {
                Activity activity = activityRepository.findById(id)
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
                activityRepository.save(activity);
                adminActionLogRepository.save(new AdminActionLog(adminName, "UPDATE_ACTIVITY",
                                "Activity: " + title, ""));
        }

        @Transactional
        public void toggleActivity(Long id, String adminName) {
                Activity activity = activityRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("活動", id));
                activity.setActive(!activity.isActive());
                activityRepository.save(activity);
                adminActionLogRepository.save(new AdminActionLog(adminName, "TOGGLE_ACTIVITY",
                                "Activity: " + activity.getTitle() + ", Active: " + activity.isActive(), ""));
        }

        @Transactional
        public void deleteActivity(Long id, String adminName) {
                activityRepository.deleteById(id);
                adminActionLogRepository.save(new AdminActionLog(adminName, "DELETE_ACTIVITY",
                                "Activity ID: " + id, ""));
        }

        @Transactional
        public void saveMemberLevel(MemberLevel level, String adminName) {
                memberLevelRepository.save(level);
                adminActionLogRepository.save(new AdminActionLog(adminName, "SAVE_MEMBER_LEVEL",
                                "Level: " + level.getName(), ""));
        }

        @Transactional
        public void deleteMemberLevel(Long id, String adminName) {
                memberLevelRepository.deleteById(id);
                adminActionLogRepository.save(new AdminActionLog(adminName, "DELETE_MEMBER_LEVEL",
                                "Level ID: " + id, ""));
        }

        @Transactional
        public CarouselSlide createCarouselSlide(String imageUrl, String linkUrl, String adminName) {
                CarouselSlide slide = new CarouselSlide();
                slide.setImageUrl(imageUrl);
                slide.setLinkUrl(linkUrl);
                slide.setSortOrder(0);
                CarouselSlide saved = carouselSlideRepository.save(slide);
                adminActionLogRepository.save(new AdminActionLog(adminName, "CREATE_CAROUSEL",
                                "Image: " + imageUrl, ""));
                return saved;
        }

        @Transactional
        public void deleteCarouselSlide(Long id, String adminName) {
                carouselSlideRepository.deleteById(id);
                adminActionLogRepository.save(new AdminActionLog(adminName, "DELETE_CAROUSEL",
                                "ID: " + id, ""));
        }

        @Transactional
        public FeaturedItem addFeaturedItem(Long productId, FeaturedItem.Type type, Integer sortOrder,
                        String adminName) {
                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new ResourceNotFoundException("產品", productId));
                FeaturedItem item = new FeaturedItem();
                item.setProduct(product);
                item.setItemType(type);
                item.setSortOrder(sortOrder);
                FeaturedItem saved = featuredItemRepository.save(item);
                adminActionLogRepository.save(new AdminActionLog(adminName, "ADD_FEATURED",
                                "Product: " + product.getName() + " Type: " + type, ""));
                return saved;
        }

        @Transactional
        public void deleteFeaturedItem(Long id, String adminName) {
                featuredItemRepository.deleteById(id);
                adminActionLogRepository.save(new AdminActionLog(adminName, "DELETE_FEATURED",
                                "ID: " + id, ""));
        }

        @Transactional
        public Notification sendNotification(Notification notification, String adminName) {
                Notification saved = notificationRepository.save(notification);
                adminActionLogRepository.save(new AdminActionLog(adminName, "SEND_NOTIFICATION",
                                "Title: " + notification.getTitle(), ""));
                return saved;
        }

        public Map<String, Object> getMemberHistory(Long memberId) {
                Map<String, Object> history = new HashMap<>();

                Member member = memberRepository.findById(memberId)
                                .orElseThrow(() -> new ResourceNotFoundException("會員", memberId));

                history.put("member", member);
                history.put("transactions", transactionRepository.findByMemberIdOrderByTimestampDesc(memberId));
                history.put("actionLogs", memberActionLogRepository.findByMemberIdOrderByTimestampDesc(memberId));

                return history;
        }

        @Transactional
        public void createCoupon(String name, String code, Coupon.CouponType type, java.math.BigDecimal value,
                        String description, String validFrom, String validUntil, String adminName) {
                LocalDateTime from = (validFrom != null && !validFrom.isEmpty()) ? LocalDateTime.parse(validFrom)
                                : null;
                LocalDateTime until = (validUntil != null && !validUntil.isEmpty()) ? LocalDateTime.parse(validUntil)
                                : null;
                Coupon coupon = couponService.createCoupon(name, code, type, value, description, from, until);
                adminActionLogRepository.save(new AdminActionLog(adminName, "CREATE_COUPON",
                                "Coupon: " + coupon.getName() + " (Code: " + coupon.getCode() + ")", ""));
        }

        @Transactional
        public void distributeToLevel(Long couponId, Long levelId, String adminName) {
                couponService.distributeToLevel(couponId, levelId);
                adminActionLogRepository.save(new AdminActionLog(adminName, "DISTRIBUTE_COUPON_LEVEL",
                                "Coupon ID: " + couponId + " to Level ID: " + levelId, ""));
        }

        @Transactional
        public void distributeToMember(Long couponId, Long memberId, String adminName) {
                couponService.distributeToMember(couponId, memberId);
                adminActionLogRepository.save(new AdminActionLog(adminName, "DISTRIBUTE_COUPON_MEMBER",
                                "Coupon ID: " + couponId + " to Member ID: " + memberId, ""));
        }

        /**
         * 計算各機台的即時 RTP (Return to Player)
         */
        public List<Map<String, Object>> getRtpStats() {
                List<GachaRecord> allRecords = recordRepository.findAll();

                // 分組計算
                Map<String, List<GachaRecord>> grouped = allRecords.stream()
                                .collect(java.util.stream.Collectors
                                                .groupingBy(r -> r.getGachaType() + "_" + r.getGameId()));

                List<Map<String, Object>> stats = new ArrayList<>();

                grouped.forEach((key, records) -> {
                        String[] parts = key.split("_");
                        GachaRecord.GachaType type = GachaRecord.GachaType.valueOf(parts[0]);
                        Long gameId = Long.parseLong(parts[1]);

                        BigDecimal totalOutcome = records.stream()
                                        .map(GachaRecord::getPrizeValue)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                        BigDecimal pricePerDraw = BigDecimal.ZERO;
                        String gameName = "Unknown";

                        // 根據類型獲取單價與名稱
                        if (null != type)
                                switch (type) {
                                        case ICHIBAN -> {
                                                IchibanBox box = ichibanBoxRepository.findById(gameId).orElse(null);
                                                if (box != null) {
                                                        pricePerDraw = box.getPricePerDraw();
                                                        gameName = box.getName();
                                                }
                                        }
                                        case ROULETTE -> {
                                                RouletteGame game = rouletteGameRepository.findById(gameId)
                                                                .orElse(null);
                                                if (game != null) {
                                                        pricePerDraw = game.getPricePerSpin();
                                                        gameName = game.getName();
                                                }
                                        }
                                        case BINGO -> {
                                                BingoGame game = bingoGameRepository.findById(gameId).orElse(null);
                                                if (game != null) {
                                                        pricePerDraw = game.getPricePerDig();
                                                        gameName = game.getName();
                                                }
                                        }
                                        default -> {
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
                        stat.put("type", type != null ? type.getDisplayName() : "未知");
                        stat.put("totalDraws", records.size());
                        stat.put("totalIncome", totalIncome);
                        stat.put("totalOutcome", totalOutcome);
                        stat.put("rtp", rtp);
                        stats.add(stat);
                });

                return stats;
        }
}
