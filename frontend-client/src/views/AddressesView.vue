<template>
  <div>
    <div class="page-header">
      <h1>我的地址</h1>
      <p>可保存多个收货地址，下单时选择使用。</p>
    </div>

    <div v-if="loading" class="loading-row">
      <div class="spinner"></div><span>加载中…</span>
    </div>

    <template v-else>
      <div v-if="addresses.length === 0" class="empty-state card">
        <span class="empty-icon">📍</span>
        <span>还没有收货地址</span>
        <button class="btn btn-yellow btn-sm" style="margin-top: 8px" @click="openForm()">
          添加第一个地址
        </button>
      </div>

      <div v-else class="address-list">
        <div v-for="addr in addresses" :key="addr.id" class="address-card card">
          <div class="address-main">
            <div class="address-head">
              <span v-if="addr.label" class="address-label">{{ addr.label }}</span>
              <span v-if="addr.isDefault" class="default-badge">默认</span>
            </div>
            <p class="address-detail">{{ addr.detail }}</p>
          </div>
          <div class="address-actions">
            <button
              v-if="!addr.isDefault"
              class="btn btn-ghost btn-sm"
              @click="makeDefault(addr.id)"
            >
              设为默认
            </button>
            <button class="btn btn-ghost btn-sm" @click="openForm(addr)">编辑</button>
            <button class="btn btn-ghost btn-sm danger-text" @click="removeAddress(addr.id)">
              删除
            </button>
          </div>
        </div>

        <button class="btn btn-yellow add-btn" @click="openForm()">+ 新增地址</button>
      </div>
    </template>

    <Teleport to="body">
      <div v-if="showForm" class="modal-overlay" @click.self="closeForm">
        <div class="modal card">
          <h2 class="modal-title">{{ editingId ? '编辑地址' : '新增地址' }}</h2>
          <form class="address-form" @submit.prevent="saveAddress">
            <div class="field">
              <label>标签（选填）</label>
              <input v-model="form.label" placeholder="如：家、公司" />
            </div>
            <div class="field">
              <label>详细地址</label>
              <input v-model="form.detail" placeholder="街道、门牌号等" required />
            </div>
            <label class="checkbox-row">
              <input v-model="form.isDefault" type="checkbox" />
              设为默认地址
            </label>
            <p v-if="formError" class="error-msg">{{ formError }}</p>
            <div class="modal-actions">
              <button type="button" class="btn btn-ghost" @click="closeForm">取消</button>
              <button type="submit" class="btn btn-yellow" :disabled="saving">
                {{ saving ? '保存中…' : '保存' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { inject, onMounted, ref } from 'vue'
import { addressApi } from '@/api'

const toast = inject('toast')

const addresses = ref([])
const loading = ref(true)
const showForm = ref(false)
const editingId = ref(null)
const saving = ref(false)
const formError = ref('')
const form = ref({ label: '', detail: '', isDefault: false })

onMounted(loadAddresses)

async function loadAddresses() {
  loading.value = true
  try {
    const res = await addressApi.list()
    if (res.code === 200) {
      addresses.value = res.data || []
    } else {
      toast(res.message || '加载失败', 'error')
    }
  } catch {
    toast('加载地址失败', 'error')
  } finally {
    loading.value = false
  }
}

function openForm(addr = null) {
  editingId.value = addr?.id ?? null
  form.value = {
    label: addr?.label || '',
    detail: addr?.detail || '',
    isDefault: addr?.isDefault ?? addresses.value.length === 0,
  }
  formError.value = ''
  showForm.value = true
}

function closeForm() {
  showForm.value = false
  editingId.value = null
}

async function saveAddress() {
  if (!form.value.detail?.trim()) {
    formError.value = '请填写详细地址'
    return
  }
  saving.value = true
  formError.value = ''
  const payload = {
    label: form.value.label?.trim() || '',
    detail: form.value.detail.trim(),
    isDefault: form.value.isDefault,
  }
  try {
    const res = editingId.value
      ? await addressApi.update(editingId.value, payload)
      : await addressApi.create(payload)
    if (res.code === 200) {
      toast(editingId.value ? '地址已更新' : '地址已添加')
      closeForm()
      await loadAddresses()
    } else {
      formError.value = res.message || '保存失败'
    }
  } catch {
    formError.value = '保存失败，请重试'
  } finally {
    saving.value = false
  }
}

async function makeDefault(id) {
  try {
    const res = await addressApi.setDefault(id)
    if (res.code === 200) {
      toast('已设为默认地址')
      await loadAddresses()
    } else {
      toast(res.message || '操作失败', 'error')
    }
  } catch {
    toast('操作失败', 'error')
  }
}

async function removeAddress(id) {
  if (!confirm('确定删除这个地址吗？')) return
  try {
    const res = await addressApi.remove(id)
    if (res.code === 200) {
      toast('地址已删除')
      await loadAddresses()
    } else {
      toast(res.message || '删除失败', 'error')
    }
  } catch {
    toast('删除失败', 'error')
  }
}
</script>

<style scoped>
.address-list { display: flex; flex-direction: column; gap: 12px; }
.address-card {
  padding: 16px 18px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}
.address-head { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.address-label { font-weight: 600; font-size: 14px; }
.default-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 99px;
  background: var(--ticket);
  color: var(--ink);
  font-weight: 600;
}
.address-detail { font-size: 14px; color: var(--ink-60); line-height: 1.5; }
.address-actions { display: flex; flex-wrap: wrap; gap: 6px; flex-shrink: 0; }
.danger-text { color: var(--status-cancelled); }
.add-btn { width: 100%; justify-content: center; margin-top: 4px; }

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(28, 28, 30, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
  padding: 20px;
}
.modal { width: 100%; max-width: 420px; padding: 24px; }
.modal-title { font-size: 18px; margin-bottom: 16px; }
.address-form { display: flex; flex-direction: column; gap: 14px; }
.checkbox-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--ink-60);
}
.modal-actions { display: flex; justify-content: flex-end; gap: 8px; margin-top: 4px; }
.error-msg { font-size: 13px; color: var(--status-cancelled); }

@media (max-width: 640px) {
  .address-card { flex-direction: column; }
  .address-actions { width: 100%; }
}
</style>
