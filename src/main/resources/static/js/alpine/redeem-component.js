/**
 * redeem-component.js
 * 碎片兌換商店 Alpine.js 元件
 * 
 * 功能：
 * - 商品列表載入
 * - 碎片餘額顯示
 * - 兌換確認流程
 * - 交易紀錄
 */

document.addEventListener('alpine:init', () => {

    Alpine.data('redeemComponent', () => ({
        // ==================== 狀態 ====================
        items: [],
        transactions: [],
        selectedItem: null,

        // 彈窗狀態
        showRedeemModal: false,
        showSuccessModal: false,
        isProcessing: false,
        redeemResult: null,

        // ==================== 計算屬性 ====================
        get shardBalance() {
            return Alpine.store('user').shards || 0;
        },

        get canAfford() {
            return this.selectedItem && this.shardBalance >= this.selectedItem.shardCost;
        },

        // ==================== 生命週期 ====================
        async init() {
            await Promise.all([
                this.fetchItems(),
                this.fetchTransactions(),
                Alpine.store('user').refreshShards()
            ]);
        },

        // ==================== 資料獲取 ====================
        async fetchItems() {
            try {
                const res = await fetch('/api/redeem-shop');
                const data = await res.json();
                if (data.success) {
                    this.items = data.data || [];
                }
            } catch (e) {
                console.error('Failed to load items:', e);
                Alpine.store('ui').error('載入商品失敗');
            }
        },

        async fetchTransactions() {
            try {
                const res = await fetch('/api/shards/transactions');
                const data = await res.json();
                if (data.success) {
                    this.transactions = data.data || [];
                }
            } catch (e) {
                console.error('Failed to load transactions:', e);
            }
        },

        // ==================== 兌換流程 ====================
        openRedeemModal(item) {
            this.selectedItem = item;
            this.showRedeemModal = true;
        },

        closeRedeemModal() {
            this.showRedeemModal = false;
            this.selectedItem = null;
        },

        async confirmRedeem() {
            if (!this.selectedItem || this.isProcessing) return;
            if (!this.canAfford) {
                Alpine.store('ui').warning('碎片不足');
                return;
            }

            this.isProcessing = true;

            try {
                const res = await fetch(`/api/redeem-shop/${this.selectedItem.id}/redeem`, {
                    method: 'POST'
                });
                const data = await res.json();

                this.closeRedeemModal();

                if (data.success) {
                    this.redeemResult = data.data;
                    this.showSuccessModal = true;

                    // 刷新資料
                    await Promise.all([
                        this.fetchItems(),
                        this.fetchTransactions(),
                        Alpine.store('user').refreshShards()
                    ]);

                    // 播放特效
                    if (typeof GameUtils !== 'undefined') {
                        GameUtils.playWinSound?.();
                        GameUtils.spawnConfetti?.();
                    }
                } else {
                    Alpine.store('ui').error(data.message || '兌換失敗');
                }
            } catch (e) {
                console.error('Redeem error:', e);
                Alpine.store('ui').error('操作失敗');
            } finally {
                this.isProcessing = false;
            }
        },

        closeSuccessModal() {
            this.showSuccessModal = false;
            this.redeemResult = null;
        },

        // ==================== 輔助方法 ====================
        getGradientClass(type) {
            const map = {
                'S_RANK': 'game-card__placeholder--orange',
                'HIDDEN': 'game-card__placeholder--purple',
                'SPECIAL': '',
                'default': 'game-card__placeholder--teal'
            };
            return map[type] || map.default;
        },

        formatNumber(num) {
            return (num || 0).toLocaleString();
        }
    }));
});
