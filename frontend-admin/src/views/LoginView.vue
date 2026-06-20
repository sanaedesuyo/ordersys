<template>
  <AuthLayout
    title="商家后台"
    subtitle="登录后管理订单、菜品与支付流水"
    footer="演示账号：18888888888 / password123"
  >
    <form class="auth-form" @submit.prevent="handleLogin">
      <div class="field">
        <label>手机号</label>
        <input
          v-model="form.phone"
          type="tel"
          autocomplete="username"
          placeholder="请输入商家手机号"
          required
        />
      </div>

      <div class="field">
        <label>密码</label>
        <div class="password-wrap">
          <input
            v-model="form.password"
            :type="showPassword ? 'text' : 'password'"
            autocomplete="current-password"
            placeholder="请输入密码"
            required
          />
          <button type="button" class="toggle-pwd" @click="showPassword = !showPassword">
            {{ showPassword ? '隐藏' : '显示' }}
          </button>
        </div>
      </div>

      <p v-if="error" class="error-msg">{{ error }}</p>

      <button type="submit" class="btn btn-primary btn-full" :disabled="loading">
        {{ loading ? '登录中…' : '登录后台' }}
      </button>

      <button type="button" class="btn btn-ghost btn-full" @click="fillDemo">
        填入演示账号
      </button>
    </form>
  </AuthLayout>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/useAuthStore'
import { authApi } from '@/api'
import AuthLayout from '@/components/AuthLayout.vue'

const router = useRouter()
const authStore = useAuthStore()

const form = ref({ phone: '', password: '' })
const loading = ref(false)
const error = ref('')
const showPassword = ref(false)

function fillDemo() {
  form.value = { phone: '18888888888', password: 'password123' }
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
      router.replace('/orders')
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

.password-wrap {
  display: flex;
  gap: 8px;
  align-items: center;
}

.password-wrap input {
  flex: 1;
}

.toggle-pwd {
  flex-shrink: 0;
  border: none;
  background: var(--surface-alt);
  color: var(--ink-60);
  font-size: 12px;
  padding: 8px 10px;
  border-radius: var(--radius-sm);
  cursor: pointer;
}

.toggle-pwd:hover {
  color: var(--ink);
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
</style>
