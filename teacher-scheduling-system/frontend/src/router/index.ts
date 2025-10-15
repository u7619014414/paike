import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/components/MainLayout.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: MainLayout,
      redirect: '/schedule',
      children: [
        {
          path: 'schedule',
          name: 'Schedule',
          component: () => import('@/views/ScheduleManagement.vue'),
          meta: { title: '排课管理' }
        },
        {
          path: 'timeslot',
          name: 'TimeSlot',
          component: () => import('@/views/TimeSlotManagement.vue'),
          meta: { title: '时间段管理' }
        },
        {
          path: 'classroom',
          name: 'Classroom',
          component: () => import('@/views/ClassroomManagement.vue'),
          meta: { title: '教室管理' }
        },
        {
          path: 'teacher',
          name: 'Teacher',
          component: () => import('@/views/TeacherManagement.vue'),
          meta: { title: '教师管理' }
        },
        {
          path: 'data-conflict',
          name: 'DataConflict',
          component: () => import('@/views/DataConflictQuery.vue'),
          meta: { title: '数据异常查询' }
        }
      ]
    }
  ]
})

router.beforeEach((to, from, next) => {
  document.title = `${to.meta.title || '教师排课系统'} - 教师排课管理系统`
  next()
})

export default router
