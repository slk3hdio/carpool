import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

import Home from '../pages/Home.vue'
import Traffic from '../pages/Traffic.vue'
import Carpool from '../pages/Carpool.vue'
import User from '../pages/User.vue'
import RoadDemo from '../pages/RoadDemo.vue'
import HistoricalTraffic from '../pages/HistoricalTraffic.vue'
import Monitor from '../pages/Monitor.vue'
import Login from '../pages/Login.vue'
import Register from '../pages/Register.vue'

const routes = [
  {
    path: '/',
    component: Home,
    meta: { requiresAuth: false }
  },
  {
    path: '/traffic',
    component: Traffic,
    meta: { requiresAuth: false }
  },
  {
    path: '/carpool',
    component: Carpool,
    meta: { requiresAuth: true }
  },
  {
    path: '/user',
    component: User,
    meta: { requiresAuth: true }
  },
  {
    path: '/demo',
    component: RoadDemo,
    meta: { requiresAuth: false }
  },
  {
    path: '/historical',
    component: HistoricalTraffic,
    meta: { requiresAuth: false }
  },
  {
    path: '/monitor',
    component: Monitor,
    meta: { requiresAuth: false }
  },
  {
    path: '/login',
    component: Login,
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    component: Register,
    meta: { requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 初始化用户状态
  if (!userStore.isAuthenticated && localStorage.getItem('token')) {
    userStore.initialize()
  }

  // 检查路由是否需要认证
  if (to.meta.requiresAuth && !userStore.isAuthenticated) {
    // 需要登录但未登录，跳转到登录页
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && userStore.isAuthenticated) {
    // 已登录用户访问登录/注册页，跳转到首页
    next('/')
  } else {
    next()
  }
})

export default router

