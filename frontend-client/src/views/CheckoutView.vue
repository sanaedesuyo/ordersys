<template>
  <div>
    <div class="page-header">
      <h1>确认订单</h1>
      <p>核对菜品和备注后提交下单。</p>
    </div>

    <div v-if="cartStore.items.length === 0" class="empty-state card" style="padding: 0">
      <div class="empty-state">
        <span class="empty-icon">🛒</span>
        <span>购物车为空，请先添加菜品</span>
        <RouterLink to="/menu" class="btn btn-yellow btn-sm" style="margin-top: 8px">去点菜</RouterLink>
      </div>
    </div>

    <div v-else class="checkout-layout">
      <div class="order-summary card">
        <div class="section-title">订单明细</div>
        <div class="order-items">
          <div v-for="item in cartStore.items" :key="item.dishId" class="order-item-row">
            <span class="oi-name">{{ item.dishName }}</span>
            <span class="oi-qty mono">× {{ item.quantity }}</span>
            <span class="oi-price">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
          </div>
        </div>
        <div class="order-total">
          <span>合计</span>
          <span class="total-amount">¥{{ cartStore.total.toFixed(2) }}</span>
        </div>
      </div>

      <div class="checkout-form card">
        <div class="section-title">订单备注</div>
        <div class="field">
          <label>备注（选填）</label>
          <input v-model="remark" placeholder="如：不要辣、少盐、多放葱……" />
        </div>
        <button
          class="btn btn-yellow btn-submit"
          :disabled="submitting"
          @click="submitOrder"
        >
          {{ submitting ? '提交中…' : '提交订单' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, inject } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/useCartStore'
import { orderApi } from '@/api'

const toast = inject('toast')
const router = useRouter()
const cartStore = useCartStore()
const remark = ref('')
const submitting = ref(false)

async function submitOrder() {
  submitting.value = true
  try {
    const payload = {
      remark: remark.value,
      items: cartStore.items.map((item) => ({
        dishId: item.dishId,
        dishName: item.dishName,
        price: item.price,
        quantity: item.quantity,
        size: item.size,
        extras: item.extras,
        note: item.note,
      })),
    }
    const res = await orderApi.create(payload)
    if (res.code === 200) {
      toast(`订单 #${res.data.id} 提交成功`)
      cartStore.clearCart()
      router.push(`/orders/${res.data.id}`)
    } else {
      toast(res.message, 'error')
    }
  } catch {
    toast('提交失败，请重试', 'error')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.checkout-layout { display: flex; flex-direction: column; gap: 16px; }
.section-title { font-size: 12px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.08em; color: var(--ink-60); margin-bottom: 14px; }
.order-summary { padding: 20px; }
.order-items { display: flex; flex-direction: column; gap: 10px; margin-bottom: 14px; }
.order-item-row { display: flex; align-items: center; gap: 8px; }
.oi-name { flex: 1; font-size: 14px; }
.oi-qty { font-size: 13px; color: var(--ink-60); }
.oi-price { font-size: 14px; font-weight: 600; min-width: 64px; text-align: right; }
.order-total { display: flex; align-items: center; justify-content: space-between; padding-top: 12px; border-top: 1px solid var(--border); font-size: 14px; color: var(--ink-60); }
.total-amount { font-family: var(--font-display); font-size: 22px; font-weight: 700; color: var(--ink); }
.checkout-form { padding: 20px; display: flex; flex-direction: column; gap: 14px; }
.btn-submit { width: 100%; justify-content: center; padding: 12px; font-size: 15px; }
</style>
