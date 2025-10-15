<template>
  <div 
    class="course-card"
    :class="[
      `status-${course.status.toLowerCase()}`,
      { 'can-enroll': course.canEnroll && currentStudent }
    ]"
  >
    <div class="course-header">
      <h4 class="course-name">{{ course.courseName }}</h4>
      <div class="student-count">
        {{ course.currentStudents }}/{{ course.maxStudents }}
      </div>
    </div>
    <div class="course-details">
      <div class="detail-item">
        <el-icon><Location /></el-icon>
        <span>{{ course.classroomName }}</span>
      </div>
      <div class="detail-item">
        <el-icon><User /></el-icon>
        <span>{{ course.teacherNames.join(', ') }}</span>
      </div>
    </div>
    <div class="course-actions" v-if="currentStudent">
      <el-button 
        v-if="course.canEnroll && course.status !== 'FULL'"
        type="primary" 
        size="small"
        :disabled="course.status === 'DISABLED'"
        @click="handleEnroll"
      >
        选课
      </el-button>
      <el-button 
        v-else-if="isEnrolled"
        type="danger" 
        size="small"
        @click="handleCancelEnrollment"
      >
        取消
      </el-button>
      <span v-else-if="course.status === 'FULL'" class="status-text full">
        已满
      </span>
      <span v-else-if="course.status === 'DISABLED'" class="status-text disabled">
        不可选
      </span>
    </div>
    <!-- 状态指示器 -->
    <div class="status-indicator" :class="`status-${course.status.toLowerCase()}`"></div>
  </div>
</template>
<script setup lang="ts">
import { computed } from 'vue'
import { Location, User } from '@element-plus/icons-vue'
import type { CourseInfo, Student } from '@/types'
interface Props {
  course: CourseInfo
  currentStudent?: Student
}
const props = defineProps<Props>()
const emit = defineEmits<{
  'enroll-course': [course: CourseInfo]
  'cancel-enrollment': [course: CourseInfo]
}>()
// 判断学生是否已选择此课程
const isEnrolled = computed(() => {
  return false
})
const handleEnroll = () => {
  emit('enroll-course', props.course)
}
const handleCancelEnrollment = () => {
  emit('cancel-enrollment', props.course)
}
</script>
<style lang="scss" scoped>
.course-card {
  position: relative;
  padding: 12px;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
  background: white;
  transition: all 0.3s ease;
  cursor: pointer;
  overflow: hidden;
  margin-bottom: 8px;
  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transform: translateY(-1px);
  }
  &.can-enroll:hover {
    border-color: #409eff;
  }
  &.status-full {
    background: #fef0f0;
    border-color: #f56c6c;
  }
  &.status-available {
    background: #fdf6ec;
    border-color: #e6a23c;
  }
  &.status-empty {
    background: #f0f9ff;
    border-color: #409eff;
  }
  &.status-disabled {
    background: #f5f5f5;
    border-color: #c0c4cc;
    opacity: 0.6;
    cursor: not-allowed;
    &:hover {
      transform: none;
      box-shadow: none;
    }
  }
}
.course-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
  .course-name {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    margin: 0;
    line-height: 1.4;
    flex: 1;
    margin-right: 8px;
  }
  .student-count {
    font-size: 12px;
    color: #909399;
    background: #f5f5f5;
    padding: 2px 6px;
    border-radius: 10px;
    white-space: nowrap;
  }
}
.course-details {
  margin-bottom: 10px;
  .detail-item {
    display: flex;
    align-items: center;
    gap: 4px;
    margin-bottom: 4px;
    font-size: 12px;
    color: #606266;
    .el-icon {
      font-size: 12px;
      color: #909399;
    }
    span {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}
.course-actions {
  display: flex;
  justify-content: center;
  .el-button {
    width: 100%;
    font-size: 12px;
  }
  .status-text {
    font-size: 12px;
    text-align: center;
    padding: 4px 0;
    width: 100%;
    &.full {
      color: #f56c6c;
    }
    &.disabled {
      color: #c0c4cc;
    }
  }
}
.status-indicator {
  position: absolute;
  top: 0;
  right: 0;
  width: 0;
  height: 0;
  border-style: solid;
  &.status-full {
    border-width: 0 12px 12px 0;
    border-color: transparent #f56c6c transparent transparent;
  }
  &.status-available {
    border-width: 0 12px 12px 0;
    border-color: transparent #e6a23c transparent transparent;
  }
  &.status-empty {
    border-width: 0 12px 12px 0;
    border-color: transparent #67c23a transparent transparent;
  }
  &.status-disabled {
    border-width: 0 12px 12px 0;
    border-color: transparent #c0c4cc transparent transparent;
  }
}
</style>
