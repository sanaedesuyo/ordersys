<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="sidebar-logo">
        <div class="logo-mark">
          <span class="logo-icon">🍜</span>
        </div>
        <div class="logo-text">
          <span class="logo-name">OrderSys</span>
          <span class="logo-ver">v0.0.1</span>
        </div>
      </div>

      <nav class="sidebar-nav">
        <RouterLink to="/orders" class="nav-item">
          <span class="nav-icon">📋</span>
          <span class="nav-label">订单管理</span>
        </RouterLink>
        <RouterLink to="/dishes" class="nav-item">
          <span class="nav-icon">🍽️</span>
          <span class="nav-label">菜品管理</span>
        </RouterLink>
        <RouterLink to="/users" class="nav-item">
          <span class="nav-icon">👤</span>
          <span class="nav-label">用户管理</span>
        </RouterLink>
        <RouterLink to="/payments" class="nav-item">
          <span class="nav-icon">💳</span>
          <span class="nav-label">支付记录</span>
        </RouterLink>
      </nav>

      <div class="sidebar-footer">
        <span class="api-dot"></span>
        <span class="api-label mono">:8080</span>
      </div>
    </aside>

    <main class="main-content">
      <RouterView v-slot="{ Component }">
        <Transition name="fade" mode="out-in">
          <component :is="Component" />
        </Transition>
      </RouterView>
    </main>
  </div>

  <!-- Global Toast -->
  <div class="toast-container">
    <TransitionGroup name="fade">
      <div
        v-for="t in toasts"
        :key="t.id"
        :class="['toast', t.type === 'error' ? 'toast-error' : 'toast-success']"
      >
        <span>{{ t.type === 'error' ? '✕' : '✓' }}</span>
        {{ t.message }}
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup>
import { provide, ref } from 'vue'

const toasts = ref([])
let tid = 0

function showToast(message, type = 'success') {
  const id = ++tid
  toasts.value.push({ id, message, type })
  setTimeout(() => {
    toasts.value = toasts.value.filter((t) => t.id !== id)
  }, 3500)
}

provide('toast', showToast)
</script>

<style scoped>
.app-shell {
  display: flex;
  min-height: 100vh;
}

/* ── Sidebar ─────────────────────────────────────────────────── */
.sidebar {
  width: 220px;
  flex-shrink: 0;
  background: var(--surface);
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 0;
  height: 100vh;
  overflow-y: auto;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 18px 18px;
  border-bottom: 1px solid var(--border);
}

.logo-mark {
  width: 36px;
  height: 36px;
  background: var(--ticket);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.logo-text {
  display: flex;
  flex-direction: column;
  line-height: 1.1;
}

.logo-name {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 15px;
  color: var(--ink);
}

.logo-ver {
  font-family: var(--font-mono);
  font-size: 10px;
  color: var(--ink-30);
}

/* ── Nav ─────────────────────────────────────────────────────── */
.sidebar-nav {
  flex: 1;
  padding: 12px 10px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 10px;
  border-radius: var(--radius-sm);
  color: var(--ink-60);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: background 0.12s, color 0.12s;
}

.nav-item:hover {
  background: var(--surface-alt);
  color: var(--ink);
}

.nav-item.router-link-active {
  background: var(--ticket);
  color: var(--ink);
}

.nav-icon { font-size: 16px; flex-shrink: 0; }

/* ── Footer ──────────────────────────────────────────────────── */
.sidebar-footer {
  padding: 14px 18px;
  border-top: 1px solid var(--border);
  display: flex;
  align-items: center;
  gap: 7px;
}

.api-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--status-completed);
  flex-shrink: 0;
  box-shadow: 0 0 0 2px rgba(34,197,94,0.2);
}

.api-label {
  font-size: 12px;
  color: var(--ink-60);
}

/* ── Main ────────────────────────────────────────────────────── */
.main-content {
  flex: 1;
  padding: 32px 36px;
  overflow-x: hidden;
  min-width: 0;
}

@media (max-width: 768px) {
  .app-shell { flex-direction: column; }
  .sidebar { width: 100%; height: auto; position: static; flex-direction: row; flex-wrap: wrap; }
  .sidebar-nav { flex-direction: row; }
  .main-content { padding: 20px 16px; }
}
</style>
