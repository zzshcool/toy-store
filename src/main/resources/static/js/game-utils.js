/**
 * 抽獎系統共用工具函式函式庫
 */
const GameUtils = {
    /**
     * 播放開獎音效
     */
    playRevealSound: function () {
        try {
            const audioContext = new (window.AudioContext || window.webkitAudioContext)();
            const oscillator = audioContext.createOscillator();
            const gainNode = audioContext.createGain();

            oscillator.connect(gainNode);
            gainNode.connect(audioContext.destination);

            oscillator.type = 'sine';
            oscillator.frequency.setValueAtTime(523.25, audioContext.currentTime); // C5
            oscillator.frequency.exponentialRampToValueAtTime(1046.50, audioContext.currentTime + 0.1); // C6

            gainNode.gain.setValueAtTime(0.2, audioContext.currentTime);
            gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.5);

            oscillator.start();
            oscillator.stop(audioContext.currentTime + 0.5);
        } catch (e) {
            console.warn('AudioContext not supported or blocked:', e);
        }
    },

    /**
     * 播放成功/中獎音效
     */
    playWinSound: function () {
        try {
            const audioContext = new (window.AudioContext || window.webkitAudioContext)();
            const oscillator = audioContext.createOscillator();
            const gainNode = audioContext.createGain();

            oscillator.connect(gainNode);
            gainNode.connect(audioContext.destination);

            oscillator.frequency.setValueAtTime(523, audioContext.currentTime);
            oscillator.frequency.setValueAtTime(659, audioContext.currentTime + 0.1);
            oscillator.frequency.setValueAtTime(784, audioContext.currentTime + 0.2);

            gainNode.gain.setValueAtTime(0.3, audioContext.currentTime);
            gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.5);

            oscillator.start(audioContext.currentTime);
            oscillator.stop(audioContext.currentTime + 0.5);
        } catch (e) { }
    },

    /**
     * 生成慶祝碎片動畫
     */
    spawnConfetti: function () {
        const container = document.createElement('div');
        container.className = 'confetti-container';
        document.body.appendChild(container);

        const colors = ['#ffd93d', '#ff6b6b', '#6c5ce7', '#00cec9', '#ffffff'];
        for (let i = 0; i < 50; i++) {
            const piece = document.createElement('div');
            piece.className = 'confetti-piece';
            piece.style.left = Math.random() * 100 + '%';
            piece.style.backgroundColor = colors[Math.floor(Math.random() * colors.length)];
            piece.style.width = (Math.random() * 10 + 5) + 'px';
            piece.style.height = piece.style.width;
            piece.style.animationDelay = Math.random() * 2 + 's';
            piece.style.animationDuration = (Math.random() * 2 + 2) + 's';
            container.appendChild(piece);
        }
        setTimeout(() => container.remove(), 5000);
    },

    /**
     * 統一導向至抽獎紀錄
     */
    redirectToHistory: function () {
        window.location.href = '/gacha-history';
    },

    /**
     * 顯示遮罩
     */
    showMask: function () {
        if (window.showMask) window.showMask();
        else {
            const mask = document.getElementById('ui-mask');
            if (mask) mask.style.display = 'block';
        }
    },

    /**
     * 隱藏遮罩
     */
    hideMask: function () {
        if (window.hideMask) window.hideMask();
        else {
            const mask = document.getElementById('ui-mask');
            if (mask) mask.style.display = 'none';
        }
    },

    // ==================== API 客戶端 ====================

    /**
     * 統一 API 請求封裝
     * @param {string} url - API 端點
     * @param {Object} options - fetch 選項
     * @returns {Promise<Object>} API 回應
     */
    async fetchApi(url, options = {}) {
        try {
            const response = await fetch(url, {
                headers: {
                    'Content-Type': 'application/json',
                    ...options.headers
                },
                ...options
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'API 請求失敗');
            }

            return data;
        } catch (error) {
            console.error('API Error:', error);
            if (window.Toast) Toast.error(error.message || '網路錯誤');
            throw error;
        }
    },

    /**
     * GET 請求簡化
     */
    async get(url) {
        return this.fetchApi(url);
    },

    /**
     * POST 請求簡化
     */
    async post(url, body) {
        return this.fetchApi(url, {
            method: 'POST',
            body: JSON.stringify(body)
        });
    },

    // ==================== 價格格式化 ====================

    /**
     * 格式化價格
     * @param {number} price - 價格
     * @param {string} currency - 貨幣符號
     * @returns {string} 格式化的價格字串
     */
    formatPrice(price, currency = '$') {
        if (typeof price !== 'number' || isNaN(price)) return currency + '0';
        return currency + price.toLocaleString('zh-TW');
    },

    /**
     * 格式化折扣後價格
     */
    formatDiscountPrice(originalPrice, discountRate) {
        const discounted = Math.floor(originalPrice * (1 - discountRate));
        return {
            original: this.formatPrice(originalPrice),
            discounted: this.formatPrice(discounted),
            savedAmount: this.formatPrice(originalPrice - discounted)
        };
    },

    // ==================== 共用 Modal 操作 ====================

    /**
     * 顯示結果 Modal
     */
    showResultModal(config) {
        const {
            containerId = 'resultModal',
            prizes = [],
            isRare = false,
            isTrial = false
        } = config;

        const modal = document.getElementById(containerId);
        if (!modal) return;

        modal.style.display = 'flex';

        if (!isTrial && isRare) {
            this.spawnConfetti();
            this.playWinSound();
        } else if (!isTrial) {
            this.playRevealSound();
        }
    },

    /**
     * 關閉 Modal
     */
    closeModal(containerId) {
        const modal = document.getElementById(containerId);
        if (modal) {
            modal.style.display = 'none';
        }
    },

    // ==================== 遊戲狀態管理 ====================

    /**
     * 檢查登入狀態
     */
    isLoggedIn() {
        return document.querySelector('.nav-user') !== null ||
            document.cookie.includes('authToken') ||
            document.body.dataset.loggedIn === 'true';
    },

    /**
     * 提示登入
     */
    promptLogin(callback) {
        if (window.LoginModal) {
            LoginModal.show(callback);
        } else if (window.requireLogin) {
            requireLogin(callback);
        } else {
            window.location.href = '/login';
        }
    },

    /**
     * 刷新使用者餘額
     */
    refreshBalance() {
        if (typeof window.refreshUserBalance === 'function') {
            window.refreshUserBalance();
        }
    },

    // ==================== 遊戲清單渲染 ====================

    /**
     * 建立遊戲卡片 HTML
     */
    createGameCard(game, options = {}) {
        const {
            onClick = 'selectGame',
            badge = '🎮',
            showPrice = true
        } = options;

        return `
            <div class="product-card" onclick="${onClick}(${game.id})" style="cursor: pointer;">
                <div class="product-image">
                    <img src="${game.imageUrl || '/images/placeholder.jpg'}" alt="${game.name}" 
                         onerror="this.src='/images/placeholder.jpg'">
                    <span class="product-badge">${badge}</span>
                </div>
                <div class="product-details">
                    <h3 class="product-title">${game.name}</h3>
                    ${showPrice ? `<p class="product-price">${this.formatPrice(game.pricePerPlay || game.pricePerDraw)}/次</p>` : ''}
                </div>
            </div>
        `;
    },

    /**
     * 渲染遊戲清單
     */
    renderGameList(containerId, games, options = {}) {
        const container = document.getElementById(containerId);
        if (!container) return;

        if (!games || games.length === 0) {
            container.innerHTML = '<p class="text-center" style="color: var(--text-secondary);">目前沒有可用的遊戲</p>';
            return;
        }

        container.innerHTML = games.map(game => this.createGameCard(game, options)).join('');
    }
};

window.GameUtils = GameUtils;

