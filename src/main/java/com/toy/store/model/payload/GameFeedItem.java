package com.toy.store.model.payload;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameFeedItem {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private Integer remainingStock;
    private Integer totalStock;
    private String type; // ICHIBAN, BLIND_BOX, etc.
    private String typeDisplay; // 一番賞, 盲盒, etc.

    // For specific badge logic or UI requirements
    public boolean isSoldOut() {
        return remainingStock != null && remainingStock <= 0;
    }
}
