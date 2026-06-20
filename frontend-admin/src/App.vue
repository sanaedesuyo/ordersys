<template>
  <template v-if="isLoginPage">
    <RouterView />
  </template>
  <template v-else>
    <div class="app-shell">
      <aside class="sidebar">
        <div class="sidebar-logo">
          <div class="logo-mark">
            <span class="logo-icon">🍜</span>
          </div>
          <div class="logo-text">
            <span class="logo-name">OrderSys</span>
            <span class="logo-ver">商家后台</span>
          </div>
        </div>

        <nav class="sidebar-nav">
          <RouterLink to="/orders" class="nav-item">
            <span class="nav-icon">📋</span>
            <span class="nav-label">订单看板</span>
          </RouterLink>
          <RouterLink to="/dishes" class="nav-item">
            <span class="nav-icon">🍽️</span>
            <span class="nav-label">菜品管理</span>
          </RouterLink>
          <RouterLink to="/payments" class="nav-item">
            <span class="nav-icon">💳</span>
            <span class="nav-label">支付流水</span>
          </RouterLink>
        </nav>

        <div class="sidebar-footer">
          <span class="user-name">{{ authStore.name }}</span>
          <button class="btn btn-ghost btn-sm" @click="handleLogout">退出</button>
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
  </template>

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
import { provide, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/useAuthStore'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const isLoginPage = computed(() => route.path === '/login')

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

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.app-shell {
  display: flex;
  min-height: 100vh;
}

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

.nav-item:hover { background: var(--surface-alt); color: var(--ink); }
.nav-item.router-link-active { background: var(--ticket); color: var(--ink); }
.nav-icon { font-size: 16px; flex-shrink: 0; }

.sidebar-footer {
  padding: 14px 18px;
  border-top: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.user-name {
  font-size: 13px;
  color: var(--ink-60);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.main-content {
  flex: 1;
  padding: 32px 36px;
  overflow-x: hidden;
  min-width: 0;
}
</style>
