/**
 * Vue Router 路由配置
 * 在线教育平台管理后台路由表
 */
import { createRouter, createWebHistory } from 'vue-router'
import NProgress from 'nprogress'

// 路由表配置
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('../layout/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'users',
        name: 'UserList',
        component: () => import('../views/UserList.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'courses',
        name: 'CourseList',
        component: () => import('../views/CourseList.vue'),
        meta: { title: '课程管理' }
      },
      {
        path: 'orders',
        name: 'OrderList',
        component: () => import('../views/OrderList.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'payments',
        name: 'PaymentList',
        component: () => import('../views/PaymentList.vue'),
        meta: { title: '支付记录' }
      },
      {
        path: 'videos',
        name: 'VideoList',
        component: () => import('../views/VideoList.vue'),
        meta: { title: '视频管理' }
      },
      {
        path: 'chapters',
        name: 'ChapterList',
        component: () => import('../views/ChapterList.vue'),
        meta: { title: '章节管理' }
      },
      {
        path: 'interactions',
        name: 'InteractionList',
        component: () => import('../views/InteractionList.vue'),
        meta: { title: '互动管理' }
      },
      {
        path: 'statistics',
        name: 'StatisticsView',
        component: () => import('../views/StatisticsView.vue'),
        meta: { title: '数据统计' }
      }
    ]
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  // 启动进度条
  NProgress.start()

  // 从 localStorage 获取 token
  const token = localStorage.getItem('token')

  // 登录页直接放行
  if (to.path === '/login') {
    next()
    return
  }

  // 无 token 跳转登录页
  if (!token) {
    next('/login')
  } else {
    next()
  }
})

// 全局后置守卫
router.afterEach((to) => {
  // 关闭进度条
  NProgress.done()
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 教育平台管理后台` : '教育平台管理后台'
})

export default router
