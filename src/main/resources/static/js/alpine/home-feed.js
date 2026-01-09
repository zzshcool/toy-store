document.addEventListener('alpine:init', () => {
    Alpine.data('gameFeed', () => ({
        items: [],
        page: 0,
        size: 10,
        loading: false,
        hasMore: true,

        async init() {
            await this.loadMore();
            this.setupInfiniteScroll();
        },

        async loadMore() {
            if (this.loading || !this.hasMore) return;

            this.loading = true;
            try {
                const res = await fetch(`/api/home/feed?page=${this.page}&size=${this.size}`);
                const data = await res.json();

                if (data.success) {
                    const newItems = data.data || [];
                    if (newItems.length < this.size) {
                        this.hasMore = false;
                    }
                    this.items.push(...newItems);
                    this.page++;
                } else {
                    this.hasMore = false;
                }
            } catch (e) {
                console.error('Failed to load feed:', e);
            } finally {
                this.loading = false;
            }
        },

        setupInfiniteScroll() {
            const observer = new IntersectionObserver((entries) => {
                const lastEntry = entries[0];
                if (lastEntry.isIntersecting) {
                    this.loadMore();
                }
            }, {
                rootMargin: '100px',
                threshold: 0.1
            });

            // Watch for the sentinel element
            this.$watch('loading', () => {
                // Re-observe if needed? No, purely sentinel based is better.
                // But Alpine init is cleaner.
            });

            // Wait for DOM
            this.$nextTick(() => {
                const sentinel = document.getElementById('feed-sentinel');
                if (sentinel) observer.observe(sentinel);
            });
        },

        getLink(item) {
            switch (item.type) {
                case 'ICHIBAN': return `/ichiban?boxId=${item.id}`;
                case 'BLIND_BOX': return `/blindbox?boxId=${item.id}`; // Assuming param differs or unified
                // Wait, BlindBoxController usually uses path variable logic or query param?
                // Let's check. Usually /blindbox/{id} or /blindbox?id
                // index.html used /ichiban(boxId=...)
                // Let's assume standard pattern. I'll check Layout/Controller URLs.
                case 'GACHA': return `/gacha?themeId=${item.id}`;
                case 'BINGO': return `/bingo?gameId=${item.id}`;
                case 'ROULETTE': return `/roulette?gameId=${item.id}`;
                default: return '#';
            }
        },

        getBadgeClass(type) {
            switch (type) {
                case 'ICHIBAN': return 'badge-ichiban';
                case 'BLIND_BOX': return 'badge-blindbox';
                case 'GACHA': return 'badge-gacha';
                default: return 'badge-default';
            }
        }
    }));
});
