import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/classroom'
    },
    {
      path: '/classroom',
      name: 'ClassroomList',
      component: () => import('@/views/classroom/ClassroomList.vue'),
      meta: { title: '教室管理' }
    },
    {
      path: '/timeslot',
      name: 'TimeSlotList',
      component: () => import('@/views/timeslot/TimeSlotList.vue'),
      meta: { title: '时间段管理' }
    }
  ]
})

router.beforeEach((to, _from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 教室时间段管理系统`
  }
  next()
})

export default router
