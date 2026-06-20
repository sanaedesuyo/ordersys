<template>
  <div>
    <div class="page-header">
      <h1>账户设置</h1>
      <p>管理你的姓名、手机号和登录密码。</p>
    </div>

    <div v-if="loading" class="loading-row">
      <div class="spinner"></div><span>加载中…</span>
    </div>

    <template v-else>
      <div class="profile-layout">
        <section class="card profile-section">
          <h2 class="section-title">基本信息</h2>
          <form class="profile-form" @submit.prevent="saveProfile">
            <div class="field">
              <label>姓名</label>
              <input v-model="profileForm.name" autocomplete="name" placeholder="请输入姓名" required />
            </div>
            <div class="field">
              <label>手机号</label>
              <input
                v-model="profileForm.phone"
                type="tel"
                autocomplete="tel"
                placeholder="登录账号"
                required
              />
            </div>
            <p v-if="profileError" class="error-msg">{{ profileError }}</p>
            <button type="submit" class="btn btn-yellow" :disabled="savingProfile">
              {{ savingProfile ? '保存中…' : '保存信息' }}
            </button>
          </form>
        </section>

        <section class="card profile-section">
          <h2 class="section-title">修改密码</h2>
          <form class="profile-form" @submit.prevent="savePassword">
            <div class="field">
              <label>当前密码</label>
              <PasswordInput
                v-model="passwordForm.currentPassword"
                autocomplete="current-password"
                placeholder="请输入当前密码"
                required
              />
            </div>
            <div class="field">
              <label>新密码</label>
              <PasswordInput
                v-model="passwordForm.newPassword"
                autocomplete="new-password"
                placeholder="至少 6 位"
                required
                :minlength="6"
              />
            </div>
            <div class="field">
              <label>确认新密码</label>
              <PasswordInput
                v-model="passwordForm.confirmPassword"
                autocomplete="new-password"
                placeholder="再次输入新密码"
                required
                :minlength="6"
              />
            </div>
            <p v-if="passwordError" class="error-msg">{{ passwordError }}</p>
            <button type="submit" class="btn btn-primary" :disabled="savingPassword">
              {{ savingPassword ? '修改中…' : '修改密码' }}
            </button>
          </form>
        </section>
      </div>
    </template>
  </div>
</template>

<script setup>
import { inject, onMounted, ref } from 'vue'
import { useAuthStore } from '@/stores/useAuthStore'
import { userApi } from '@/api'
import PasswordInput from '@/components/PasswordInput.vue'

const toast = inject('toast')
const authStore = useAuthStore()

const loading = ref(true)
const savingProfile = ref(false)
const savingPassword = ref(false)
const profileError = ref('')
const passwordError = ref('')

const profileForm = ref({ name: '', phone: '' })
const passwordForm = ref({ currentPassword: '', newPassword: '', confirmPassword: '' })

onMounted(loadProfile)

async function loadProfile() {
  loading.value = true
  try {
    const res = await userApi.me()
    if (res.code === 200 && res.data) {
      profileForm.value = {
        name: res.data.name || '',
        phone: res.data.phone || '',
      }
    } else {
      toast(res.message || '加载失败', 'error')
    }
  } catch {
    toast('加载账户信息失败', 'error')
  } finally {
    loading.value = false
  }
}

async function saveProfile() {
  if (!profileForm.value.name?.trim()) {
    profileError.value = '请填写姓名'
    return
  }
  if (!profileForm.value.phone?.trim()) {
    profileError.value = '请填写手机号'
    return
  }

  savingProfile.value = true
  profileError.value = ''
  try {
    const res = await userApi.updateProfile({
      name: profileForm.value.name.trim(),
      phone: profileForm.value.phone.trim(),
    })
    if (res.code === 200) {
      authStore.updateName(res.data.name)
      toast('账户信息已更新')
    } else {
      profileError.value = res.message || '保存失败'
    }
  } catch {
    profileError.value = '保存失败，请重试'
  } finally {
    savingProfile.value = false
  }
}

async function savePassword() {
  if (!passwordForm.value.currentPassword) {
    passwordError.value = '请填写当前密码'
    return
  }
  if (!passwordForm.value.newPassword || passwordForm.value.newPassword.length < 6) {
    passwordError.value = '新密码至少 6 位'
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    passwordError.value = '两次输入的新密码不一致'
    return
  }

  savingPassword.value = true
  passwordError.value = ''
  try {
    const res = await userApi.changePassword({
      currentPassword: passwordForm.value.currentPassword,
      newPassword: passwordForm.value.newPassword,
    })
    if (res.code === 200) {
      toast('密码已修改')
      passwordForm.value = { currentPassword: '', newPassword: '', confirmPassword: '' }
    } else {
      passwordError.value = res.message || '修改失败'
    }
  } catch {
    passwordError.value = '修改失败，请重试'
  } finally {
    savingPassword.value = false
  }
}
</script>

<style scoped>
.profile-layout {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.profile-section {
  padding: 20px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 16px;
  color: var(--ink);
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
  max-width: 400px;
}

.error-msg {
  font-size: 13px;
  color: var(--status-cancelled);
  line-height: 1.5;
}
</style>
