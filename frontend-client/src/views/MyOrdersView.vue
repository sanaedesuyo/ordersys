<template>
  <div>
    <div class="page-header">
      <h1>我的订单</h1>
      <p>查看你提交的所有订单记录。</p>
    </div>

    <div class="filter-tabs">
      <button
        v-for="f in filters"
        :key="f.value"
        :class="['filter-tab', { active: activeFilter === f.value }]"
        @click="activeFilter = f.value; loadOrders()"
      >
        {{ f.label }}
      </button>
    </div>

    <div v-if="loading" class="loading-row">
      <div class="spinner"></div><span>加载中…</span>
    </div>

    <div v-else-if="orders.length" class="order-list">
      <RouterLink
        v-for="order in orders"
        :key="order.id"
        :to="`/orders/${order.id}`"
        class="order-row card"
      >
        <div class="order-row-id mono">#{{ order.id }}</div>
        <div class="order-row-info">
          <span :class="['status-badge', `badge-${order.status}`]">{{ statusLabel(order.status) }}</span>
          <span class="order-row-time">{{ formatTime(order.createTime) }}</span>
        </div>
        <div class="order-row-amount">¥{{ Number(order.totalAmount).toFixed(2) }}</div>
        <span class="order-row-arrow">›</span>
      </RouterLink>
    </div>

    <div v-else class="empty-state card" style="padding: 0">
      <div class="empty-state">
        <span class="empty-icon">📋</span>
        <span>暂无订单记录</span>
        <RouterLink to="/menu" class="btn btn-yellow btn-sm" style="margin-top: 8px">去点菜</RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, inject, onMounted } from 'vue'
import { orderApi } from '@/api'

const toast = inject('toast')
const orders = ref([])
const loading = ref(false)
const activeFilter = ref('')

const filters = [
  { value: '', label: '全部' },
  { value: 'PENDING_PAYMENT', label: '待支付' },
  { value: 'PAID', label: '已支付' },
  { value: 'PREPARING', label: '制作中' },
  { value: 'DELIVERING', label: '配送中' },
  { value: 'COMPLETED', label: '已完成' },
  { value: 'CANCELLED', label: '已取消' },
]

const STATUS_LABELS = {
  PENDING_PAYMENT: '待支付',
  PAID: '已支付',
  PREPARING: '制作中',
  DELIVERING: '配送中',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
}

function statusLabel(s) { return STATUS_LABELS[s] ?? s }
function formatTime(t) { return t ? t.replace('T', ' ').slice(0, 16) : '—' }

async function loadOrders() {
  loading.value = true
  try {
    const res = await orderApi.list(activeFilter.value || undefined)
    if (res.code === 200) orders.value = res.data ?? []
    else toast(res.message, 'error')
  } catch { toast('加载订单失败', 'error') }
  finally { loading.value = false }
}

onMounted(loadOrders)
</script>

<style scoped>
.filter-tabs { display: flex; gap: 6px; margin-bottom: 20px; flex-wrap: wrap; }
.filter-tab { padding: 5px 12px; border-radius: 99px; font-size: 13px; font-weight: 500; cursor: pointer; background: var(--surface-alt); border: 1.5px solid var(--border); color: var(--ink-60); transition: all 0.12s; }
.filter-tab.active { background: var(--ticket); border-color: var(--ticket-dark); color: var(--ink); }
.loading-row { display: flex; align-items: center; gap: 10px; color: var(--ink-60); font-size: 14px; padding: 40px 0; }
.spinner { width: 18px; height: 18px; border: 2px solid var(--border); border-top-color: var(--ink); border-radius: 50%; animation: spin 0.7s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.order-list { display: flex; flex-direction: column; gap: 10px; }
.order-row { padding: 14px 16px; display: flex; align-items: center; gap: 14px; cursor: pointer; text-decoration: none; transition: box-shadow 0.15s; }
.order-row:hover { box-shadow: var(--shadow); }
.order-row-id { font-size: 15px; font-weight: 600; color: var(--ink); min-width: 48px; }
.order-row-info { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.order-row-time { font-size: 12px; color: var(--ink-30); font-family: var(--font-mono); }
.order-row-amount { font-family: var(--font-display); font-size: 16px; font-weight: 700; color: var(--ink); }
.order-row-arrow { font-size: 20px; color: var(--ink-30); }
</style>
