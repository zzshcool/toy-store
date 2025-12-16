package com.toy.store.service;

import com.toy.store.model.*;
import com.toy.store.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 九宮格服務
 * 處理挖掘、連線檢查等核心邏輯
 */
@Service
public class BingoService {

    @Autowired
    private BingoGameRepository gameRepository;

    @Autowired
    private BingoCellRepository cellRepository;

    @Autowired
    private MemberLuckyValueRepository luckyValueRepository;

    @Autowired
    private ShardTransactionRepository shardTransactionRepository;

    @Autowired
    private GachaRecordRepository recordRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private SystemSettingService settingService;

    private final Random random = new Random();

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
        return cellRepository.findByGameIdOrderByPositionAsc(gameId);
    }

    /**
     * 挖掘格子
     * 
     * @return DigResult 包含挖到的獎品、是否連線等資訊
     */
    @Transactional
    public DigResult dig(Long gameId, Integer position, Long memberId) {
        BingoGame game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("遊戲不存在"));

        BingoCell cell = cellRepository.findByGameIdAndPosition(gameId, position)
                .orElseThrow(() -> new RuntimeException("格子不存在"));

        if (cell.getIsRevealed()) {
            throw new RuntimeException("該格子已被挖掘");
        }

        // 扣款
        transactionService.updateWalletBalance(memberId, game.getPricePerDig().negate(),
                Transaction.TransactionType.MYSTERY_BOX_COST, "BINGO-" + gameId + "-" + position);

        // 挖掘格子
        cell.dig(memberId);
        cellRepository.save(cell);

        // 產出碎片
        int shardsEarned = generateShards();
        addShardsToMember(memberId, shardsEarned, "BINGO", gameId);

        // 檢查連線
        List<BingoLine> bingoLines = checkBingoLines(gameId, game.getGridSize());
        boolean hasBingo = !bingoLines.isEmpty();

        // 如果有新連線，發放連線獎勵
        if (hasBingo && game.getBingoRewardName() != null) {
            // 記錄連線獎勵
            GachaRecord bonusRecord = GachaRecord.createBingoRecord(memberId, gameId,
                    "連線獎勵: " + game.getBingoRewardName(), 0);
            recordRepository.save(bonusRecord);
        }

        // 記錄抽獎
        GachaRecord record = GachaRecord.createBingoRecord(memberId, gameId,
                cell.getPrizeName(), shardsEarned);
        recordRepository.save(record);

        return new DigResult(cell, shardsEarned, hasBingo, bingoLines,
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

    private int generateShards() {
        int min = settingService.getIntSetting(SystemSetting.GACHA_SHARD_MIN, 10);
        int max = settingService.getIntSetting(SystemSetting.GACHA_SHARD_MAX, 50);
        return random.nextInt(max - min + 1) + min;
    }

    private void addShardsToMember(Long memberId, int amount, String sourceType, Long sourceId) {
        MemberLuckyValue luckyValue = luckyValueRepository.findByMemberId(memberId)
                .orElse(new MemberLuckyValue(memberId));
        luckyValue.addShards(amount);
        luckyValueRepository.save(luckyValue);

        ShardTransaction tx = ShardTransaction.createEarn(memberId, amount,
                ShardTransaction.TransactionType.EARN_DRAW,
                "九宮格挖掘獲得", sourceType, sourceId);
        shardTransactionRepository.save(tx);
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
