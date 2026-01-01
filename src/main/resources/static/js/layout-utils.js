/**
 * ToySoul Layout Utilities
 * 從 layout.html 抽離的核心功能模組
 * 包含: 骨架屏、流程防護、Toast 通知、登入 Modal、規則彈窗
 */

(function () {
    'use strict';

    // ==================== 全域餘額刷新 ====================
    window.refreshUserBalance = function () {
        fetch('/api/member/balance')
            .then(res => res.json())
            .then(response => {
                if (response.success && response.data) {
                    const balanceElements = document.querySelectorAll('[data-balance-display]');
                    balanceElements.forEach(el => {
                        el.textContent = response.data.balance;
                    });
                    const navBalance = document.querySelector('.nav-menu span[style*="accent-color"]');
                    if (navBalance && navBalance.textContent.includes('$')) {
                        navBalance.innerHTML = '💰 $' + response.data.balance;
                    }
                }
            })
            .catch(err => console.error('Failed to refresh balance:', err));
    };

    // ==================== 導航選單切換 ====================
    window.toggleMenu = function () {
        const menu = document.getElementById('navMenu');
        if (menu) menu.classList.toggle('active');
    };

    // ==================== 購物車功能 ====================
    window.toggleCart = function () {
        const panel = document.getElementById('floatingCartPanel');
        if (!panel) return;
        if (panel.style.right === '0px') {
            panel.style.right = '-350px';
        } else {
            panel.style.right = '0px';
            loadCartItems();
        }
    };

    window.loadCartItems = function () {
        fetch('/cart/api/items')
            .then(response => {
                if (response.redirected && response.url.includes('login')) {
                    window.location.href = '/login';
                    return null;
                }
                if (!response.ok && (response.status === 401 || response.status === 403)) {
                    window.location.href = '/login';
                    return null;
                }
                return response.text();
            })
            .then(html => {
                if (html) {
                    if (html.includes('<!DOCTYPE html>') || html.includes('<html')) {
                        window.location.href = '/login';
                    } else {
                        const cartContent = document.getElementById('cartContent');
                        if (cartContent) cartContent.innerHTML = html;
                    }
                }
            })
            .catch(error => console.error('Error loading cart:', error));
    };

    window.addToCartAjax = function (event, form) {
        event.preventDefault();
        const formData = new FormData(form);

        fetch('/cart/api/add', { method: 'POST', body: formData })
            .then(response => {
                if (response.status === 401) {
                    window.location.href = '/login';
                    return;
                }
                return response.json();
            })
            .then(data => {
                if (data && data.success) {
                    const panel = document.getElementById('floatingCartPanel');
                    if (panel) panel.style.right = '0px';
                    loadCartItems();

                    const badge = document.getElementById('cartBadge');
                    if (badge) {
                        if (data.totalItems > 0) {
                            badge.innerText = data.totalItems;
                            badge.style.display = 'block';
                        } else {
                            badge.style.display = 'none';
                        }
                    }
                } else {
                    if (window.Toast) Toast.error('加入失敗: ' + (data ? data.message : 'Unknown error'));
                }
            })
            .catch(error => console.error('Error:', error));
    };

    window.removeFromCartAjax = function (event, form) {
        event.preventDefault();
        const formData = new FormData(form);

        fetch('/cart/api/remove', { method: 'POST', body: formData })
            .then(response => {
                if (response.status === 401) {
                    window.location.href = '/login';
                    return;
                }
                return response.json();
            })
            .then(data => {
                if (data && data.success) {
                    loadCartItems();
                    const badge = document.getElementById('cartBadge');
                    if (badge) {
                        if (data.totalItems > 0) {
                            badge.innerText = data.totalItems;
                            badge.style.display = 'block';
                        } else {
                            badge.style.display = 'none';
                        }
                    }
                } else {
                    if (window.Toast) Toast.error('移除失敗: ' + (data ? data.message : 'Unknown error'));
                }
            })
            .catch(error => console.error('Error:', error));
    };

    // ==================== 回到頂端 ====================
    window.onscroll = function () { scrollFunction(); };

    function scrollFunction() {
        const btn = document.getElementById('backToTopBtn');
        if (btn) {
            btn.style.display = (document.body.scrollTop > 300 || document.documentElement.scrollTop > 300) ? 'flex' : 'none';
        }
    }

    window.scrollToTop = function () {
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    // ==================== 跑馬燈 ====================
    window.loadMarquee = function () {
        fetch('/api/gacha/recent?limit=10')
            .then(res => res.json())
            .then(data => {
                if (data.success && data.data.length > 0) {
                    const container = document.getElementById('marqueeContent');
                    if (container) {
                        container.innerHTML = data.data.map(r => {
                            const icon = r.gachaType === 'ICHIBAN' ? '🎯' :
                                r.gachaType === 'ROULETTE' ? '🎡' :
                                    r.gachaType === 'BINGO' ? '🎲' : '🎁';
                            const cls = r.isRare ? 'win-item win-rare' : 'win-item';
                            return `<span class="${cls}">${icon} ${r.player} 獲得 ${r.prizeName}${r.prizeRank ? ' [' + r.prizeRank + '賞]' : ''}</span>`;
                        }).join('');
                    }
                }
            })
            .catch(err => console.log('Marquee load failed'));
    };

    // ==================== UI 遮罩 ====================
    window.showMask = function () {
        const mask = document.getElementById('ui-mask');
        if (mask) mask.style.display = 'block';
    };

    window.hideMask = function () {
        const mask = document.getElementById('ui-mask');
        if (mask) mask.style.display = 'none';
    };

    // ==================== Skeleton 骨架屏 ====================
    window.Skeleton = {
        cardTemplate: `
            <div class="skeleton-card">
                <div class="skeleton skeleton-image"></div>
                <div class="skeleton skeleton-title"></div>
                <div class="skeleton skeleton-text"></div>
                <div class="skeleton skeleton-button"></div>
            </div>
        `,
        rowTemplate: `
            <div class="skeleton-row">
                <div class="skeleton skeleton-avatar"></div>
                <div class="skeleton-content">
                    <div class="skeleton skeleton-title"></div>
                    <div class="skeleton skeleton-text-short"></div>
                </div>
            </div>
        `,
        show(container, count = 4, type = 'card') {
            const el = typeof container === 'string' ? document.querySelector(container) : container;
            if (!el) return;

            const template = type === 'row' ? this.rowTemplate : this.cardTemplate;
            const wrapper = type === 'row' ? '' : 'skeleton-grid';

            let html = wrapper ? `<div class="${wrapper}">` : '';
            for (let i = 0; i < count; i++) {
                html += template;
            }
            html += wrapper ? '</div>' : '';

            el.innerHTML = html;
            el.dataset.skeletonActive = 'true';
        },
        hide(container) {
            const el = typeof container === 'string' ? document.querySelector(container) : container;
            if (el && el.dataset.skeletonActive) {
                el.innerHTML = '';
                delete el.dataset.skeletonActive;
            }
        },
        isActive(container) {
            const el = typeof container === 'string' ? document.querySelector(container) : container;
            return el && el.dataset.skeletonActive === 'true';
        }
    };

    // ==================== ProcessSafety 流程防護 ====================
    window.ProcessSafety = {
        _active: false,
        start() {
            if (this._active) return;
            this._active = true;
            const mask = document.getElementById('process-mask');
            if (mask) mask.style.display = 'flex';
        },
        end() {
            this._active = false;
            const mask = document.getElementById('process-mask');
            if (mask) mask.style.display = 'none';
        },
        isActive() {
            return this._active;
        },
        wrapButton(btn, asyncFn) {
            const el = typeof btn === 'string' ? document.querySelector(btn) : btn;
            if (!el) return;

            el.addEventListener('click', async (e) => {
                if (el.classList.contains('btn-processing')) {
                    e.preventDefault();
                    return;
                }

                const originalText = el.textContent;
                el.classList.add('btn-processing');
                el.textContent = '處理中...';

                try {
                    await asyncFn(e);
                } finally {
                    el.classList.remove('btn-processing');
                    el.textContent = originalText;
                }
            });
        },
        async run(asyncFn, opts = { showMask: true }) {
            if (this._active) return;

            if (opts.showMask) this.start();
            try {
                return await asyncFn();
            } finally {
                if (opts.showMask) this.end();
            }
        }
    };

    // ==================== Toast 通知系統 ====================
    window.Toast = {
        _icons: {
            success: '✅',
            error: '❌',
            info: 'ℹ️',
            warning: '⚠️'
        },
        show(message, type = 'info', duration = 3500) {
            const container = document.getElementById('toast-container');
            if (!container) return;

            const toast = document.createElement('div');
            toast.className = `toast ${type}`;
            toast.innerHTML = `
                <span class="toast-icon">${this._icons[type] || 'ℹ️'}</span>
                <span class="toast-message">${message}</span>
                <button class="toast-close" onclick="Toast.dismiss(this.parentElement)">&times;</button>
            `;
            container.appendChild(toast);

            setTimeout(() => this.dismiss(toast), duration);
        },
        dismiss(toast) {
            if (!toast || toast.classList.contains('hiding')) return;
            toast.classList.add('hiding');
            setTimeout(() => toast.remove(), 300);
        },
        success(msg, duration) { this.show(msg, 'success', duration); },
        error(msg, duration) { this.show(msg, 'error', duration); },
        info(msg, duration) { this.show(msg, 'info', duration); },
        warning(msg, duration) { this.show(msg, 'warning', duration); }
    };

    // ==================== LoginModal 登入彈窗 ====================
    window.LoginModal = {
        _callback: null,
        show(callback) {
            this._callback = callback;
            const modal = document.getElementById('login-modal');
            if (modal) {
                modal.style.display = 'flex';
                const usernameField = document.getElementById('modal-username');
                if (usernameField) usernameField.focus();
                const errorDiv = document.getElementById('login-modal-error');
                if (errorDiv) errorDiv.style.display = 'none';
            }
        },
        hide() {
            const modal = document.getElementById('login-modal');
            const form = document.getElementById('login-modal-form');
            if (modal) modal.style.display = 'none';
            if (form) form.reset();
            this._callback = null;
        },
        submit(e) {
            e.preventDefault();
            const btn = document.getElementById('login-submit-btn');
            const errorDiv = document.getElementById('login-modal-error');
            if (btn) {
                btn.disabled = true;
                btn.textContent = '登入中...';
            }

            const form = document.getElementById('login-modal-form');
            const formData = new FormData(form);

            fetch('/api/member/login', {
                method: 'POST',
                body: new URLSearchParams(formData)
            })
                .then(res => res.json())
                .then(data => {
                    if (data.success) {
                        Toast.success('登入成功！');
                        this.hide();
                        if (typeof refreshUserBalance === 'function') refreshUserBalance();
                        if (this._callback) this._callback(data.data);
                        setTimeout(() => location.reload(), 500);
                    } else {
                        if (errorDiv) {
                            errorDiv.textContent = data.message || '登入失敗';
                            errorDiv.style.display = 'block';
                        }
                    }
                })
                .catch(() => {
                    if (errorDiv) {
                        errorDiv.textContent = '網路錯誤，請稍後再試';
                        errorDiv.style.display = 'block';
                    }
                })
                .finally(() => {
                    if (btn) {
                        btn.disabled = false;
                        btn.textContent = '登入';
                    }
                });
        }
    };

    window.requireLogin = function (callback) {
        const isLoggedIn = document.querySelector('.nav-user') !== null ||
            document.cookie.includes('authToken');
        if (isLoggedIn) {
            if (callback) callback();
        } else {
            LoginModal.show(callback);
        }
    };

    // ==================== 遊戲規則彈窗 ====================
    const GAME_RULES = {
        ichiban: {
            title: '🎯 一番賞遊戲規則',
            content: `
                <h4>遊戲說明</h4>
                <p>一番賞是經典的日本抽獎玩法，每個系列都有固定數量的獎品，抽中即得！</p>
                <h4>玩法流程</h4>
                <ol>
                    <li>選擇想要參與的一番賞系列</li>
                    <li>選擇抽獎次數（1抽 / 5抽 / 10抽）</li>
                    <li>確認購買後即可揭曉獎品</li>
                    <li>獲得的獎品會進入您的盒櫃</li>
                </ol>
                <h4>獎品等級</h4>
                <ul>
                    <li><strong>A賞、B賞</strong>：稀有大獎</li>
                    <li><strong>C賞、D賞</strong>：中等獎品</li>
                    <li><strong>E賞以下</strong>：基本獎品</li>
                    <li><strong>LAST賞</strong>：最後一抽特別獎</li>
                </ul>
                <h4>注意事項</h4>
                <p>每個獎品數量有限，抽完即止。您可以先使用「試抽」功能體驗抽獎樂趣！</p>
            `
        },
        roulette: {
            title: '🎡 轉盤遊戲規則',
            content: `
                <h4>遊戲說明</h4>
                <p>經典轉盤玩法，轉動轉盤指針停在哪個位置就獲得該獎品！</p>
                <h4>玩法流程</h4>
                <ol>
                    <li>選擇想要的轉盤</li>
                    <li>點擊「開始旋轉」</li>
                    <li>等待轉盤停止，揭曉獎品</li>
                </ol>
                <h4>機率說明</h4>
                <p>轉盤上各區塊的面積與中獎機率成正比，大獎區域較小但獎品更豐厚！</p>
            `
        },
        bingo: {
            title: '🎲 九宮格遊戲規則',
            content: `
                <h4>遊戲說明</h4>
                <p>3x3 的九宮格挖寶遊戲，挖開格子即可獲得獎品，連線還有額外獎勵！</p>
                <h4>玩法流程</h4>
                <ol>
                    <li>選擇九宮格遊戲</li>
                    <li>點擊格子挖開</li>
                    <li>挖出的獎品即歸您所有</li>
                    <li>若完成連線，可獲得額外連線獎勵</li>
                </ol>
                <h4>連線規則</h4>
                <p>橫向、縱向、對角線任一條線全部挖開，即可達成連線獎勵！</p>
            `
        },
        blindbox: {
            title: '📦 盲盒遊戲規則',
            content: `
                <h4>遊戲說明</h4>
                <p>動漫周邊盲盒玩法，每個中盒內有多個小盒，選擇您感興趣的盒子購買！</p>
                <h4>玩法流程</h4>
                <ol>
                    <li>選擇一個盲盒系列</li>
                    <li>點擊您想要的小盒進行「鎖定」</li>
                    <li>鎖定後有 <strong>180 秒</strong> 決定時間</li>
                    <li>確認購買即可揭曉盒內獎品</li>
                </ol>
                <h4>道具卡</h4>
                <ul>
                    <li><strong>提示卡</strong>：根據稀有度篩選可選盒子</li>
                    <li><strong>透視卡</strong>：偷看盒內獎品</li>
                    <li><strong>換一盒</strong>：釋放當前鎖定，隨機鎖定新盒子</li>
                </ul>
                <h4>其他玩法</h4>
                <ul>
                    <li><strong>全包</strong>：一次購買所有剩餘盒子</li>
                    <li><strong>天選抽</strong>：系統隨機為您選擇一個盒子</li>
                </ul>
            `
        }
    };

    window.showRulesModal = function (gameType) {
        const rules = GAME_RULES[gameType];
        if (!rules) return;

        const modal = document.createElement('div');
        modal.id = 'rulesModal';
        modal.className = 'rules-modal';
        modal.innerHTML = `
            <div class="rules-modal-backdrop" onclick="closeRulesModal()"></div>
            <div class="rules-modal-content">
                <button class="rules-modal-close" onclick="closeRulesModal()">&times;</button>
                <h3>${rules.title}</h3>
                <div class="rules-body">${rules.content}</div>
            </div>
        `;
        document.body.appendChild(modal);
        document.body.style.overflow = 'hidden';

        if (typeof gsap !== 'undefined') {
            gsap.from('.rules-modal-content', {
                duration: 0.4,
                y: 30,
                opacity: 0,
                ease: 'back.out(1.2)'
            });
        }
    };

    window.closeRulesModal = function () {
        const modal = document.getElementById('rulesModal');
        if (!modal) return;

        if (typeof gsap !== 'undefined') {
            gsap.to('.rules-modal-content', {
                duration: 0.2,
                y: 20,
                opacity: 0,
                onComplete: () => {
                    modal.remove();
                    document.body.style.overflow = 'auto';
                }
            });
        } else {
            modal.remove();
            document.body.style.overflow = 'auto';
        }
    };

    // ==================== 初始化 ====================
    document.addEventListener('DOMContentLoaded', function () {
        loadMarquee();
        setInterval(loadMarquee, 30000);
    });

})();
