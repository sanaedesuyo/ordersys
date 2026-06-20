<template>
  <div>
    <div class="page-header">
      <h1>确认订单</h1>
      <p>核对菜品和收货地址后提交下单。</p>
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
        <div class="section-title">收货地址</div>

        <div v-if="loadingAddresses" class="loading-row compact">
          <div class="spinner"></div><span>加载地址…</span>
        </div>

        <div v-else-if="addresses.length === 0" class="no-address">
          <p>下单前请先添加收货地址</p>
          <RouterLink to="/addresses" class="btn btn-yellow btn-sm">去添加地址</RouterLink>
        </div>

        <div v-else class="address-options">
          <label
            v-for="addr in addresses"
            :key="addr.id"
            :class="['address-option', { selected: selectedAddressId === addr.id }]"
          >
            <input
              v-model="selectedAddressId"
              type="radio"
              :value="addr.id"
              name="deliveryAddress"
            />
            <div class="option-body">
              <div class="option-head">
                <span v-if="addr.label" class="option-label">{{ addr.label }}</span>
                <span v-if="addr.isDefault" class="default-badge">默认</span>
              </div>
              <span class="option-detail">{{ addr.detail }}</span>
            </div>
          </label>
          <RouterLink to="/addresses" class="manage-link">管理地址</RouterLink>
        </div>

        <div class="section-title" style="margin-top: 8px">订单备注</div>
        <div class="field">
          <label>备注（选填）</label>
          <input v-model="remark" placeholder="如：不要辣、少盐、多放葱……" />
        </div>
        <button
          class="btn btn-yellow btn-submit"
          :disabled="submitting || !selectedAddressId"
          @click="submitOrder"
        >
          {{ submitting ? '提交中…' : '提交订单' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { inject, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/useCartStore'
import { addressApi, orderApi } from '@/api'

const toast = inject('toast')
const router = useRouter()
const cartStore = useCartStore()
const remark = ref('')
const submitting = ref(false)
const addresses = ref([])
const loadingAddresses = ref(true)
const selectedAddressId = ref(null)

onMounted(loadAddresses)

async function loadAddresses() {
  loadingAddresses.value = true
  try {
    const res = await addressApi.list()
    if (res.code === 200) {
      addresses.value = res.data || []
      const defaultAddr = addresses.value.find((a) => a.isDefault)
      selectedAddressId.value = defaultAddr?.id ?? addresses.value[0]?.id ?? null
    }
  } catch {
    toast('加载地址失败', 'error')
  } finally {
    loadingAddresses.value = false
  }
}

async function submitOrder() {
  if (!selectedAddressId.value) {
    toast('请选择收货地址', 'error')
    return
  }
  submitting.value = true
  try {
    const payload = {
      remark: remark.value,
      addressId: selectedAddressId.value,
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
.loading-row.compact { padding: 8px 0; font-size: 13px; }
.no-address { display: flex; flex-direction: column; align-items: flex-start; gap: 10px; font-size: 14px; color: var(--ink-60); }
.address-options { display: flex; flex-direction: column; gap: 8px; }
.address-option {
  display: flex;
  gap: 10px;
  padding: 12px 14px;
  border: 1.5px solid var(--border);
  border-radius: var(--radius);
  cursor: pointer;
  transition: border-color 0.12s, background 0.12s;
}
.address-option:hover { border-color: var(--border-dark); }
.address-option.selected { border-color: var(--ink); background: var(--surface-alt); }
.address-option input { margin-top: 3px; flex-shrink: 0; }
.option-body { flex: 1; min-width: 0; }
.option-head { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.option-label { font-weight: 600; font-size: 14px; }
.default-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 99px;
  background: var(--ticket);
  font-weight: 600;
}
.option-detail { font-size: 13px; color: var(--ink-60); line-height: 1.4; }
.manage-link { font-size: 13px; color: var(--ink-60); text-decoration: none; margin-top: 4px; }
.manage-link:hover { color: var(--ink); text-decoration: underline; }
</style>
