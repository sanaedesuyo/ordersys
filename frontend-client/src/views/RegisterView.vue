<template>
  <AuthLayout
    title="创建账号"
    subtitle="注册后即可浏览菜单、下单并跟踪配送"
  >
    <form class="auth-form" @submit.prevent="handleRegister">
      <div class="field">
        <label>姓名</label>
        <input v-model="form.name" autocomplete="name" placeholder="请输入姓名" required />
      </div>

      <div class="field">
        <label>手机号</label>
        <input
          v-model="form.phone"
          type="tel"
          autocomplete="tel"
          placeholder="将作为登录账号"
          required
        />
      </div>

      <div class="field">
        <label>密码</label>
        <PasswordInput
          v-model="form.password"
          autocomplete="new-password"
          placeholder="至少 6 位"
          required
          :minlength="6"
        />
      </div>

      <div class="field">
        <label>确认密码</label>
        <PasswordInput
          v-model="form.confirmPassword"
          autocomplete="new-password"
          placeholder="再次输入密码"
          required
          :minlength="6"
        />
      </div>

      <p v-if="error" class="error-msg">{{ error }}</p>

      <button type="submit" class="btn btn-yellow btn-full" :disabled="loading">
        {{ loading ? '注册中…' : '立即注册' }}
      </button>
    </form>

    <p class="auth-switch">
      已有账号？
      <RouterLink to="/login">返回登录</RouterLink>
    </p>
  </AuthLayout>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/useAuthStore'
import { authApi } from '@/api'
import AuthLayout from '@/components/AuthLayout.vue'
import PasswordInput from '@/components/PasswordInput.vue'

const router = useRouter()
const authStore = useAuthStore()

const form = ref({ name: '', phone: '', password: '', confirmPassword: '' })
const loading = ref(false)
const error = ref('')

async function handleRegister() {
  if (!form.value.name?.trim()) {
    error.value = '请填写姓名'
    return
  }
  if (!form.value.phone?.trim()) {
    error.value = '请填写手机号'
    return
  }
  if (!form.value.password || form.value.password.length < 6) {
    error.value = '密码至少 6 位'
    return
  }
  if (form.value.password !== form.value.confirmPassword) {
    error.value = '两次输入的密码不一致'
    return
  }

  loading.value = true
  error.value = ''
  try {
    const res = await authApi.register({
      name: form.value.name.trim(),
      phone: form.value.phone.trim(),
      password: form.value.password,
    })
    if (res.code === 200) {
      authStore.setAuth(res.data)
      router.replace('/menu')
    } else {
      error.value = res.message || '注册失败'
    }
  } catch (err) {
    error.value = extractError(err)
  } finally {
    loading.value = false
  }
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
