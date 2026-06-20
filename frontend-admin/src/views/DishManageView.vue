<template>
  <div>
    <div class="page-header">
      <h1>菜品管理</h1>
      <p>创建、搜索和编辑菜品，支持主食、饮料、甜点、小吃、配菜、汤品。</p>
    </div>

    <div class="toolbar">
      <button class="btn btn-yellow" @click="openCreate">+ 新增菜品</button>
      <div class="search-row">
        <input
          v-model="keyword"
          class="search-input"
          placeholder="搜索菜品名称或描述…"
          @keyup.enter="loadDishes"
        />
        <button class="btn btn-ghost" @click="loadDishes">搜索</button>
      </div>
      <button class="btn btn-ghost" style="margin-left: auto" @click="loadDishes">↻ 刷新</button>
    </div>

    <div class="filter-tabs">
      <button
        v-for="f in filters"
        :key="f.value"
        :class="['filter-tab', { active: activeFilter === f.value }]"
        @click="activeFilter = f.value; loadDishes()"
      >
        {{ f.label }}
      </button>
    </div>

    <div v-if="loading" class="loading-row">
      <div class="spinner"></div>
      <span>加载中…</span>
    </div>

    <div v-else-if="dishes.length" class="dish-grid">
      <div v-for="dish in dishes" :key="dish.id" class="dish-card card">
        <div class="dish-card-top">
          <span class="dish-type-icon">{{ typeIcon(dish.type) }}</span>
          <div class="dish-badges">
            <span :class="['status-badge', `badge-${dish.type}`]">{{ typeLabel(dish.type) }}</span>
            <span :class="['status-badge', dish.status === 1 ? 'badge-SUCCESS' : 'badge-FAILED']">
              {{ dish.status === 1 ? '上架' : '下架' }}
            </span>
          </div>
        </div>
        <div class="dish-card-body">
          <h3 class="dish-name">{{ dish.name }}</h3>
          <p v-if="dish.description" class="dish-desc">{{ dish.description }}</p>
        </div>
        <div class="dish-card-footer">
          <span class="dish-price">¥{{ Number(dish.price).toFixed(2) }}</span>
          <button class="btn btn-ghost btn-sm" @click="openEdit(dish)">编辑</button>
        </div>
      </div>
    </div>

    <div v-else class="empty-state card" style="padding: 0">
      <div class="empty-state">
        <span class="empty-icon">🍽️</span>
        <span>{{ keyword ? '未找到匹配的菜品' : '暂无菜品，点击「新增菜品」开始添加' }}</span>
      </div>
    </div>

    <Teleport to="body">
      <Transition name="fade">
        <div v-if="showModal" class="modal-backdrop" @click.self="closeModal">
          <div class="modal card" style="max-width: 480px">
            <div class="modal-header">
              <h2>{{ editingId ? '编辑菜品' : '新增菜品' }}</h2>
              <button class="btn-icon" @click="closeModal">✕</button>
            </div>
            <div class="modal-body">
              <div class="field">
                <label>菜品类型</label>
                <select v-model="form.type">
                  <option v-for="t in dishTypes" :key="t.value" :value="t.value">
                    {{ t.icon }} {{ t.label }}
                  </option>
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
              <div v-if="editingId" class="field">
                <label>上架状态</label>
                <select v-model.number="form.status">
                  <option :value="1">上架</option>
                  <option :value="0">下架</option>
                </select>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn btn-ghost" @click="closeModal">取消</button>
              <button class="btn btn-primary" :disabled="submitting" @click="submitForm">
                {{ submitting ? '提交中…' : (editingId ? '保存修改' : '创建菜品') }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, onMounted, inject } from 'vue'
import { dishApi } from '@/api'
import { DISH_TYPES, DISH_FILTERS, typeLabel, typeIcon } from '@/constants/dishTypes'

const toast = inject('toast')
const dishes = ref([])
const loading = ref(false)
const showModal = ref(false)
const submitting = ref(false)
const editingId = ref(null)
const keyword = ref('')
const activeFilter = ref('ALL')

const filters = DISH_FILTERS
const dishTypes = DISH_TYPES

const defaultForm = () => ({ type: 'MAIN_DISH', name: '', price: null, description: '', status: 1 })
const form = ref(defaultForm())

function openCreate() {
  editingId.value = null
  form.value = defaultForm()
  showModal.value = true
}

function openEdit(dish) {
  editingId.value = dish.id
  form.value = {
    type: dish.type,
    name: dish.name,
    price: Number(dish.price),
    description: dish.description || '',
    status: dish.status ?? 1,
  }
  showModal.value = true
}

function closeModal() {
  showModal.value = false
  editingId.value = null
}

async function loadDishes() {
  loading.value = true
  try {
    const params = {}
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    if (activeFilter.value !== 'ALL') params.type = activeFilter.value
    const res = await dishApi.list(params)
    if (res.code === 200) dishes.value = res.data ?? []
    else toast(res.message, 'error')
  } catch {
    toast('加载菜品失败', 'error')
  } finally {
    loading.value = false
  }
}

async function submitForm() {
  if (!form.value.name || !form.value.price) return toast('请填写菜品名称和价格', 'error')
  submitting.value = true
  try {
    const payload = {
      type: form.value.type,
      name: form.value.name,
      price: form.value.price,
      description: form.value.description,
    }
    const res = editingId.value
      ? await dishApi.update(editingId.value, { ...payload, status: form.value.status })
      : await dishApi.create(payload)
    if (res.code === 200) {
      toast(editingId.value ? `菜品「${res.data.name}」已更新` : `菜品「${res.data.name}」创建成功`)
      closeModal()
      await loadDishes()
    } else {
      toast(res.message, 'error')
    }
  } catch {
    toast(editingId.value ? '更新失败' : '创建失败', 'error')
  } finally {
    submitting.value = false
  }
}

onMounted(loadDishes)
</script>

<style scoped>
.toolbar { display: flex; align-items: center; gap: 10px; margin-bottom: 16px; flex-wrap: wrap; }
.search-row { display: flex; gap: 6px; flex: 1; min-width: 220px; max-width: 360px; }
.search-input {
  flex: 1;
  padding: 8px 12px;
  border: 1.5px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  background: var(--surface);
  color: var(--ink);
  outline: none;
}
.search-input:focus { border-color: var(--ink); }
.filter-tabs { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 24px; }
.filter-tab {
  padding: 5px 12px;
  border-radius: 99px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  background: var(--surface-alt);
  border: 1.5px solid var(--border);
  color: var(--ink-60);
  transition: all 0.12s;
}
.filter-tab.active { background: var(--ticket); border-color: var(--ticket-dark); color: var(--ink); }
.loading-row { display: flex; align-items: center; gap: 10px; color: var(--ink-60); font-size: 14px; padding: 40px 0; }
.spinner { width: 18px; height: 18px; border: 2px solid var(--border); border-top-color: var(--ink); border-radius: 50%; animation: spin 0.7s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.dish-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 16px; }
.dish-card { display: flex; flex-direction: column; transition: box-shadow 0.18s, transform 0.18s; }
.dish-card:hover { box-shadow: var(--shadow); transform: translateY(-2px); }
.dish-card-top { padding: 14px 16px 10px; display: flex; align-items: center; justify-content: space-between; }
.dish-type-icon { font-size: 28px; }
.dish-badges { display: flex; gap: 6px; flex-wrap: wrap; justify-content: flex-end; }
.dish-card-body { padding: 0 16px 12px; flex: 1; }
.dish-name { font-size: 16px; font-weight: 600; margin-bottom: 4px; }
.dish-desc { font-size: 13px; color: var(--ink-60); line-height: 1.5; }
.dish-card-footer { padding: 12px 16px; border-top: 1px solid var(--border); display: flex; align-items: center; justify-content: space-between; }
.dish-price { font-family: var(--font-display); font-size: 18px; font-weight: 700; }
.modal-backdrop { position: fixed; inset: 0; background: rgba(28,28,30,0.4); backdrop-filter: blur(3px); z-index: 1000; display: flex; align-items: center; justify-content: center; padding: 24px; }
.modal { width: 100%; display: flex; flex-direction: column; max-height: 90vh; }
.modal-header { display: flex; align-items: center; justify-content: space-between; padding: 18px 20px 16px; border-bottom: 1px solid var(--border); }
.modal-header h2 { font-size: 17px; }
.btn-icon { background: none; border: none; cursor: pointer; color: var(--ink-60); font-size: 16px; padding: 4px 8px; border-radius: var(--radius-sm); }
.btn-icon:hover { background: var(--surface-alt); }
.modal-body { padding: 20px; overflow-y: auto; display: flex; flex-direction: column; gap: 14px; }
.modal-footer { padding: 14px 20px; border-top: 1px solid var(--border); display: flex; justify-content: flex-end; gap: 8px; }
</style>
