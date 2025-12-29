package com.toy.store.service;

import com.toy.store.exception.AppException;
import com.toy.store.model.*;
import com.toy.store.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 九宮格服務
 * 處理挖掘、連線檢查等核心邏輯
 */
@Service
public class BingoService extends BaseGachaService {

    @Autowired
    private BingoGameRepository gameRepository;

    @Autowired
    private BingoCellRepository cellRepository;

    /**
     * 取得所有進行中的九宮格遊戲
     */
    public List<BingoGame> getActiveGames() {
        return gameRepository.findByStatus(BingoGame.Status.ACTIVE);
    }

    /**
     * 取得遊戲詳情（含格子）
     */
    public BingoGame getGameWithCells(Long gameId) {
        return gameRepository.findById(gameId).orElse(null);
    }

    /**
     * 取得遊戲的格子列表
     */
    public List<BingoCell> getCells(Long gameId) {
        if (gameId == null)
            return new ArrayList<>();
        return cellRepository.findByGameIdOrderByPositionAsc(gameId);
    }

    /**
     * 挖掘格子 (單個)
     */
    @Transactional
    public DigResult dig(Long gameId, Integer position, Long memberId) {
        List<Integer> positions = new ArrayList<>();
        positions.add(position);
        DigBatchResult result = digMultiple(gameId, positions, memberId);
        return new DigResult(result.getCells().get(0), result.getTotalShards(),
                result.isHasBingo(), result.getBingoLines(),
                result.getBingoRewardName(), result.getGridSize());
    }

    /**
     * 挖掘多個格子 (批次處理)
     */
    @Transactional
    public DigBatchResult digMultiple(Long gameId, List<Integer> positions, Long memberId) {
        BingoGame game = gameRepository.findById(gameId)
                .orElseThrow(() -> new AppException("遊戲不存在"));

        if (positions == null || positions.isEmpty()) {
            throw new AppException("請選擇至少一個格子");
        }

        // 1. 驗證所有格子可用
        List<BingoCell> targetCells = new ArrayList<>();
        for (Integer pos : positions) {
            BingoCell cell = cellRepository.findByGameIdAndPosition(gameId, pos)
                    .orElseThrow(() -> new AppException("格子 " + pos + " 不存在"));
            if (cell.getIsRevealed()) {
                throw new AppException("格子 " + pos + " 已被挖掘");
            }
            targetCells.add(cell);
        }

        // 2. 計算總價並扣款
        java.math.BigDecimal totalCost = game.getPricePerDig().multiply(new java.math.BigDecimal(positions.size()));
        deductWallet(memberId, totalCost, Transaction.TransactionType.BINGO_COST,
                "九宮格批次消費: " + game.getName() + " (" + positions.size() + " 格)");

        // 3. 處理收益保護與挖掘
        int totalCells = game.getTotalCells();
        int revealedCount = cellRepository.countRevealedCells(gameId);
        int totalShards = 0;
        List<BingoCell> revealedCells = new ArrayList<>();

        for (BingoCell cell : targetCells) {
            double progress = (double) revealedCount / totalCells;

            // 如果進度未滿 70% 且抽中大獎，則嘗試與未開格子交換
            if (progress < 0.7 && cell.getTier() == GachaProbabilityEngine.PrizeTier.JACKPOT) {
                List<BingoCell> availableNormalCells = cellRepository.findByGameIdAndIsRevealed(gameId, false).stream()
                        .filter(c -> c.getTier() != GachaProbabilityEngine.PrizeTier.JACKPOT
                                && !positions.contains(c.getPosition()))
                        .collect(java.util.stream.Collectors.toList());

                if (!availableNormalCells.isEmpty()) {
                    BingoCell swapCell = availableNormalCells
                            .get(new java.util.Random().nextInt(availableNormalCells.size()));
                    // 交換獎品內容
                    String tempName = cell.getPrizeName();
                    java.math.BigDecimal tempValue = cell.getPrizeValue();
                    cell.setPrizeName(swapCell.getPrizeName());
                    cell.setPrizeValue(swapCell.getPrizeValue());
                    swapCell.setPrizeName(tempName);
                    swapCell.setPrizeValue(tempValue);
                    cellRepository.save(swapCell);
                }
            }

            // 挖掘
            cell.dig(memberId);
            cellRepository.save(cell);
            revealedCells.add(cell);
            revealedCount++;

            // 產出隨機積分 1~20
            int shardsEarned = processGachaShards(memberId, "BINGO", gameId, "九宮格挖掘獲得");
            totalShards += shardsEarned;

            // 記錄抽獎
            saveBingoRecord(memberId, gameId, cell.getPrizeName(), shardsEarned);
        }

        // 4. 檢查連線
        List<BingoLine> bingoLines = checkBingoLines(gameId, game.getGridSize());
        boolean hasBingo = !bingoLines.isEmpty();

        if (hasBingo && game.getBingoRewardName() != null) {
            saveBingoRecord(memberId, gameId, "連線獎勵: " + game.getBingoRewardName(), 0);
        }

        return new DigBatchResult(revealedCells, totalShards, hasBingo, bingoLines,
                game.getBingoRewardName(), game.getGridSize());
    }

    /**
     * 檢查所有連線（橫、豎、對角）
     */
    public List<BingoLine> checkBingoLines(Long gameId, int gridSize) {
        List<BingoCell> cells = cellRepository.findByGameIdOrderByPositionAsc(gameId);
        List<BingoLine> bingoLines = new ArrayList<>();

        // 建立 2D 陣列方便檢查
        boolean[][] revealed = new boolean[gridSize][gridSize];
        for (BingoCell cell : cells) {
            if (cell.getRow() < gridSize && cell.getCol() < gridSize) {
                revealed[cell.getRow()][cell.getCol()] = cell.getIsRevealed();
            }
        }

        // 檢查橫列
        for (int row = 0; row < gridSize; row++) {
            boolean complete = true;
            for (int col = 0; col < gridSize; col++) {
                if (!revealed[row][col]) {
                    complete = false;
                    break;
                }
            }
            if (complete) {
                bingoLines.add(new BingoLine(BingoLine.Type.ROW, row));
            }
        }

        // 檢查直行
        for (int col = 0; col < gridSize; col++) {
            boolean complete = true;
            for (int row = 0; row < gridSize; row++) {
                if (!revealed[row][col]) {
                    complete = false;
                    break;
                }
            }
            if (complete) {
                bingoLines.add(new BingoLine(BingoLine.Type.COL, col));
            }
        }

        // 檢查主對角線（左上到右下）
        boolean diag1Complete = true;
        for (int i = 0; i < gridSize; i++) {
            if (!revealed[i][i]) {
                diag1Complete = false;
                break;
            }
        }
        if (diag1Complete) {
            bingoLines.add(new BingoLine(BingoLine.Type.DIAGONAL, 0));
        }

        // 檢查副對角線（右上到左下）
        boolean diag2Complete = true;
        for (int i = 0; i < gridSize; i++) {
            if (!revealed[i][gridSize - 1 - i]) {
                diag2Complete = false;
                break;
            }
        }
        if (diag2Complete) {
            bingoLines.add(new BingoLine(BingoLine.Type.DIAGONAL, 1));
        }

        return bingoLines;
    }

    /**
     * 連線資訊
     */
    public static class BingoLine {
        public enum Type {
            ROW, COL, DIAGONAL
        }

        private final Type type;
        private final int index;

        public BingoLine(Type type, int index) {
            this.type = type;
            this.index = index;
        }

        public Type getType() {
            return type;
        }

        public int getIndex() {
            return index;
        }
    }

    /**
     * 批次挖掘結果封裝類
     */
    public static class DigBatchResult {
        private final List<BingoCell> cells;
        private final int totalShards;
        private final boolean hasBingo;
        private final List<BingoLine> bingoLines;
        private final String bingoRewardName;
        private final int gridSize;

        public DigBatchResult(List<BingoCell> cells, int totalShards, boolean hasBingo,
                List<BingoLine> bingoLines, String bingoRewardName, int gridSize) {
            this.cells = cells;
            this.totalShards = totalShards;
            this.hasBingo = hasBingo;
            this.bingoLines = bingoLines;
            this.bingoRewardName = bingoRewardName;
            this.gridSize = gridSize;
        }

        public List<BingoCell> getCells() {
            return cells;
        }

        public int getTotalShards() {
            return totalShards;
        }

        public boolean isHasBingo() {
            return hasBingo;
        }

        public List<BingoLine> getBingoLines() {
            return bingoLines;
        }

        public String getBingoRewardName() {
            return bingoRewardName;
        }

        public int getGridSize() {
            return gridSize;
        }
    }

    /**
     * 挖掘結果封裝類
     */
    public static class DigResult {
        private final BingoCell cell;
        private final int shardsEarned;
        private final boolean hasBingo;
        private final List<BingoLine> bingoLines;
        private final String bingoRewardName;
        private final int gridSize;

        public DigResult(BingoCell cell, int shardsEarned, boolean hasBingo,
                List<BingoLine> bingoLines, String bingoRewardName, int gridSize) {
            this.cell = cell;
            this.shardsEarned = shardsEarned;
            this.hasBingo = hasBingo;
            this.bingoLines = bingoLines;
            this.bingoRewardName = bingoRewardName;
            this.gridSize = gridSize;
        }

        public BingoCell getCell() {
            return cell;
        }

        public int getShardsEarned() {
            return shardsEarned;
        }

        public boolean isHasBingo() {
            return hasBingo;
        }

        public List<BingoLine> getBingoLines() {
            return bingoLines;
        }

        public String getBingoRewardName() {
            return bingoRewardName;
        }

        public int getGridSize() {
            return gridSize;
        }
    }
}
