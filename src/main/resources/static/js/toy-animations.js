/**
 * ToySoul 撕票咪牌動效系統
 * 使用 GSAP 實現沉浸式抽獎揭曉動畫
 */

(function () {
    'use strict';

    // 確保 GSAP 存在
    if (typeof gsap === 'undefined') {
        console.warn('GSAP not found, animations will be disabled');
        return;
    }

    // ================= 稀有度配置 =================
    const RARITY_CONFIG = {
        'SSR': {
            colors: ['#FFD700', '#FFA500', '#FF6347'],
            glow: '0 0 40px rgba(255, 215, 0, 0.9), 0 0 80px rgba(255, 165, 0, 0.6)',
            particleCount: 50,
            label: '✨ SSR',
            bgGradient: 'linear-gradient(145deg, #FFD700, #FFA500)'
        },
        'SR': {
            colors: ['#9B59B6', '#8E44AD', '#E056FD'],
            glow: '0 0 30px rgba(155, 89, 182, 0.8), 0 0 60px rgba(142, 68, 173, 0.5)',
            particleCount: 35,
            label: '💎 SR',
            bgGradient: 'linear-gradient(145deg, #9B59B6, #8E44AD)'
        },
        'R': {
            colors: ['#3498DB', '#2980B9', '#74B9FF'],
            glow: '0 0 20px rgba(52, 152, 219, 0.7)',
            particleCount: 20,
            label: '🔷 R',
            bgGradient: 'linear-gradient(145deg, #3498DB, #2980B9)'
        },
        'N': {
            colors: ['#95A5A6', '#7F8C8D', '#BDC3C7'],
            glow: '0 0 10px rgba(149, 165, 166, 0.5)',
            particleCount: 10,
            label: 'N',
            bgGradient: 'linear-gradient(145deg, #95A5A6, #7F8C8D)'
        }
    };

    // 獲取稀有度配置
    function getRarityConfig(rarity) {
        return RARITY_CONFIG[rarity] || RARITY_CONFIG['N'];
    }

    // ================= 撕票動效 (一番賞) =================

    window.ToyAnimations = {

        /**
         * 一番賞撕票揭曉動畫
         * @param {Element} cardElement - 卡片元素
         * @param {Object} prizeData - 獎品資料 {name, rank, imageUrl}
         * @param {Function} callback - 完成後回調
         */
        tearReveal: function (cardElement, prizeData, callback) {
            const container = document.createElement('div');
            container.className = 'tear-animation-container';
            container.innerHTML = `
                <div class="tear-wrapper">
                    <div class="tear-card">
                        <div class="tear-front">
                            <div class="scratch-overlay"></div>
                            <div class="card-content">
                                <span class="card-number">#?</span>
                                <span class="card-hint">向上滑動揭曉</span>
                            </div>
                        </div>
                        <div class="tear-back">
                            <div class="prize-reveal">
                                ${prizeData.imageUrl ? `<img src="${prizeData.imageUrl}" alt="${prizeData.name}">` : ''}
                                <div class="prize-rank">${prizeData.rank || ''}</div>
                                <div class="prize-name">${prizeData.name}</div>
                            </div>
                        </div>
                    </div>
                    <div class="tear-particles"></div>
                </div>
            `;
            document.body.appendChild(container);

            // 動畫時間軸
            const tl = gsap.timeline({
                onComplete: () => {
                    setTimeout(() => {
                        gsap.to(container, {
                            opacity: 0,
                            duration: 0.3,
                            onComplete: () => {
                                container.remove();
                                if (callback) callback(prizeData);
                            }
                        });
                    }, 2000);
                }
            });

            // 入場動畫
            tl.from(container, {
                opacity: 0,
                duration: 0.3
            })
                .from('.tear-card', {
                    scale: 0.8,
                    y: 50,
                    duration: 0.5,
                    ease: 'back.out(1.5)'
                })
                // 自動撕票效果
                .to('.tear-front', {
                    y: -400,
                    rotationX: 30,
                    opacity: 0,
                    duration: 0.8,
                    ease: 'power2.in'
                }, '+=0.5')
                .from('.tear-back', {
                    scale: 0.9,
                    opacity: 0,
                    duration: 0.5,
                    ease: 'back.out(1.2)'
                })
                // 粒子效果
                .add(() => {
                    this.createParticles('.tear-particles', prizeData.rank);
                }, '-=0.3')
                // 獎品名稱入場
                .from('.prize-name', {
                    y: 20,
                    opacity: 0,
                    duration: 0.4,
                    ease: 'power2.out'
                }, '-=0.2');

            // 支持手動撕票（觸控滑動）
            let startY = 0;
            let isDragging = false;
            const tearFront = container.querySelector('.tear-front');

            if (tearFront) {
                tearFront.addEventListener('touchstart', (e) => {
                    startY = e.touches[0].clientY;
                    isDragging = true;
                });

                tearFront.addEventListener('touchmove', (e) => {
                    if (!isDragging) return;
                    const deltaY = startY - e.touches[0].clientY;
                    if (deltaY > 0) {
                        gsap.set(tearFront, { y: -deltaY, rotationX: deltaY * 0.1 });
                    }
                });

                tearFront.addEventListener('touchend', (e) => {
                    if (!isDragging) return;
                    isDragging = false;
                    const deltaY = startY - e.changedTouches[0].clientY;
                    if (deltaY > 80) {
                        // 完成撕票
                        tl.play();
                    } else {
                        // 回彈
                        gsap.to(tearFront, { y: 0, rotationX: 0, duration: 0.3 });
                    }
                });
            }
        },

        /**
         * 轉盤旋轉動畫
         * @param {Element} wheelElement - 轉盤元素
         * @param {number} targetAngle - 目標角度
         * @param {Function} callback - 完成後回調
         */
        spinWheel: function (wheelElement, targetAngle, callback) {
            const fullRotations = 5; // 轉 5 圈
            const totalAngle = fullRotations * 360 + targetAngle;

            gsap.to(wheelElement, {
                rotation: totalAngle,
                duration: 4,
                ease: 'power4.out',
                onComplete: callback
            });
        },

        /**
         * 九宮格翻牌動畫
         * @param {Element} cellElement - 格子元素
         * @param {Object} prizeData - 獎品資料
         * @param {Function} callback - 完成後回調
         */
        flipCard: function (cellElement, prizeData, callback) {
            const tl = gsap.timeline({
                onComplete: callback
            });

            tl.to(cellElement, {
                rotationY: 90,
                duration: 0.3,
                ease: 'power2.in'
            })
                .set(cellElement, {
                    innerHTML: `
                    <div class="revealed-content">
                        <div class="prize-icon">${prizeData.isGold ? '🏆' : '🎁'}</div>
                        <div class="prize-text">${prizeData.name}</div>
                    </div>
                `
                })
                .to(cellElement, {
                    rotationY: 0,
                    duration: 0.3,
                    ease: 'power2.out'
                })
                .from(cellElement.querySelector('.revealed-content'), {
                    scale: 0.8,
                    duration: 0.3,
                    ease: 'back.out(1.5)'
                });
        },

        /**
         * 扭蛋機出球動畫
         * @param {Element} gachaElement - 扭蛋機元素
         * @param {Object} prizeData - 獎品資料
         * @param {Function} callback - 完成後回調
         */
        gachaBall: function (gachaElement, prizeData, callback) {
            const ball = document.createElement('div');
            ball.className = 'gacha-ball';
            ball.innerHTML = `
                <div class="ball-outer" style="background: ${this.getBallColor(prizeData.rarity)}">
                    <div class="ball-inner">?</div>
                </div>
            `;
            gachaElement.appendChild(ball);

            const tl = gsap.timeline({
                onComplete: () => {
                    ball.querySelector('.ball-inner').textContent = prizeData.name.charAt(0);
                    if (callback) callback(prizeData);
                }
            });

            tl.from(ball, {
                y: -200,
                duration: 0.8,
                ease: 'bounce.out'
            })
                .to(ball, {
                    scale: 1.2,
                    duration: 0.2,
                    ease: 'power2.out'
                })
                .to(ball, {
                    scale: 1,
                    duration: 0.2,
                    ease: 'power2.in'
                })
                .to('.ball-outer', {
                    rotationY: 360,
                    duration: 0.6,
                    ease: 'power2.inOut'
                });
        },

        /**
         * 創建粒子效果
         */
        createParticles: function (container, rank) {
            const el = document.querySelector(container);
            if (!el) return;

            const colors = rank === 'A' || rank === 'A賞'
                ? ['#FFD700', '#FFA500', '#FF6347']
                : ['#667eea', '#764ba2', '#a29bfe'];

            for (let i = 0; i < 30; i++) {
                const particle = document.createElement('div');
                particle.className = 'particle';
                particle.style.cssText = `
                    position: absolute;
                    width: ${Math.random() * 10 + 5}px;
                    height: ${Math.random() * 10 + 5}px;
                    background: ${colors[Math.floor(Math.random() * colors.length)]};
                    border-radius: 50%;
                    left: 50%;
                    top: 50%;
                `;
                el.appendChild(particle);

                gsap.to(particle, {
                    x: (Math.random() - 0.5) * 400,
                    y: (Math.random() - 0.5) * 400,
                    opacity: 0,
                    scale: 0,
                    duration: 1.5,
                    ease: 'power2.out',
                    onComplete: () => particle.remove()
                });
            }
        },

        /**
         * 獲取扭蛋球顏色
         */
        getBallColor: function (rarity) {
            const colors = {
                'SECRET': 'linear-gradient(145deg, #FFD700, #FFA500)',
                'ULTRA_RARE': 'linear-gradient(145deg, #9B59B6, #8E44AD)',
                'RARE': 'linear-gradient(145deg, #3498DB, #2980B9)',
                'NORMAL': 'linear-gradient(145deg, #95A5A6, #7F8C8D)'
            };
            return colors[rarity] || colors['NORMAL'];
        },

        /**
         * 增強版獎品揭曉動畫 (支援稀有度光效 & 跳過按鈕)
         * @param {Object} prizeData - {name, rarity, imageUrl, rank}
         * @param {Function} callback - 完成後回調
         */
        prizeRevealEnhanced: function (prizeData, callback) {
            const config = getRarityConfig(prizeData.rarity || 'N');
            let isSkipped = false;

            const container = document.createElement('div');
            container.className = 'prize-reveal-enhanced';
            container.innerHTML = `
                <div class="reveal-backdrop"></div>
                <div class="reveal-card" style="--glow-effect: ${config.glow}">
                    <div class="card-flip-inner">
                        <div class="card-front">
                            <div class="mystery-icon">?</div>
                            <div class="suspense-text">正在揭曉...</div>
                        </div>
                        <div class="card-back" style="background: ${config.bgGradient}">
                            ${prizeData.imageUrl ? `<img src="${prizeData.imageUrl}" alt="${prizeData.name}">` : ''}
                            <div class="rarity-badge">${config.label}</div>
                            <div class="prize-title">${prizeData.name}</div>
                            ${prizeData.rank ? `<div class="prize-rank-label">${prizeData.rank}</div>` : ''}
                        </div>
                    </div>
                </div>
                <button class="skip-animation-btn">跳過動畫 ▶▶</button>
                <div class="reveal-particles"></div>
            `;
            document.body.appendChild(container);

            const tl = gsap.timeline({
                onComplete: () => {
                    if (!isSkipped) {
                        setTimeout(() => this.closeReveal(container, callback, prizeData), 2500);
                    }
                }
            });

            // 跳過按鈕
            container.querySelector('.skip-animation-btn').onclick = () => {
                isSkipped = true;
                tl.progress(1);
                this.closeReveal(container, callback, prizeData);
            };

            // 入場
            tl.from('.reveal-backdrop', { opacity: 0, duration: 0.3 })
                .from('.reveal-card', { scale: 0.5, opacity: 0, duration: 0.4, ease: 'back.out(1.5)' })
                // 懸念等待
                .to('.mystery-icon', { scale: 1.2, duration: 0.3, yoyo: true, repeat: 2 }, '+=0.3')
                // 3D 翻轉
                .to('.card-flip-inner', { rotateY: 180, duration: 0.8, ease: 'power2.inOut' })
                // 光效爆發
                .to('.reveal-card', { boxShadow: config.glow, duration: 0.3 }, '-=0.3')
                // 粒子
                .add(() => this.createParticlesEnhanced('.reveal-particles', config), '-=0.2');
        },

        /**
         * 關閉揭曉彈窗
         */
        closeReveal: function (container, callback, prizeData) {
            gsap.to(container, {
                opacity: 0,
                duration: 0.3,
                onComplete: () => {
                    container.remove();
                    if (callback) callback(prizeData);
                }
            });
        },

        /**
         * 增強版粒子效果 (使用稀有度配置)
         */
        createParticlesEnhanced: function (containerSelector, config) {
            const el = document.querySelector(containerSelector);
            if (!el) return;

            for (let i = 0; i < config.particleCount; i++) {
                const particle = document.createElement('div');
                particle.className = 'reveal-particle';
                particle.style.cssText = `
                    position: absolute;
                    width: ${Math.random() * 12 + 6}px;
                    height: ${Math.random() * 12 + 6}px;
                    background: ${config.colors[Math.floor(Math.random() * config.colors.length)]};
                    border-radius: 50%;
                    left: 50%;
                    top: 50%;
                    box-shadow: 0 0 6px currentColor;
                `;
                el.appendChild(particle);

                gsap.to(particle, {
                    x: (Math.random() - 0.5) * 500,
                    y: (Math.random() - 0.5) * 500,
                    opacity: 0,
                    scale: 0,
                    duration: 1.8,
                    ease: 'power2.out',
                    onComplete: () => particle.remove()
                });
            }
        },

        /**
         * 連線達成動畫（九宮格）
         */
        bingoLine: function (cells, callback) {
            const tl = gsap.timeline({
                onComplete: callback
            });

            cells.forEach((cell, index) => {
                tl.to(cell, {
                    scale: 1.1,
                    boxShadow: '0 0 30px rgba(255, 215, 0, 0.8)',
                    duration: 0.2,
                    ease: 'power2.out'
                }, index * 0.1);
            });

            tl.to(cells, {
                scale: 1,
                duration: 0.3,
                ease: 'power2.in'
            }, '+=0.5');
        }
    };

    // ================= 樣式注入 =================
    const styles = document.createElement('style');
    styles.textContent = `
        .tear-animation-container {
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(0, 0, 0, 0.9);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 10004;
        }

        .tear-wrapper {
            position: relative;
            perspective: 1000px;
        }

        .tear-card {
            position: relative;
            width: 280px;
            height: 400px;
            transform-style: preserve-3d;
        }

        .tear-front, .tear-back {
            position: absolute;
            width: 100%;
            height: 100%;
            backface-visibility: hidden;
            border-radius: 20px;
            overflow: hidden;
        }

        .tear-front {
            background: linear-gradient(145deg, #1a1a2e, #16213e);
            border: 3px solid #667eea;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            cursor: grab;
        }

        .scratch-overlay {
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: repeating-linear-gradient(
                45deg,
                rgba(102, 126, 234, 0.1),
                rgba(102, 126, 234, 0.1) 10px,
                transparent 10px,
                transparent 20px
            );
        }

        .card-content {
            position: relative;
            z-index: 1;
            text-align: center;
            color: white;
        }

        .card-number {
            font-size: 5rem;
            font-weight: bold;
            color: #667eea;
        }

        .card-hint {
            display: block;
            margin-top: 20px;
            color: #aaa;
            font-size: 0.9rem;
        }

        .tear-back {
            background: linear-gradient(145deg, #667eea, #764ba2);
        }

        .prize-reveal {
            height: 100%;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 30px;
            text-align: center;
            color: white;
        }

        .prize-reveal img {
            max-width: 150px;
            max-height: 150px;
            object-fit: contain;
            margin-bottom: 20px;
            border-radius: 15px;
        }

        .prize-rank {
            font-size: 2.5rem;
            font-weight: bold;
            color: #FFD700;
            text-shadow: 0 0 20px rgba(255, 215, 0, 0.5);
            margin-bottom: 10px;
        }

        .prize-name {
            font-size: 1.3rem;
            font-weight: bold;
        }

        .tear-particles {
            position: absolute;
            top: 50%;
            left: 50%;
            pointer-events: none;
        }

        .gacha-ball {
            position: absolute;
            bottom: 50px;
            left: 50%;
            transform: translateX(-50%);
        }

        .ball-outer {
            width: 80px;
            height: 80px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
        }

        .ball-inner {
            width: 50px;
            height: 50px;
            border-radius: 50%;
            background: white;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.5rem;
            font-weight: bold;
            color: #333;
        }

        .revealed-content {
            text-align: center;
            padding: 10px;
        }

        .prize-icon {
            font-size: 2rem;
        }

        .prize-text {
            font-size: 0.8rem;
            margin-top: 5px;
            color: #333;
        }
    `;
    document.head.appendChild(styles);

})();
