package com.toy.store.service;

import com.toy.store.model.*;
import com.toy.store.repository.*;
import com.toy.store.exception.AppException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Service
public class AdminService {

        @Autowired
        private MemberRepository memberRepository;

        @Autowired
        private TransactionRepository transactionRepository;

        @Autowired
        private AdminActionLogRepository adminActionLogRepository;

        @Autowired
        private MemberActionLogRepository memberActionLogRepository;

        @Autowired
        private GachaThemeRepository gachaThemeRepository;

        @Autowired
        private ProductService productService;

        @Autowired
        private CategoryRepository categoryRepository;

        @Autowired
        private MemberLevelRepository memberLevelRepository;

        @Autowired
        private CouponRepository couponRepository;

        @Autowired
        private CarouselSlideRepository carouselSlideRepository;

        @Autowired
        private FeaturedItemRepository featuredItemRepository;

        @Autowired
        private NotificationRepository notificationRepository;

        @Autowired
        private GachaIpRepository gachaIpRepository;

        @Autowired
        private IchibanBoxRepository ichibanBoxRepository;

        @Autowired
        private RouletteGameRepository rouletteGameRepository;

        @Autowired
        private BingoGameRepository bingoGameRepository;

        @Autowired
        private RedeemShopItemRepository redeemShopItemRepository;

        @Autowired
        private CouponService couponService;

        @Autowired
        private SubCategoryRepository subCategoryRepository;

        @Autowired
        private GachaItemRepository gachaItemRepository;

        @Autowired
        private ActivityRepository activityRepository;

        @Autowired
        private ProductRepository productRepository;

        /**
         * 獲取儀表板所需的匯總數據 (Get summary data for dashboard)
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
                data.put("ichibanPage", ichibanBoxRepository.findAll(gachaPageable));
                data.put("roulettePage", rouletteGameRepository.findAll(gachaPageable));
                data.put("bingoPage", bingoGameRepository.findAll(gachaPageable));

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
                if (id == null)
                        throw new AppException("ID 不能為空 (ID cannot be null)");
                Member member = memberRepository.findById(id)
                                .orElseThrow(() -> new AppException("找不到該會員 (Member not found)"));
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
                if (id == null)
                        throw new AppException("ID 不能為空 (ID cannot be null)");
                Member member = memberRepository.findById(id)
                                .orElseThrow(() -> new AppException("找不到該會員 (Member not found)"));

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
                if (id == null)
                        throw new AppException("ID 不能為空 (ID cannot be null)");
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
                if (categoryId == null)
                        throw new AppException("分類 ID 不能為空 (Category ID cannot be null)");
                Category cat = categoryRepository.findById(categoryId)
                                .orElseThrow(() -> new AppException("找不到主分類 (Category not found)"));
                SubCategory sub = new SubCategory();
                sub.setName(name);
                sub.setCategory(cat);
                subCategoryRepository.save(sub);

                adminActionLogRepository.save(new AdminActionLog(adminName, "CREATE_SUBCATEGORY",
                                "SubCategory: " + name + " under " + cat.getName(), ""));
        }

        @Transactional
        public void updateCategory(Long id, String name, String adminName) {
                if (id == null)
                        throw new AppException("ID 不能為空 (ID cannot be null)");
                Category cat = categoryRepository.findById(id)
                                .orElseThrow(() -> new AppException("找不到分類 (Category not found)"));
                String oldName = cat.getName();
                cat.setName(name);
                categoryRepository.save(cat);
                adminActionLogRepository.save(new AdminActionLog(adminName, "UPDATE_CATEGORY",
                                String.format("Updated %s -> %s", oldName, name), ""));
        }

        @Transactional
        public void deleteCategory(Long id, String adminName) {
                if (id == null)
                        throw new AppException("ID 不能為空 (ID cannot be null)");
                categoryRepository.deleteById(id);
                adminActionLogRepository.save(new AdminActionLog(adminName, "DELETE_CATEGORY",
                                "Category ID: " + id, ""));
        }

        @Transactional
        public void updateSubCategory(Long id, String name, String adminName) {
                if (id == null)
                        throw new AppException("ID 不能為空 (ID cannot be null)");
                SubCategory sub = subCategoryRepository.findById(id)
                                .orElseThrow(() -> new AppException("找不到次分類 (SubCategory not found)"));
                String oldName = sub.getName();
                sub.setName(name);
                subCategoryRepository.save(sub);
                adminActionLogRepository.save(new AdminActionLog(adminName, "UPDATE_SUBCATEGORY",
                                String.format("Updated %s -> %s", oldName, name), ""));
        }

        @Transactional
        public void deleteSubCategory(Long id, String adminName) {
                if (id == null)
                        throw new AppException("ID 不能為空 (ID cannot be null)");
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
                if (id == null)
                        throw new AppException("ID 不能為空 (ID cannot be null)");
                GachaTheme theme = gachaThemeRepository.findById(id)
                                .orElseThrow(() -> new AppException("找不到主題 (Theme not found)"));
                theme.setName(name);
                theme.setPrice(price);
                gachaThemeRepository.save(theme);

                adminActionLogRepository.save(new AdminActionLog(adminName, "UPDATE_THEME",
                                "Theme: " + name + " (ID: " + id + ")", ""));
        }

        @Transactional
        public void deleteTheme(Long id, String adminName) {
                if (id == null)
                        throw new AppException("ID 不能為空 (ID cannot be null)");
                gachaThemeRepository.deleteById(id);
                adminActionLogRepository.save(new AdminActionLog(adminName, "DELETE_THEME",
                                "Theme ID: " + id, ""));
        }

        @Transactional
        public void createGachaItem(Long themeId, GachaItem item, String adminName) {
                if (themeId == null)
                        throw new AppException("主題 ID 不能為空 (Theme ID cannot be null)");
                GachaTheme theme = gachaThemeRepository.findById(themeId)
                                .orElseThrow(() -> new AppException("找不到主題 (Theme not found)"));
                item.setTheme(theme);
                gachaItemRepository.save(item);
                adminActionLogRepository.save(new AdminActionLog(adminName, "CREATE_GACHA_ITEM",
                                "Item: " + item.getName() + " in " + theme.getName(), ""));
        }

        @Transactional
        public void deleteGachaItem(Long id, String adminName) {
                if (id == null)
                        throw new AppException("ID 不能為空 (ID cannot be null)");
                gachaItemRepository.deleteById(id);
                adminActionLogRepository.save(new AdminActionLog(adminName, "DELETE_GACHA_ITEM",
                                "Item ID: " + id, ""));
        }

        @Transactional
        public void updateGachaItem(Long id, String name, BigDecimal estimatedValue, Integer weight, String adminName) {
                if (id == null)
                        throw new AppException("ID 不能為空 (ID cannot be null)");
                GachaItem item = gachaItemRepository.findById(id)
                                .orElseThrow(() -> new AppException("找不到獎項 (Item not found)"));
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
                if (id == null)
                        throw new AppException("ID 不能為空 (ID cannot be null)");
                Activity activity = activityRepository.findById(id)
                                .orElseThrow(() -> new AppException("找不到活動 (Activity not found)"));
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
                if (id == null)
                        throw new AppException("ID 不能為空 (ID cannot be null)");
                Activity activity = activityRepository.findById(id)
                                .orElseThrow(() -> new AppException("找不到活動 (Activity not found)"));
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
                                .orElseThrow(() -> new AppException("找不到產品 (Product not found)"));
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
                                .orElseThrow(() -> new AppException("找不到該會員 (Member not found)"));

                history.put("member", member);
                history.put("transactions", transactionRepository.findByMemberIdOrderByTimestampDesc(memberId));

                // 補足會員日誌 (補完功能)
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
                if (couponId == null || levelId == null)
                        throw new AppException("ID 不能為空 (ID cannot be null)");
                couponService.distributeToLevel(couponId, levelId);
                adminActionLogRepository.save(new AdminActionLog(adminName, "DISTRIBUTE_COUPON_LEVEL",
                                "Coupon ID: " + couponId + " to Level ID: " + levelId, ""));
        }

        @Transactional
        public void distributeToMember(Long couponId, Long memberId, String adminName) {
                if (couponId == null || memberId == null)
                        throw new AppException("ID 不能為空 (ID cannot be null)");
                couponService.distributeToMember(couponId, memberId);
                adminActionLogRepository.save(new AdminActionLog(adminName, "DISTRIBUTE_COUPON_MEMBER",
                                "Coupon ID: " + couponId + " to Member ID: " + memberId, ""));
        }
}
