/**
 * gacha-component.js
 * 扭蛋機 Alpine.js 元件
 * 
 * 功能：
 * - 主題列表載入與選擇
 * - 轉蛋動畫觸發
 * - 試抽/正式抽獎
 */

document.addEventListener('alpine:init', () => {

    Alpine.data('gachaComponent', () => ({
        // ==================== 狀態 ====================
        themes: [],
        selectedTheme: null,
        isSpinning: false,
        result: null,

        // 彈窗狀態
        showConfirmModal: false,
        showResultModal: false,
        isTrial: false,

        // ==================== 生命週期 ====================
        async init() {
            await this.fetchThemes();

            // 檢查 URL 參數，若有 themeId 則自動選擇
            const urlParams = new URLSearchParams(window.location.search);
            const themeId = urlParams.get('themeId');
            if (themeId) {
                const targetTheme = this.themes.find(t => t.id == themeId);
                if (targetTheme) {
                    this.selectTheme(targetTheme);
                }
            }
        },

        // ==================== 資料獲取 ====================
        async fetchThemes() {
            try {
                const res = await fetch('/api/gacha/themes');
                const data = await res.json();
                if (data.success) {
                    this.themes = data.data || [];
                }
            } catch (e) {
                console.error('Failed to load themes:', e);
                Alpine.store('ui').error('載入主題失敗');
            }
        },

        // ==================== 主題選擇 ====================
        selectTheme(theme) {
            this.selectedTheme = theme;
            this.scrollToMachine();
        },

        clearSelection() {
            this.selectedTheme = null;
        },

        scrollToMachine() {
            const machine = document.querySelector('.gacha-machine-section');
            if (machine) {
                machine.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        },

        // ==================== 抽獎流程 ====================
        openConfirmModal() {
            if (!this.selectedTheme) {
                Alpine.store('ui').warning('請先選擇主題');
                return;
            }
            this.showConfirmModal = true;
        },

        closeConfirmModal() {
            this.showConfirmModal = false;
        },

        /**
         * 開始抽獎
         * @param {boolean} trial - 是否為試抽
         */
        async spin(trial = false) {
            if (this.isSpinning || !this.selectedTheme) return;

            this.isTrial = trial;
            this.closeConfirmModal();
            this.isSpinning = true;

            // 觸發 GSAP 動畫
            this.playSpinAnimation();

            try {
                const endpoint = trial
                    ? `/api/gacha/${this.selectedTheme.id}/trial`
                    : `/api/gacha/${this.selectedTheme.id}/draw`;

                const res = await fetch(endpoint, { method: 'POST' });
                const data = await res.json();

                // 等待動畫完成
                await this.waitForAnimation(2000);

                if (data.success) {
                    this.result = data.data;
                    this.showResultModal = true;
                    this.playCapsuleReveal();

                    if (!trial) {
                        Alpine.store('user').refreshBalance();
                    }
                } else {
                    Alpine.store('ui').error(data.message || '抽獎失敗');
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
        playSpinAnimation() {
            const handle = document.getElementById('gachaHandle');
            if (handle && typeof gsap !== 'undefined') {
                gsap.to(handle, {
                    rotation: 720,
                    duration: 1.5,
                    ease: 'power2.inOut'
                });
            }

            // 球體晃動
            const balls = document.querySelectorAll('.gacha-ball');
            balls.forEach((ball, i) => {
                if (typeof gsap !== 'undefined') {
                    gsap.to(ball, {
                        y: 'random(-20, 20)',
                        x: 'random(-10, 10)',
                        duration: 0.3,
                        repeat: 5,
                        yoyo: true,
                        delay: i * 0.05
                    });
                }
            });
        },

        playCapsuleReveal() {
            const capsule = document.getElementById('capsuleContainer');
            if (capsule && typeof gsap !== 'undefined') {
                capsule.style.display = 'block';
                gsap.fromTo(capsule,
                    { y: -100, opacity: 0, scale: 0.5 },
                    { y: 0, opacity: 1, scale: 1, duration: 0.8, ease: 'bounce.out' }
                );
            }
        },

        waitForAnimation(ms) {
            return new Promise(resolve => setTimeout(resolve, ms));
        },

        // ==================== 計算屬性 ====================
        get hasThemes() {
            return this.themes.length > 0;
        },

        get displayPrice() {
            return this.selectedTheme?.price || 0;
        }
    }));
});
