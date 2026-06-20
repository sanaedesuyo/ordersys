<template>
  <div>
    <div class="page-header">
      <h1>今日菜单</h1>
      <p>选择你喜欢的菜品，加入购物车后前往结算。</p>
    </div>

    <div class="search-bar">
      <input
        v-model="keyword"
        class="search-input"
        placeholder="搜索菜品名称或描述…"
        @keyup.enter="loadMenu"
      />
      <button class="btn btn-ghost" @click="loadMenu">搜索</button>
    </div>

    <div class="filter-tabs">
      <button
        v-for="f in filters"
        :key="f.value"
        :class="['filter-tab', { active: activeFilter === f.value }]"
        @click="activeFilter = f.value; loadMenu()"
      >
        {{ f.label }}
      </button>
    </div>

    <div v-if="loading" class="loading-row">
      <div class="spinner"></div><span>加载菜单中…</span>
    </div>

    <div v-else-if="dishes.length" class="dish-grid">
      <div v-for="dish in dishes" :key="dish.id" class="dish-card card">
        <div class="dish-card-top">
          <span class="dish-type-icon">{{ typeIcon(dish.type) }}</span>
          <span :class="['status-badge', `badge-${dish.type}`]">{{ typeLabel(dish.type) }}</span>
        </div>
        <div class="dish-card-body">
          <h3 class="dish-name">{{ dish.name }}</h3>
          <p v-if="dish.description" class="dish-desc">{{ dish.description }}</p>
        </div>
        <div class="dish-card-footer">
          <span class="dish-price">¥{{ Number(dish.price).toFixed(2) }}</span>
          <button class="btn btn-yellow btn-sm" @click="addToCart(dish)">
            + 加入购物车
          </button>
        </div>
      </div>
    </div>

    <div v-else class="empty-state card" style="padding: 0">
      <div class="empty-state">
        <span class="empty-icon">🍽️</span>
        <span>{{ keyword ? '未找到匹配的菜品' : '暂无上架菜品' }}</span>
      </div>
    </div>

    <Transition name="slide-up">
      <div v-if="cartStore.count > 0" class="cart-bar" @click="$router.push('/cart')">
        <span class="cart-bar-count">{{ cartStore.count }} 件</span>
        <span class="cart-bar-label">查看购物车</span>
        <span class="cart-bar-total">¥{{ cartStore.total.toFixed(2) }}</span>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, onMounted, inject } from 'vue'
import { dishApi } from '@/api'
import { useCartStore } from '@/stores/useCartStore'
import { DISH_FILTERS, typeLabel, typeIcon } from '@/constants/dishTypes'

const toast = inject('toast')
const cartStore = useCartStore()
const dishes = ref([])
const loading = ref(false)
const activeFilter = ref('ALL')
const keyword = ref('')

const filters = DISH_FILTERS

function addToCart(dish) {
  cartStore.addItem(dish)
  toast(`「${dish.name}」已加入购物车`)
}

async function loadMenu() {
  loading.value = true
  try {
    const params = {}
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    if (activeFilter.value !== 'ALL') params.type = activeFilter.value
    const res = await dishApi.list(params)
    if (res.code === 200) dishes.value = res.data ?? []
    else toast(res.message, 'error')
  } catch {
    toast('加载菜单失败', 'error')
  } finally {
    loading.value = false
  }
}

onMounted(loadMenu)
</script>

<style scoped>
.page-header { margin-bottom: 20px; }
.search-bar { display: flex; gap: 8px; margin-bottom: 16px; }
.search-input {
  flex: 1;
  padding: 10px 14px;
  border: 1.5px solid var(--border);
  border-radius: var(--radius);
  font-size: 14px;
  background: var(--surface);
  color: var(--ink);
  outline: none;
}
.search-input:focus { border-color: var(--ink); }
.filter-tabs { display: flex; gap: 6px; margin-bottom: 24px; flex-wrap: wrap; }
.filter-tab { padding: 7px 16px; border-radius: 99px; font-size: 14px; font-weight: 500; cursor: pointer; background: var(--surface-alt); border: 1.5px solid var(--border); color: var(--ink-60); transition: all 0.12s; }
.filter-tab.active { background: var(--ticket); border-color: var(--ticket-dark); color: var(--ink); }
.loading-row { display: flex; align-items: center; gap: 10px; color: var(--ink-60); font-size: 14px; padding: 40px 0; }
.spinner { width: 18px; height: 18px; border: 2px solid var(--border); border-top-color: var(--ink); border-radius: 50%; animation: spin 0.7s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.dish-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 16px; padding-bottom: 80px; }
.dish-card { display: flex; flex-direction: column; transition: box-shadow 0.18s, transform 0.18s; }
.dish-card:hover { box-shadow: var(--shadow); transform: translateY(-2px); }
.dish-card-top { padding: 14px 16px 10px; display: flex; align-items: center; justify-content: space-between; }
.dish-type-icon { font-size: 28px; }
.dish-card-body { padding: 0 16px 12px; flex: 1; }
.dish-name { font-size: 16px; font-weight: 600; margin-bottom: 4px; }
.dish-desc { font-size: 13px; color: var(--ink-60); line-height: 1.5; }
.dish-card-footer { padding: 12px 16px; border-top: 1px solid var(--border); display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.dish-price { font-family: var(--font-display); font-size: 18px; font-weight: 700; }
.cart-bar { position: fixed; bottom: 24px; left: 50%; transform: translateX(-50%); width: calc(100% - 48px); max-width: 480px; background: var(--ink); color: #fff; border-radius: var(--radius-lg); padding: 14px 20px; display: flex; align-items: center; gap: 12px; cursor: pointer; box-shadow: var(--shadow-lg); z-index: 200; }
.cart-bar-count { font-family: var(--font-mono); font-size: 12px; background: var(--ticket); color: var(--ink); border-radius: 99px; padding: 2px 8px; font-weight: 700; }
.cart-bar-label { flex: 1; font-size: 14px; font-weight: 500; }
.cart-bar-total { font-family: var(--font-display); font-size: 16px; font-weight: 700; }
.slide-up-enter-active, .slide-up-leave-active { transition: all 0.25s; }
.slide-up-enter-from, .slide-up-leave-to { transform: translateX(-50%) translateY(80px); opacity: 0; }
</style>
