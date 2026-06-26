import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页概览', icon: 'HomeFilled' }
      },
      {
        path: 'child',
        name: 'Child',
        component: () => import('@/views/child/index.vue'),
        meta: { title: '孩子管理', icon: 'User' }
      },
      {
        path: 'task',
        name: 'Task',
        component: () => import('@/views/task/index.vue'),
        meta: { title: '任务设置', icon: 'List' }
      },
      {
        path: 'checkin',
        name: 'Checkin',
        component: () => import('@/views/checkin/index.vue'),
        meta: { title: '打卡审核', icon: 'CircleCheck' }
      },
      {
        path: 'supplement',
        name: 'Supplement',
        component: () => import('@/views/supplement/index.vue'),
        meta: { title: '手动补卡', icon: 'EditPen' }
      },
      {
        path: 'reward',
        name: 'Reward',
        component: () => import('@/views/reward/index.vue'),
        meta: { title: '奖励管理', icon: 'Gift' }
      },
      {
        path: 'exchange',
        name: 'Exchange',
        component: () => import('@/views/exchange/index.vue'),
        meta: { title: '兑换记录', icon: 'Tickets' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth === false) {
    next()
  } else {
    if (token) {
      next()
    } else {
      next('/login')
    }
  }
})

export default router
