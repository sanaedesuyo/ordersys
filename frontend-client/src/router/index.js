import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/useAuthStore'

const routes = [
  { path: '/', redirect: '/menu' },
  {
    path: '/login',
    component: () => import('@/views/LoginView.vue'),
    meta: { public: true, guest: true, title: '登录' },
  },
  {
    path: '/register',
    component: () => import('@/views/RegisterView.vue'),
    meta: { public: true, guest: true, title: '注册' },
  },
  {
    path: '/menu',
    component: () => import('@/views/MenuView.vue'),
    meta: { public: true, title: '菜单' },
  },
  {
    path: '/cart',
    component: () => import('@/views/CartView.vue'),
    meta: { title: '购物车' },
  },
  {
    path: '/checkout',
    component: () => import('@/views/CheckoutView.vue'),
    meta: { title: '结算' },
  },
  {
    path: '/orders',
    component: () => import('@/views/MyOrdersView.vue'),
    meta: { title: '我的订单' },
  },
  {
    path: '/orders/:id',
    component: () => import('@/views/OrderDetailView.vue'),
    meta: { title: '订单详情' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const auth = useAuthStore()

  if (to.meta.guest && auth.isLoggedIn) {
    return { path: '/menu' }
  }

  if (!to.meta.public && !auth.isLoggedIn) {
    return {
      path: '/login',
      query: { redirect: to.fullPath },
    }
  }
})

export default router
