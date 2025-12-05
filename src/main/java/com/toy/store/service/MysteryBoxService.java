package com.toy.store.service;

import com.toy.store.model.*;
import com.toy.store.repository.MysteryBoxThemeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.math.BigDecimal;

@Service
public class MysteryBoxService {

    @Autowired
    private MysteryBoxThemeRepository themeRepository;

    @Autowired
    private com.toy.store.repository.MemberRepository memberRepository;

    @Autowired
    private com.toy.store.repository.OrderRepository orderRepository;

    @Autowired
    private TransactionService transactionService;

    private final Random random = new Random();

    /**
     * Draws a mystery box.
     * 1. Check/Deduct Balance
     * 2. Weighted Random Selection
     * 3. Return Item (and maybe record prize transaction/inventory if needed)
     */
    @Transactional
    public MysteryBoxItem drawBox(Long memberId, Long themeId) {
        // 1. Get Theme
        MysteryBoxTheme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new RuntimeException("Theme not found"));

        // 2. Deduct Cost (Transaction Service handles balance check)
        transactionService.updateWalletBalance(memberId, theme.getPrice().negate(),
                Transaction.TransactionType.MYSTERY_BOX_COST, "THEME-" + themeId);

        // 3. Weighted Random Selection
        List<MysteryBoxItem> items = theme.getItems();
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("No items in this box theme");
        }

        int totalWeight = items.stream().mapToInt(MysteryBoxItem::getWeight).sum();
        int randomValue = random.nextInt(totalWeight);
        int currentWeight = 0;

        MysteryBoxItem selectedItem = null;
        for (MysteryBoxItem item : items) {
            currentWeight += item.getWeight();
            if (randomValue < currentWeight) {
                selectedItem = item;
                break;
            }
        }

        // 4. Create Order Record for the Prize
        Order prizeOrder = new Order();
        prizeOrder.setMember(memberRepository.findById(memberId).orElseThrow());
        prizeOrder.setTotalPrice(BigDecimal.ZERO); // It's a prize
        prizeOrder.setStatus(Order.OrderStatus.COMPLETED); // Instant win

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(prizeOrder);
        orderItem.setProductName("獎品: " + selectedItem.getName());
        orderItem.setPriceAtPurchase(selectedItem.getEstimatedValue());
        orderItem.setQuantity(1);

        prizeOrder.addItem(orderItem);
        orderRepository.save(prizeOrder);

        return selectedItem;
    }

    public List<MysteryBoxTheme> getAllThemes() {
        return themeRepository.findAll();
    }
}
