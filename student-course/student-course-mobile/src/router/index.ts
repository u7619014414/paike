import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: { title: '学生登录', requiresAuth: false }
    },
    {
      path: '/course-selection',
      name: 'CourseSelection',
      component: () => import('@/views/CourseSelection.vue'),
      meta: { title: '选课中心', requiresAuth: true }
    },
    {
      path: '/student-management',
      name: 'StudentManagement',
      component: () => import('@/views/StudentManagement.vue'),
      meta: { title: '学生管理', requiresAuth: true }
    },
    {
      path: '/my',
      name: 'My',
      component: () => import('@/views/My.vue'),
      meta: { title: '我的', requiresAuth: true }
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 学生选课系统`
  }

  // 初始化用户状态
  const userStore = useUserStore()
  userStore.init()

  // 检查路由是否需要认证
  const requiresAuth = to.meta.requiresAuth !== false
  const isLoggedIn = userStore.isLoggedIn

  if (requiresAuth && !isLoggedIn) {
    // 需要认证但未登录,跳转到登录页
    next('/login')
  } else if (to.path === '/login' && isLoggedIn) {
    // 已登录但访问登录页,跳转到选课页
    next('/course-selection')
  } else {
    next()
  }
})

export default router
