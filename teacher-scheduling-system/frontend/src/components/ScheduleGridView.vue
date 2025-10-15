<template>
  <div class="schedule-grid-view">
    <!-- 周选择器 -->
    <div class="week-selector">
      <el-button-group>
        <el-button @click="previousWeek">
          <el-icon><ArrowLeft /></el-icon>
          上一周
        </el-button>
        <el-button disabled>
          {{ formatDateRange(currentWeekStart, currentWeekEnd) }}
        </el-button>
        <el-button @click="nextWeek">
          下一周
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </el-button-group>
      <el-button type="primary" @click="goToCurrentWeek">当前周</el-button>
    </div>

    <!-- 课程表格 -->
    <div class="schedule-grid" v-loading="loading">
      <div class="grid-header">
        <div class="time-header">时间段</div>
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
        v-for="timeSlot in uniqueTimeSlots"
        :key="timeSlot.id"
        class="grid-row"
      >
        <div class="time-cell">
          <div class="time-name">{{ timeSlot.slotName }}</div>
          <div class="time-range">{{ timeSlot.startTime }}-{{ timeSlot.endTime }}</div>
        </div>

        <div
          v-for="dayOfWeek in 7"
          :key="`${timeSlot.id}-${dayOfWeek}`"
          class="course-cell"
          @click="handleAddSchedule(dayOfWeek, timeSlot)"
        >
          <div
            v-for="schedule in getSchedulesForSlot(dayOfWeek, timeSlot)"
            :key="schedule.id"
            class="schedule-card"
            :class="getScheduleStatusClass(schedule.status)"
            @click.stop="handleScheduleClick(schedule)"
          >
            <div class="schedule-header">
              <span class="course-name">{{ schedule.courseName }}</span>
              <el-tag :type="getStatusType(schedule.status)" size="small">
                {{ getStatusLabel(schedule.status) }}
              </el-tag>
            </div>
            <div class="schedule-body">
              <div class="schedule-info">
                <el-icon><Location /></el-icon>
                <span>{{ schedule.classroomName }}</span>
              </div>
              <div class="schedule-info" v-if="schedule.teacherNames && schedule.teacherNames.length > 0">
                <el-icon><User /></el-icon>
                <span>{{ schedule.teacherNames.join(', ') }}</span>
              </div>
              <div class="schedule-info">
                <el-icon><Calendar /></el-icon>
                <span>{{ schedule.scheduleDate }}</span>
              </div>
            </div>
            <div class="schedule-actions" @click.stop>
              <el-button
                v-if="schedule.status === 1"
                type="primary"
                size="small"
                @click="handleAssignTeachers(schedule)"
              >
                分配教师
              </el-button>
              <el-button
                v-if="schedule.status === 1"
                type="danger"
                size="small"
                @click="handleCancel(schedule)"
              >
                取消
              </el-button>
            </div>
          </div>

          <div
            v-if="getSchedulesForSlot(dayOfWeek, timeSlot).length === 0"
            class="empty-slot"
          >
            <el-icon class="empty-icon"><Plus /></el-icon>
            <span class="empty-text">点击添加</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ArrowRight, Location, User, Calendar, Plus } from '@element-plus/icons-vue'
import dayjs, { Dayjs } from 'dayjs'
import weekday from 'dayjs/plugin/weekday'

// 配置 dayjs 使用周一作为一周的开始
dayjs.extend(weekday)
dayjs.Ls.en.weekStart = 1

const WEEK_DAYS = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

const SCHEDULE_STATUS = [
  { value: 1, label: '待分配', type: 'info' },
  { value: 2, label: '已分配', type: 'success' },
  { value: 3, label: '已取消', type: 'danger' }
]

interface TimeSlot {
  id: number
  slotName: string
  startTime: string
  endTime: string
  dayOfWeek: number
}

interface Schedule {
  id: number
  courseId: number
  courseName: string
  ageGroup: number
  timeSlotId: number
  timeSlotName: string
  startTime: string
  endTime: string
  classroomId: number
  classroomName: string
  scheduleDate: string
  status: number
  teacherNames?: string[]
  maxStudents: number
  currentStudents: number
}

interface Props {
  schedules: Schedule[]
  timeSlots: TimeSlot[]
  loading?: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  addSchedule: [dayOfWeek: number, timeSlot: TimeSlot, date: string]
  assignTeachers: [schedule: Schedule]
  cancelSchedule: [schedule: Schedule]
  scheduleClick: [schedule: Schedule]
}>()

// 响应式数据
const currentWeekStart = ref<Dayjs>(dayjs().startOf('week'))

// 计算属性
const currentWeekEnd = computed(() => currentWeekStart.value.add(6, 'day'))

// 获取唯一的时间段（去重，只保留不同的时间段名称和时间）
const uniqueTimeSlots = computed(() => {
  const seen = new Set<string>()
  const unique: TimeSlot[] = []

  for (const slot of props.timeSlots) {
    const key = `${slot.startTime}-${slot.endTime}`
    if (!seen.has(key)) {
      seen.add(key)
      unique.push(slot)
    }
  }

  return unique
})

// 方法
const getSchedulesForSlot = (dayOfWeek: number, timeSlot: TimeSlot): Schedule[] => {
  return props.schedules.filter(s => {
    const scheduleDate = dayjs(s.scheduleDate)
    const scheduleDayOfWeek = scheduleDate.day() === 0 ? 7 : scheduleDate.day()

    // 检查是否在当前周
    const isInCurrentWeek = scheduleDate.isSameOrAfter(currentWeekStart.value, 'day') &&
                           scheduleDate.isSameOrBefore(currentWeekEnd.value, 'day')

    // 按时间段匹配（而不是按ID），因为不同星期的相同时间段有不同的ID
    const timeMatch = s.startTime === timeSlot.startTime && s.endTime === timeSlot.endTime

    return timeMatch && scheduleDayOfWeek === dayOfWeek && isInCurrentWeek
  })
}

const isToday = (dayOfWeek: number): boolean => {
  const today = dayjs()
  const todayDayOfWeek = today.day() === 0 ? 7 : today.day()
  return todayDayOfWeek === dayOfWeek &&
         today.isSameOrAfter(currentWeekStart.value, 'day') &&
         today.isSameOrBefore(currentWeekEnd.value, 'day')
}

const getDateForDay = (dayOfWeek: number): string => {
  // dayOfWeek: 1-7 (周一到周日)
  // 从周一开始算，周日是第7天，即 +6 天
  return currentWeekStart.value.add(dayOfWeek - 1, 'day').format('MM/DD')
}

const formatDateRange = (start: Dayjs, end: Dayjs): string => {
  return `${start.format('YYYY年MM月DD日')} - ${end.format('MM月DD日')}`
}

const getStatusLabel = (status: number): string => {
  return SCHEDULE_STATUS.find(s => s.value === status)?.label || ''
}

const getStatusType = (status: number): string => {
  return SCHEDULE_STATUS.find(s => s.value === status)?.type || 'info'
}

const getScheduleStatusClass = (status: number): string => {
  const statusMap: Record<number, string> = {
    1: 'status-pending',
    2: 'status-assigned',
    3: 'status-cancelled'
  }
  return statusMap[status] || ''
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

const handleAddSchedule = (dayOfWeek: number, timeSlot: TimeSlot) => {
  // dayOfWeek: 1-7 (周一到周日)
  const date = currentWeekStart.value.add(dayOfWeek - 1, 'day').format('YYYY-MM-DD')
  emit('addSchedule', dayOfWeek, timeSlot, date)
}

const handleAssignTeachers = (schedule: Schedule) => {
  emit('assignTeachers', schedule)
}

const handleCancel = (schedule: Schedule) => {
  emit('cancelSchedule', schedule)
}

const handleScheduleClick = (schedule: Schedule) => {
  emit('scheduleClick', schedule)
}

// 当 schedules 改变时，检查是否需要自动跳转到有数据的周
watch(() => props.schedules, (newSchedules) => {
  if (newSchedules.length > 0) {
    // 检查当前周是否有数据
    const hasSchedulesInCurrentWeek = newSchedules.some(s => {
      const scheduleDate = dayjs(s.scheduleDate)
      return scheduleDate.isSameOrAfter(currentWeekStart.value, 'day') &&
             scheduleDate.isSameOrBefore(currentWeekEnd.value, 'day')
    })

    // 如果当前周没有数据，跳转到第一条记录所在的周
    if (!hasSchedulesInCurrentWeek) {
      const firstScheduleDate = dayjs(newSchedules[0].scheduleDate)
      currentWeekStart.value = firstScheduleDate.startOf('week')
    }
  }
}, { immediate: true })
</script>

<style scoped lang="scss">
.schedule-grid-view {
  padding: 20px;

  .week-selector {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 15px;
    background: #f5f7fa;
    border-radius: 8px;
  }

  .schedule-grid {
    border: 1px solid #e4e7ed;
    border-radius: 8px;
    overflow: hidden;
    background: white;

    .grid-header {
      display: grid;
      grid-template-columns: 120px repeat(7, minmax(0, 1fr));
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;

      .time-header,
      .day-header {
        padding: 15px;
        text-align: center;
        font-weight: bold;
        border-right: 1px solid rgba(255, 255, 255, 0.2);

        &:last-child {
          border-right: none;
        }
      }

      .day-header {
        display: flex;
        flex-direction: column;
        gap: 5px;

        &.today {
          background: rgba(255, 255, 255, 0.2);
        }

        .date {
          font-size: 12px;
          opacity: 0.9;
        }
      }
    }

    .grid-row {
      display: grid;
      grid-template-columns: 120px repeat(7, minmax(0, 1fr));
      border-bottom: 1px solid #e4e7ed;

      &:last-child {
        border-bottom: none;
      }

      .time-cell {
        padding: 15px;
        background: #f5f7fa;
        border-right: 1px solid #e4e7ed;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;

        .time-name {
          font-weight: bold;
          font-size: 14px;
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
        min-height: 140px;
        display: flex;
        flex-direction: column;
        gap: 8px;
        cursor: pointer;
        transition: background-color 0.3s;
        position: relative;

        &:last-child {
          border-right: none;
        }

        &:hover {
          background: #f5f7fa;
        }

        .schedule-card {
          background: white;
          border: 1px solid #dcdfe6;
          border-radius: 6px;
          padding: 10px;
          cursor: pointer;
          transition: all 0.3s;
          width: 100%;
          box-sizing: border-box;
          overflow: hidden;

          &:hover {
            box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
            transform: translateY(-2px);
          }

          &.status-pending {
            border-left: 3px solid #409eff;
          }

          &.status-assigned {
            border-left: 3px solid #67c23a;
          }

          &.status-cancelled {
            border-left: 3px solid #f56c6c;
            opacity: 0.6;
          }

          .schedule-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 8px;
            gap: 5px;

            .course-name {
              font-weight: bold;
              font-size: 14px;
              color: #303133;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
              flex: 1;
            }
          }

          .schedule-body {
            .schedule-info {
              display: flex;
              align-items: center;
              gap: 5px;
              font-size: 12px;
              color: #606266;
              margin-bottom: 4px;
              overflow: hidden;

              .el-icon {
                font-size: 14px;
                color: #909399;
                flex-shrink: 0;
              }

              span {
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
              }
            }
          }

          .schedule-actions {
            margin-top: 8px;
            display: flex;
            gap: 5px;
            padding-top: 8px;
            border-top: 1px solid #e4e7ed;

            .el-button {
              flex: 1;
            }
          }
        }

        .empty-slot {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          flex: 1;
          min-height: 100px;
          color: #c0c4cc;
          cursor: pointer;
          border: 1px dashed #dcdfe6;
          border-radius: 6px;
          background: #fafafa;
          transition: all 0.3s;

          .empty-icon {
            font-size: 24px;
            margin-bottom: 5px;
          }

          .empty-text {
            font-size: 12px;
          }

          &:hover {
            color: #409eff;
            border-color: #409eff;
            background: #ecf5ff;
          }
        }
      }
    }
  }
}
</style>
