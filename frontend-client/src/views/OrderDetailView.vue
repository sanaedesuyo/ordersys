<template>
  <div>
    <div class="back-row">
      <button class="btn btn-ghost btn-sm" @click="$router.push('/orders')">← 返回订单列表</button>
    </div>

    <div v-if="loading" class="loading-row">
      <div class="spinner"></div><span>加载中…</span>
    </div>

    <div v-else-if="order" class="detail-layout">
      <!-- 订单状态卡片 -->
      <div class="status-card card">
        <div class="ticket-header">
          <div class="ticket-id-block">
            <span class="ticket-label">ORDER</span>
            <span class="ticket-id mono">#{{ order.id }}</span>
          </div>
          <span :class="['status-badge', `badge-${order.status}`]">{{ statusLabel(order.status) }}</span>
        </div>

        <div class="tear-line"></div>

        <div class="ticket-body">
          <div class="meta-grid">
            <div class="meta-item">
              <span class="meta-label">总金额</span>
              <span class="meta-val amount">¥{{ Number(order.totalAmount).toFixed(2) }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">下单时间</span>
              <span class="meta-val mono small">{{ formatTime(order.createTime) }}</span>
            </div>
            <div v-if="order.remark" class="meta-item">
              <span class="meta-label">备注</span>
              <span class="meta-val">{{ order.remark }}</span>
            </div>
            <div v-if="order.deliveryAddress" class="meta-item">
              <span class="meta-label">收货地址</span>
              <span class="meta-val">{{ order.deliveryAddress }}</span>
            </div>
          </div>

          <div class="order-actions">
            <button
              v-if="order.status === 'PENDING_PAYMENT'"
              class="btn btn-yellow"
              @click="openPayModal"
            >
              💳 去支付
            </button>
            <button
              v-if="!['COMPLETED', 'CANCELLED'].includes(order.status)"
              class="btn btn-danger btn-sm"
              @click="cancelOrder"
              :disabled="cancelling"
            >
              {{ cancelling ? '取消中…' : '取消订单' }}
            </button>
          </div>
        </div>
      </div>

      <!-- 订单状态流程 -->
      <div class="status-flow card">
        <div class="section-title">订单进度</div>
        <div class="flow-steps">
          <div
            v-for="(step, i) in flowSteps"
            :key="step.status"
            :class="['flow-step', { done: isStepDone(step.status), current: order.status === step.status }]"
          >
            <div class="step-dot"></div>
            <div class="step-info">
              <span class="step-label">{{ step.label }}</span>
            </div>
            <span v-if="i < flowSteps.length - 1" class="step-line"></span>
          </div>
        </div>
      </div>
    </div>

    <!-- 支付弹窗 -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="showPay" class="modal-backdrop" @click.self="showPay = false">
          <div class="modal card" style="max-width: 380px">
            <div class="modal-header">
              <h2>发起支付</h2>
              <button class="btn-icon" @click="showPay = false">✕</button>
            </div>
            <div class="modal-body">
              <div class="field">
                <label>订单 ID</label>
                <input :value="order?.id" readonly class="mono" />
              </div>
              <div class="field">
                <label>支付金额</label>
                <input v-model.number="payAmount" type="number" step="0.01" />
              </div>
              <div class="field">
                <label>支付方式</label>
                <select v-model="payMethod">
                  <option value="WECHAT">微信支付</option>
                  <option value="ALIPAY">支付宝</option>
                </select>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn btn-ghost" @click="showPay = false">取消</button>
              <button class="btn btn-yellow" :disabled="paying" @click="submitPay">
                {{ paying ? '支付中…' : '确认支付' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, inject, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { orderApi, paymentApi } from '@/api'

const toast = inject('toast')
const route = useRoute()
const order = ref(null)
const loading = ref(false)
const cancelling = ref(false)
const showPay = ref(false)
const payAmount = ref(0)
const payMethod = ref('WECHAT')
const paying = ref(false)

const flowSteps = [
  { status: 'PENDING_PAYMENT', label: '待支付' },
  { status: 'PAID',            label: '已支付' },
  { status: 'PREPARING',       label: '制作中' },
  { status: 'DELIVERING',      label: '配送中' },
  { status: 'COMPLETED',       label: '已完成' },
]

const statusOrder = ['PENDING_PAYMENT', 'PAID', 'PREPARING', 'DELIVERING', 'COMPLETED']

function isStepDone(status) {
  if (!order.value) return false
  if (order.value.status === 'CANCELLED') return false
  const orderIdx = statusOrder.indexOf(order.value.status)
  const stepIdx = statusOrder.indexOf(status)
  return stepIdx <= orderIdx
}

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

async function loadOrder() {
  loading.value = true
  try {
    const res = await orderApi.get(route.params.id)
    if (res.code === 200) order.value = res.data
    else toast(res.message, 'error')
  } catch { toast('加载订单失败', 'error') }
  finally { loading.value = false }
}

function openPayModal() {
  payAmount.value = order.value.totalAmount
  showPay.value = true
}

async function submitPay() {
  paying.value = true
  try {
    const res = await paymentApi.pay({
      orderId: order.value.id,
      amount: payAmount.value,
      method: payMethod.value,
    })
    if (res.code === 200) {
      toast(`支付${res.data.status === 'SUCCESS' ? '成功' : '失败'}`)
      showPay.value = false
      await loadOrder()
    } else {
      toast(res.message, 'error')
    }
  } catch { toast('支付请求失败', 'error') }
  finally { paying.value = false }
}

async function cancelOrder() {
  cancelling.value = true
  try {
    const res = await orderApi.cancel(order.value.id)
    if (res.code === 200) {
      toast('订单已取消')
      order.value = res.data
    } else {
      toast(res.message, 'error')
    }
  } catch { toast('取消失败', 'error') }
  finally { cancelling.value = false }
}

onMounted(loadOrder)
</script>

<style scoped>
.back-row { margin-bottom: 20px; }
.loading-row { display: flex; align-items: center; gap: 10px; color: var(--ink-60); font-size: 14px; padding: 40px 0; }
.spinner { width: 18px; height: 18px; border: 2px solid var(--border); border-top-color: var(--ink); border-radius: 50%; animation: spin 0.7s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.detail-layout { display: flex; flex-direction: column; gap: 16px; }

.status-card { overflow: visible; }
.ticket-header { background: var(--ticket); padding: 14px 18px 12px; display: flex; align-items: center; justify-content: space-between; }
.ticket-id-block { display: flex; flex-direction: column; line-height: 1; gap: 2px; }
.ticket-label { font-family: var(--font-mono); font-size: 9px; font-weight: 500; letter-spacing: 0.12em; color: rgba(28,28,30,0.5); }
.ticket-id { font-size: 22px; font-weight: 500; color: var(--ink); }
.tear-line { height: 0; border-top: 2px dashed var(--border-dark); margin: 0 18px; position: relative; }
.tear-line::before, .tear-line::after { content: ''; position: absolute; top: -8px; width: 14px; height: 14px; border-radius: 50%; background: var(--bg); border: 1px solid var(--border-dark); }
.tear-line::before { left: -26px; }
.tear-line::after { right: -26px; }
.ticket-body { padding: 16px 18px 18px; }
.meta-grid { display: flex; flex-wrap: wrap; gap: 18px; margin-bottom: 16px; }
.meta-item { display: flex; flex-direction: column; gap: 2px; }
.meta-label { font-size: 10px; text-transform: uppercase; letter-spacing: 0.08em; font-weight: 600; color: var(--ink-60); }
.meta-val { font-size: 14px; color: var(--ink); }
.meta-val.amount { font-family: var(--font-display); font-size: 20px; font-weight: 700; }
.meta-val.small { font-size: 12px; }
.order-actions { display: flex; gap: 10px; flex-wrap: wrap; }

.status-flow { padding: 20px; }
.section-title { font-size: 12px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.08em; color: var(--ink-60); margin-bottom: 16px; }
.flow-steps { display: flex; align-items: center; gap: 0; flex-wrap: wrap; gap: 8px; }
.flow-step { display: flex; align-items: center; gap: 6px; }
.step-dot { width: 10px; height: 10px; border-radius: 50%; background: var(--border-dark); flex-shrink: 0; transition: background 0.2s; }
.flow-step.done .step-dot { background: var(--status-completed); }
.flow-step.current .step-dot { background: var(--ticket); box-shadow: 0 0 0 3px rgba(255,214,10,0.3); }
.step-info { display: flex; flex-direction: column; }
.step-label { font-size: 12px; color: var(--ink-60); }
.flow-step.done .step-label { color: var(--ink); }
.flow-step.current .step-label { font-weight: 600; color: var(--ink); }
.step-line { width: 20px; height: 2px; background: var(--border); margin: 0 2px; flex-shrink: 0; }

.modal-backdrop { position: fixed; inset: 0; background: rgba(28,28,30,0.4); backdrop-filter: blur(3px); z-index: 1000; display: flex; align-items: center; justify-content: center; padding: 24px; }
.modal { width: 100%; display: flex; flex-direction: column; max-height: 90vh; }
.modal-header { display: flex; align-items: center; justify-content: space-between; padding: 18px 20px 16px; border-bottom: 1px solid var(--border); }
.modal-header h2 { font-size: 17px; }
.btn-icon { background: none; border: none; cursor: pointer; color: var(--ink-60); font-size: 16px; padding: 4px 8px; border-radius: var(--radius-sm); transition: background 0.12s; }
.btn-icon:hover { background: var(--surface-alt); }
.modal-body { padding: 20px; overflow-y: auto; display: flex; flex-direction: column; gap: 14px; }
.modal-footer { padding: 14px 20px; border-top: 1px solid var(--border); display: flex; justify-content: flex-end; gap: 8px; }
</style>
