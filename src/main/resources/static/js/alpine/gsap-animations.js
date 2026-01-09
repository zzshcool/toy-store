/**
 * gsap-animations.js
 * GSAP 動畫工具模組
 * 
 * 提供可重用的動畫函數供 Alpine 元件調用
 */

const GsapAnimations = {

    /**
     * 轉盤旋轉動畫
     * @param {HTMLElement} canvas - 轉盤 Canvas 元素
     * @param {number} targetRotation - 目標旋轉角度
     * @returns {Promise}
     */
    spinWheel(canvas, targetRotation) {
        return new Promise(resolve => {
            if (!canvas || typeof gsap === 'undefined') {
                resolve();
                return;
            }
            gsap.to(canvas, {
                rotation: targetRotation,
                duration: 5,
                ease: 'expo.inOut',
                onComplete: resolve
            });
        });
    },

    /**
     * 手把旋轉動畫 (扭蛋機)
     * @param {HTMLElement} handle - 手把元素
     */
    rotateHandle(handle) {
        if (!handle || typeof gsap === 'undefined') return;
        gsap.to(handle, {
            rotation: 720,
            duration: 1.5,
            ease: 'power2.inOut'
        });
    },

    /**
     * 球體晃動動畫 (扭蛋機)
     * @param {NodeList} balls - 球體元素集合
     */
    shakeBalls(balls) {
        if (!balls || typeof gsap === 'undefined') return;
        balls.forEach((ball, i) => {
            gsap.to(ball, {
                y: 'random(-20, 20)',
                x: 'random(-10, 10)',
                duration: 0.3,
                repeat: 5,
                yoyo: true,
                delay: i * 0.05
            });
        });
    },

    /**
     * 膠囊掉落動畫 (扭蛋機)
     * @param {HTMLElement} capsule - 膠囊容器
     */
    dropCapsule(capsule) {
        if (!capsule || typeof gsap === 'undefined') return;
        capsule.style.display = 'block';
        gsap.fromTo(capsule,
            { y: -100, opacity: 0, scale: 0.5 },
            { y: 0, opacity: 1, scale: 1, duration: 0.8, ease: 'bounce.out' }
        );
    },

    /**
     * 格子抖動動畫 (九宮格/一番賞)
     * @param {HTMLElement} cell - 格子元素
     */
    shakeCell(cell) {
        if (!cell || typeof gsap === 'undefined') return;
        gsap.to(cell, {
            x: 'random(-3, 3)',
            y: 'random(-3, 3)',
            duration: 0.08,
            repeat: 6,
            yoyo: true
        });
    },

    /**
     * 翻轉動畫 (九宮格)
     * @param {HTMLElement} cellInner - 格子內部元素
     */
    flipCell(cellInner) {
        if (!cellInner || typeof gsap === 'undefined') return;
        gsap.to(cellInner, {
            rotationY: 180,
            duration: 0.6,
            ease: 'power2.out'
        });
    },

    /**
     * 彈窗進入動畫
     * @param {HTMLElement} modal - 彈窗元素
     */
    modalEnter(modal) {
        if (!modal || typeof gsap === 'undefined') return;
        gsap.from(modal, {
            y: -50,
            opacity: 0,
            duration: 0.4,
            ease: 'back.out(1.5)'
        });
    },

    /**
     * 彈窗離開動畫
     * @param {HTMLElement} modal - 彈窗元素
     * @returns {Promise}
     */
    modalLeave(modal) {
        return new Promise(resolve => {
            if (!modal || typeof gsap === 'undefined') {
                resolve();
                return;
            }
            gsap.to(modal, {
                y: -20,
                opacity: 0,
                duration: 0.2,
                ease: 'power2.in',
                onComplete: resolve
            });
        });
    },

    /**
     * 撕票動畫 (一番賞)
     * @param {HTMLElement} topPart - 上半部
     * @param {HTMLElement} bottomPart - 下半部
     * @param {HTMLElement} content - 內容區
     * @returns {Promise}
     */
    tearTicket(topPart, bottomPart, content) {
        return new Promise(resolve => {
            if (typeof gsap === 'undefined') {
                resolve();
                return;
            }

            const tl = gsap.timeline({ onComplete: resolve });
            tl.to(topPart, { y: -200, rotation: -15, opacity: 0, duration: 0.8, ease: 'power2.in' }, 0);
            tl.to(bottomPart, { y: 200, rotation: 15, opacity: 0, duration: 0.8, ease: 'power2.in' }, 0);
            tl.from(content, { scale: 0.5, opacity: 0, duration: 0.6, ease: 'back.out(1.7)' }, 0.2);
        });
    },

    /**
     * 倒數計時圓圈動畫
     * @param {SVGCircleElement} circle - 圓形 SVG 元素
     * @param {number} progress - 進度 0-1
     */
    updateCountdownCircle(circle, progress) {
        if (!circle) return;
        const circumference = 2 * Math.PI * 45;
        circle.style.strokeDashoffset = circumference * (1 - progress);
    }
};

// 導出供全域使用
window.GsapAnimations = GsapAnimations;
