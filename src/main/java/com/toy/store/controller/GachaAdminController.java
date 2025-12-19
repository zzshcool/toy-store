package com.toy.store.controller;

import com.toy.store.model.*;
import com.toy.store.repository.*;
import com.toy.store.service.SystemSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 抽獎系統後台管理控制器
 */
@Controller
@RequestMapping("/admin/gacha")
public class GachaAdminController {

    @Autowired
    private SystemSettingService settingService;

    @Autowired
    private GachaIpRepository ipRepository;

    @Autowired
    private IchibanBoxRepository boxRepository;

    @Autowired
    private IchibanPrizeRepository prizeRepository;

    @Autowired
    private IchibanSlotRepository slotRepository;

    @Autowired
    private RouletteGameRepository rouletteGameRepository;

    @Autowired
    private RouletteSlotRepository rouletteSlotRepository;

    @Autowired
    private BingoGameRepository bingoGameRepository;

    @Autowired
    private BingoCellRepository bingoCellRepository;

    @Autowired
    private RedeemShopItemRepository redeemShopRepository;

    // ==================== IP 管理 ====================

    @GetMapping("/ips")
    public String listIps(Model model) {
        model.addAttribute("activePage", "ips");
        model.addAttribute("ips", ipRepository.findAllByOrderByCreatedAtDesc());
        model.addAttribute("newIp", new GachaIp());
        return "admin/gacha-ips";
    }

    @PostMapping("/ips/create")
    public String createIp(@ModelAttribute GachaIp ip, RedirectAttributes ra) {
        ip.setStatus(GachaIp.Status.ACTIVE);
        ipRepository.save(ip);
        ra.addFlashAttribute("success", "IP 主題已建立");
        return "redirect:/admin/gacha/ips";
    }

    @PostMapping("/ips/{id}/update")
    public String updateIp(@PathVariable Long id,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String imageUrl,
            @RequestParam GachaIp.Status status,
            RedirectAttributes ra) {
        ipRepository.findById(id).ifPresent(ip -> {
            ip.setName(name);
            ip.setDescription(description);
            ip.setImageUrl(imageUrl);
            ip.setStatus(status);
            ipRepository.save(ip);
        });
        ra.addFlashAttribute("success", "IP 主題已更新");
        return "redirect:/admin/gacha/ips";
    }

    // ==================== 一番賞管理 ====================

    @GetMapping("/ichiban")
    public String listIchibanBoxes(Model model) {
        model.addAttribute("activePage", "ichiban");
        model.addAttribute("boxes", boxRepository.findAllByOrderByCreatedAtDesc());
        model.addAttribute("ips", ipRepository.findByStatus(GachaIp.Status.ACTIVE));
        return "admin/gacha-ichiban";
    }

    @PostMapping("/ichiban/create")
    public String createBox(@RequestParam Long ipId,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam BigDecimal pricePerDraw,
            @RequestParam Integer totalSlots,
            RedirectAttributes ra) {
        GachaIp ip = ipRepository.findById(ipId).orElse(null);
        if (ip == null) {
            ra.addFlashAttribute("error", "請選擇有效的 IP");
            return "redirect:/admin/gacha/ichiban";
        }

        IchibanBox box = new IchibanBox();
        box.setIp(ip);
        box.setName(name);
        box.setDescription(description);
        box.setPricePerDraw(pricePerDraw);
        box.setTotalSlots(Math.min(totalSlots, 80));
        box.setStatus(IchibanBox.Status.DRAFT);
        boxRepository.save(box);

        ra.addFlashAttribute("success", "一番賞箱體已建立，請繼續設定獎品");
        return "redirect:/admin/gacha/ichiban/" + box.getId() + "/prizes";
    }

    @GetMapping("/ichiban/{id}/prizes")
    public String editBoxPrizes(@PathVariable Long id, Model model) {
        IchibanBox box = boxRepository.findById(id).orElse(null);
        if (box == null)
            return "redirect:/admin/gacha/ichiban";

        model.addAttribute("activePage", "ichiban");
        model.addAttribute("box", box);
        model.addAttribute("prizes", prizeRepository.findByBoxIdOrderBySortOrderAsc(id));
        model.addAttribute("ranks", IchibanPrize.Rank.values());
        return "admin/gacha-ichiban-prizes";
    }

    @PostMapping("/ichiban/{id}/prizes/add")
    public String addPrize(@PathVariable Long id,
            @RequestParam IchibanPrize.Rank rank,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String imageUrl,
            @RequestParam BigDecimal estimatedValue,
            @RequestParam Integer quantity,
            RedirectAttributes ra) {
        IchibanBox box = boxRepository.findById(id).orElse(null);
        if (box == null)
            return "redirect:/admin/gacha/ichiban";

        IchibanPrize prize = new IchibanPrize();
        prize.setBox(box);
        prize.setRank(rank);
        prize.setName(name);
        prize.setDescription(description);
        prize.setImageUrl(imageUrl);
        prize.setEstimatedValue(estimatedValue);
        prize.setTotalQuantity(quantity);
        prize.setRemainingQuantity(quantity);
        prize.setSortOrder(rank.getOrder()); // 修正：使用 getOrder()
        prizeRepository.save(prize);

        ra.addFlashAttribute("success", "獎品已新增");
        return "redirect:/admin/gacha/ichiban/" + id + "/prizes";
    }

    @PostMapping("/ichiban/{id}/activate")
    public String activateBox(@PathVariable Long id, RedirectAttributes ra) {
        IchibanBox box = boxRepository.findById(id).orElse(null);
        if (box == null)
            return "redirect:/admin/gacha/ichiban";

        List<IchibanPrize> prizes = prizeRepository.findByBoxIdOrderBySortOrderAsc(id);
        int slotNumber = 1;
        for (IchibanPrize prize : prizes) {
            for (int i = 0; i < prize.getTotalQuantity() && slotNumber <= box.getTotalSlots(); i++) {
                IchibanSlot slot = new IchibanSlot();
                slot.setBox(box);
                slot.setSlotNumber(slotNumber++);
                slot.setPrize(prize);
                slot.setStatus(IchibanSlot.Status.AVAILABLE);
                slotRepository.save(slot);
            }
        }

        box.setTotalSlots(slotNumber - 1);
        box.setStatus(IchibanBox.Status.ACTIVE);
        boxRepository.save(box);

        ra.addFlashAttribute("success", "一番賞已上架，共 " + (slotNumber - 1) + " 格");
        return "redirect:/admin/gacha/ichiban";
    }

    // ==================== 轉盤管理 ====================

    @GetMapping("/roulette")
    public String listRouletteGames(Model model) {
        model.addAttribute("activePage", "roulette");
        model.addAttribute("games", rouletteGameRepository.findAllByOrderByCreatedAtDesc());
        model.addAttribute("ips", ipRepository.findByStatus(GachaIp.Status.ACTIVE));
        return "admin/gacha-roulette";
    }

    @PostMapping("/roulette/create")
    public String createRouletteGame(@RequestParam Long ipId,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam BigDecimal pricePerSpin,
            @RequestParam Integer totalSlots,
            RedirectAttributes ra) {
        GachaIp ip = ipRepository.findById(ipId).orElse(null);
        if (ip == null) {
            ra.addFlashAttribute("error", "請選擇有效的 IP");
            return "redirect:/admin/gacha/roulette";
        }

        RouletteGame game = new RouletteGame();
        game.setIp(ip);
        game.setName(name);
        game.setDescription(description);
        game.setPricePerSpin(pricePerSpin);
        game.setTotalSlots(Math.min(totalSlots, 25));
        game.setStatus(RouletteGame.Status.DRAFT);
        rouletteGameRepository.save(game);

        ra.addFlashAttribute("success", "轉盤已建立，請繼續設定獎格");
        return "redirect:/admin/gacha/roulette/" + game.getId() + "/slots";
    }

    @GetMapping("/roulette/{id}/slots")
    public String editRouletteSlots(@PathVariable Long id, Model model) {
        RouletteGame game = rouletteGameRepository.findById(id).orElse(null);
        if (game == null)
            return "redirect:/admin/gacha/roulette";

        List<RouletteSlot> slots = rouletteSlotRepository.findByGameIdOrderBySlotOrderAsc(id);
        int totalWeight = slots.stream().mapToInt(RouletteSlot::getWeight).sum();

        model.addAttribute("activePage", "roulette");
        model.addAttribute("game", game);
        model.addAttribute("slots", slots);
        model.addAttribute("totalWeight", totalWeight);
        model.addAttribute("slotTypes", RouletteSlot.SlotType.values());
        return "admin/gacha-roulette-slots";
    }

    @PostMapping("/roulette/{id}/slots/add")
    public String addRouletteSlot(@PathVariable Long id,
            @RequestParam RouletteSlot.SlotType slotType,
            @RequestParam String prizeName,
            @RequestParam(required = false) String prizeDescription,
            @RequestParam Integer weight,
            @RequestParam(required = false) Integer shardAmount,
            @RequestParam(required = false) String color,
            RedirectAttributes ra) {
        RouletteGame game = rouletteGameRepository.findById(id).orElse(null);
        if (game == null)
            return "redirect:/admin/gacha/roulette";

        int currentCount = rouletteSlotRepository.findByGameIdOrderBySlotOrderAsc(id).size();
        if (currentCount >= game.getTotalSlots()) {
            ra.addFlashAttribute("error", "已達到最大獎格數量");
            return "redirect:/admin/gacha/roulette/" + id + "/slots";
        }

        RouletteSlot slot = new RouletteSlot();
        slot.setGame(game);
        slot.setSlotOrder(currentCount + 1);
        slot.setSlotType(slotType);
        slot.setPrizeName(prizeName);
        slot.setPrizeDescription(prizeDescription);
        slot.setWeight(weight);
        slot.setShardAmount(slotType == RouletteSlot.SlotType.SHARD ? shardAmount : null);
        slot.setColor(color != null && !color.isEmpty() ? color : slotType.getDefaultColor());
        rouletteSlotRepository.save(slot);

        ra.addFlashAttribute("success", "獎格已新增");
        return "redirect:/admin/gacha/roulette/" + id + "/slots";
    }

    @PostMapping("/roulette/{gid}/slots/{sid}/delete")
    public String deleteRouletteSlot(@PathVariable Long gid, @PathVariable Long sid, RedirectAttributes ra) {
        rouletteSlotRepository.deleteById(sid);
        // 重新排序
        List<RouletteSlot> slots = rouletteSlotRepository.findByGameIdOrderBySlotOrderAsc(gid);
        int order = 1;
        for (RouletteSlot slot : slots) {
            slot.setSlotOrder(order++);
            rouletteSlotRepository.save(slot);
        }
        ra.addFlashAttribute("success", "獎格已刪除");
        return "redirect:/admin/gacha/roulette/" + gid + "/slots";
    }

    @PostMapping("/roulette/{id}/activate")
    public String activateRouletteGame(@PathVariable Long id, RedirectAttributes ra) {
        RouletteGame game = rouletteGameRepository.findById(id).orElse(null);
        if (game == null)
            return "redirect:/admin/gacha/roulette";

        game.setStatus(RouletteGame.Status.ACTIVE);
        rouletteGameRepository.save(game);
        ra.addFlashAttribute("success", "轉盤已上架");
        return "redirect:/admin/gacha/roulette";
    }

    // ==================== 九宮格管理 ====================

    @GetMapping("/bingo")
    public String listBingoGames(Model model) {
        model.addAttribute("activePage", "bingo");
        model.addAttribute("games", bingoGameRepository.findAllByOrderByCreatedAtDesc());
        model.addAttribute("ips", ipRepository.findByStatus(GachaIp.Status.ACTIVE));
        return "admin/gacha-bingo";
    }

    @PostMapping("/bingo/create")
    public String createBingoGame(@RequestParam Long ipId,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam BigDecimal pricePerDig,
            @RequestParam Integer gridSize,
            @RequestParam(required = false) String bingoRewardName,
            @RequestParam(required = false) BigDecimal bingoRewardValue,
            RedirectAttributes ra) {
        GachaIp ip = ipRepository.findById(ipId).orElse(null);
        if (ip == null) {
            ra.addFlashAttribute("error", "請選擇有效的 IP");
            return "redirect:/admin/gacha/bingo";
        }

        BingoGame game = new BingoGame();
        game.setIp(ip);
        game.setName(name);
        game.setDescription(description);
        game.setPricePerDig(pricePerDig);
        game.setGridSize(Math.min(Math.max(gridSize, 3), 6));
        game.setBingoRewardName(bingoRewardName);
        game.setBingoRewardValue(bingoRewardValue);
        game.setStatus(BingoGame.Status.DRAFT);
        bingoGameRepository.save(game);

        ra.addFlashAttribute("success", "九宮格已建立，請繼續設定格子");
        return "redirect:/admin/gacha/bingo/" + game.getId() + "/cells";
    }

    @GetMapping("/bingo/{id}/cells")
    public String editBingoCells(@PathVariable Long id, Model model) {
        BingoGame game = bingoGameRepository.findById(id).orElse(null);
        if (game == null)
            return "redirect:/admin/gacha/bingo";

        List<BingoCell> cells = bingoCellRepository.findByGameIdOrderByPositionAsc(id);
        Map<Integer, BingoCell> cellMap = new HashMap<>();
        for (BingoCell cell : cells) {
            cellMap.put(cell.getPosition(), cell);
        }

        model.addAttribute("activePage", "bingo");
        model.addAttribute("game", game);
        model.addAttribute("cells", cells);
        model.addAttribute("cellMap", cellMap);
        return "admin/gacha-bingo-cells";
    }

    @PostMapping("/bingo/{id}/cells/add")
    public String addBingoCell(@PathVariable Long id,
            @RequestParam Integer position,
            @RequestParam String prizeName,
            @RequestParam(required = false) String prizeDescription,
            RedirectAttributes ra) {
        BingoGame game = bingoGameRepository.findById(id).orElse(null);
        if (game == null)
            return "redirect:/admin/gacha/bingo";

        int gridSize = game.getGridSize();
        int row = (position - 1) / gridSize;
        int col = (position - 1) % gridSize;

        BingoCell cell = new BingoCell();
        cell.setGame(game);
        cell.setPosition(position);
        cell.setRow(row);
        cell.setCol(col);
        cell.setPrizeName(prizeName);
        cell.setPrizeDescription(prizeDescription);
        cell.setIsRevealed(false);
        bingoCellRepository.save(cell);

        ra.addFlashAttribute("success", "格子已設定");
        return "redirect:/admin/gacha/bingo/" + id + "/cells";
    }

    @PostMapping("/bingo/{id}/cells/auto-fill")
    public String autoFillBingoCells(@PathVariable Long id, RedirectAttributes ra) {
        BingoGame game = bingoGameRepository.findById(id).orElse(null);
        if (game == null)
            return "redirect:/admin/gacha/bingo";

        List<BingoCell> existing = bingoCellRepository.findByGameIdOrderByPositionAsc(id);
        Map<Integer, BingoCell> cellMap = new HashMap<>();
        for (BingoCell cell : existing) {
            cellMap.put(cell.getPosition(), cell);
        }

        int gridSize = game.getGridSize();
        int totalCells = gridSize * gridSize;
        String[] defaultPrizes = { "貼紙", "徽章", "書籤", "明信片", "鑰匙圈", "杯墊", "便條紙", "原子筆", "資料夾" };

        for (int pos = 1; pos <= totalCells; pos++) {
            if (!cellMap.containsKey(pos)) {
                int row = (pos - 1) / gridSize;
                int col = (pos - 1) % gridSize;

                BingoCell cell = new BingoCell();
                cell.setGame(game);
                cell.setPosition(pos);
                cell.setRow(row);
                cell.setCol(col);
                cell.setPrizeName(defaultPrizes[(pos - 1) % defaultPrizes.length]);
                cell.setPrizeDescription("精美周邊");
                cell.setIsRevealed(false);
                bingoCellRepository.save(cell);
            }
        }

        ra.addFlashAttribute("success", "已自動填充所有空白格子");
        return "redirect:/admin/gacha/bingo/" + id + "/cells";
    }

    @PostMapping("/bingo/{id}/activate")
    public String activateBingoGame(@PathVariable Long id, RedirectAttributes ra) {
        BingoGame game = bingoGameRepository.findById(id).orElse(null);
        if (game == null)
            return "redirect:/admin/gacha/bingo";

        int cellCount = bingoCellRepository.findByGameIdOrderByPositionAsc(id).size();
        int required = game.getGridSize() * game.getGridSize();
        if (cellCount < required) {
            ra.addFlashAttribute("error", "請先設定完所有格子（需要 " + required + " 格，目前 " + cellCount + " 格）");
            return "redirect:/admin/gacha/bingo/" + id + "/cells";
        }

        game.setStatus(BingoGame.Status.ACTIVE);
        bingoGameRepository.save(game);
        ra.addFlashAttribute("success", "九宮格已上架");
        return "redirect:/admin/gacha/bingo";
    }

    // ==================== 系統設定 ====================

    @GetMapping("/settings")
    public String systemSettings(Model model) {
        model.addAttribute("activePage", "settings");
        model.addAttribute("settings", settingService.getAllSettings());
        return "admin/gacha-settings";
    }

    @PostMapping("/settings/update")
    public String updateSettings(@RequestParam String key,
            @RequestParam String value,
            RedirectAttributes ra) {
        settingService.updateSetting(key, value);
        ra.addFlashAttribute("success", "設定已更新");
        return "redirect:/admin/gacha/settings";
    }

    // ==================== 兌換商店管理 ====================

    @GetMapping("/redeem")
    public String listRedeemItems(Model model) {
        model.addAttribute("activePage", "redeem");
        model.addAttribute("items", redeemShopRepository.findAllByOrderBySortOrderAsc());
        model.addAttribute("itemTypes", RedeemShopItem.ItemType.values());
        return "admin/gacha-redeem";
    }

    @PostMapping("/redeem/create")
    public String createRedeemItem(@RequestParam String name,
            @RequestParam String description,
            @RequestParam String imageUrl,
            @RequestParam Integer shardCost,
            @RequestParam BigDecimal estimatedValue,
            @RequestParam Integer stock,
            @RequestParam RedeemShopItem.ItemType itemType,
            RedirectAttributes ra) {
        RedeemShopItem item = new RedeemShopItem();
        item.setName(name);
        item.setDescription(description);
        item.setImageUrl(imageUrl);
        item.setShardCost(shardCost);
        item.setEstimatedValue(estimatedValue);
        item.setStock(stock);
        item.setTotalStock(stock);
        item.setItemType(itemType);
        item.setStatus(RedeemShopItem.Status.ACTIVE);
        redeemShopRepository.save(item);

        ra.addFlashAttribute("success", "兌換商品已新增");
        return "redirect:/admin/gacha/redeem";
    }
}
