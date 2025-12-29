package com.toy.store.controller.api;

import com.toy.store.dto.ApiResponse;
import com.toy.store.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * 後台儀表板統計 API
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardApiController {

    private final MemberRepository memberRepository;
    private final CabinetItemRepository cabinetItemRepository;

    /**
     * 獲取總覽 KPI 數據
     */
    @GetMapping("/kpi")
    public ApiResponse<Map<String, Object>> getKpiData() {
        Map<String, Object> kpi = new HashMap<>();

        // 會員統計
        kpi.put("totalMembers", memberRepository.count());
        kpi.put("todayNewMembers", countTodayNewMembers());

        // 交易統計
        kpi.put("todayRevenue", calculateTodayRevenue());
        kpi.put("monthRevenue", calculateMonthRevenue());

        // 抽獎統計
        kpi.put("todayDraws", countTodayDraws());
        kpi.put("totalDraws", 0L);

        // 發貨統計
        kpi.put("pendingShipments", countPendingShipments());
        kpi.put("totalPrizes", cabinetItemRepository.count());

        return ApiResponse.ok(kpi);
    }

    /**
     * 獲取圖表數據（7天會員增長）
     */
    @GetMapping("/chart/members")
    public ApiResponse<Map<String, Object>> getMemberChart() {
        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            labels.add(date.getMonthValue() + "/" + date.getDayOfMonth());
            data.add((long) (Math.random() * 50 + 10)); // 模擬數據
        }

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("data", data);
        return ApiResponse.ok(result);
    }

    /**
     * 獲取圖表數據（7天營收）
     */
    @GetMapping("/chart/revenue")
    public ApiResponse<Map<String, Object>> getRevenueChart() {
        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            labels.add(date.getMonthValue() + "/" + date.getDayOfMonth());
            data.add(BigDecimal.valueOf(Math.random() * 10000 + 1000)
                    .setScale(2, RoundingMode.HALF_UP));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("data", data);
        return ApiResponse.ok(result);
    }

    /**
     * 獲取最新交易記錄
     */
    @GetMapping("/transactions/latest")
    public ApiResponse<List<Map<String, Object>>> getLatestTransactions() {
        List<Map<String, Object>> transactions = new ArrayList<>();
        // 模擬最近 5 筆交易
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> tx = new HashMap<>();
            tx.put("id", i);
            tx.put("member", "user" + (int) (Math.random() * 100));
            tx.put("type", Math.random() > 0.5 ? "RECHARGE" : "GACHA_SPEND");
            tx.put("amount", BigDecimal.valueOf(Math.random() * 500 + 50).setScale(2, RoundingMode.HALF_UP));
            tx.put("time", "5分鐘前");
            transactions.add(tx);
        }
        return ApiResponse.ok(transactions);
    }

    /**
     * 獲取熱門遊戲
     */
    @GetMapping("/games/hot")
    public ApiResponse<List<Map<String, Object>>> getHotGames() {
        List<Map<String, Object>> games = new ArrayList<>();
        String[] gameNames = { "海賊王一番賞", "鬼滅之刃轉盤", "火影忍者九宮格", "七龍珠盲盒" };
        for (int i = 0; i < gameNames.length; i++) {
            Map<String, Object> game = new HashMap<>();
            game.put("rank", i + 1);
            game.put("name", gameNames[i]);
            game.put("draws", (int) (Math.random() * 500 + 100));
            game.put("revenue", BigDecimal.valueOf(Math.random() * 20000 + 5000).setScale(0, RoundingMode.HALF_UP));
            games.add(game);
        }
        return ApiResponse.ok(games);
    }

    // ========== 輔助方法 ==========

    private long countTodayNewMembers() {
        return (long) (Math.random() * 30 + 5);
    }

    private BigDecimal calculateTodayRevenue() {
        return BigDecimal.valueOf(Math.random() * 5000 + 1000).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateMonthRevenue() {
        return BigDecimal.valueOf(Math.random() * 150000 + 30000).setScale(2, RoundingMode.HALF_UP);
    }

    private long countTodayDraws() {
        return (long) (Math.random() * 200 + 50);
    }

    private long countPendingShipments() {
        return (long) (Math.random() * 20 + 5);
    }
}
