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
        <div class="password-wrap">
          <input
            v-model="form.password"
            :type="showPassword ? 'text' : 'password'"
            autocomplete="new-password"
            placeholder="至少 6 位"
            required
            minlength="6"
          />
          <button type="button" class="toggle-pwd" @click="showPassword = !showPassword">
            {{ showPassword ? '隐藏' : '显示' }}
          </button>
        </div>
      </div>

      <div class="field">
        <label>收货地址（选填）</label>
        <input
          v-model="form.address"
          autocomplete="street-address"
          placeholder="如：北京市朝阳区XXX路1号"
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

const router = useRouter()
const authStore = useAuthStore()

const form = ref({ name: '', phone: '', password: '', address: '' })
const loading = ref(false)
const error = ref('')
const showPassword = ref(false)

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

  loading.value = true
  error.value = ''
  try {
    const res = await authApi.register({
      name: form.value.name.trim(),
      phone: form.value.phone.trim(),
      password: form.value.password,
      address: form.value.address?.trim() || '',
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
