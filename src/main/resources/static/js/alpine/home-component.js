/**
 * home-component.js
 * 首頁 Alpine.js 元件
 * 
 * 功能：
 * - 輪播圖控制
 * - 用戶儀表板即時更新
 * - 入場動畫觸發
 */

document.addEventListener('alpine:init', () => {

    // ==================== 輪播圖元件 (Premium GSAP Transitions) ====================
    Alpine.data('homeCarousel', () => ({
        slides: [],
        currentSlide: 0,
        autoPlayTimer: null,
        isTransitioning: false,

        init() {
            // 從 DOM 讀取輪播資料
            const slideElements = document.querySelectorAll('.carousel-slide-data');
            this.slides = Array.from(slideElements).map(el => ({
                imageUrl: el.dataset.image,
                linkUrl: el.dataset.link || null
            }));

            if (this.slides.length > 0) {
                this.startAutoPlay();
                // 初始化第一張 slide
                this.initFirstSlide();
            }

            // 入場動畫
            this.$nextTick(() => this.playEntranceAnimations());
        },

        initFirstSlide() {
            const firstSlide = document.querySelector('.carousel-slide');
            if (firstSlide && typeof gsap !== 'undefined') {
                gsap.set(firstSlide, { scale: 1, opacity: 1 });
            }
        },

        next() {
            if (this.isTransitioning) return;
            this.goTo(this.currentSlide + 1);
        },

        prev() {
            if (this.isTransitioning) return;
            this.goTo(this.currentSlide - 1);
        },

        goTo(index) {
            if (this.slides.length === 0 || this.isTransitioning) return;

            const prevIndex = this.currentSlide;
            const nextIndex = (index + this.slides.length) % this.slides.length;

            if (prevIndex === nextIndex) return;

            this.isTransitioning = true;

            const slides = document.querySelectorAll('.carousel-slide');
            const prevSlide = slides[prevIndex];
            const nextSlide = slides[nextIndex];

            if (typeof gsap !== 'undefined' && prevSlide && nextSlide) {
                // Premium 轉場動畫 - Scale + Fade
                const tl = gsap.timeline({
                    onComplete: () => {
                        this.currentSlide = nextIndex;
                        this.isTransitioning = false;
                    }
                });

                // 前一張淡出 + 放大
                tl.to(prevSlide, {
                    scale: 1.1,
                    opacity: 0,
                    duration: 0.6,
                    ease: 'power2.inOut'
                }, 0);

                // 下一張淡入 + 縮放回正常
                tl.fromTo(nextSlide,
                    { scale: 0.95, opacity: 0 },
                    { scale: 1, opacity: 1, duration: 0.6, ease: 'power2.inOut' },
                    0.1
                );
            } else {
                // Fallback without GSAP
                this.currentSlide = nextIndex;
                setTimeout(() => {
                    this.isTransitioning = false;
                }, 600);
            }
        },

        startAutoPlay() {
            this.stopAutoPlay();
            this.autoPlayTimer = setInterval(() => this.next(), 6000);
        },

        stopAutoPlay() {
            if (this.autoPlayTimer) {
                clearInterval(this.autoPlayTimer);
                this.autoPlayTimer = null;
            }
        },

        // Premium 入場動畫
        playEntranceAnimations() {
            if (typeof gsap === 'undefined') return;

            // 輪播區塊淡入
            gsap.from('.carousel-container', {
                opacity: 0,
                duration: 1,
                ease: 'power2.out'
            });

            // 用戶儀表板
            gsap.from('.personal-dashboard', {
                y: 40,
                opacity: 0,
                duration: 0.8,
                delay: 0.3,
                ease: 'power3.out'
            });

            // Section 標題依序淡入
            gsap.from('.premium-section-title', {
                x: -30,
                opacity: 0,
                duration: 0.6,
                stagger: 0.2,
                ease: 'power2.out',
                scrollTrigger: {
                    trigger: '.section-block',
                    start: 'top 85%'
                }
            });

            // 遊戲卡片依序淡入 + 上浮
            gsap.from('.game-card', {
                y: 60,
                opacity: 0,
                duration: 0.7,
                stagger: 0.12,
                ease: 'power3.out',
                scrollTrigger: {
                    trigger: '.game-card',
                    start: 'top 85%'
                }
            });

            // 商品卡片
            gsap.from('.product-card', {
                y: 40,
                opacity: 0,
                duration: 0.6,
                stagger: 0.08,
                ease: 'power2.out',
                scrollTrigger: {
                    trigger: '.product-card',
                    start: 'top 90%'
                }
            });
        }
    }));

    // ==================== 用戶儀表板元件 ====================
    Alpine.data('homeDashboard', () => ({
        balance: 0,
        points: 0,
        growthValue: 0,
        luckyValue: 0,

        init() {
            // 監聽全域 store 變化
            this.$watch('$store.user.balance', val => {
                if (val > 0) this.balance = val;
            });
        },

        animateValue(el, start, end, duration = 500) {
            if (typeof gsap !== 'undefined') {
                gsap.to(el, {
                    innerText: end,
                    duration: duration / 1000,
                    snap: { innerText: 1 },
                    ease: 'power1.out'
                });
            }
        }
    }));

    // ==================== 跑馬燈元件 ====================
    Alpine.data('winnerReel', () => ({
        winners: [],
        isPaused: false,

        init() {
            // 懸停時暫停
            const reel = document.querySelector('.winner-reel');
            if (reel) {
                reel.addEventListener('mouseenter', () => this.pause());
                reel.addEventListener('mouseleave', () => this.resume());
            }
        },

        pause() {
            this.isPaused = true;
            const reel = document.querySelector('.winner-reel');
            if (reel) reel.style.animationPlayState = 'paused';
        },

        resume() {
            this.isPaused = false;
            const reel = document.querySelector('.winner-reel');
            if (reel) reel.style.animationPlayState = 'running';
        }
    }));
});
