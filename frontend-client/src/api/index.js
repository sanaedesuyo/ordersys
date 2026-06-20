import axios from 'axios'

function isTokenValid(token) {
  if (!token) return false
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return typeof payload.exp === 'number' && payload.exp * 1000 > Date.now()
  } catch {
    return false
  }
}

const http = axios.create({
  baseURL: '/api/client',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('user_token')
  if (token && isTokenValid(token)) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (res) => res.data,
  (err) => {
    if (err.response?.status === 401) {
      const path = window.location.pathname
      const isAuthPage = path === '/login' || path === '/register'
      localStorage.removeItem('user_token')
      localStorage.removeItem('user_userId')
      localStorage.removeItem('user_name')
      if (!isAuthPage && path !== '/menu') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(err)
  },
)

export const authApi = {
  login: (data) => axios.post('/api/client/auth/login', data).then((r) => r.data),
  register: (data) => axios.post('/api/client/auth/register', data).then((r) => r.data),
}

export const dishApi = {
  list: (params = {}) => axios.get('/api/client/dish', { params }).then((r) => r.data),
  get: (id) => axios.get(`/api/client/dish/${id}`).then((r) => r.data),
}

export const orderApi = {
  create: (data) => http.post('/order', data),
  list: (status) => http.get('/order', { params: status ? { status } : {} }),
  get: (id) => http.get(`/order/${id}`),
  cancel: (id) => http.put(`/order/${id}/cancel`),
}

export const paymentApi = {
  pay: (data) => http.post('/payment', data),
  getByOrder: (orderId) => http.get(`/payment/order/${orderId}`),
}

export const userApi = {
  me: () => http.get('/user/me'),
  updateProfile: (data) => http.put('/user/me', data),
  changePassword: (data) => http.put('/user/me/password', data),
}

export const addressApi = {
  list: () => http.get('/address'),
  create: (data) => http.post('/address', data),
  update: (id, data) => http.put(`/address/${id}`, data),
  remove: (id) => http.delete(`/address/${id}`),
  setDefault: (id) => http.put(`/address/${id}/default`),
}
