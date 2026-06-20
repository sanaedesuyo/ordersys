<template>
  <AuthLayout
    title="欢迎回来"
    subtitle="登录后即可下单、支付并查看订单"
    footer="演示账号：13800138001 / password123"
  >
    <form class="auth-form" @submit.prevent="handleLogin">
      <div class="field">
        <label>手机号</label>
        <input
          v-model="form.phone"
          type="tel"
          autocomplete="username"
          placeholder="请输入手机号"
          required
        />
      </div>

      <div class="field">
        <label>密码</label>
        <PasswordInput
          v-model="form.password"
          autocomplete="current-password"
          placeholder="请输入密码"
          required
        />
      </div>

      <p v-if="error" class="error-msg">{{ error }}</p>

      <button type="submit" class="btn btn-primary btn-full" :disabled="loading">
        {{ loading ? '登录中…' : '登录' }}
      </button>

      <button type="button" class="btn btn-ghost btn-full" @click="fillDemo">
        填入演示账号
      </button>
    </form>

    <p class="auth-switch">
      还没有账号？
      <RouterLink to="/register">立即注册</RouterLink>
    </p>
  </AuthLayout>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/useAuthStore'
import { authApi } from '@/api'
import AuthLayout from '@/components/AuthLayout.vue'
import PasswordInput from '@/components/PasswordInput.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const form = ref({ phone: '', password: '' })
const loading = ref(false)
const error = ref('')

function fillDemo() {
  form.value = { phone: '13800138001', password: 'password123' }
  error.value = ''
}

function extractError(err) {
  const data = err.response?.data
  if (data && typeof data === 'object' && data.message) return data.message
  if (typeof data === 'string') {
    try {
      return JSON.parse(data).message || data
    } catch {
      return data
    }
  }
  return '网络错误，请确认后端已启动并已执行数据库迁移'
}

async function handleLogin() {
  if (!form.value.phone || !form.value.password) {
    error.value = '请填写手机号和密码'
    return
  }

  loading.value = true
  error.value = ''
  try {
    const res = await authApi.login(form.value)
    if (res.code === 200) {
      authStore.setAuth(res.data)
      const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/menu'
      router.replace(redirect)
    } else {
      error.value = res.message || '登录失败'
    }
  } catch (err) {
    error.value = extractError(err)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.btn-full {
  width: 100%;
  justify-content: center;
  padding: 10px;
  font-size: 15px;
}

.error-msg {
  font-size: 13px;
  color: var(--status-cancelled);
  line-height: 1.5;
}

.auth-switch {
  margin-top: 16px;
  text-align: center;
  font-size: 13px;
  color: var(--ink-60);
}

.auth-switch a {
  color: var(--ink);
  font-weight: 600;
  text-decoration: none;
}

.auth-switch a:hover {
  text-decoration: underline;
}
</style>
