<template>
  <div>
    <div class="page-header">
      <h1>购物车</h1>
      <p>确认菜品和数量后前往结算。</p>
    </div>

    <div v-if="cartStore.items.length === 0" class="empty-state card" style="padding: 0">
      <div class="empty-state">
        <span class="empty-icon">🛒</span>
        <span>购物车为空</span>
        <RouterLink to="/menu" class="btn btn-yellow btn-sm" style="margin-top: 8px">去点菜</RouterLink>
      </div>
    </div>

    <div v-else>
      <div class="cart-list">
        <div v-for="item in cartStore.items" :key="item.dishId" class="cart-item card">
          <div class="cart-item-info">
            <span class="item-name">{{ item.dishName }}</span>
            <span class="item-price">¥{{ Number(item.price).toFixed(2) }}</span>
          </div>
          <div class="cart-item-controls">
            <button class="qty-btn" @click="cartStore.removeItem(item.dishId)">−</button>
            <span class="qty-val">{{ item.quantity }}</span>
            <button class="qty-btn" @click="cartStore.addItem({ id: item.dishId, name: item.dishName, price: item.price })">+</button>
            <span class="item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
          </div>
        </div>
      </div>

      <div class="cart-footer card">
        <div class="cart-summary">
          <span class="summary-label">合计</span>
          <span class="summary-total">¥{{ cartStore.total.toFixed(2) }}</span>
        </div>
        <div class="cart-actions">
          <button class="btn btn-ghost" @click="cartStore.clearCart()">清空购物车</button>
          <RouterLink to="/checkout" class="btn btn-yellow">去结算 ({{ cartStore.count }} 件)</RouterLink>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useCartStore } from '@/stores/useCartStore'

const cartStore = useCartStore()
</script>

<style scoped>
.cart-list { display: flex; flex-direction: column; gap: 10px; margin-bottom: 16px; }
.cart-item { padding: 14px 16px; display: flex; align-items: center; justify-content: space-between; gap: 16px; flex-wrap: wrap; }
.cart-item-info { display: flex; flex-direction: column; gap: 2px; flex: 1; }
.item-name { font-size: 15px; font-weight: 600; color: var(--ink); }
.item-price { font-size: 13px; color: var(--ink-60); }
.cart-item-controls { display: flex; align-items: center; gap: 10px; }
.qty-btn { width: 28px; height: 28px; border-radius: 50%; border: 1.5px solid var(--border); background: var(--surface-alt); cursor: pointer; font-size: 16px; font-weight: 500; display: flex; align-items: center; justify-content: center; transition: background 0.12s; }
.qty-btn:hover { background: var(--border); }
.qty-val { font-size: 15px; font-weight: 600; min-width: 24px; text-align: center; font-family: var(--font-mono); }
.item-subtotal { font-family: var(--font-display); font-size: 15px; font-weight: 700; min-width: 60px; text-align: right; }
.cart-footer { padding: 20px; display: flex; flex-direction: column; gap: 14px; }
.cart-summary { display: flex; align-items: center; justify-content: space-between; }
.summary-label { font-size: 14px; color: var(--ink-60); }
.summary-total { font-family: var(--font-display); font-size: 24px; font-weight: 700; color: var(--ink); }
.cart-actions { display: flex; justify-content: flex-end; gap: 10px; }
</style>
