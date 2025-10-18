import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/components/MainLayout.vue'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: { title: '登录', requiresAuth: false }
    },
    {
      path: '/',
      component: MainLayout,
      redirect: '/schedule',
      meta: { requiresAuth: true },
      children: [
        {
          path: 'schedule',
          name: 'Schedule',
          component: () => import('@/views/ScheduleManagement.vue'),
          meta: { title: '排课管理', requiresAuth: true }
        },
        {
          path: 'timeslot',
          name: 'TimeSlot',
          component: () => import('@/views/TimeSlotManagement.vue'),
          meta: { title: '时间段管理', requiresAuth: true }
        },
        {
          path: 'classroom',
          name: 'Classroom',
          component: () => import('@/views/ClassroomManagement.vue'),
          meta: { title: '教室管理', requiresAuth: true }
        },
        {
          path: 'teacher',
          name: 'Teacher',
          component: () => import('@/views/TeacherManagement.vue'),
          meta: { title: '教师管理', requiresAuth: true }
        },
        {
          path: 'data-conflict',
          name: 'DataConflict',
          component: () => import('@/views/DataConflictQuery.vue'),
          meta: { title: '数据异常查询', requiresAuth: true }
        },
        {
          path: 'user-management',
          name: 'UserManagement',
          component: () => import('@/views/UserManagement.vue'),
          meta: { title: '用户管理', requiresAuth: true, requiresAdmin: true }
        }
      ]
    }
  ]
})

router.beforeEach((to, from, next) => {
  document.title = `${to.meta.title || '教师排课系统'} - 教师排课管理系统`

  const authStore = useAuthStore()

  // 恢复用户信息
  if (!authStore.token) {
    authStore.restoreUserInfo()
  }

  // 检查是否需要登录
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth !== false)

  if (requiresAuth && !authStore.isAuthenticated()) {
    // 需要登录但未登录，跳转到登录页
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (to.name === 'Login' && authStore.isAuthenticated()) {
    // 已登录但访问登录页，跳转到首页
    next({ path: '/' })
  } else {
    next()
  }
})

export default router
