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
  localStorage.removeItem('user_token')
  localStorage.removeItem('user_userId')
  localStorage.removeItem('user_name')
}

export const useAuthStore = defineStore('auth', () => {
  const storedToken = localStorage.getItem('user_token') || ''
  if (storedToken && !isTokenValid(storedToken)) {
    clearStorage()
  }

  const token = ref(localStorage.getItem('user_token') || '')
  const userId = ref(Number(localStorage.getItem('user_userId') || 0))
  const name = ref(localStorage.getItem('user_name') || '')

  const isLoggedIn = computed(() => isTokenValid(token.value))

  function setAuth(data) {
    token.value = data.token
    userId.value = data.userId
    name.value = data.name
    localStorage.setItem('user_token', data.token)
    localStorage.setItem('user_userId', data.userId)
    localStorage.setItem('user_name', data.name)
  }

  function logout() {
    token.value = ''
    userId.value = 0
    name.value = ''
    clearStorage()
  }

  return { token, userId, name, isLoggedIn, setAuth, logout }
})
