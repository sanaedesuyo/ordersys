<template>
  <div>
    <div class="page-header">
      <h1>用户管理</h1>
      <p>注册新用户，或通过 ID 查询用户信息。</p>
    </div>

    <div class="user-layout">
      <!-- Create Form -->
      <div class="panel card">
        <div class="panel-header">
          <h2>注册用户</h2>
        </div>
        <div class="panel-body">
          <div class="field">
            <label>姓名 *</label>
            <input v-model="form.name" placeholder="张三" />
          </div>
          <div class="field">
            <label>手机号</label>
            <input v-model="form.phone" placeholder="138 0000 0000" />
          </div>
          <div class="field">
            <label>收货地址</label>
            <textarea v-model="form.address" placeholder="北京市朝阳区…" rows="2"></textarea>
          </div>
          <button class="btn btn-primary" style="align-self: flex-end" :disabled="submitting" @click="createUser">
            {{ submitting ? '提交中…' : '创建用户' }}
          </button>
        </div>
      </div>

      <!-- Query + Result -->
      <div class="panel-right">
        <div class="panel card">
          <div class="panel-header">
            <h2>查询用户</h2>
          </div>
          <div class="panel-body">
            <div style="display: flex; gap: 8px">
              <div class="field" style="flex: 1">
                <label>用户 ID</label>
                <input v-model.number="queryId" type="number" placeholder="1" @keyup.enter="fetchUser" />
              </div>
              <button class="btn btn-ghost" style="align-self: flex-end" @click="fetchUser">查询</button>
            </div>
          </div>
        </div>

        <!-- User Result -->
        <Transition name="fade">
          <div v-if="queriedUser" class="user-result card">
            <div class="user-avatar">
              {{ queriedUser.name?.[0]?.toUpperCase() }}
            </div>
            <div class="user-details">
              <h3 class="user-name">{{ queriedUser.name }}</h3>
              <div class="user-meta">
                <div class="user-meta-item">
                  <span class="meta-label">用户 ID</span>
                  <span class="mono">{{ queriedUser.id }}</span>
                </div>
                <div v-if="queriedUser.phone" class="user-meta-item">
                  <span class="meta-label">手机号</span>
                  <span>{{ queriedUser.phone }}</span>
                </div>
                <div v-if="queriedUser.address" class="user-meta-item">
                  <span class="meta-label">地址</span>
                  <span>{{ queriedUser.address }}</span>
                </div>
                <div class="user-meta-item">
                  <span class="meta-label">注册时间</span>
                  <span class="mono small">{{ formatTime(queriedUser.createTime) }}</span>
                </div>
              </div>
            </div>
          </div>
        </Transition>

        <Transition name="fade">
          <div v-if="userNotFound" class="not-found card">
            <span class="not-found-icon">🔍</span>
            <span>用户不存在</span>
          </div>
        </Transition>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, inject } from 'vue'
import { userApi } from '@/api'

const toast = inject('toast')

const form = ref({ name: '', phone: '', address: '' })
const submitting = ref(false)
const queryId = ref(null)
const queriedUser = ref(null)
const userNotFound = ref(false)

function formatTime(t) {
  if (!t) return '—'
  return t.replace('T', ' ').slice(0, 16)
}

async function createUser() {
  if (!form.value.name) return toast('请填写姓名', 'error')
  submitting.value = true
  try {
    const res = await userApi.create(form.value)
    if (res.code === 200) {
      toast(`用户「${res.data.name}」创建成功，ID: ${res.data.id}`)
      queriedUser.value = res.data
      userNotFound.value = false
      queryId.value = res.data.id
      form.value = { name: '', phone: '', address: '' }
    } else {
      toast(res.message, 'error')
    }
  } catch {
    toast('创建失败', 'error')
  } finally {
    submitting.value = false
  }
}

async function fetchUser() {
  if (!queryId.value) return
  queriedUser.value = null
  userNotFound.value = false
  try {
    const res = await userApi.get(queryId.value)
    if (res.code === 200 && res.data) {
      queriedUser.value = res.data
    } else {
      userNotFound.value = true
    }
  } catch {
    userNotFound.value = true
  }
}
</script>

<style scoped>
.user-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  align-items: start;
}

@media (max-width: 700px) {
  .user-layout { grid-template-columns: 1fr; }
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

.panel-right {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* User result card */
.user-result {
  padding: 20px;
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.user-avatar {
  width: 52px;
  height: 52px;
  background: var(--ticket);
  border-radius: var(--radius);
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 700;
  flex-shrink: 0;
  color: var(--ink);
}

.user-details { flex: 1; }

.user-name {
  font-size: 18px;
  margin-bottom: 10px;
}

.user-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.user-meta-item {
  display: flex;
  gap: 8px;
  align-items: baseline;
  font-size: 14px;
}

.meta-label {
  font-size: 10px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-weight: 600;
  color: var(--ink-60);
  min-width: 54px;
  flex-shrink: 0;
}

.small { font-size: 12px; }

/* Not found */
.not-found {
  padding: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: var(--ink-60);
  font-size: 14px;
}
.not-found-icon { font-size: 22px; }
</style>
