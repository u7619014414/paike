<template>
  <div class="course-schedule-grid">
    <!-- 学生信息卡片 -->
    <el-card class="student-info-card" v-if="currentStudent">
      <div class="student-header">
        <el-avatar :size="60" class="student-avatar">
          {{ currentStudent.studentName.charAt(0) }}
        </el-avatar>
        <div class="student-details">
          <h3>{{ currentStudent.studentName }}</h3>
          <p>年龄：{{ currentStudent.age }}岁 | {{ getAgeGroupLabel(currentStudent.ageGroup) }}</p>
          <p>家长：{{ currentStudent.parentName }} | {{ currentStudent.parentPhone }}</p>
        </div>
      </div>
    </el-card>
    <!-- 周选择器 -->
    <div class="week-selector">
      <el-button-group>
        <el-button @click="previousWeek" :icon="ArrowLeft">上一周</el-button>
        <el-button disabled>
          {{ formatDateRange(currentWeekStart, currentWeekEnd) }}
        </el-button>
        <el-button @click="nextWeek" :icon="ArrowRight">下一周</el-button>
      </el-button-group>
      <el-button type="primary" @click="goToCurrentWeek">当前周</el-button>
    </div>
    <!-- 课程表格 -->
    <div class="schedule-grid">
      <div class="grid-header">
        <div class="time-header">时间</div>
        <div 
          v-for="(day, index) in WEEK_DAYS" 
          :key="index"
          class="day-header"
          :class="{ 'today': isToday(index + 1) }"
        >
          {{ day }}
          <span class="date">{{ getDateForDay(index + 1) }}</span>
        </div>
      </div>
      <div 
        v-for="timeSlot in timeSlots" 
        :key="timeSlot.name"
        class="grid-row"
      >
        <div class="time-cell">
          <div class="time-name">{{ timeSlot.name }}</div>
          <div class="time-range">{{ timeSlot.startTime }}-{{ timeSlot.endTime }}</div>
        </div>
        <div 
          v-for="(day, dayIndex) in WEEK_DAYS" 
          :key="`${timeSlot.name}-${dayIndex}`"
          class="course-cell"
        >
          <CourseCard
            v-for="course in getCoursesForSlot(dayIndex + 1, timeSlot.name)"
            :key="course.scheduleId"
            :course="course"
            :current-student="currentStudent"
            @enroll-course="handleEnrollCourse"
            @cancel-enrollment="handleCancelEnrollment"
          />
          <div v-if="getCoursesForSlot(dayIndex + 1, timeSlot.name).length === 0" class="empty-slot">
            <el-icon class="empty-icon"><CirclePlus /></el-icon>
            <span class="empty-text">暂无课程</span>
          </div>
        </div>
      </div>
    </div>
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-mask">
      <el-icon class="loading-icon"><Loading /></el-icon>
      <span>加载中...</span>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ArrowRight, Loading, CirclePlus } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import CourseCard from './CourseCard.vue'
import { useCourseStore } from '@/stores/course'
import type { Student, CourseInfo } from '@/types'
import { AGE_GROUPS, WEEK_DAYS } from '@/types'
interface Props {
  currentStudent?: Student
}
const props = defineProps<Props>()
const courseStore = useCourseStore()
const loading = computed(() => courseStore.loading)
const scheduleData = computed(() => courseStore.scheduleData)
// 响应式数据
const currentWeekStart = ref(dayjs().startOf('week'))
// 时间段定义
const timeSlots = [
  { name: '早班', startTime: '09:00', endTime: '11:00' },
  { name: '中班', startTime: '11:00', endTime: '13:00' },
  { name: '晚班', startTime: '14:00', endTime: '16:00' },
  { name: '夜班', startTime: '16:00', endTime: '18:00' }
]
// 计算属性
const currentWeekEnd = computed(() => currentWeekStart.value.add(6, 'day'))
// 方法
const loadScheduleData = async () => {
  if (!props.currentStudent) return
  await courseStore.fetchScheduleView(
    props.currentStudent.ageGroup,
    currentWeekStart.value.format('YYYY-MM-DD')
  )
}
const getCoursesForSlot = (dayOfWeek: number, timeSlotName: string): CourseInfo[] => {
  const dayData = scheduleData.value.find(d => d.dayOfWeek === dayOfWeek)
  if (!dayData) return []
  const slotData = dayData.timeSlots.find(s => s.slotName === timeSlotName)
  return slotData?.courses || []
}
const isToday = (dayOfWeek: number): boolean => {
  return dayjs().day() === dayOfWeek || (dayOfWeek === 7 && dayjs().day() === 0)
}
const getDateForDay = (dayOfWeek: number): string => {
  const adjustedDay = dayOfWeek === 7 ? 0 : dayOfWeek
  return currentWeekStart.value.day(adjustedDay).format('MM/DD')
}
const formatDateRange = (start: dayjs.Dayjs, end: dayjs.Dayjs): string => {
  return `${start.format('YYYY年MM月DD日')} - ${end.format('MM月DD日')}`
}
const getAgeGroupLabel = (ageGroup: number): string => {
  return AGE_GROUPS.find(g => g.value === ageGroup)?.label || ''
}
const previousWeek = () => {
  currentWeekStart.value = currentWeekStart.value.subtract(1, 'week')
}
const nextWeek = () => {
  currentWeekStart.value = currentWeekStart.value.add(1, 'week')
}
const goToCurrentWeek = () => {
  currentWeekStart.value = dayjs().startOf('week')
}
const handleEnrollCourse = async (courseCard: CourseInfo) => {
  if (!props.currentStudent) return
  try {
    await courseStore.enrollCourse(props.currentStudent.id, courseCard.scheduleId)
    ElMessage.success('选课成功！')
    await loadScheduleData()
  } catch (error) {
    // 错误信息由拦截器处理
  }
}
const handleCancelEnrollment = async (courseCard: CourseInfo) => {
  if (!props.currentStudent) return
  try {
    await courseStore.cancelEnrollment(props.currentStudent.id, courseCard.scheduleId)
    ElMessage.success('取消选课成功！')
    await loadScheduleData()
  } catch (error) {
    // 错误信息由拦截器处理
  }
}
// 监听器
watch(() => props.currentStudent, () => {
  if (props.currentStudent) {
    loadScheduleData()
  }
})
watch(() => currentWeekStart.value, () => {
  loadScheduleData()
})
// 生命周期
onMounted(() => {
  if (props.currentStudent) {
    loadScheduleData()
  }
})
</script>
<style lang="scss" scoped>
.course-schedule-grid {
  padding: 20px;
  min-height: 100vh;
  background: #f5f5f5;
  position: relative;
}
.student-info-card {
  margin-bottom: 20px;
  .student-header {
    display: flex;
    align-items: center;
    gap: 15px;
    .student-avatar {
      background: linear-gradient(45deg, #409eff, #67c23a);
      color: white;
      font-weight: bold;
    }
    .student-details {
      h3 {
        margin: 0 0 5px 0;
        color: #303133;
        font-size: 18px;
      }
      p {
        margin: 2px 0;
        color: #606266;
        font-size: 14px;
      }
    }
  }
}
.week-selector {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 10px;
}
.schedule-grid {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}
.grid-header {
  display: grid;
  grid-template-columns: 120px repeat(7, 1fr);
  background: #fafafa;
  border-bottom: 2px solid #e4e7ed;
  .time-header, .day-header {
    padding: 15px 10px;
    text-align: center;
    font-weight: bold;
    color: #303133;
    border-right: 1px solid #e4e7ed;
  }
  .day-header {
    display: flex;
    flex-direction: column;
    gap: 5px;
    &.today {
      background: #e6f7ff;
      color: #1890ff;
    }
    .date {
      font-size: 12px;
      font-weight: normal;
      color: #909399;
    }
  }
}
.grid-row {
  display: grid;
  grid-template-columns: 120px repeat(7, 1fr);
  border-bottom: 1px solid #e4e7ed;
  &:last-child {
    border-bottom: none;
  }
}
.time-cell {
  padding: 20px 10px;
  text-align: center;
  background: #f9f9f9;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  justify-content: center;
  .time-name {
    font-weight: bold;
    color: #303133;
    margin-bottom: 5px;
  }
  .time-range {
    font-size: 12px;
    color: #909399;
  }
}
.course-cell {
  padding: 10px;
  border-right: 1px solid #e4e7ed;
  min-height: 120px;
  &:last-child {
    border-right: none;
  }
}
.empty-slot {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #c0c4cc;
  .empty-icon {
    font-size: 24px;
    margin-bottom: 8px;
  }
  .empty-text {
    font-size: 12px;
  }
}
.loading-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 100;
  .loading-icon {
    font-size: 32px;
    margin-bottom: 10px;
    animation: rotate 2s linear infinite;
  }
  span {
    color: #606266;
    font-size: 14px;
  }
}
@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
