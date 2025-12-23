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
    }
};

window.GameUtils = GameUtils;
