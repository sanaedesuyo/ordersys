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
  baseURL: '/api/admin',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('admin_token')
  if (token && isTokenValid(token)) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (res) => res.data,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_userId')
      localStorage.removeItem('admin_name')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(err)
  },
)

export const authApi = {
  login: (data) => axios.post('/api/admin/auth/login', data).then((r) => r.data),
}

export const orderApi = {
  list: (status) => http.get('/order', { params: status ? { status } : {} }),
  get: (id) => http.get(`/order/${id}`),
  accept: (id) => http.put(`/order/${id}/accept`),
  deliver: (id) => http.put(`/order/${id}/deliver`),
  complete: (id) => http.put(`/order/${id}/complete`),
  cancel: (id) => http.put(`/order/${id}/cancel`),
}

export const dishApi = {
  list: (params = {}) => http.get('/dish', { params }),
  create: (data) => http.post('/dish', data),
  update: (id, data) => http.put(`/dish/${id}`, data),
  get: (id) => http.get(`/dish/${id}`),
}

export const paymentApi = {
  getByOrder: (orderId) => http.get(`/payment/order/${orderId}`),
}
