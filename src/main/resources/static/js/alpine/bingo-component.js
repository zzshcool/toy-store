/**
 * bingo-component.js
 * 九宮格挖寶 Alpine.js 元件
 * 
 * 功能：
 * - 遊戲列表載入
 * - 九宮格挖掘
 * - 批次模式
 * - BINGO 連線獎勵
 */

document.addEventListener('alpine:init', () => {

    Alpine.data('bingoComponent', () => ({
        // ==================== 狀態 ====================
        games: [],
        currentGame: null,
        cells: [],
        prizes: [],

        // 批次模式
        isBatchMode: false,
        selectedCells: [],

        // UI 狀態
        isDigging: false,
        result: null,
        showConfirmModal: false,
        showResultModal: false,
        pendingPositions: [],
        isTrial: false,

        // ==================== 計算屬性 ====================
        get totalBatchCost() {
            return this.selectedCells.length * (this.currentGame?.pricePerDig || 0);
        },

        get gridClass() {
            const size = this.currentGame?.gridSize || 3;
            return `grid-${size}`;
        },

        // ==================== 生命週期 ====================
        async init() {
            await this.fetchGames();

            // 檢查 URL 參數，若有 gameId 則自動選擇
            const urlParams = new URLSearchParams(window.location.search);
            const gameId = urlParams.get('gameId');
            if (gameId) {
                const targetGame = this.games.find(g => g.id == gameId);
                if (targetGame) {
                    this.selectGame(targetGame);
                }
            }
        },

        // ==================== 資料獲取 ====================
        async fetchGames() {
            try {
                const res = await fetch('/api/bingo');
                const data = await res.json();
                if (data.success) {
                    this.games = data.data || [];
                }
            } catch (e) {
                console.error('Failed to load games:', e);
                Alpine.store('ui').error('載入遊戲失敗');
            }
        },

        async fetchGameDetails(gameId) {
            try {
                const res = await fetch(`/api/bingo/${gameId}`);
                const data = await res.json();
                if (data.success) {
                    this.currentGame = data.data;
                    this.cells = data.data.cells || [];
                    this.prizes = data.data.prizes || [];
                }
            } catch (e) {
                console.error('Failed to load game details:', e);
            }
        },

        // ==================== 遊戲選擇 ====================
        selectGame(game) {
            this.fetchGameDetails(game.id);
        },

        backToList() {
            this.currentGame = null;
            this.cells = [];
            this.selectedCells = [];
            this.isBatchMode = false;
        },

        // ==================== 批次模式 ====================
        toggleBatchMode() {
            this.isBatchMode = !this.isBatchMode;
            if (!this.isBatchMode) {
                this.selectedCells = [];
            }
        },

        // ==================== 格子操作 ====================
        handleCellClick(cell) {
            if (cell.isRevealed) return;

            if (this.isBatchMode) {
                const idx = this.selectedCells.indexOf(cell.position);
                if (idx > -1) {
                    this.selectedCells.splice(idx, 1);
                } else {
                    this.selectedCells.push(cell.position);
                }
            } else {
                this.openDigConfirm([cell.position]);
            }
        },

        isCellSelected(cell) {
            return this.selectedCells.includes(cell.position);
        },

        // ==================== 挖掘流程 ====================
        openDigConfirm(positions) {
            this.pendingPositions = positions;
            this.showConfirmModal = true;
        },

        closeConfirmModal() {
            this.showConfirmModal = false;
            this.pendingPositions = [];
        },

        confirmBatchDig() {
            if (this.selectedCells.length === 0) {
                Alpine.store('ui').warning('請選擇至少一個格子');
                return;
            }
            this.openDigConfirm([...this.selectedCells]);
        },

        async dig(trial = false) {
            if (this.isDigging || this.pendingPositions.length === 0) return;

            this.isTrial = trial;
            this.closeConfirmModal();
            this.isDigging = true;

            // 動畫
            this.playDigAnimation();

            try {
                let resultData;

                if (trial) {
                    // 試挖
                    await this.wait(800);
                    resultData = {
                        isTrial: true,
                        cells: this.pendingPositions.map(p => ({
                            position: p,
                            prizeName: '試挖獎品 ' + Math.floor(Math.random() * 10)
                        })),
                        shardsEarned: Math.floor(Math.random() * 50)
                    };
                } else {
                    // 正式挖掘
                    const isSingle = this.pendingPositions.length === 1;
                    const endpoint = isSingle
                        ? `/api/bingo/${this.currentGame.id}/dig/${this.pendingPositions[0]}`
                        : `/api/bingo/${this.currentGame.id}/dig-batch`;

                    const options = isSingle
                        ? { method: 'POST' }
                        : { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ positions: this.pendingPositions }) };

                    const res = await fetch(endpoint, options);
                    const data = await res.json();

                    if (!data.success) {
                        Alpine.store('ui').error(data.message || '挖掘失敗');
                        this.isDigging = false;
                        return;
                    }

                    resultData = data.data;
                }

                await this.wait(600);
                this.flipCells(resultData.cells || [resultData.cell]);

                this.result = resultData;
                this.showResultModal = true;

                if (!trial) {
                    await this.fetchGameDetails(this.currentGame.id);
                    Alpine.store('user').refreshBalance();
                }

                this.selectedCells = [];

                if (resultData.hasBingo && typeof GameUtils !== 'undefined') {
                    GameUtils.spawnConfetti?.();
                }
            } catch (e) {
                console.error('Dig error:', e);
                Alpine.store('ui').error('操作失敗');
            } finally {
                this.isDigging = false;
            }
        },

        closeResultModal() {
            this.showResultModal = false;
            this.result = null;
        },

        // ==================== 動畫輔助 ====================
        playDigAnimation() {
            this.pendingPositions.forEach(pos => {
                const cell = document.querySelector(`[data-pos="${pos}"]`);
                if (cell && typeof gsap !== 'undefined') {
                    gsap.to(cell, {
                        x: 'random(-3, 3)',
                        y: 'random(-3, 3)',
                        duration: 0.08,
                        repeat: 6,
                        yoyo: true
                    });
                }
            });
        },

        flipCells(cells) {
            cells.forEach((c, i) => {
                setTimeout(() => {
                    const cell = document.querySelector(`[data-pos="${c.position}"]`);
                    if (cell) cell.classList.add('flipped');
                }, i * 100);
            });
        },

        wait(ms) {
            return new Promise(r => setTimeout(r, ms));
        }
    }));
});
