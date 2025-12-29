package com.toy.store.service;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 抽獎機率引擎
 * 封裝機率計算、權重過濾及收益保護（70% 門檻）邏輯
 */
@Component
public class GachaProbabilityEngine {

    private final Random random = new Random();

    /**
     * 獎項等極定義
     */
    public enum PrizeTier {
        JACKPOT, // 大獎 (如一番賞 A/B, 轉盤大獎)
        RARE, // 稀有獎
        NORMAL // 普通獎
    }

    /**
     * 可抽獎項介面
     */
    public interface ProbableItem {
        Integer getWeight();

        PrizeTier getTier();
    }

    /**
     * 執行機率抽取，具備收益保護
     *
     * @param items     所有可選獎項
     * @param progress  當前抽出進度 (0.0 ~ 1.0)
     * @param threshold 大獎開啟門檻 (預設 0.7)
     * @return 抽中的獎項
     */
    public <T extends ProbableItem> T draw(List<T> items, double progress, double threshold) {
        if (items == null || items.isEmpty())
            return null;

        // 1. 根據進度過濾大獎
        boolean isJackpotLocked = progress < threshold;

        // 2. 計算剩餘權重
        List<T> candidates = new ArrayList<>();
        int totalWeight = 0;

        for (T item : items) {
            int weight = item.getWeight();

            // 如果大獎被鎖定且該項是大獎，則暫時將權重設為 0
            if (isJackpotLocked && (item.getTier() == PrizeTier.JACKPOT || item.getTier() == PrizeTier.RARE)) {
                weight = 0;
            }

            if (weight > 0) {
                candidates.add(item);
                totalWeight += weight;
            }
        }

        // 3. 如果所有權重都被過濾掉了（不應該發生，除非池子只有大獎），則回退到普通抽取
        if (candidates.isEmpty() || totalWeight == 0) {
            return drawSimple(items);
        }

        // 4. 加權隨機抽取
        int rand = random.nextInt(totalWeight);
        int current = 0;
        for (T item : candidates) {
            current += item.getWeight();
            if (rand < current) {
                return item;
            }
        }

        return candidates.get(0);
    }

    /**
     * 簡單加權隨機
     */
    public <T extends ProbableItem> T drawSimple(List<T> items) {
        int totalWeight = items.stream().mapToInt(ProbableItem::getWeight).sum();
        if (totalWeight <= 0)
            return items.get(0);

        int rand = random.nextInt(totalWeight);
        int current = 0;
        for (T item : items) {
            current += item.getWeight();
            if (rand < current) {
                return item;
            }
        }
        return items.get(0);
    }
}
