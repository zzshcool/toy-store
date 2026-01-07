package com.toy.store.controller.api;

import com.toy.store.annotation.CurrentUser;
import com.toy.store.dto.ApiResponse;
import com.toy.store.exception.AppException;
import com.toy.store.model.*;
import com.toy.store.mapper.MemberMapper;
import com.toy.store.service.ShardService;
import com.toy.store.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 碎片與兌換商店 API
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ShardApiController {

    private final ShardService shardService;
    private final MemberMapper memberMapper;

    @GetMapping("/shards/balance")
    public ApiResponse<Map<String, Object>> getBalance(@CurrentUser TokenService.TokenInfo info) {
        Long memberId = getMemberId(info);
        if (memberId == null) {
            return ApiResponse.error("請先登入");
        }

        int balance = shardService.getShardBalance(memberId);
        Map<String, Object> result = new HashMap<>();
        result.put("balance", balance);
        return ApiResponse.ok(result);
    }

    @GetMapping("/shards/transactions")
    public ApiResponse<List<Map<String, Object>>> getTransactions(@CurrentUser TokenService.TokenInfo info) {
        Long memberId = getMemberId(info);
        if (memberId == null) {
            return ApiResponse.error("請先登入");
        }

        List<ShardTransaction> transactions = shardService.getTransactions(memberId);
        List<Map<String, Object>> result = transactions.stream()
                .map(this::transactionToMap)
                .collect(Collectors.toList());
        return ApiResponse.ok(result);
    }

    @GetMapping("/redeem-shop")
    public ApiResponse<List<Map<String, Object>>> getShopItems() {
        List<RedeemShopItem> items = shardService.getAllItems();
        List<Map<String, Object>> result = items.stream()
                .map(this::itemToMap)
                .collect(Collectors.toList());
        return ApiResponse.ok(result);
    }

    @PostMapping("/redeem-shop/{id}/redeem")
    public ApiResponse<Map<String, Object>> redeem(
            @PathVariable Long id,
            @CurrentUser TokenService.TokenInfo info) {

        Long memberId = getMemberId(info);
        if (memberId == null) {
            throw new AppException("請先登入");
        }

        ShardService.RedeemResult result = shardService.redeem(memberId, id);
        Map<String, Object> response = new HashMap<>();
        response.put("item", itemToMap(result.getItem()));
        response.put("remainingBalance", result.getRemainingBalance());
        return ApiResponse.ok(response, "🎉 兌換成功！");
    }

    private Long getMemberId(TokenService.TokenInfo info) {
        if (info == null)
            return null;
        return memberMapper.findByUsername(info.getUsername())
                .map(Member::getId)
                .orElse(null);
    }

    private Map<String, Object> transactionToMap(ShardTransaction tx) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", tx.getId());
        map.put("type", tx.getType());
        ShardTransaction.TransactionType typeEnum = tx.getTypeEnum();
        map.put("typeDisplay", typeEnum != null ? typeEnum.name() : tx.getType());
        map.put("amount", tx.getAmount());
        map.put("description", tx.getDescription());
        map.put("createdAt", tx.getCreatedAt());
        return map;
    }

    private Map<String, Object> itemToMap(RedeemShopItem item) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", item.getId());
        map.put("name", item.getName());
        map.put("description", item.getDescription());
        map.put("imageUrl", item.getImageUrl());
        map.put("shardCost", item.getShardCost());
        map.put("estimatedValue", item.getEstimatedValue());
        map.put("stock", item.getStock());
        map.put("totalStock", item.getTotalStock());
        map.put("stockPercentage", item.getStockPercentage());
        map.put("itemType", item.getItemType());
        RedeemShopItem.ItemType itemTypeEnum = item.getItemTypeEnum();
        map.put("itemTypeDisplay", itemTypeEnum != null ? itemTypeEnum.name() : item.getItemType());
        map.put("status", item.getStatus());
        map.put("hasStock", item.hasStock());
        return map;
    }
}
