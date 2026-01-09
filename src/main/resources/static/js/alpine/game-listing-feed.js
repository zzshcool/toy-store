/**
 * 遊戲列表卡片元件 - 無限捲動載入
 * 可在各遊戲頁面重複使用，只需傳入不同的 API 端點與類型設定
 */
document.addEventListener('alpine:init', () => {
    /**
     * 建立遊戲列表資料
     * @param {Object} config
     * @param {string} config.apiUrl - API 端點 (如 /api/ichiban)
     * @param {string} config.type - 類型 (ICHIBAN, BLIND_BOX, GACHA, BINGO, ROULETTE)
     * @param {string} config.typeDisplay - 類型顯示名稱 (一番賞, 盲盒, 轉蛋, 九宮格, 轉盤)
     * @param {string} config.detailPath - 詳情頁路徑 (如 /ichiban)
     * @param {string} config.priceField - 價格欄位名稱 (pricePerDraw, pricePerBox, etc.)
     * @param {string} config.stockField - 剩餘數量欄位名稱 (remainingSlots, remainingCount, etc.)
     * @param {string} config.totalField - 總數量欄位名稱 (totalSlots, totalBoxes, etc.)
     * @param {function} config.onSelect - 選擇項目時的回調函數
     */
    Alpine.data('gameListingFeed', (config) => ({
        items: [],
        page: 0,
        size: 12,
        loading: false,
        hasMore: true,
        config: config,

        async init() {
            await this.loadMore();
            this.setupInfiniteScroll();
        },

        async loadMore() {
            if (this.loading || !this.hasMore) return;

            this.loading = true;
            try {
                // 大多數 API 不支援分頁，一次載入全部
                const res = await fetch(this.config.apiUrl);
                const data = await res.json();

                if (data.success && data.data) {
                    const newItems = Array.isArray(data.data) ? data.data : [];
                    this.items = newItems.map(item => this.transformItem(item));
                    this.hasMore = false; // 大多遊戲一次載入
                }
            } catch (e) {
                console.error('Failed to load game list:', e);
            } finally {
                this.loading = false;
            }
        },

        transformItem(item) {
            return {
                id: item.id,
                name: item.name,
                description: item.description || '',
                imageUrl: item.imageUrl || null,
                price: item[this.config.priceField] || 0,
                remainingStock: item[this.config.stockField] ?? null,
                totalStock: item[this.config.totalField] ?? null,
                type: this.config.type,
                typeDisplay: this.config.typeDisplay,
                ipName: item.ipName || null,
                raw: item // 保留原始資料
            };
        },

        setupInfiniteScroll() {
            // 此版本遊戲較少，一次載入，不需捲動
            // 若未來資料量大可啟用
        },

        selectItem(item) {
            if (typeof this.config.onSelect === 'function') {
                this.config.onSelect(item.raw);
            }
        },

        getLink(item) {
            if (this.config.detailPath) {
                return `${this.config.detailPath}?id=${item.id}`;
            }
            return '#';
        },

        getBadgeClass(type) {
            const classes = {
                'ICHIBAN': 'badge-ichiban',
                'BLIND_BOX': 'badge-blindbox',
                'GACHA': 'badge-gacha',
                'BINGO': 'badge-bingo',
                'ROULETTE': 'badge-roulette'
            };
            return classes[type] || 'badge-default';
        },

        hasStockDisplay() {
            return this.config.stockField && this.config.totalField;
        },

        formatStock(item) {
            if (item.remainingStock !== null && item.totalStock !== null) {
                return `${item.remainingStock}/${item.totalStock}`;
            }
            return '';
        }
    }));
});
