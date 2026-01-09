package com.toy.store.mapper;

import com.toy.store.model.payload.GameFeedItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface HomeMapper {

    @Select("""
                SELECT * FROM (
                    -- 1. Ichiban
                    SELECT
                        ib.id,
                        ib.name,
                        ib.description,
                        ib.image_url as imageUrl,
                        ib.price_per_draw as price,
                        (SELECT COUNT(*) FROM ichiban_slots s WHERE s.box_id = ib.id AND s.status != 'SOLD' AND s.status != 'REVEALED') as remainingStock,
                        ib.total_slots as totalStock,
                        'ICHIBAN' as type,
                        '一番賞' as typeDisplay,
                        1 as type_sort
                    FROM ichiban_boxes ib
                    WHERE ib.status = 'ACTIVE'

                    UNION ALL

                    -- 2. Blind Box Items (Grouped by Box?) No, show Boxes.
                    SELECT
                        bb.id,
                        bb.name,
                        bb.description,
                        bb.image_url as imageUrl,
                        bb.price_per_box as price,
                        (SELECT COUNT(*) FROM blind_box_items bbi WHERE bbi.blind_box_id = bb.id AND bbi.status != 'SOLD' AND bbi.status != 'PURCHASED') as remainingStock,
                        bb.total_boxes as totalStock,
                        'BLIND_BOX' as type,
                        '盒玩' as typeDisplay,
                        2 as type_sort
                    FROM blind_boxes bb
                    WHERE bb.status = 'ACTIVE'

                    UNION ALL

                    -- 3. Gacha (Use Gacha Themes)
                    SELECT
                        gt.id,
                        gt.name,
                        gt.description,
                        gt.image_url as imageUrl,
                        gt.price_per_gacha as price,
                        100 as remainingStock, -- Mock infinite stock for display
                        100 as totalStock,
                        'GACHA' as type,
                        '轉蛋' as typeDisplay,
                        3 as type_sort
                    FROM gacha_themes gt
                    WHERE gt.status = 'ACTIVE'

                    UNION ALL

                    -- 4. Bingo
                    SELECT
                        bg.id,
                        bg.name,
                        bg.description,
                        bg.image_url as imageUrl,
                        bg.price_per_dig as price,
                        (SELECT COUNT(*) FROM bingo_cells bc WHERE bc.game_id = bg.id AND bc.is_revealed = FALSE) as remainingStock,
                        bg.total_cells as totalStock,
                        'BINGO' as type,
                        '九宮格' as typeDisplay,
                        4 as type_sort
                    FROM bingo_games bg
                    WHERE bg.status = 'ACTIVE'

                    UNION ALL

                    -- 5. Roulette
                    SELECT
                        rg.id,
                        rg.name,
                        rg.description,
                        rg.image_url as imageUrl,
                        rg.price_per_spin as price,
                        100 as remainingStock, -- Mock
                        100 as totalStock,
                        'ROULETTE' as type,
                        '轉盤' as typeDisplay,
                        5 as type_sort
                    FROM roulette_games rg
                    WHERE rg.status = 'ACTIVE'
                ) combined
                ORDER BY type_sort ASC, id DESC
                LIMIT #{limit} OFFSET #{offset}
            """)
    List<GameFeedItem> getGameFeed(@Param("offset") int offset, @Param("limit") int limit);
}
