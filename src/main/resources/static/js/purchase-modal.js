/**
 * ToySoul 購買確認彈窗系統
 * 統一的購買確認流程，支援紅利折抵與優惠券
 * 樣式已移至 purchase-modal.css
 */

(function () {
    'use strict';

    // ================= 購買確認系統 =================

    window.PurchaseModal = {

        currentConfig: null,
        selectedCoupon: null,
        useBonusPoints: false,
        bonusPointsToUse: 0,

        /**
         * 顯示購買確認彈窗
         * @param {Object} config - 配置
         * @param {string} config.title - 標題
         * @param {Array} config.items - 購買項目 [{name, price, quantity, imageUrl}]
         * @param {number} config.totalPrice - 總價
         * @param {number} config.availableBonusPoints - 可用紅利點數
         * @param {number} config.bonusPointsRate - 紅利折抵比率 (如 10 表示 10 點 = $1)
         * @param {Array} config.availableCoupons - 可用優惠券 [{id, name, discount, type}]
         * @param {Function} config.onConfirm - 確認購買回調
         * @param {Function} config.onCancel - 取消回調
         */
        show: function (config) {
            this.currentConfig = config;
            this.selectedCoupon = null;
            this.useBonusPoints = false;
            this.bonusPointsToUse = 0;

            const modal = this.createModal(config);
            document.body.appendChild(modal);
            document.body.style.overflow = 'hidden';

            // 動畫入場
            if (typeof gsap !== 'undefined') {
                gsap.from('.purchase-modal-content', {
                    duration: 0.4,
                    y: 50,
                    opacity: 0,
                    ease: 'back.out(1.2)'
                });
            }

            this.updateTotal();
        },

        /**
         * 創建彈窗 DOM
         */
        createModal: function (config) {
            const modal = document.createElement('div');
            modal.id = 'purchaseModal';
            modal.className = 'purchase-modal';

            const itemsHtml = config.items.map(item => `
                <div class="purchase-item">
                    <div class="item-image">
                        ${item.imageUrl ? `<img src="${item.imageUrl}" alt="${item.name}">` : '🎁'}
                    </div>
                    <div class="item-info">
                        <div class="item-name">${item.name}</div>
                        <div class="item-quantity">x${item.quantity || 1}</div>
                    </div>
                    <div class="item-price">$${item.price}</div>
                </div>
            `).join('');

            const couponsHtml = config.availableCoupons && config.availableCoupons.length > 0
                ? `
                    <div class="coupon-section">
                        <div class="section-title">🎫 優惠券</div>
                        <select id="couponSelect" onchange="PurchaseModal.selectCoupon(this.value)">
                            <option value="">不使用優惠券</option>
                            ${config.availableCoupons.map(c =>
                    `<option value="${c.id}" data-discount="${c.discount}" data-type="${c.type}">
                                    ${c.name} (${c.type === 'PERCENT' ? c.discount + '% OFF' : '-$' + c.discount})
                                </option>`
                ).join('')}
                        </select>
                    </div>
                ` : '';

            const bonusHtml = config.availableBonusPoints && config.availableBonusPoints > 0
                ? `
                    <div class="bonus-section">
                        <div class="section-title">💰 紅利折抵</div>
                        <div class="bonus-row">
                            <label class="toggle-label">
                                <input type="checkbox" id="useBonusToggle" onchange="PurchaseModal.toggleBonus(this.checked)">
                                <span class="toggle-text">使用紅利點數</span>
                            </label>
                            <span class="bonus-available">可用: ${config.availableBonusPoints} 點</span>
                        </div>
                        <div id="bonusSliderContainer" class="bonus-slider-container" style="display: none;">
                            <input type="range" id="bonusSlider" 
                                min="0" max="${config.availableBonusPoints}" value="0"
                                oninput="PurchaseModal.updateBonusPoints(this.value)">
                            <div class="bonus-amount">
                                使用 <span id="bonusPointsDisplay">0</span> 點 
                                = 折抵 $<span id="bonusDiscountDisplay">0</span>
                            </div>
                        </div>
                    </div>
                ` : '';

            modal.innerHTML = `
                <div class="purchase-modal-backdrop" onclick="PurchaseModal.cancel()"></div>
                <div class="purchase-modal-content">
                    <div class="purchase-modal-header">
                        <h3>${config.title || '確認購買'}</h3>
                        <button class="modal-close" onclick="PurchaseModal.cancel()">&times;</button>
                    </div>
                    
                    <div class="purchase-modal-body">
                        <div class="purchase-items">
                            ${itemsHtml}
                        </div>

                        ${couponsHtml}
                        ${bonusHtml}

                        <div class="purchase-summary">
                            <div class="summary-row">
                                <span>商品小計</span>
                                <span>$<span id="subtotalDisplay">${config.totalPrice}</span></span>
                            </div>
                            <div class="summary-row discount-row" id="couponDiscountRow" style="display: none;">
                                <span>優惠券折扣</span>
                                <span class="discount-value">-$<span id="couponDiscountDisplay">0</span></span>
                            </div>
                            <div class="summary-row discount-row" id="bonusDiscountRow" style="display: none;">
                                <span>紅利折抵</span>
                                <span class="discount-value">-$<span id="bonusPointsDiscountDisplay">0</span></span>
                            </div>
                            <div class="summary-row total-row">
                                <span>應付金額</span>
                                <span class="total-price">$<span id="finalTotalDisplay">${config.totalPrice}</span></span>
                            </div>
                        </div>
                    </div>

                    <div class="purchase-modal-footer">
                        <button class="btn-cancel" onclick="PurchaseModal.cancel()">取消</button>
                        <button class="btn-confirm" onclick="PurchaseModal.confirm()">
                            確認購買
                        </button>
                    </div>
                </div>
            `;

            return modal;
        },

        /**
         * 選擇優惠券
         */
        selectCoupon: function (couponId) {
            if (!couponId) {
                this.selectedCoupon = null;
            } else {
                const select = document.getElementById('couponSelect');
                const option = select.options[select.selectedIndex];
                this.selectedCoupon = {
                    id: couponId,
                    discount: parseFloat(option.dataset.discount),
                    type: option.dataset.type
                };
            }
            this.updateTotal();
        },

        /**
         * 切換紅利使用
         */
        toggleBonus: function (checked) {
            this.useBonusPoints = checked;
            const container = document.getElementById('bonusSliderContainer');
            if (container) {
                container.style.display = checked ? 'block' : 'none';
            }
            if (!checked) {
                this.bonusPointsToUse = 0;
                const slider = document.getElementById('bonusSlider');
                if (slider) slider.value = 0;
            }
            this.updateTotal();
        },

        /**
         * 更新紅利點數
         */
        updateBonusPoints: function (value) {
            this.bonusPointsToUse = parseInt(value) || 0;

            const rate = this.currentConfig.bonusPointsRate || 10;
            const discount = Math.floor(this.bonusPointsToUse / rate);

            const pointsDisplay = document.getElementById('bonusPointsDisplay');
            const discountDisplay = document.getElementById('bonusDiscountDisplay');

            if (pointsDisplay) pointsDisplay.textContent = this.bonusPointsToUse;
            if (discountDisplay) discountDisplay.textContent = discount;

            this.updateTotal();
        },

        /**
         * 更新總計
         */
        updateTotal: function () {
            if (!this.currentConfig) return;

            let total = this.currentConfig.totalPrice;
            let couponDiscount = 0;
            let bonusDiscount = 0;

            // 優惠券折扣
            if (this.selectedCoupon) {
                if (this.selectedCoupon.type === 'PERCENT') {
                    couponDiscount = Math.floor(total * this.selectedCoupon.discount / 100);
                } else {
                    couponDiscount = this.selectedCoupon.discount;
                }
            }

            // 紅利折抵
            if (this.useBonusPoints && this.bonusPointsToUse > 0) {
                const rate = this.currentConfig.bonusPointsRate || 10;
                bonusDiscount = Math.floor(this.bonusPointsToUse / rate);
            }

            // 計算最終價格
            total = Math.max(0, total - couponDiscount - bonusDiscount);

            // 更新顯示
            const couponRow = document.getElementById('couponDiscountRow');
            const couponDisplay = document.getElementById('couponDiscountDisplay');
            if (couponRow && couponDisplay) {
                couponRow.style.display = couponDiscount > 0 ? 'flex' : 'none';
                couponDisplay.textContent = couponDiscount;
            }

            const bonusRow = document.getElementById('bonusDiscountRow');
            const bonusDisplay = document.getElementById('bonusPointsDiscountDisplay');
            if (bonusRow && bonusDisplay) {
                bonusRow.style.display = bonusDiscount > 0 ? 'flex' : 'none';
                bonusDisplay.textContent = bonusDiscount;
            }

            const finalDisplay = document.getElementById('finalTotalDisplay');
            if (finalDisplay) {
                finalDisplay.textContent = total;

                // 價格變化動畫
                if (typeof gsap !== 'undefined') {
                    gsap.from(finalDisplay, {
                        scale: 1.2,
                        duration: 0.3,
                        ease: 'back.out(2)'
                    });
                }
            }
        },

        /**
         * 確認購買
         */
        confirm: function () {
            if (!this.currentConfig || !this.currentConfig.onConfirm) return;

            const result = {
                items: this.currentConfig.items,
                originalTotal: this.currentConfig.totalPrice,
                couponId: this.selectedCoupon ? this.selectedCoupon.id : null,
                bonusPointsUsed: this.useBonusPoints ? this.bonusPointsToUse : 0,
                finalTotal: parseInt(document.getElementById('finalTotalDisplay').textContent)
            };

            this.close();
            this.currentConfig.onConfirm(result);
        },

        /**
         * 取消
         */
        cancel: function () {
            if (this.currentConfig && this.currentConfig.onCancel) {
                this.currentConfig.onCancel();
            }
            this.close();
        },

        /**
         * 關閉彈窗
         */
        close: function () {
            const modal = document.getElementById('purchaseModal');
            if (!modal) return;

            if (typeof gsap !== 'undefined') {
                gsap.to('.purchase-modal-content', {
                    duration: 0.2,
                    y: 30,
                    opacity: 0,
                    ease: 'power2.in',
                    onComplete: () => {
                        modal.remove();
                        document.body.style.overflow = 'auto';
                    }
                });
            } else {
                modal.remove();
                document.body.style.overflow = 'auto';
            }

            this.currentConfig = null;
        }
    };

})();
