import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/useAuthStore'

const routes = [
  {
    path: '/',
    redirect: () => {
      const auth = useAuthStore()
      return auth.isLoggedIn ? '/orders' : '/login'
    },
  },
  {
    path: '/login',
    component: () => import('@/views/LoginView.vue'),
    meta: { public: true, guest: true, title: '商家登录' },
  },
  {
    path: '/orders',
    component: () => import('@/views/OrderBoardView.vue'),
    meta: { title: '订单看板' },
  },
  {
    path: '/dishes',
    component: () => import('@/views/DishManageView.vue'),
    meta: { title: '菜品管理' },
  },
  {
    path: '/payments',
    component: () => import('@/views/PaymentListView.vue'),
    meta: { title: '支付流水' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const auth = useAuthStore()

  if (to.meta.guest && auth.isLoggedIn) {
    return { path: '/orders' }
  }

  if (!to.meta.public && !auth.isLoggedIn) {
    return { path: '/login' }
  }
})

export default router
