<template>
  <div>
    <div class="page-header">
      <h1>支付流水</h1>
      <p>查询任意订单的支付记录。</p>
    </div>

    <div class="toolbar">
      <div class="search-row">
        <input
          v-model.number="orderId"
          class="id-input mono"
          type="number"
          placeholder="输入订单 ID"
          @keyup.enter="query"
        />
        <button class="btn btn-primary" @click="query" :disabled="loading">查询</button>
      </div>
    </div>

    <div v-if="loading" class="loading-row">
      <div class="spinner"></div>
      <span>查询中…</span>
    </div>

    <div v-else-if="payments.length" class="payment-list">
      <div v-for="p in payments" :key="p.id" class="payment-card card">
        <div class="payment-row">
          <div class="payment-field">
            <span class="field-label">支付 ID</span>
            <span class="field-val mono">#{{ p.id }}</span>
          </div>
          <div class="payment-field">
            <span class="field-label">订单 ID</span>
            <span class="field-val mono">#{{ p.orderId }}</span>
          </div>
          <div class="payment-field">
            <span class="field-label">金额</span>
            <span class="field-val amount">¥{{ Number(p.amount).toFixed(2) }}</span>
          </div>
          <div class="payment-field">
            <span class="field-label">方式</span>
            <span :class="['status-badge', `badge-${p.method}`]">{{ p.method }}</span>
          </div>
          <div class="payment-field">
            <span class="field-label">状态</span>
            <span :class="['status-badge', `badge-${p.status}`]">{{ p.status }}</span>
          </div>
          <div class="payment-field">
            <span class="field-label">流水号</span>
            <span class="field-val mono small">{{ p.transactionId }}</span>
          </div>
          <div class="payment-field">
            <span class="field-label">时间</span>
            <span class="field-val mono small">{{ formatTime(p.createTime) }}</span>
          </div>
        </div>
      </div>
    </div>

    <div v-else-if="searched" class="empty-state card" style="padding: 0">
      <div class="empty-state">
        <span class="empty-icon">💳</span>
        <span>该订单暂无支付记录</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, inject } from 'vue'
import { paymentApi } from '@/api'

const toast = inject('toast')
const orderId = ref(null)
const payments = ref([])
const loading = ref(false)
const searched = ref(false)

function formatTime(t) { return t ? t.replace('T', ' ').slice(0, 16) : '—' }

async function query() {
  if (!orderId.value) return toast('请输入订单 ID', 'error')
  loading.value = true
  searched.value = false
  try {
    const res = await paymentApi.getByOrder(orderId.value)
    if (res.code === 200) {
      payments.value = res.data ?? []
      searched.value = true
    } else {
      toast(res.message, 'error')
    }
  } catch { toast('查询失败', 'error') }
  finally { loading.value = false }
}
</script>

<style scoped>
.toolbar { margin-bottom: 24px; }
.search-row { display: flex; gap: 6px; }
.id-input { padding: 8px 12px; border: 1.5px solid var(--border); border-radius: var(--radius-sm); font-size: 14px; background: var(--surface); color: var(--ink); outline: none; width: 200px; }
.id-input:focus { border-color: var(--ink); }
.loading-row { display: flex; align-items: center; gap: 10px; color: var(--ink-60); font-size: 14px; padding: 40px 0; }
.spinner { width: 18px; height: 18px; border: 2px solid var(--border); border-top-color: var(--ink); border-radius: 50%; animation: spin 0.7s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.payment-list { display: flex; flex-direction: column; gap: 12px; }
.payment-card { padding: 18px 20px; }
.payment-row { display: flex; flex-wrap: wrap; gap: 20px; align-items: center; }
.payment-field { display: flex; flex-direction: column; gap: 3px; }
.field-label { font-size: 10px; text-transform: uppercase; letter-spacing: 0.08em; font-weight: 600; color: var(--ink-60); }
.field-val { font-size: 14px; color: var(--ink); }
.field-val.amount { font-family: var(--font-display); font-size: 18px; font-weight: 700; }
.field-val.small { font-size: 12px; }
</style>
