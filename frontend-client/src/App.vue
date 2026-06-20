<template>
  <template v-if="isAuthPage">
    <RouterView />
  </template>
  <template v-else>
    <div class="app-shell">
      <header class="top-bar">
        <div class="top-bar-brand">
          <span class="brand-icon">🍜</span>
          <span class="brand-name">OrderSys</span>
        </div>
        <nav class="top-nav">
          <RouterLink to="/menu" class="nav-item">菜单</RouterLink>
          <RouterLink to="/cart" class="nav-item">
            购物车
            <span v-if="cartStore.count > 0" class="cart-badge">{{ cartStore.count }}</span>
          </RouterLink>
          <RouterLink v-if="authStore.isLoggedIn" to="/orders" class="nav-item">我的订单</RouterLink>
        </nav>
        <nav v-if="authStore.isLoggedIn" class="top-nav top-nav-right">
          <RouterLink to="/addresses" class="nav-item">我的地址</RouterLink>
          <RouterLink to="/profile" class="nav-item">账户</RouterLink>
        </nav>
        <div class="top-bar-actions">
          <template v-if="authStore.isLoggedIn">
            <RouterLink to="/profile" class="user-greeting">你好，{{ authStore.name }}</RouterLink>
            <button class="btn btn-ghost btn-sm" @click="handleLogout">退出</button>
          </template>
          <template v-else>
            <RouterLink to="/login" class="btn btn-ghost btn-sm">登录</RouterLink>
            <RouterLink to="/register" class="btn btn-yellow btn-sm">注册</RouterLink>
          </template>
        </div>
      </header>

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
import { useCartStore } from '@/stores/useCartStore'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const cartStore = useCartStore()

const isAuthPage = computed(() => ['/login', '/register'].includes(route.path))

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
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.top-bar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: var(--surface);
  border-bottom: 1px solid var(--border);
  padding: 0 24px;
  height: 56px;
  display: flex;
  align-items: center;
  gap: 24px;
}

.top-bar-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.brand-icon { font-size: 20px; }
.brand-name { font-family: var(--font-display); font-weight: 700; font-size: 16px; color: var(--ink); }

.top-nav {
  display: flex;
  align-items: center;
  gap: 4px;
}

.top-nav-right {
  margin-left: auto;
}

.nav-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border-radius: var(--radius-sm);
  color: var(--ink-60);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: background 0.12s, color 0.12s;
  position: relative;
}
.nav-item:hover { background: var(--surface-alt); color: var(--ink); }
.nav-item.router-link-active { background: var(--ticket); color: var(--ink); }

.cart-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 4px;
  background: var(--status-cancelled);
  color: #fff;
  border-radius: 99px;
  font-size: 11px;
  font-weight: 700;
  font-family: var(--font-mono);
}

.top-bar-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.user-greeting {
  font-size: 13px;
  color: var(--ink-60);
  text-decoration: none;
}
.user-greeting:hover {
  color: var(--ink);
  text-decoration: underline;
}

.main-content {
  flex: 1;
  padding: 32px 24px;
  max-width: 960px;
  margin: 0 auto;
  width: 100%;
}

@media (max-width: 640px) {
  .top-bar { padding: 0 16px; gap: 12px; }
  .main-content { padding: 20px 16px; }
  .user-greeting { display: none; }
}
</style>
