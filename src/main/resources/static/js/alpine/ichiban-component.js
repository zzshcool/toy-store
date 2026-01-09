/**
 * ichiban-component.js
 * 一番賞 Alpine.js 元件
 * 
 * 功能：
 * - 遊戲列表載入
 * - 格子選擇（最多10格）
 * - 購物車管理
 * - 撕票動畫流程
 * - 保底進度
 */

document.addEventListener('alpine:init', () => {

    Alpine.data('ichibanComponent', () => ({
        // ==================== 狀態 ====================
        boxes: [],
        currentBox: null,
        slots: [],
        prizes: [],

        // 選號
        selectedSlots: [],
        pricePerDraw: 0,

        // 保底
        pityValue: 0,
        pityLimit: 50,

        // UI 狀態
        isLoading: false,
        isProcessing: false,
        showPurchaseModal: false,
        showResultModal: false,
        results: [],
        totalShards: 0,
        isTrial: false,

        // 自動刷新
        refreshTimer: null,

        // ==================== 計算屬性 ====================
        get totalPrice() {
            return this.selectedSlots.length * this.pricePerDraw;
        },

        get pityProgress() {
            return Math.min((this.pityValue / this.pityLimit) * 100, 100);
        },

        get pityRemaining() {
            return Math.max(0, this.pityLimit - this.pityValue);
        },

        // ==================== 生命週期 ====================
        async init() {
            await this.fetchBoxes();
            this.startAutoRefresh();

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
                const res = await fetch('/api/ichiban');
                const data = await res.json();
                if (data.success) {
                    this.boxes = data.data || [];
                }
            } catch (e) {
                console.error('Failed to load boxes:', e);
                Alpine.store('ui').error('載入遊戲失敗');
            }
        },

        async fetchBoxDetails(boxId) {
            try {
                const [boxRes, slotsRes] = await Promise.all([
                    fetch(`/api/ichiban/${boxId}`).then(r => r.json()),
                    fetch(`/api/ichiban/${boxId}/slots`).then(r => r.json())
                ]);

                if (boxRes.success) {
                    this.currentBox = boxRes.data;
                    this.pricePerDraw = boxRes.data.pricePerDraw;
                    this.prizes = boxRes.data.prizes || [];
                }

                if (slotsRes.success) {
                    this.slots = slotsRes.data || [];
                    this.updatePityFromSlots();
                }
            } catch (e) {
                console.error('Failed to load box details:', e);
            }
        },

        async refreshSlots() {
            if (!this.currentBox) return;
            try {
                const res = await fetch(`/api/ichiban/${this.currentBox.id}/slots`);
                const data = await res.json();
                if (data.success) {
                    this.slots = data.data || [];
                }
            } catch (e) {
                console.warn('Refresh failed:', e);
            }
        },

        startAutoRefresh() {
            this.refreshTimer = setInterval(() => {
                if (this.currentBox) this.refreshSlots();
            }, 10000);
        },

        updatePityFromSlots() {
            const opened = this.slots.filter(s => s.status === 'SOLD' || s.status === 'REVEALED').length;
            this.pityValue = opened % this.pityLimit;
        },

        // ==================== 遊戲選擇 ====================
        selectBox(box) {
            this.currentBox = null;
            this.selectedSlots = [];
            this.fetchBoxDetails(box.id);
        },

        backToList() {
            this.currentBox = null;
            this.slots = [];
            this.selectedSlots = [];
        },

        // ==================== 格子操作 ====================
        isSlotSelected(slot) {
            return this.selectedSlots.includes(slot.slotNumber);
        },

        isSlotClickable(slot) {
            if (slot.status === 'SOLD' || slot.status === 'REVEALED') return false;
            if (slot.status === 'LOCKED' && !slot.isLockExpired) return false;
            return true;
        },

        toggleSlot(slot) {
            if (!this.isSlotClickable(slot)) return;

            const idx = this.selectedSlots.indexOf(slot.slotNumber);
            if (idx > -1) {
                this.selectedSlots.splice(idx, 1);
            } else {
                if (this.selectedSlots.length >= 10) {
                    Alpine.store('ui').warning('單次最多選擇 10 格');
                    return;
                }
                this.selectedSlots.push(slot.slotNumber);
            }
        },

        clearSelection() {
            this.selectedSlots = [];
        },

        async randomSelect(count) {
            const available = this.slots
                .filter(s => this.isSlotClickable(s) && !this.selectedSlots.includes(s.slotNumber))
                .map(s => s.slotNumber);

            if (available.length === 0) {
                Alpine.store('ui').warning('已無可選格子');
                return;
            }

            const remaining = 10 - this.selectedSlots.length;
            const actual = Math.min(count, remaining, available.length);

            const shuffled = available.sort(() => Math.random() - 0.5);
            this.selectedSlots.push(...shuffled.slice(0, actual));

            Alpine.store('ui').success(`已隨機選擇 ${actual} 格`);
        },

        // ==================== 購買流程 ====================
        openPurchaseModal() {
            if (this.selectedSlots.length === 0) {
                Alpine.store('ui').warning('請至少選擇一個格子');
                return;
            }
            this.showPurchaseModal = true;
        },

        closePurchaseModal() {
            this.showPurchaseModal = false;
        },

        async purchase(trial = false) {
            if (this.isProcessing || this.selectedSlots.length === 0) return;

            this.isTrial = trial;
            this.closePurchaseModal();
            this.isProcessing = true;

            try {
                let resultData;

                if (trial) {
                    const res = await fetch(`/api/ichiban/${this.currentBox.id}/trial`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ count: this.selectedSlots.length })
                    });
                    const data = await res.json();
                    if (!data.success) throw new Error(data.message);
                    resultData = {
                        prizes: data.data.results.map(r => ({
                            slotNumber: r.slotNumber,
                            prize: r.prize,
                            shards: r.shards
                        })),
                        totalShards: data.data.results.reduce((s, r) => s + r.shards, 0),
                        totalCost: this.totalPrice,
                        isTrial: true
                    };
                } else {
                    const res = await fetch(`/api/ichiban/${this.currentBox.id}/purchase`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ slotNumbers: this.selectedSlots })
                    });
                    const data = await res.json();
                    if (!data.success) throw new Error(data.message);
                    resultData = data.data;
                }

                this.results = resultData.prizes || [];
                this.totalShards = resultData.totalShards || 0;
                this.showResultModal = true;

                this.selectedSlots = [];

                if (!trial) {
                    await this.fetchBoxDetails(this.currentBox.id);
                    Alpine.store('user').refreshBalance();
                }

                if (typeof GameUtils !== 'undefined') {
                    GameUtils.playWinSound?.();
                    GameUtils.spawnConfetti?.();
                }
            } catch (e) {
                console.error('Purchase error:', e);
                Alpine.store('ui').error(e.message || '購買失敗');
            } finally {
                this.isProcessing = false;
            }
        },

        closeResultModal() {
            this.showResultModal = false;
            this.results = [];
        }
    }));
});
