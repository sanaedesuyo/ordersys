<template>
  <div>
    <div class="page-header">
      <h1>订单看板</h1>
      <p>查看所有订单状态，完成接单、配送、完成等履约操作。</p>
    </div>

    <div class="toolbar">
      <div class="search-row">
        <input
          v-model="queryId"
          class="id-input mono"
          placeholder="输入订单 ID 查询"
          type="number"
          @keyup.enter="fetchOrder"
        />
        <button class="btn btn-ghost" @click="fetchOrder">查询</button>
      </div>
      <button class="btn btn-ghost" style="margin-left: auto" @click="fetchOrdersList">
        ↻ 刷新
      </button>
    </div>

    <Transition name="fade">
      <div v-if="queriedOrder" class="section-block">
        <div class="section-label">查询结果</div>
        <AdminOrderTicket :order="queriedOrder" @action="doAction" />
      </div>
    </Transition>

    <div class="flow-board-card card">
      <div class="flow-board-section">
        <span class="section-label">各阶段订单</span>
        <div class="flow-board">
          <div v-for="(step, i) in flowSteps" :key="step.status" class="flow-board-col">
            <div class="flow-board-header">
              <span class="status-badge" :class="`badge-${step.status}`">{{ step.label }}</span>
              <span v-if="i < flowSteps.length - 1" class="flow-board-arrow">→</span>
            </div>
            <div class="flow-board-body">
              <button
                v-for="order in ordersByStatus[step.status]"
                :key="order.id"
                class="order-chip"
                :class="{ active: queriedOrder?.id === order.id }"
                @click="selectOrder(order.id)"
              >
                <span class="order-chip-id mono">#{{ order.id }}</span>
                <span class="order-chip-amount">¥{{ formatAmount(order.totalAmount) }}</span>
              </button>
              <span v-if="!ordersByStatus[step.status]?.length" class="flow-board-empty">暂无</span>
            </div>
          </div>
        </div>
      </div>

      <div v-if="cancelledOrders.length" class="flow-cancelled">
        <span class="flow-cancelled-label">已取消</span>
        <div class="flow-cancelled-list">
          <button
            v-for="order in cancelledOrders"
            :key="order.id"
            class="order-chip order-chip-cancelled"
            :class="{ active: queriedOrder?.id === order.id }"
            @click="selectOrder(order.id)"
          >
            <span class="order-chip-id mono">#{{ order.id }}</span>
            <span class="order-chip-amount">¥{{ formatAmount(order.totalAmount) }}</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, inject, computed, onMounted } from 'vue'
import { orderApi } from '@/api'
import AdminOrderTicket from '@/components/AdminOrderTicket.vue'

const toast = inject('toast')
const queryId = ref('')
const queriedOrder = ref(null)
const allOrders = ref([])

const flowSteps = [
  { status: 'PENDING_PAYMENT', label: '待支付' },
  { status: 'PAID',            label: '已支付' },
  { status: 'PREPARING',       label: '制作中' },
  { status: 'DELIVERING',      label: '配送中' },
  { status: 'COMPLETED',       label: '已完成' },
]

const ordersByStatus = computed(() => {
  const grouped = Object.fromEntries(flowSteps.map((s) => [s.status, []]))
  for (const order of allOrders.value) {
    if (grouped[order.status]) grouped[order.status].push(order)
  }
  return grouped
})

const cancelledOrders = computed(() => allOrders.value.filter((o) => o.status === 'CANCELLED'))

function formatAmount(amount) { return Number(amount ?? 0).toFixed(2) }

async function fetchOrdersList() {
  try {
    const res = await orderApi.list()
    if (res.code === 200) allOrders.value = res.data ?? []
    else toast(res.message, 'error')
  } catch {
    toast('加载订单列表失败', 'error')
  }
}

async function selectOrder(id) {
  queryId.value = id
  await fetchOrder()
}

async function fetchOrder() {
  if (!queryId.value) return
  try {
    const res = await orderApi.get(queryId.value)
    if (res.code === 200) queriedOrder.value = res.data
    else toast(res.message, 'error')
  } catch {
    toast('订单不存在或请求失败', 'error')
  }
}

async function doAction({ orderId, action }) {
  try {
    const res = await orderApi[action](orderId)
    if (res.code === 200) {
      toast(`操作成功，当前状态：${res.data.status}`)
      queriedOrder.value = res.data
      await fetchOrdersList()
    } else {
      toast(res.message, 'error')
    }
  } catch {
    toast('操作失败', 'error')
  }
}

onMounted(fetchOrdersList)
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.search-row { display: flex; gap: 6px; }

.id-input {
  padding: 8px 12px;
  border: 1.5px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  background: var(--surface);
  color: var(--ink);
  outline: none;
  width: 180px;
}
.id-input:focus { border-color: var(--ink); }

.section-block { margin-bottom: 24px; }
.section-label {
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--ink-60);
  margin-bottom: 10px;
  display: block;
}

.flow-board-card { padding: 24px; display: flex; flex-direction: column; gap: 20px; }
.flow-board-section { display: flex; flex-direction: column; gap: 10px; }

.flow-board {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 10px;
}

.flow-board-col {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 120px;
  padding: 12px;
  background: var(--surface-alt);
  border: 1px solid var(--border);
  border-radius: var(--radius);
}

.flow-board-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
}

.flow-board-arrow { color: var(--ink-30); font-size: 13px; flex-shrink: 0; }
.flow-board-body { display: flex; flex-direction: column; gap: 6px; flex: 1; }
.flow-board-empty { font-size: 12px; color: var(--ink-30); padding: 8px 0; }

.order-chip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  width: 100%;
  padding: 7px 10px;
  border: 1.5px solid var(--border);
  border-radius: var(--radius-sm);
  background: var(--surface-alt);
  cursor: pointer;
  transition: border-color 0.12s, background 0.12s;
  text-align: left;
}
.order-chip:hover { border-color: var(--ink-30); background: var(--surface); }
.order-chip.active { border-color: var(--ink); background: #FFF9E6; }
.order-chip-id { font-size: 12px; font-weight: 600; color: var(--ink); }
.order-chip-amount { font-size: 12px; color: var(--ink-60); }
.order-chip-cancelled { opacity: 0.75; }

.flow-cancelled { display: flex; align-items: flex-start; gap: 12px; padding-top: 4px; }
.flow-cancelled-label { font-size: 11px; font-weight: 600; color: var(--status-cancelled); font-family: var(--font-mono); flex-shrink: 0; padding-top: 8px; }
.flow-cancelled-list { display: flex; flex-wrap: wrap; gap: 6px; flex: 1; }
.flow-cancelled-list .order-chip { width: auto; min-width: 120px; }

@media (max-width: 900px) {
  .flow-board { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .flow-board-arrow { display: none; }
}
</style>
