<template>
  <div>
    <div class="page-header">
      <h1>订单管理</h1>
      <p>查看、创建订单，跟踪每一张出票从下单到送达的全过程。</p>
    </div>

    <div class="toolbar">
      <button class="btn btn-yellow" @click="showCreate = true">+ 新建订单</button>
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
    </div>

    <!-- Single order result -->
    <Transition name="fade">
      <div v-if="queriedOrder" class="section-block">
        <div class="section-label">查询结果</div>
        <OrderTicket :order="queriedOrder" @action="doAction" @pay="openPay" />
      </div>
    </Transition>

    <!-- Demo orders panel -->
    <div class="demo-note card">
      <div class="demo-note-inner" v-if="!queriedOrder">
        <span class="demo-note-icon">💡</span>
        <div>
          <p class="demo-note-title">如何使用</p>
          <p class="demo-note-body">通过 <strong>新建订单</strong> 创建一笔订单，获得订单 ID 后在查询框中输入即可查看详情并进行接单、配送、完成等操作。也可直接在 <strong>菜品管理</strong> 中先创建菜品。</p>
        </div>
      </div>
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
            <span v-if="!ordersByStatus[step.status]?.length" class="flow-board-empty">暂无订单</span>
          </div>
        </div>
        <div class="flow-board-cancel">
          <span class="flow-cancel-arrow">↕ cancel（任意阶段可取消）</span>
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

    <!-- Create Order Modal -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="showCreate" class="modal-backdrop" @click.self="showCreate = false">
          <div class="modal card">
            <div class="modal-header">
              <h2>新建订单</h2>
              <button class="btn-icon" @click="showCreate = false">✕</button>
            </div>
            <div class="modal-body">
              <div class="field">
                <label>用户 ID</label>
                <input v-model.number="form.userId" type="number" placeholder="e.g. 1" />
              </div>
              <div class="field">
                <label>订单备注</label>
                <input v-model="form.remark" placeholder="不要辣、少盐……" />
              </div>

              <div class="items-section">
                <div class="items-header">
                  <span class="section-label">订单项</span>
                  <button class="btn btn-ghost btn-sm" @click="addItem">+ 添加菜品</button>
                </div>

                <div v-for="(item, i) in form.items" :key="i" class="order-item-row card">
                  <div class="item-row-grid">
                    <div class="field">
                      <label>菜品 ID</label>
                      <input v-model.number="item.dishId" type="number" placeholder="1" />
                    </div>
                    <div class="field">
                      <label>菜品名称</label>
                      <input v-model="item.dishName" placeholder="红烧肉盖饭" />
                    </div>
                    <div class="field">
                      <label>单价</label>
                      <input v-model.number="item.price" type="number" step="0.01" placeholder="28.00" />
                    </div>
                    <div class="field">
                      <label>数量</label>
                      <input v-model.number="item.quantity" type="number" min="1" placeholder="1" />
                    </div>
                    <div class="field">
                      <label>规格</label>
                      <select v-model="item.size">
                        <option value="LARGE">大份</option>
                        <option value="SMALL">小份</option>
                      </select>
                    </div>
                    <div class="field">
                      <label>加料（逗号分隔）</label>
                      <input v-model="item.extrasRaw" placeholder="加辣,加蛋" />
                    </div>
                  </div>
                  <div class="field" style="margin-top:6px">
                    <label>单项备注</label>
                    <input v-model="item.note" placeholder="不要香菜" />
                  </div>
                  <button class="btn btn-danger btn-sm remove-btn" @click="removeItem(i)">移除</button>
                </div>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn btn-ghost" @click="showCreate = false">取消</button>
              <button class="btn btn-primary" :disabled="submitting" @click="createOrder">
                {{ submitting ? '提交中…' : '创建订单' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- Pay Modal -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="showPay" class="modal-backdrop" @click.self="showPay = false">
          <div class="modal card" style="max-width: 400px">
            <div class="modal-header">
              <h2>发起支付</h2>
              <button class="btn-icon" @click="showPay = false">✕</button>
            </div>
            <div class="modal-body">
              <div class="field">
                <label>订单 ID</label>
                <input :value="payForm.orderId" readonly class="mono" />
              </div>
              <div class="field">
                <label>支付金额</label>
                <input v-model.number="payForm.amount" type="number" step="0.01" />
              </div>
              <div class="field">
                <label>支付方式</label>
                <select v-model="payForm.method">
                  <option value="WECHAT">微信支付</option>
                  <option value="ALIPAY">支付宝</option>
                </select>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn btn-ghost" @click="showPay = false">取消</button>
              <button class="btn btn-yellow" :disabled="submitting" @click="submitPay">
                {{ submitting ? '支付中…' : '确认支付' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, inject, computed, onMounted } from 'vue'
import { orderApi, paymentApi } from '@/api'
import OrderTicket from '@/components/OrderTicket.vue'

const toast = inject('toast')

const queryId = ref('')
const queriedOrder = ref(null)
const allOrders = ref([])
const showCreate = ref(false)
const showPay = ref(false)
const submitting = ref(false)

const flowSteps = [
  { status: 'PENDING_PAYMENT', label: '待支付' },
  { status: 'PAID', label: '已支付' },
  { status: 'PREPARING', label: '制作中' },
  { status: 'DELIVERING', label: '配送中' },
  { status: 'COMPLETED', label: '已完成' },
]

const ordersByStatus = computed(() => {
  const grouped = Object.fromEntries(flowSteps.map((s) => [s.status, []]))
  for (const order of allOrders.value) {
    if (grouped[order.status]) grouped[order.status].push(order)
  }
  return grouped
})

const cancelledOrders = computed(() =>
  allOrders.value.filter((o) => o.status === 'CANCELLED'),
)

function formatAmount(amount) {
  return Number(amount ?? 0).toFixed(2)
}

async function fetchOrdersList() {
  try {
    const res = await orderApi.list()
    if (res.code === 200) {
      allOrders.value = res.data ?? []
    } else {
      toast(res.message || '加载订单列表失败', 'error')
    }
  } catch {
    toast('加载订单列表失败，请确认后端已启动', 'error')
  }
}

async function selectOrder(id) {
  queryId.value = id
  await fetchOrder()
}

const defaultItem = () => ({
  dishId: null,
  dishName: '',
  price: null,
  quantity: 1,
  size: 'LARGE',
  extrasRaw: '',
  note: '',
})

const form = ref({ userId: null, remark: '', items: [defaultItem()] })
const payForm = ref({ orderId: null, amount: null, method: 'WECHAT' })

function addItem() { form.value.items.push(defaultItem()) }
function removeItem(i) { form.value.items.splice(i, 1) }

async function fetchOrder() {
  if (!queryId.value) return
  try {
    const res = await orderApi.get(queryId.value)
    if (res.code === 200) {
      queriedOrder.value = res.data
    } else {
      toast(res.message, 'error')
    }
  } catch (e) {
    toast('订单不存在或请求失败', 'error')
  }
}

async function createOrder() {
  if (!form.value.userId || !form.value.items.length) {
    return toast('请填写用户 ID 并至少添加一个菜品', 'error')
  }
  submitting.value = true
  try {
    const payload = {
      remark: form.value.remark,
      items: form.value.items.map((it) => ({
        dishId: it.dishId,
        dishName: it.dishName,
        price: it.price,
        quantity: it.quantity,
        size: it.size,
        extras: it.extrasRaw ? it.extrasRaw.split(',').map((s) => s.trim()).filter(Boolean) : [],
        note: it.note,
      })),
    }
    const res = await orderApi.create(form.value.userId, payload)
    if (res.code === 200) {
      toast(`订单 #${res.data.id} 创建成功`)
      queriedOrder.value = res.data
      queryId.value = res.data.id
      showCreate.value = false
      form.value = { userId: null, remark: '', items: [defaultItem()] }
      await fetchOrdersList()
    } else {
      toast(res.message, 'error')
    }
  } catch {
    toast('创建失败，请检查参数', 'error')
  } finally {
    submitting.value = false
  }
}

function openPay(order) {
  payForm.value = { orderId: order.id, amount: order.totalAmount, method: 'WECHAT' }
  showPay.value = true
}

async function submitPay() {
  submitting.value = true
  try {
    const res = await paymentApi.pay(payForm.value)
    if (res.code === 200) {
      toast(`支付${res.data.status === 'SUCCESS' ? '成功' : '失败'} — ${res.data.transactionId}`)
      showPay.value = false
      await fetchOrder()
      await fetchOrdersList()
    } else {
      toast(res.message, 'error')
    }
  } catch {
    toast('支付请求失败', 'error')
  } finally {
    submitting.value = false
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

.search-row {
  display: flex;
  gap: 6px;
  margin-left: auto;
}

.id-input {
  padding: 8px 12px;
  border: 1.5px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  background: var(--surface);
  color: var(--ink);
  outline: none;
  width: 180px;
  transition: border-color 0.15s;
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

/* Demo note */
.demo-note {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.demo-note-inner {
  display: flex;
  gap: 14px;
}

.demo-note-icon { font-size: 28px; flex-shrink: 0; }
.demo-note-title { font-weight: 600; margin-bottom: 4px; }
.demo-note-body { font-size: 14px; color: var(--ink-60); line-height: 1.6; }

.flow-board-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.flow-board {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 10px;
  position: relative;
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

.flow-board-arrow {
  color: var(--ink-30);
  font-size: 13px;
  flex-shrink: 0;
}

.flow-board-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}

.flow-board-empty {
  font-size: 12px;
  color: var(--ink-30);
  padding: 8px 0;
}

.flow-board-cancel {
  grid-column: 1 / -1;
  margin-top: 2px;
}

.flow-cancel-arrow { font-size: 12px; color: var(--status-cancelled); font-family: var(--font-mono); }

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

.order-chip:hover {
  border-color: var(--ink-30);
  background: var(--surface);
}

.order-chip.active {
  border-color: var(--ink);
  background: #FFF9E6;
}

.order-chip-id {
  font-size: 12px;
  font-weight: 600;
  color: var(--ink);
}

.order-chip-amount {
  font-size: 12px;
  color: var(--ink-60);
}

.order-chip-cancelled {
  opacity: 0.75;
}

.flow-cancelled {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding-top: 4px;
}

.flow-cancelled-label {
  font-size: 11px;
  font-weight: 600;
  color: var(--status-cancelled);
  font-family: var(--font-mono);
  flex-shrink: 0;
  padding-top: 8px;
}

.flow-cancelled-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  flex: 1;
}

.flow-cancelled-list .order-chip {
  width: auto;
  min-width: 120px;
}

@media (max-width: 900px) {
  .flow-board {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .flow-board-arrow {
    display: none;
  }
}

/* Modal */
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(28,28,30,0.4);
  backdrop-filter: blur(3px);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.modal {
  width: 100%;
  max-width: 600px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 20px 16px;
  border-bottom: 1px solid var(--border);
}

.modal-header h2 { font-size: 17px; }

.btn-icon {
  background: none;
  border: none;
  cursor: pointer;
  color: var(--ink-60);
  font-size: 16px;
  padding: 4px 8px;
  border-radius: var(--radius-sm);
  transition: background 0.12s;
}
.btn-icon:hover { background: var(--surface-alt); }

.modal-body {
  padding: 20px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.modal-footer {
  padding: 14px 20px;
  border-top: 1px solid var(--border);
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.items-section { display: flex; flex-direction: column; gap: 10px; }
.items-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 2px; }

.order-item-row {
  padding: 14px;
  position: relative;
}

.item-row-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.remove-btn {
  margin-top: 8px;
  align-self: flex-start;
}

/* Flow steps in modal */
.flow-step-inline { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
</style>
