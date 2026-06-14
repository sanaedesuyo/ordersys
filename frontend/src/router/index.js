import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/orders' },
  {
    path: '/dishes',
    component: () => import('@/views/DishView.vue'),
    meta: { title: '菜品管理' },
  },
  {
    path: '/orders',
    component: () => import('@/views/OrderView.vue'),
    meta: { title: '订单管理' },
  },
  {
    path: '/users',
    component: () => import('@/views/UserView.vue'),
    meta: { title: '用户管理' },
  },
  {
    path: '/payments',
    component: () => import('@/views/PaymentView.vue'),
    meta: { title: '支付记录' },
  },
]

export default createRouter({
  history: createWebHistory(),
  routes,
})
