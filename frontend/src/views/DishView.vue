<template>
  <div>
    <div class="page-header">
      <h1>菜品管理</h1>
      <p>管理上架菜品，按类型创建主食、饮料和甜点。</p>
    </div>

    <div class="toolbar">
      <button class="btn btn-yellow" @click="showCreate = true">+ 新增菜品</button>
      <div class="filter-tabs">
        <button
          v-for="f in filters"
          :key="f.value"
          :class="['filter-tab', { active: activeFilter === f.value }]"
          @click="activeFilter = f.value"
        >
          {{ f.label }}
        </button>
      </div>
      <button class="btn btn-ghost" style="margin-left: auto" @click="loadDishes">
        ↻ 刷新
      </button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-row">
      <div class="spinner"></div>
      <span>加载中…</span>
    </div>

    <!-- Grid -->
    <div v-else-if="filteredDishes.length" class="dish-grid">
      <div v-for="dish in filteredDishes" :key="dish.id" class="dish-card card">
        <div class="dish-card-top">
          <span class="dish-type-icon">{{ typeIcon(dish.type) }}</span>
          <span :class="['status-badge', `badge-${dish.type}`]">
            {{ typeLabel(dish.type) }}
          </span>
        </div>
        <div class="dish-card-body">
          <h3 class="dish-name">{{ dish.name }}</h3>
          <p v-if="dish.description" class="dish-desc">{{ dish.description }}</p>
        </div>
        <div class="dish-card-footer">
          <span class="dish-price">¥{{ dish.price?.toFixed(2) }}</span>
          <span class="mono dish-id">ID: {{ dish.id }}</span>
        </div>
      </div>
    </div>

    <!-- Empty -->
    <div v-else class="empty-state card" style="padding: 0;">
      <div class="empty-state">
        <span class="empty-icon">🍽️</span>
        <span>暂无菜品，点击「新增菜品」开始添加</span>
      </div>
    </div>

    <!-- Create Modal -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="showCreate" class="modal-backdrop" @click.self="showCreate = false">
          <div class="modal card" style="max-width: 460px">
            <div class="modal-header">
              <h2>新增菜品</h2>
              <button class="btn-icon" @click="showCreate = false">✕</button>
            </div>
            <div class="modal-body">
              <div class="field">
                <label>菜品类型</label>
                <select v-model="form.type">
                  <option value="MAIN_DISH">主食</option>
                  <option value="BEVERAGE">饮料</option>
                  <option value="DESSERT">甜点</option>
                </select>
              </div>
              <div class="field">
                <label>菜品名称</label>
                <input v-model="form.name" placeholder="如：红烧肉盖饭" />
              </div>
              <div class="field">
                <label>价格（元）</label>
                <input v-model.number="form.price" type="number" step="0.01" placeholder="28.00" />
              </div>
              <div class="field">
                <label>描述（选填）</label>
                <textarea v-model="form.description" placeholder="经典红烧肉配米饭" rows="2"></textarea>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn btn-ghost" @click="showCreate = false">取消</button>
              <button class="btn btn-primary" :disabled="submitting" @click="createDish">
                {{ submitting ? '提交中…' : '创建菜品' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, inject } from 'vue'
import { dishApi } from '@/api'

const toast = inject('toast')

const dishes = ref([])
const loading = ref(false)
const showCreate = ref(false)
const submitting = ref(false)
const activeFilter = ref('ALL')

const filters = [
  { value: 'ALL', label: '全部' },
  { value: 'MAIN_DISH', label: '主食' },
  { value: 'BEVERAGE', label: '饮料' },
  { value: 'DESSERT', label: '甜点' },
]

const form = ref({ type: 'MAIN_DISH', name: '', price: null, description: '' })

const filteredDishes = computed(() =>
  activeFilter.value === 'ALL'
    ? dishes.value
    : dishes.value.filter((d) => d.type === activeFilter.value),
)

function typeLabel(t) {
  return { MAIN_DISH: '主食', BEVERAGE: '饮料', DESSERT: '甜点' }[t] ?? t
}

function typeIcon(t) {
  return { MAIN_DISH: '🍚', BEVERAGE: '🥤', DESSERT: '🍰' }[t] ?? '🍽️'
}

async function loadDishes() {
  loading.value = true
  try {
    const res = await dishApi.list()
    if (res.code === 200) dishes.value = res.data ?? []
    else toast(res.message, 'error')
  } catch {
    toast('加载菜品失败', 'error')
  } finally {
    loading.value = false
  }
}

async function createDish() {
  if (!form.value.name || !form.value.price) return toast('请填写菜品名称和价格', 'error')
  submitting.value = true
  try {
    const res = await dishApi.create(form.value)
    if (res.code === 200) {
      toast(`菜品「${res.data.name}」创建成功`)
      dishes.value.unshift(res.data)
      showCreate.value = false
      form.value = { type: 'MAIN_DISH', name: '', price: null, description: '' }
    } else {
      toast(res.message, 'error')
    }
  } catch {
    toast('创建失败', 'error')
  } finally {
    submitting.value = false
  }
}

onMounted(loadDishes)
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.filter-tabs {
  display: flex;
  background: var(--surface-alt);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  padding: 3px;
  gap: 2px;
}

.filter-tab {
  padding: 5px 12px;
  border-radius: 3px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  background: none;
  border: none;
  color: var(--ink-60);
  transition: background 0.12s, color 0.12s;
}

.filter-tab.active {
  background: var(--surface);
  color: var(--ink);
  box-shadow: var(--shadow-sm);
}

.loading-row {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--ink-60);
  font-size: 14px;
  padding: 40px 0;
}

.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid var(--border);
  border-top-color: var(--ink);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.dish-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}

.dish-card {
  display: flex;
  flex-direction: column;
  gap: 0;
  transition: box-shadow 0.18s, transform 0.18s;
}
.dish-card:hover { box-shadow: var(--shadow); transform: translateY(-2px); }

.dish-card-top {
  padding: 14px 16px 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.dish-type-icon { font-size: 28px; }

.dish-card-body {
  padding: 0 16px 12px;
  flex: 1;
}

.dish-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
}

.dish-desc {
  font-size: 13px;
  color: var(--ink-60);
  line-height: 1.5;
}

.dish-card-footer {
  padding: 12px 16px;
  border-top: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.dish-price {
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 700;
  color: var(--ink);
}

.dish-id {
  font-size: 11px;
  color: var(--ink-30);
}

/* Modal shared */
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
  display: flex;
  flex-direction: column;
  max-height: 90vh;
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
</style>
