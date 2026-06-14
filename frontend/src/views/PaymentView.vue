<template>
  <div>
    <div class="page-header">
      <h1>支付记录</h1>
      <p>按订单 ID 查询支付流水，或直接对指定订单发起支付。</p>
    </div>

    <div class="pay-layout">
      <!-- Query panel -->
      <div class="panel card">
        <div class="panel-header">
          <h2>查询支付记录</h2>
        </div>
        <div class="panel-body">
          <div style="display: flex; gap: 8px">
            <div class="field" style="flex: 1">
              <label>订单 ID</label>
              <input v-model.number="queryOrderId" type="number" placeholder="1" @keyup.enter="fetchPayments" />
            </div>
            <button class="btn btn-ghost" style="align-self: flex-end" @click="fetchPayments">查询</button>
          </div>
        </div>
      </div>

      <!-- Pay now panel -->
      <div class="panel card">
        <div class="panel-header">
          <h2>发起支付</h2>
        </div>
        <div class="panel-body">
          <div class="field">
            <label>订单 ID</label>
            <input v-model.number="payForm.orderId" type="number" placeholder="1" />
          </div>
          <div class="field">
            <label>支付金额（元）</label>
            <input v-model.number="payForm.amount" type="number" step="0.01" placeholder="28.00" />
          </div>
          <div class="field">
            <label>支付方式</label>
            <div class="method-tabs">
              <button
                :class="['method-tab', { active: payForm.method === 'WECHAT' }]"
                @click="payForm.method = 'WECHAT'"
              >
                💚 微信支付
              </button>
              <button
                :class="['method-tab', { active: payForm.method === 'ALIPAY' }]"
                @click="payForm.method = 'ALIPAY'"
              >
                💙 支付宝
              </button>
            </div>
          </div>
          <button class="btn btn-yellow" style="align-self: flex-end" :disabled="submitting" @click="submitPay">
            {{ submitting ? '处理中…' : '确认支付' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Payment records -->
    <Transition name="fade">
      <div v-if="payments.length" class="records-section">
        <div class="section-label">
          订单 #{{ queryOrderId }} 的支付记录（{{ payments.length }} 条）
        </div>
        <div class="records-list">
          <div v-for="p in payments" :key="p.id" class="record-row card">
            <div class="record-left">
              <div class="record-id mono">#{{ p.id }}</div>
              <div class="record-info">
                <span class="record-txn mono">{{ p.transactionId }}</span>
                <span class="record-time mono small">{{ formatTime(p.createTime) }}</span>
              </div>
            </div>
            <div class="record-right">
              <span :class="['status-badge', `badge-${p.method}`]">
                {{ p.method === 'WECHAT' ? '微信' : '支付宝' }}
              </span>
              <span :class="['status-badge', `badge-${p.status}`]">
                {{ p.status === 'SUCCESS' ? '成功' : '失败' }}
              </span>
              <span class="record-amount">¥{{ p.amount?.toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </div>
    </Transition>

    <Transition name="fade">
      <div v-if="searched && !payments.length" class="empty-state card" style="padding: 0">
        <div class="empty-state">
          <span class="empty-icon">💳</span>
          <span>该订单暂无支付记录</span>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, inject } from 'vue'
import { paymentApi } from '@/api'

const toast = inject('toast')

const queryOrderId = ref(null)
const payments = ref([])
const searched = ref(false)
const submitting = ref(false)
const payForm = ref({ orderId: null, amount: null, method: 'WECHAT' })

function formatTime(t) {
  if (!t) return '—'
  return t.replace('T', ' ').slice(0, 16)
}

async function fetchPayments() {
  if (!queryOrderId.value) return
  searched.value = false
  payments.value = []
  try {
    const res = await paymentApi.getByOrder(queryOrderId.value)
    if (res.code === 200) {
      payments.value = res.data ?? []
    } else {
      toast(res.message, 'error')
    }
  } catch {
    toast('查询失败', 'error')
  } finally {
    searched.value = true
  }
}

async function submitPay() {
  if (!payForm.value.orderId || !payForm.value.amount) {
    return toast('请填写订单 ID 和支付金额', 'error')
  }
  submitting.value = true
  try {
    const res = await paymentApi.pay(payForm.value)
    if (res.code === 200) {
      toast(
        res.data.status === 'SUCCESS'
          ? `支付成功！交易号：${res.data.transactionId}`
          : '支付失败，请重试',
        res.data.status === 'SUCCESS' ? 'success' : 'error',
      )
      queryOrderId.value = payForm.value.orderId
      await fetchPayments()
    } else {
      toast(res.message, 'error')
    }
  } catch {
    toast('支付请求失败', 'error')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.pay-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 28px;
  align-items: start;
}

@media (max-width: 700px) {
  .pay-layout { grid-template-columns: 1fr; }
}

.panel { overflow: visible; }

.panel-header {
  padding: 16px 20px 14px;
  border-bottom: 1px solid var(--border);
}
.panel-header h2 { font-size: 16px; }

.panel-body {
  padding: 18px 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.method-tabs {
  display: flex;
  gap: 8px;
}

.method-tab {
  flex: 1;
  padding: 9px 12px;
  border: 1.5px solid var(--border);
  border-radius: var(--radius-sm);
  background: var(--surface-alt);
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: border-color 0.15s, background 0.15s;
  color: var(--ink-60);
}

.method-tab.active {
  border-color: var(--ink);
  background: var(--surface);
  color: var(--ink);
}

.section-label {
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--ink-60);
  margin-bottom: 10px;
  display: block;
}

.records-section { margin-top: 4px; }

.records-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.record-row {
  padding: 14px 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.record-left { display: flex; align-items: center; gap: 14px; }

.record-id {
  font-size: 20px;
  font-weight: 500;
  color: var(--ink-30);
  min-width: 36px;
}

.record-info { display: flex; flex-direction: column; gap: 2px; }

.record-txn { font-size: 12px; color: var(--ink-60); }
.record-time { font-size: 11px; color: var(--ink-30); }
.small { font-size: 11px; }

.record-right { display: flex; align-items: center; gap: 8px; }

.record-amount {
  font-family: var(--font-display);
  font-size: 17px;
  font-weight: 700;
  color: var(--ink);
  margin-left: 8px;
}
</style>
