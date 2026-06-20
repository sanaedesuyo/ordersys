import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

function isTokenValid(token) {
  if (!token) return false
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return typeof payload.exp === 'number' && payload.exp * 1000 > Date.now()
  } catch {
    return false
  }
}

function clearStorage() {
  localStorage.removeItem('admin_token')
  localStorage.removeItem('admin_userId')
  localStorage.removeItem('admin_name')
}

export const useAuthStore = defineStore('auth', () => {
  const storedToken = localStorage.getItem('admin_token') || ''
  if (storedToken && !isTokenValid(storedToken)) {
    clearStorage()
  }

  const token = ref(localStorage.getItem('admin_token') || '')
  const userId = ref(Number(localStorage.getItem('admin_userId') || 0))
  const name = ref(localStorage.getItem('admin_name') || '')

  const isLoggedIn = computed(() => isTokenValid(token.value))

  function setAuth(data) {
    token.value = data.token
    userId.value = data.userId
    name.value = data.name
    localStorage.setItem('admin_token', data.token)
    localStorage.setItem('admin_userId', data.userId)
    localStorage.setItem('admin_name', data.name)
  }

  function logout() {
    token.value = ''
    userId.value = 0
    name.value = ''
    clearStorage()
  }

  return { token, userId, name, isLoggedIn, setAuth, logout }
})
