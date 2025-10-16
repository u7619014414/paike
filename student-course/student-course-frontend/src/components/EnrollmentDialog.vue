<template>
  <el-dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="500px"
    @close="handleClose"
  >
    <div class="enrollment-dialog-content">
      <!-- 课程信息 -->
      <el-descriptions :column="1" border v-if="course">
        <el-descriptions-item label="课程名称">
          <span class="course-name">{{ course.courseName }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="教室">
          <el-icon><Location /></el-icon>
          {{ course.classroomName }}
        </el-descriptions-item>
        <el-descriptions-item label="教师">
          <el-icon><User /></el-icon>
          {{ course.teacherNames.join(', ') }}
        </el-descriptions-item>
        <el-descriptions-item label="已选/容量">
          <el-tag
            :type="getCapacityTagType()"
            effect="dark"
          >
            {{ course.currentStudents }}/{{ course.classroomCapacity || course.maxStudents }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="课程状态">
          <el-tag :type="getStatusTagType()" effect="plain">
            {{ getStatusText() }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 学生信息 -->
      <div class="student-info" v-if="currentStudent">
        <el-divider content-position="left">
          <el-icon><UserFilled /></el-icon>
          学生信息
        </el-divider>
        <el-descriptions :column="2" size="small">
          <el-descriptions-item label="姓名">
            {{ currentStudent.studentName }}
          </el-descriptions-item>
          <el-descriptions-item label="年龄">
            {{ currentStudent.age }}岁
          </el-descriptions-item>
          <el-descriptions-item label="家长">
            {{ currentStudent.parentName }}
          </el-descriptions-item>
          <el-descriptions-item label="联系电话">
            {{ currentStudent.parentPhone }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 警告信息 -->
      <el-alert
        v-if="course && course.status === 'FULL'"
        title="教室已满"
        type="error"
        :closable="false"
        show-icon
        class="alert-message"
      >
        该课程教室容量已满,无法选课
      </el-alert>
      <el-alert
        v-else-if="course && course.status === 'DISABLED'"
        title="年龄组不匹配"
        type="warning"
        :closable="false"
        show-icon
        class="alert-message"
      >
        该课程不适合当前学生的年龄组
      </el-alert>
      <el-alert
        v-else-if="course && !currentStudent"
        title="请先登录"
        type="info"
        :closable="false"
        show-icon
        class="alert-message"
      >
        请先登录学生账号后再选课
      </el-alert>
      <el-alert
        v-else-if="course && currentStudent && canEnroll"
        title="可以选课"
        type="success"
        :closable="false"
        show-icon
        class="alert-message"
      >
        您可以为该学生报名此课程
      </el-alert>
      <el-alert
        v-else-if="course && currentStudent && !canEnroll"
        title="无法选课"
        type="warning"
        :closable="false"
        show-icon
        class="alert-message"
      >
        年龄组: 课程{{ course.ageGroup }} vs 学生{{ currentStudent.ageGroup }} |
        容量: {{ course.currentStudents }}/{{ course.classroomCapacity || course.maxStudents }}
      </el-alert>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button
          type="primary"
          @click="handleConfirm"
          :disabled="!canEnroll"
          :loading="loading"
        >
          确认选课
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Location, User, UserFilled } from '@element-plus/icons-vue'
import type { CourseInfo, Student } from '@/types'

interface Props {
  modelValue: boolean
  course: CourseInfo | null
  currentStudent?: Student
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'confirm': [course: CourseInfo]
}>()

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const dialogTitle = computed(() => {
  if (!props.course) return '课程详情'
  return `选课 - ${props.course.courseName}`
})

const canEnroll = computed(() => {
  if (!props.course || !props.currentStudent) return false

  // 检查是否已满
  const capacity = props.course.classroomCapacity || props.course.maxStudents
  const isFull = props.course.currentStudents >= capacity

  // 检查年龄组是否匹配
  const ageGroupMatches = props.course.ageGroup === props.currentStudent.ageGroup

  // 只要不满且年龄组匹配就可以选课
  return !isFull && ageGroupMatches
})

const getStatusText = () => {
  if (!props.course) return ''
  switch (props.course.status) {
    case 'FULL': return '已满'
    case 'AVAILABLE': return '可选'
    case 'EMPTY': return '空闲'
    case 'DISABLED': return '不可选'
    default: return props.course.status
  }
}

const getStatusTagType = () => {
  if (!props.course) return 'info'
  switch (props.course.status) {
    case 'FULL': return 'danger'
    case 'AVAILABLE': return 'warning'
    case 'EMPTY': return 'success'
    case 'DISABLED': return 'info'
    default: return 'info'
  }
}

const getCapacityTagType = () => {
  if (!props.course) return 'info'
  const capacity = props.course.classroomCapacity || props.course.maxStudents
  const rate = props.course.currentStudents / capacity
  if (rate >= 1) return 'danger'
  if (rate >= 0.8) return 'warning'
  return 'success'
}

const handleClose = () => {
  emit('update:modelValue', false)
}

const handleConfirm = () => {
  if (props.course && canEnroll.value) {
    emit('confirm', props.course)
  }
}
</script>

<style lang="scss" scoped>
.enrollment-dialog-content {
  .course-name {
    font-size: 16px;
    font-weight: 600;
    color: #409eff;
  }

  .student-info {
    margin-top: 20px;
  }

  .alert-message {
    margin-top: 20px;
  }

  :deep(.el-descriptions__label) {
    width: 100px;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
