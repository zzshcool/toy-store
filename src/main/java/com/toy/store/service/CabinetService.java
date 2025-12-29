package com.toy.store.service;

import com.toy.store.exception.AppException;
import com.toy.store.model.*;
import com.toy.store.repository.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 盒櫃服務
 * 對應規格書 §4.D, §8.A - 獎品收集與發貨系統
 */
@Service
@RequiredArgsConstructor
public class CabinetService {

    private static final int FREE_SHIPPING_THRESHOLD = 5;
    private static final BigDecimal DEFAULT_SHIPPING_FEE = new BigDecimal("60");

    private final CabinetItemRepository itemRepository;
    private final ShipmentRequestRepository shipmentRepository;
    private final MemberRepository memberRepository;

    /**
     * 新增獎品到盒櫃
     */
    @Transactional
    public CabinetItem addToCabinet(Long memberId, CabinetItem.SourceType sourceType, Long sourceId,
            String prizeName, String prizeDescription, String prizeImageUrl, String prizeRank) {
        CabinetItem item = new CabinetItem();
        item.setMemberId(memberId);
        item.setSourceType(sourceType);
        item.setSourceId(sourceId);
        item.setPrizeName(prizeName);
        item.setPrizeDescription(prizeDescription);
        item.setPrizeImageUrl(prizeImageUrl);
        item.setPrizeRank(prizeRank);
        item.setStatus(CabinetItem.Status.IN_CABINET);

        return itemRepository.save(item);
    }

    /**
     * 取得會員盒櫃內的獎品
     */
    public List<CabinetItem> getCabinetItems(Long memberId) {
        return itemRepository.findByMemberIdAndStatusOrderByObtainedAtDesc(memberId, CabinetItem.Status.IN_CABINET);
    }

    /**
     * 取得會員所有獎品（含已發貨）
     */
    public List<CabinetItem> getAllItems(Long memberId) {
        return itemRepository.findByMemberIdOrderByObtainedAtDesc(memberId);
    }

    /**
     * 盒櫃獎品數量
     */
    public int getCabinetCount(Long memberId) {
        return itemRepository.countItemsInCabinet(memberId);
    }

    /**
     * 計算運費
     */
    public ShippingInfo calculateShipping(int itemCount) {
        boolean isFree = itemCount >= FREE_SHIPPING_THRESHOLD;
        BigDecimal fee = isFree ? BigDecimal.ZERO : DEFAULT_SHIPPING_FEE;
        return new ShippingInfo(isFree, fee, FREE_SHIPPING_THRESHOLD - itemCount);
    }

    /**
     * 提交發貨申請
     */
    @Transactional
    public ShipmentRequest requestShipment(Long memberId, List<Long> itemIds,
            String recipientName, String recipientPhone,
            String recipientAddress, String postalCode) {

        if (itemIds.isEmpty()) {
            throw new AppException("請選擇要發貨的獎品");
        }

        // 驗證獎品歸屬
        List<CabinetItem> items = itemRepository.findAllById(itemIds);
        for (CabinetItem item : items) {
            if (!memberId.equals(item.getMemberId())) {
                throw new AppException("部分獎品不屬於您");
            }
            if (item.getStatus() != CabinetItem.Status.IN_CABINET) {
                throw new AppException("部分獎品已申請發貨");
            }
        }

        // 計算運費
        ShippingInfo shippingInfo = calculateShipping(items.size());

        // 建立發貨申請
        ShipmentRequest request = new ShipmentRequest();
        request.setMemberId(memberId);
        request.setRecipientName(recipientName);
        request.setRecipientPhone(recipientPhone);
        request.setRecipientAddress(recipientAddress);
        request.setPostalCode(postalCode);
        request.setItemCount(items.size());
        request.setIsFreeShipping(shippingInfo.isFreeShipping());
        request.setShippingFee(shippingInfo.getFee());
        request.setStatus(ShipmentRequest.Status.PENDING);

        request = shipmentRepository.save(request);

        // 更新獎品狀態
        final Long requestId = request.getId();
        items.forEach(item -> {
            item.setStatus(CabinetItem.Status.PENDING_SHIP);
            item.setShipmentRequestId(requestId);
            item.setRequestedAt(LocalDateTime.now());
        });
        itemRepository.saveAll(items);

        return request;
    }

    /**
     * 取得會員的發貨申請
     */
    public List<ShipmentRequest> getMemberShipments(Long memberId) {
        return shipmentRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
    }

    /**
     * 取消發貨申請（僅限待處理狀態）
     */
    @Transactional
    public void cancelShipment(Long memberId, Long requestId) {
        ShipmentRequest request = shipmentRepository.findById(requestId)
                .orElseThrow(() -> new AppException("發貨申請不存在"));

        if (!memberId.equals(request.getMemberId())) {
            throw new AppException("此發貨申請不屬於您");
        }

        if (request.getStatus() != ShipmentRequest.Status.PENDING) {
            throw new AppException("此發貨申請無法取消");
        }

        // 返還獎品到盒櫃
        List<CabinetItem> items = itemRepository.findByShipmentRequestId(requestId);
        items.forEach(item -> {
            item.setStatus(CabinetItem.Status.IN_CABINET);
            item.setShipmentRequestId(null);
            item.setRequestedAt(null);
        });
        itemRepository.saveAll(items);

        request.setStatus(ShipmentRequest.Status.CANCELLED);
        shipmentRepository.save(request);
    }

    /**
     * 兌換獎品為積分
     */
    @Transactional
    public int exchangeForPoints(Long memberId, Long itemId) {
        CabinetItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new AppException("獎品不存在"));

        if (!memberId.equals(item.getMemberId())) {
            throw new AppException("此獎品不屬於您");
        }

        if (item.getStatus() != CabinetItem.Status.IN_CABINET) {
            throw new AppException("此獎品無法兌換");
        }

        // 根據獎品等級計算積分
        int points = calculateExchangePoints(item);

        // 更新會員積分
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AppException("會員不存在"));
        member.setPoints(member.getPoints() + points);
        memberRepository.save(member);

        // 更新獎品狀態
        item.setStatus(CabinetItem.Status.EXCHANGED);
        itemRepository.save(item);

        return points;
    }

    private int calculateExchangePoints(CabinetItem item) {
        if (item.getPrizeRank() == null)
            return 50;

        return switch (item.getPrizeRank().toUpperCase()) {
            case "A", "A賞" -> 500;
            case "B", "B賞" -> 300;
            case "C", "C賞" -> 200;
            case "D", "D賞" -> 150;
            case "E", "E賞" -> 100;
            case "LAST", "最後賞" -> 600;
            default -> 50;
        };
    }

    // ============== 後台管理方法 ==============

    /**
     * 取得待處理的發貨申請
     */
    public List<ShipmentRequest> getPendingShipments() {
        return shipmentRepository.findByStatusOrderByCreatedAtAsc(ShipmentRequest.Status.PENDING);
    }

    /**
     * 更新發貨狀態
     */
    @Transactional
    public ShipmentRequest updateShipmentStatus(Long requestId, ShipmentRequest.Status status,
            String trackingNumber, String shippingCompany, String note) {
        ShipmentRequest request = shipmentRepository.findById(requestId)
                .orElseThrow(() -> new AppException("發貨申請不存在"));

        request.setStatus(status);
        if (trackingNumber != null)
            request.setTrackingNumber(trackingNumber);
        if (shippingCompany != null)
            request.setShippingCompany(shippingCompany);
        if (note != null)
            request.setAdminNote(note);

        if (status == ShipmentRequest.Status.SHIPPED) {
            request.setShippedAt(LocalDateTime.now());
            // 更新獎品狀態
            List<CabinetItem> items = itemRepository.findByShipmentRequestId(requestId);
            items.forEach(item -> item.setStatus(CabinetItem.Status.SHIPPED));
            itemRepository.saveAll(items);
        }

        if (status == ShipmentRequest.Status.DELIVERED) {
            request.setDeliveredAt(LocalDateTime.now());
            // 更新獎品狀態
            List<CabinetItem> items = itemRepository.findByShipmentRequestId(requestId);
            items.forEach(item -> item.setStatus(CabinetItem.Status.DELIVERED));
            itemRepository.saveAll(items);
        }

        return shipmentRepository.save(request);
    }

    // ============== DTO ==============

    @Data
    public static class ShippingInfo {
        private final boolean freeShipping;
        private final BigDecimal fee;
        private final int itemsNeededForFree; // 還差幾件免運
    }
}
