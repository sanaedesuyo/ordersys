import axios from 'axios'

const http = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
})

http.interceptors.response.use(
  (res) => res.data,
  (err) => Promise.reject(err),
)

// User
export const userApi = {
  create: (data) => http.post('/user', data),
  get: (id) => http.get(`/user/${id}`),
}

// Dish
export const dishApi = {
  create: (data) => http.post('/dish', data),
  list: () => http.get('/dish'),
  get: (id) => http.get(`/dish/${id}`),
}

// Order
export const orderApi = {
  create: (userId, data) => http.post(`/order?userId=${userId}`, data),
  get: (id) => http.get(`/order/${id}`),
  accept: (id) => http.put(`/order/${id}/accept`),
  deliver: (id) => http.put(`/order/${id}/deliver`),
  complete: (id) => http.put(`/order/${id}/complete`),
  cancel: (id) => http.put(`/order/${id}/cancel`),
}

// Payment
export const paymentApi = {
  pay: (data) => http.post('/payment', data),
  getByOrder: (orderId) => http.get(`/payment/order/${orderId}`),
}
