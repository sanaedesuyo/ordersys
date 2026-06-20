<template>
  <div class="ticket card">
    <div class="ticket-header">
      <div class="ticket-id-block">
        <span class="ticket-label">ORDER</span>
        <span class="ticket-id mono">#{{ order.id }}</span>
      </div>
      <div class="ticket-status">
        <span :class="['status-badge', `badge-${order.status}`]">
          {{ statusLabel(order.status) }}
        </span>
      </div>
    </div>

    <div class="tear-line"></div>

    <div class="ticket-body">
      <div class="ticket-meta-row">
        <div class="ticket-meta-item">
          <span class="meta-label">用户</span>
          <span class="meta-val mono">UID-{{ order.userId }}</span>
        </div>
        <div class="ticket-meta-item">
          <span class="meta-label">总金额</span>
          <span class="meta-val amount">¥{{ order.totalAmount?.toFixed(2) }}</span>
        </div>
        <div class="ticket-meta-item">
          <span class="meta-label">下单时间</span>
          <span class="meta-val mono small">{{ formatTime(order.createTime) }}</span>
        </div>
        <div v-if="order.remark" class="ticket-meta-item">
          <span class="meta-label">备注</span>
          <span class="meta-val">{{ order.remark }}</span>
        </div>
      </div>

      <div class="ticket-actions">
        <button
          v-if="order.status === 'PAID'"
          class="btn btn-primary btn-sm"
          @click="$emit('action', { orderId: order.id, action: 'accept' })"
        >
          ✓ 接单
        </button>
        <button
          v-if="order.status === 'PREPARING'"
          class="btn btn-primary btn-sm"
          @click="$emit('action', { orderId: order.id, action: 'deliver' })"
        >
          🚴 开始配送
        </button>
        <button
          v-if="order.status === 'DELIVERING'"
          class="btn btn-primary btn-sm"
          @click="$emit('action', { orderId: order.id, action: 'complete' })"
        >
          ✓ 完成订单
        </button>
        <button
          v-if="!['COMPLETED', 'CANCELLED'].includes(order.status)"
          class="btn btn-danger btn-sm"
          @click="$emit('action', { orderId: order.id, action: 'cancel' })"
        >
          取消
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({ order: { type: Object, required: true } })
defineEmits(['action'])

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
</script>

<style scoped>
.ticket { overflow: visible; }

.ticket-header {
  background: var(--ticket);
  padding: 14px 18px 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.ticket-id-block { display: flex; flex-direction: column; line-height: 1; gap: 2px; }
.ticket-label { font-family: var(--font-mono); font-size: 9px; font-weight: 500; letter-spacing: 0.12em; color: rgba(28,28,30,0.5); }
.ticket-id { font-size: 22px; font-weight: 500; color: var(--ink); }

.tear-line {
  height: 0;
  border-top: 2px dashed var(--border-dark);
  margin: 0 18px;
  position: relative;
}
.tear-line::before, .tear-line::after {
  content: '';
  position: absolute;
  top: -8px;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: var(--bg);
  border: 1px solid var(--border-dark);
}
.tear-line::before { left: -26px; }
.tear-line::after  { right: -26px; }

.ticket-body { padding: 16px 18px 18px; }

.ticket-meta-row { display: flex; flex-wrap: wrap; gap: 18px; margin-bottom: 16px; }
.ticket-meta-item { display: flex; flex-direction: column; gap: 2px; }
.meta-label { font-size: 10px; text-transform: uppercase; letter-spacing: 0.08em; font-weight: 600; color: var(--ink-60); }
.meta-val { font-size: 14px; color: var(--ink); }
.meta-val.amount { font-family: var(--font-display); font-size: 18px; font-weight: 700; }
.meta-val.small { font-size: 12px; }

.ticket-actions { display: flex; gap: 8px; flex-wrap: wrap; }
</style>
