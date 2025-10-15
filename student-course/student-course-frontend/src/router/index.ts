import { createRouter, createWebHistory } from 'vue-router'
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
      meta: { title: '学生选课', requiresAuth: true }
    },
    {
      path: '/student-management',
      name: 'StudentManagement',
      component: () => import('@/views/StudentManagement.vue'),
      meta: { title: '学生管理', requiresAuth: true }
    }
  ]
})
router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = `${to.meta.title} - 学生选课系统`
  }

  // 检查路由是否需要认证
  const requiresAuth = to.meta.requiresAuth !== false
  const token = localStorage.getItem('token')

  if (requiresAuth && !token) {
    // 需要认证但未登录，跳转到登录页
    next('/login')
  } else if (to.path === '/login' && token) {
    // 已登录但访问登录页，跳转到选课页
    next('/course-selection')
  } else {
    next()
  }
})
export default router
