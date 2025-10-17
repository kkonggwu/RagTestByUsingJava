<template>
    <div class="app">
        <aside class="sidebar" :class="{ open: sidebarOpen }">
            <div class="brand">
                <span class="brand-avatar">🌟</span>
                <span>小秋同学</span>
            </div>
            <nav>
                <RouterLink to="/" class="nav-item"><span class="nav-emoji">🏠</span><span>首页</span></RouterLink>
                <RouterLink to="/chat" class="nav-item"><span class="nav-emoji">💬</span><span>问答应用</span></RouterLink>
                <RouterLink to="/manus" class="nav-item"><span class="nav-emoji">🧠</span><span>Manus</span>
                </RouterLink>
            </nav>
        </aside>
        <main class="content">
            <button class="mobile-toggle" @click="sidebarOpen = !sidebarOpen">☰</button>
            <RouterView />
        </main>
    </div>
</template>

<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
import { ref } from 'vue'
const sidebarOpen = ref(false)
</script>

<style scoped>
.app {
    display: flex;
    height: 100vh;
    gap: 16px;
}

.sidebar {
    width: 260px;
    background: var(--brand-100);
    border-right: 1px solid var(--border);
    padding: 16px;
    height: 100vh;
    box-sizing: border-box;
}

.brand {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 800;
    font-size: 20px;
    margin-bottom: 16px;
}

.brand-avatar {
    width: 28px;
    height: 28px;
    display: grid;
    place-items: center;
    background: #fff;
    border: 1px solid var(--border);
    border-radius: 50%;
}

.nav-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 12px;
    border-radius: 10px;
    margin-bottom: 8px;
    text-decoration: none;
    color: var(--brand);
    background: #fff;
    border: 1px solid var(--border);
    transition: background-color .15s ease, color .15s ease, border-color .15s ease;
}

.nav-emoji {
    width: 20px;
    text-align: center;
}

.nav-item:hover {
    background: #ffe6f1;
    border-color: #ffbddc;
}

.nav-item.router-link-active {
    background: var(--brand);
    color: #fff;
    border-color: var(--brand);
    font-weight: 700;
}

.content {
    flex: 1;
    background: #fff;
    border: 1px solid var(--border);
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow);
    position: relative;
}

.mobile-toggle {
    display: none;
    position: absolute;
    top: 10px;
    left: 10px;
    background: var(--brand);
    color: #fff;
    border: none;
    border-radius: 8px;
    padding: 6px 10px;
}

@media (max-width: 1024px) {
    .app {
        gap: 12px;
    }
}

@media (max-width: 768px) {
    .app {
        gap: 8px;
    }

    .sidebar {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        height: auto;
        z-index: 30;
        transform: translateY(-200%);
        transition: transform .2s ease;
    }

    .sidebar.open {
        transform: translateY(0);
    }

    .content {
        min-height: 80vh;
        margin-top: 52px;
    }

    .mobile-toggle {
        display: inline-block;
    }
}
</style>
