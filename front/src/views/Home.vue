<template>
    <div class="home">
        <div class="hero">
            <div class="hero-badge">🌟 小秋同学</div>
            <h1 class="gradient-title">你好呀！一起和小秋聊天吧～</h1>
            <p>选择一个应用开始你的 AI 之旅：</p>
            <div class="apps">
                <RouterLink to="/chat" class="app-card">
                    <div class="app-avatar">💬</div>
                    <div class="app-name">问答应用</div>
                    <div class="app-desc">像聊天一样问问题，实时流式回复</div>
                </RouterLink>
                <RouterLink to="/manus" class="app-card">
                    <div class="app-avatar">🧠</div>
                    <div class="app-name">Manus</div>
                    <div class="app-desc">一步一步执行任务，过程可视</div>
                </RouterLink>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router'
</script>

<style scoped>
.home {
    height: 100%;
    display: grid;
    place-items: center;
    padding: 24px;
}

.hero {
    text-align: center;
}

.hero-badge {
    display: inline-block;
    background: var(--brand-100);
    color: var(--brand);
    border: 1px solid var(--border);
    padding: 6px 10px;
    border-radius: 999px;
    font-weight: 700;
    box-shadow: var(--shadow);
}

.hero h1 {
    margin: 16px 0 8px;
    font-size: 28px;
}

.hero p {
    margin: 0 0 20px;
    color: var(--muted);
}

.apps {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 16px;
}

.app-card {
    text-decoration: none;
    color: inherit;
    background: #fff;
    border: 1px solid var(--border);
    border-radius: var(--radius-lg);
    padding: 16px;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    box-shadow: var(--shadow);
    transition: transform .12s ease, box-shadow .12s ease, border-color .12s ease;
}

.app-card:hover {
    transform: translateY(-3px);
    box-shadow: 0 14px 32px rgba(34, 197, 94, 0.18);
    border-color: var(--accent);
}

.app-avatar {
    width: 48px;
    height: 48px;
    display: grid;
    place-items: center;
    background: #fff;
    border: 2px solid transparent;
    border-radius: 50%;
    background-image: linear-gradient(#fff, #fff), linear-gradient(90deg, var(--brand), var(--accent));
    background-origin: border-box;
    background-clip: content-box, border-box;
    animation: float 6s ease-in-out infinite;
}

.app-name {
    font-weight: 800;
}

.app-desc {
    color: var(--muted);
    font-size: 14px;
}

.gradient-title {
    background: linear-gradient(90deg, var(--brand), var(--accent));
    -webkit-background-clip: text;
    background-clip: text;
    color: transparent;
    animation: hue 8s linear infinite;
}

@keyframes hue {
    0% {
        filter: hue-rotate(0deg);
    }

    100% {
        filter: hue-rotate(360deg);
    }
}

@media (max-width: 640px) {
    .apps {
        grid-template-columns: 1fr;
    }
}
</style>
