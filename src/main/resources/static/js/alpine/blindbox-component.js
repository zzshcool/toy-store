/**
 * blindbox-component.js
 * 盲盒 Alpine.js 元件
 * 
 * 功能：
 * - 盲盒列表載入
 * - 格子選擇與鎖定
 * - 倒數計時
 * - 購買流程
 */

document.addEventListener('alpine:init', () => {

    Alpine.data('blindboxComponent', () => ({
        // ==================== 狀態 ====================
        boxes: [],
        currentBox: null,
        items: [],

        // 購買流程
        selectedItem: null,
        countdownTimer: null,
        remainingSeconds: 0,

        // UI 狀態
        isProcessing: false,
        result: null,
        showPurchaseModal: false,
        showResultModal: false,
        isTrial: false,

        // ==================== 計算屬性 ====================
        get pricePerBox() {
            return this.currentBox?.pricePerBox || 0;
        },

        get fullBoxPrice() {
            return this.currentBox?.fullBoxPrice || 0;
        },

        // ==================== 生命週期 ====================
        async init() {
            await this.fetchBoxes();

            // 檢查 URL 參數，若有 boxId 則自動選擇
            const urlParams = new URLSearchParams(window.location.search);
            const boxId = urlParams.get('boxId');
            if (boxId) {
                const targetBox = this.boxes.find(b => b.id == boxId);
                if (targetBox) {
                    this.selectBox(targetBox);
                }
            }
        },

        // ==================== 資料獲取 ====================
        async fetchBoxes() {
            try {
                const res = await fetch('/api/blindbox');
                const data = await res.json();
                if (data.success) {
                    this.boxes = data.data || [];
                }
            } catch (e) {
                console.error('Failed to load boxes:', e);
                Alpine.store('ui').error('載入盲盒失敗');
            }
        },

        async fetchBoxDetails(boxId) {
            try {
                const res = await fetch(`/api/blindbox/${boxId}`);
                const data = await res.json();
                if (data.success) {
                    this.currentBox = data.data;
                    this.items = data.data.items || [];
                }
            } catch (e) {
                console.error('Failed to load box details:', e);
            }
        },

        // ==================== 盲盒選擇 ====================
        selectBox(box) {
            this.fetchBoxDetails(box.id);
        },

        backToList() {
            this.currentBox = null;
            this.items = [];
            this.selectedItem = null;
            this.clearCountdown();
        },

        // ==================== 格子操作 ====================
        async selectItem(item) {
            if (item.status === 'SOLD') return;
            if (item.status === 'LOCKED' && !item.isLockExpired) return;

            const isLoggedIn = Alpine.store('user').isLoggedIn;
            if (!isLoggedIn) {
                this.trialDraw();
                return;
            }

            try {
                const res = await fetch(`/api/blindbox/${this.currentBox.id}/items/${item.boxNumber}/lock`, { method: 'POST' });
                const data = await res.json();

                if (data.success) {
                    this.selectedItem = item;
                    this.startCountdown(data.data.remainingSeconds || 180);
                    this.showPurchaseModal = true;
                    await this.fetchBoxDetails(this.currentBox.id);
                } else {
                    Alpine.store('ui').error(data.message);
                }
            } catch (e) {
                console.error('Lock error:', e);
                Alpine.store('ui').error('操作失敗');
            }
        },

        getItemStatus(item) {
            if (item.status === 'SOLD') return '已售';
            if (item.status === 'LOCKED' && !item.isLockExpired) return '鎖定中';
            return '可選';
        },

        isItemClickable(item) {
            if (item.status === 'SOLD') return false;
            if (item.status === 'LOCKED' && !item.isLockExpired) return false;
            return true;
        },

        // ==================== 倒數計時 ====================
        startCountdown(seconds) {
            this.clearCountdown();
            this.remainingSeconds = seconds;

            this.countdownTimer = setInterval(() => {
                this.remainingSeconds--;
                if (this.remainingSeconds <= 0) {
                    this.clearCountdown();
                    this.closePurchaseModal();
                    Alpine.store('ui').warning('鎖定已過期');
                    this.fetchBoxDetails(this.currentBox.id);
                }
            }, 1000);
        },

        clearCountdown() {
            if (this.countdownTimer) {
                clearInterval(this.countdownTimer);
                this.countdownTimer = null;
            }
        },

        // ==================== 購買流程 ====================
        closePurchaseModal() {
            this.showPurchaseModal = false;
            this.selectedItem = null;
            this.clearCountdown();
        },

        async confirmPurchase() {
            if (!this.selectedItem || this.isProcessing) return;

            this.isProcessing = true;

            try {
                const res = await fetch(`/api/blindbox/${this.currentBox.id}/items/${this.selectedItem.boxNumber}/purchase`, { method: 'POST' });
                const data = await res.json();

                this.closePurchaseModal();

                if (data.success) {
                    this.result = data.data;
                    this.showResultModal = true;
                    await this.fetchBoxDetails(this.currentBox.id);
                    Alpine.store('user').refreshBalance();

                    if (typeof GameUtils !== 'undefined') {
                        GameUtils.playWinSound?.();
                        GameUtils.spawnConfetti?.();
                    }
                } else {
                    Alpine.store('ui').error(data.message || '購買失敗');
                }
            } catch (e) {
                console.error('Purchase error:', e);
                Alpine.store('ui').error('操作失敗');
            } finally {
                this.isProcessing = false;
            }
        },

        async randomPurchase() {
            const isLoggedIn = Alpine.store('user').isLoggedIn;
            if (!isLoggedIn) {
                this.trialDraw();
                return;
            }

            this.isProcessing = true;

            try {
                const res = await fetch(`/api/blindbox/${this.currentBox.id}/random-purchase`, { method: 'POST' });
                const data = await res.json();

                if (data.success) {
                    this.result = data.data;
                    this.showResultModal = true;
                    await this.fetchBoxDetails(this.currentBox.id);
                    Alpine.store('user').refreshBalance();
                } else {
                    Alpine.store('ui').error(data.message);
                }
            } catch (e) {
                console.error('Random purchase error:', e);
                Alpine.store('ui').error('操作失敗');
            } finally {
                this.isProcessing = false;
            }
        },

        async trialDraw() {
            this.isTrial = true;
            this.isProcessing = true;

            try {
                const res = await fetch(`/api/blindbox/${this.currentBox.id}/trial`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ count: 1 })
                });
                const data = await res.json();

                if (data.success) {
                    this.result = { isTrial: true, ...data.data };
                    this.showResultModal = true;
                } else {
                    Alpine.store('ui').error(data.message);
                }
            } catch (e) {
                console.error('Trial error:', e);
                Alpine.store('ui').error('操作失敗');
            } finally {
                this.isProcessing = false;
            }
        },

        closeResultModal() {
            this.showResultModal = false;
            this.result = null;
            this.isTrial = false;
        }
    }));
});
