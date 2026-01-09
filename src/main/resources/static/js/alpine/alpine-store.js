/**
 * alpine-store.js
 * Alpine.js 全域狀態管理
 * 
 * 提供跨頁面共享的狀態：
 * - user: 使用者登入狀態與餘額
 * - ui: 全域 UI 狀態 (loading、toast)
 */

document.addEventListener('alpine:init', () => {

    // ==================== 使用者狀態 ====================
    Alpine.store('user', {
        isLoggedIn: false,
        username: '',
        nickname: '',
        balance: 0,
        shards: 0,
        levelName: '',

        /**
         * 初始化 - 從 API 取得使用者資料
         */
        async init() {
            try {
                const res = await fetch('/api/member/profile');
                if (res.ok) {
                    const data = await res.json();
                    if (data.success && data.data) {
                        this.isLoggedIn = true;
                        this.username = data.data.username || '';
                        this.nickname = data.data.nickname || '';
                        this.balance = data.data.platformWalletBalance || 0;
                        this.shards = data.data.shards || 0;
                        this.levelName = data.data.level?.name || '';
                    }
                }
            } catch (e) {
                console.warn('User profile fetch failed:', e);
            }
        },

        /**
         * 刷新餘額
         */
        async refreshBalance() {
            try {
                const res = await fetch('/api/member/balance');
                if (res.ok) {
                    const data = await res.json();
                    if (data.success) {
                        this.balance = data.data.balance || 0;
                    }
                }
            } catch (e) {
                console.warn('Balance refresh failed:', e);
            }
        },

        /**
         * 刷新碎片
         */
        async refreshShards() {
            try {
                const res = await fetch('/api/shards/balance');
                if (res.ok) {
                    const data = await res.json();
                    if (data.success) {
                        this.shards = data.data.balance || 0;
                    }
                }
            } catch (e) {
                console.warn('Shards refresh failed:', e);
            }
        }
    });

    // ==================== UI 狀態 ====================
    Alpine.store('ui', {
        isLoading: false,
        toasts: [],

        /**
         * 顯示 Loading 遮罩
         */
        showLoading() {
            this.isLoading = true;
        },

        /**
         * 隱藏 Loading 遮罩
         */
        hideLoading() {
            this.isLoading = false;
        },

        /**
         * 顯示 Toast 訊息
         * @param {string} message - 訊息內容
         * @param {string} type - success | error | warning | info
         */
        toast(message, type = 'info') {
            const id = Date.now();
            this.toasts.push({ id, message, type });

            // 3 秒後自動移除
            setTimeout(() => {
                this.toasts = this.toasts.filter(t => t.id !== id);
            }, 3000);
        },

        success(message) { this.toast(message, 'success'); },
        error(message) { this.toast(message, 'error'); },
        warning(message) { this.toast(message, 'warning'); },
        info(message) { this.toast(message, 'info'); }
    });

    // ==================== 全域初始化 ====================
    // 頁面載入時自動初始化使用者狀態
    Alpine.store('user').init();
});

/**
 * 全域輔助函數 - 供舊代碼相容使用
 */
window.AlpineHelpers = {
    /**
     * 取得 Alpine Store
     */
    getStore(name) {
        return Alpine.store(name);
    },

    /**
     * 顯示 Toast (相容舊 Toast 系統)
     */
    toast(message, type = 'info') {
        Alpine.store('ui').toast(message, type);
    },

    /**
     * 刷新使用者餘額
     */
    async refreshBalance() {
        await Alpine.store('user').refreshBalance();
    }
};

// 相容舊版 Toast 全域函數
window.Toast = {
    success: (msg) => Alpine.store('ui').success(msg),
    error: (msg) => Alpine.store('ui').error(msg),
    warning: (msg) => Alpine.store('ui').warning(msg),
    info: (msg) => Alpine.store('ui').info(msg)
};
