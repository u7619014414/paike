<template>
  <div class="course-selection-page">
    <!-- 顶部导航栏 -->
    <van-nav-bar title="选课中心" fixed placeholder>
      <template #left>
        <van-icon name="apps-o" size="18" @click="showMenu = true" />
      </template>
      <template #right>
        <van-icon name="user-o" size="18" @click="$router.push('/my')" />
      </template>
    </van-nav-bar>

    <!-- 学生信息卡片 -->
    <div class="student-info">
      <div class="info-item">
        <span class="label">学号:</span>
        <span class="value">{{ userStore.student?.studentCode }}</span>
      </div>
      <div class="info-item">
        <span class="label">姓名:</span>
        <span class="value">{{ userStore.student?.studentName }}</span>
      </div>
      <div class="info-item">
        <span class="label">年龄组:</span>
        <van-tag type="primary" size="medium">
          {{ getAgeGroupLabel(userStore.student?.ageGroup) }}
        </van-tag>
      </div>
    </div>

    <!-- 周选择器 -->
    <div class="week-selector">
      <van-icon name="arrow-left" @click="prevWeek" />
      <span class="week-text">{{ currentWeekText }}</span>
      <van-icon name="arrow" @click="nextWeek" />
    </div>

    <!-- 课程表 -->
    <van-pull-refresh v-model="refreshing" @refresh="loadCourses">
      <div class="course-schedule">
        <van-empty v-if="!loading && scheduleData.length === 0" description="暂无课程" />

        <div v-for="daySchedule in scheduleData" :key="daySchedule.dayOfWeek" class="day-schedule">
          <div class="day-header">
            <van-tag type="primary">{{ daySchedule.dayName }}</van-tag>
          </div>

          <div v-for="timeSlot in daySchedule.timeSlots" :key="timeSlot.timeSlotId" class="time-slot">
            <div class="time-info">
              <div class="time-name">{{ timeSlot.slotName }}</div>
              <div class="time-range">{{ timeSlot.startTime }} - {{ timeSlot.endTime }}</div>
            </div>

            <div class="courses">
              <div
                v-for="course in timeSlot.courses"
                :key="course.scheduleId"
                class="course-card"
                :class="{
                  enrolled: enrolledCourses.includes(course.scheduleId),
                  disabled: !course.canEnroll || course.status === 'DISABLED'
                }"
                @click="handleCourseClick(course)"
              >
                <div class="course-header">
                  <h4 class="course-name">{{ course.courseName }}</h4>
                  <van-tag
                    :type="getStatusTagType(course.status)"
                    size="small"
                  >
                    {{ getStatusText(course.status) }}
                  </van-tag>
                </div>

                <div class="course-info">
                  <div class="info-row">
                    <van-icon name="location-o" />
                    <span>{{ course.classroomName }}</span>
                  </div>
                  <div class="info-row">
                    <van-icon name="friends-o" />
                    <span>{{ course.currentStudents }}/{{ course.maxStudents }}人</span>
                  </div>
                  <div v-if="course.teacherNames.length > 0" class="info-row">
                    <van-icon name="manager-o" />
                    <span>{{ course.teacherNames.join(', ') }}</span>
                  </div>
                </div>

                <div v-if="enrolledCourses.includes(course.scheduleId)" class="enrolled-badge">
                  <van-icon name="success" />
                  已选
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </van-pull-refresh>

    <!-- 底部导航 -->
    <van-tabbar v-model="activeTab" fixed placeholder>
      <van-tabbar-item icon="orders-o" to="/course-selection">选课</van-tabbar-item>
      <van-tabbar-item icon="contact" to="/student-management">学生</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/my">我的</van-tabbar-item>
    </van-tabbar>

    <!-- 菜单抽屉 -->
    <van-action-sheet v-model:show="showMenu" title="菜单">
      <div class="menu-content">
        <van-cell icon="orders-o" title="选课中心" is-link @click="navigateTo('/course-selection')" />
        <van-cell icon="contact" title="学生管理" is-link @click="navigateTo('/student-management')" />
        <van-cell icon="user-o" title="个人中心" is-link @click="navigateTo('/my')" />
      </div>
    </van-action-sheet>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showSuccessToast, showToast } from 'vant'
import dayjs from 'dayjs'
import { courseApi, enrollmentApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { AGE_GROUPS } from '@/types'
import type { CourseScheduleView, CourseInfo } from '@/types'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref(0)
const loading = ref(false)
const refreshing = ref(false)
const showMenu = ref(false)

const currentWeekStart = ref(dayjs().startOf('week'))
const scheduleData = ref<CourseScheduleView[]>([])
const enrolledCourses = ref<number[]>([])

// 当前周文本
const currentWeekText = computed(() => {
  const start = currentWeekStart.value.format('MM/DD')
  const end = currentWeekStart.value.add(6, 'day').format('MM/DD')
  return `${start} - ${end}`
})

// 获取年龄组标签
const getAgeGroupLabel = (ageGroup?: number) => {
  if (!ageGroup) return ''
  const group = AGE_GROUPS.find(g => g.value === ageGroup)
  return group?.label || ''
}

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  switch (status) {
    case 'AVAILABLE':
      return 'success'
    case 'FULL':
      return 'warning'
    case 'DISABLED':
      return 'danger'
    default:
      return 'default'
  }
}

// 获取状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'AVAILABLE':
      return '可选'
    case 'FULL':
      return '已满'
    case 'DISABLED':
      return '不可选'
    case 'EMPTY':
      return '空闲'
    default:
      return status
  }
}

// 加载课程
const loadCourses = async () => {
  if (!userStore.student) return

  loading.value = true
  try {
    const startDate = currentWeekStart.value.format('YYYY-MM-DD')
    const [schedules, enrolled] = await Promise.all([
      courseApi.getScheduleView(userStore.student.ageGroup, startDate),
      enrollmentApi.getStudentEnrollments(userStore.student.id)
    ])

    scheduleData.value = schedules
    enrolledCourses.value = enrolled
  } catch (error) {
    console.error('加载课程失败:', error)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

// 处理课程点击
const handleCourseClick = async (course: CourseInfo) => {
  if (!userStore.student) return

  const isEnrolled = enrolledCourses.value.includes(course.scheduleId)

  if (isEnrolled) {
    // 取消选课
    try {
      await showConfirmDialog({
        title: '取消选课',
        message: `确定要取消《${course.courseName}》吗?`
      })

      await enrollmentApi.cancelEnrollment(userStore.student.id, course.scheduleId)
      showSuccessToast('取消成功')
      loadCourses()
    } catch (error: any) {
      if (error !== 'cancel') {
        console.error('取消选课失败:', error)
      }
    }
  } else {
    // 选课
    if (!course.canEnroll) {
      showToast('该课程不可选')
      return
    }

    if (course.status === 'FULL') {
      showToast('该课程已满')
      return
    }

    try {
      await showConfirmDialog({
        title: '确认选课',
        message: `确定要选择《${course.courseName}》吗?`
      })

      await enrollmentApi.enrollCourse(userStore.student.id, course.scheduleId)
      showSuccessToast('选课成功')
      loadCourses()
    } catch (error: any) {
      if (error !== 'cancel') {
        console.error('选课失败:', error)
      }
    }
  }
}

// 上一周
const prevWeek = () => {
  currentWeekStart.value = currentWeekStart.value.subtract(1, 'week')
  loadCourses()
}

// 下一周
const nextWeek = () => {
  currentWeekStart.value = currentWeekStart.value.add(1, 'week')
  loadCourses()
}

// 导航
const navigateTo = (path: string) => {
  showMenu.value = false
  router.push(path)
}

onMounted(() => {
  loadCourses()
})
</script>

<style lang="scss" scoped>
.course-selection-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 60px;
}

.student-info {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .info-item {
    display: flex;
    flex-direction: column;
    gap: 4px;

    .label {
      font-size: 12px;
      opacity: 0.8;
    }

    .value {
      font-size: 14px;
      font-weight: bold;
    }
  }
}

.week-selector {
  background: white;
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;

  .week-text {
    font-size: 16px;
    font-weight: bold;
    color: #323233;
  }

  .van-icon {
    font-size: 20px;
    color: #667eea;
    cursor: pointer;
  }
}

.course-schedule {
  padding: 0 12px 12px;
}

.day-schedule {
  margin-bottom: 16px;

  .day-header {
    padding: 8px 0;
    margin-bottom: 8px;
  }
}

.time-slot {
  background: white;
  border-radius: 12px;
  padding: 12px;
  margin-bottom: 12px;

  .time-info {
    margin-bottom: 12px;
    padding-bottom: 8px;
    border-bottom: 1px solid #eee;

    .time-name {
      font-size: 16px;
      font-weight: bold;
      color: #323233;
      margin-bottom: 4px;
    }

    .time-range {
      font-size: 12px;
      color: #969799;
    }
  }
}

.courses {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.course-card {
  background: #f7f8fa;
  border-radius: 8px;
  padding: 12px;
  position: relative;
  transition: all 0.3s;

  &.enrolled {
    background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
    border: 1px solid #4caf50;
  }

  &.disabled {
    opacity: 0.6;
    pointer-events: none;
  }

  .course-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;

    .course-name {
      font-size: 15px;
      font-weight: bold;
      color: #323233;
      margin: 0;
    }
  }

  .course-info {
    display: flex;
    flex-direction: column;
    gap: 4px;

    .info-row {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 12px;
      color: #646566;

      .van-icon {
        font-size: 14px;
      }
    }
  }

  .enrolled-badge {
    position: absolute;
    top: -6px;
    right: -6px;
    background: #4caf50;
    color: white;
    padding: 4px 8px;
    border-radius: 12px;
    font-size: 12px;
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.menu-content {
  padding: 16px 0;
}
</style>
