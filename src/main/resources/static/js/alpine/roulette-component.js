/**
 * roulette-component.js
 * 幸運轉盤 Alpine.js 元件
 * 
 * 功能：
 * - 遊戲列表載入
 * - 轉盤繪製與旋轉動畫
 * - 幸運值進度
 * - 試抽/正式抽獎
 */

document.addEventListener('alpine:init', () => {

    Alpine.data('rouletteComponent', () => ({
        // ==================== 狀態 ====================
        games: [],
        currentGame: null,
        wheelSlots: [],
        isSpinning: false,
        result: null,

        // 幸運值
        luckyValue: 0,
        luckyThreshold: 1000,

        // 彈窗狀態
        showConfirmModal: false,
        showResultModal: false,
        isTrial: false,

        // ==================== 生命週期 ====================
        async init() {
            await Promise.all([
                this.fetchGames(),
                this.fetchLuckyValue(),
                this.fetchSettings()
            ]);

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
                const res = await fetch('/api/roulette');
                const data = await res.json();
                if (data.success) {
                    this.games = data.data || [];
                }
            } catch (e) {
                console.error('Failed to load games:', e);
                Alpine.store('ui').error('載入遊戲失敗');
            }
        },

        async fetchLuckyValue() {
            try {
                const res = await fetch('/api/roulette/lucky-value');
                const data = await res.json();
                if (data.success) {
                    this.luckyValue = data.data.luckyValue || 0;
                    this.luckyThreshold = data.data.threshold || this.luckyThreshold;
                }
            } catch (e) {
                console.warn('Lucky value fetch failed:', e);
            }
        },

        async fetchSettings() {
            try {
                const res = await fetch('/api/system/settings');
                const data = await res.json();
                if (data.success) {
                    this.luckyThreshold = data.data.luckyThreshold || 1000;
                }
            } catch (e) {
                console.warn('Settings fetch failed:', e);
            }
        },

        async fetchGameDetails(gameId) {
            try {
                const res = await fetch(`/api/roulette/${gameId}`);
                const data = await res.json();
                if (data.success) {
                    this.currentGame = data.data;
                    this.wheelSlots = data.data.slots || [];
                    this.$nextTick(() => this.drawWheel());
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
            this.wheelSlots = [];
        },

        // ==================== 轉盤繪製 ====================
        drawWheel() {
            const canvas = document.getElementById('wheelCanvas');
            if (!canvas || this.wheelSlots.length === 0) return;

            const ctx = canvas.getContext('2d');
            const rect = canvas.getBoundingClientRect();
            canvas.width = rect.width * window.devicePixelRatio;
            canvas.height = rect.height * window.devicePixelRatio;

            if (canvas.width === 0 || canvas.height === 0) return;

            const centerX = canvas.width / 2;
            const centerY = canvas.height / 2;
            const radius = Math.min(centerX, centerY) - 10;

            ctx.clearRect(0, 0, canvas.width, canvas.height);

            const sliceAngle = (2 * Math.PI) / this.wheelSlots.length;
            const colors = ['#FF6B6B', '#4ECDC4', '#FFD93D', '#6C5CE7', '#A8E6CF', '#FF8B94', '#FFEAA7', '#DFE6E9'];

            this.wheelSlots.forEach((slot, index) => {
                const startAngle = index * sliceAngle - Math.PI / 2;
                const endAngle = startAngle + sliceAngle;

                // 繪製扇形
                ctx.beginPath();
                ctx.moveTo(centerX, centerY);
                ctx.arc(centerX, centerY, radius, startAngle, endAngle);
                ctx.closePath();
                ctx.fillStyle = slot.color || colors[index % colors.length];
                ctx.fill();
                ctx.strokeStyle = 'white';
                ctx.lineWidth = 2;
                ctx.stroke();

                // 繪製文字
                ctx.save();
                ctx.translate(centerX, centerY);
                ctx.rotate(startAngle + sliceAngle / 2);
                ctx.textAlign = 'right';
                ctx.fillStyle = 'white';
                ctx.font = 'bold 12px sans-serif';
                ctx.fillText(slot.prizeName.substring(0, 6), radius - 15, 5);
                ctx.restore();
            });
        },

        // ==================== 抽獎流程 ====================
        openConfirmModal() {
            if (!this.currentGame) return;
            this.showConfirmModal = true;
        },

        closeConfirmModal() {
            this.showConfirmModal = false;
        },

        async spin(trial = false) {
            if (this.isSpinning || !this.currentGame) return;

            this.isTrial = trial;
            this.closeConfirmModal();
            this.isSpinning = true;

            try {
                let resultData;

                if (trial) {
                    // 試抽 - 隨機選一個
                    const slotIndex = Math.floor(Math.random() * this.wheelSlots.length);
                    resultData = {
                        isTrial: true,
                        slot: this.wheelSlots[slotIndex],
                        shardsEarned: Math.floor(Math.random() * 100)
                    };
                    await this.animateWheel(slotIndex);
                } else {
                    // 正式抽獎
                    const res = await fetch(`/api/roulette/${this.currentGame.id}/spin`, { method: 'POST' });
                    const data = await res.json();

                    if (!data.success) {
                        Alpine.store('ui').error(data.message || '旋轉失敗');
                        this.isSpinning = false;
                        return;
                    }

                    const slotIndex = this.wheelSlots.findIndex(s => s.id === data.data.slot.id);
                    await this.animateWheel(slotIndex);
                    resultData = data.data;
                }

                this.result = resultData;
                this.showResultModal = true;

                if (!trial) {
                    await this.fetchLuckyValue();
                    Alpine.store('user').refreshBalance();
                }

                if (typeof GameUtils !== 'undefined') {
                    GameUtils.playWinSound?.();
                    GameUtils.spawnConfetti?.();
                }
            } catch (e) {
                console.error('Spin error:', e);
                Alpine.store('ui').error('操作失敗');
            } finally {
                this.isSpinning = false;
            }
        },

        closeResultModal() {
            this.showResultModal = false;
            this.result = null;
        },

        // ==================== 動畫控制 ====================
        animateWheel(targetIndex) {
            return new Promise(resolve => {
                const canvas = document.getElementById('wheelCanvas');
                if (!canvas || typeof gsap === 'undefined') {
                    resolve();
                    return;
                }

                const totalSlots = this.wheelSlots.length;
                const sliceAngle = 360 / totalSlots;
                const currentAngle = gsap.getProperty(canvas, 'rotation') || 0;
                const extraSpins = 5;
                const targetRotation = currentAngle + (360 * extraSpins) + (360 - (targetIndex * sliceAngle + sliceAngle / 2));

                gsap.to(canvas, {
                    rotation: targetRotation,
                    duration: 5,
                    ease: 'expo.inOut',
                    onComplete: resolve
                });
            });
        },

        // ==================== 計算屬性 ====================
        get luckyProgress() {
            return Math.min((this.luckyValue / this.luckyThreshold) * 100, 100);
        },

        get isGuaranteeReady() {
            return this.luckyValue >= this.luckyThreshold;
        },

        get displayPrice() {
            return this.currentGame?.pricePerSpin || 0;
        }
    }));
});
