<template>
  <div class="course-selection-page">
    <div class="page-header">
      <div class="header-content">
        <div>
          <h1>学生选课</h1>
          <p class="page-description" v-if="currentStudent">
            {{ currentStudent.studentName }} ({{ getAgeGroupLabel(currentStudent.ageGroup) }})
          </p>
        </div>
        <el-button 
          type="danger" 
          @click="handleLogout"
        >
          退出登录
        </el-button>
      </div>
    </div>
    
    <!-- 课程表 -->
    <div class="schedule-container" v-if="currentStudent">
      <CourseScheduleGrid 
        :current-student="currentStudent"
        @enroll-success="handleEnrollSuccess"
        @cancel-success="handleCancelSuccess"
      />
    </div>
    
    <!-- 空状态 -->
    <div class="empty-state" v-else>
      <el-empty description="未找到登录学生信息">
        <el-button 
          type="primary" 
          @click="handleLogout"
        >
          返回登录
        </el-button>
      </el-empty>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import CourseScheduleGrid from '@/components/CourseScheduleGrid.vue'
import { useStudentStore } from '@/stores/student'
import type { Student } from '@/types'
import { AGE_GROUPS } from '@/types'

const router = useRouter()
const studentStore = useStudentStore()
const currentStudent = ref<Student | null>(null)

// 从localStorage加载登录的学生信息
const loadCurrentStudent = () => {
  const studentInfoStr = localStorage.getItem('studentInfo')
  if (studentInfoStr) {
    try {
      currentStudent.value = JSON.parse(studentInfoStr)
      // 同时更新store中的当前学生
      studentStore.setCurrentStudent(currentStudent.value)
    } catch (error) {
      console.error('解析学生信息失败:', error)
      ElMessage.error('获取学生信息失败，请重新登录')
      handleLogout()
    }
  } else {
    ElMessage.error('未找到登录信息，请重新登录')
    router.push('/login')
  }
}

const handleEnrollSuccess = () => {
  ElMessage.success('选课成功！')
}

const handleCancelSuccess = () => {
  ElMessage.success('取消选课成功！')
}

const getAgeGroupLabel = (ageGroup: number): string => {
  return AGE_GROUPS.find(g => g.value === ageGroup)?.label || ''
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 清除登录信息
    localStorage.removeItem('token')
    localStorage.removeItem('studentInfo')
    studentStore.setCurrentStudent(null)
    
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch {
    // 用户取消
  }
}

onMounted(() => {
  loadCurrentStudent()
})
</script>
<style lang="scss" scoped>
.course-selection-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
  
  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  h1 {
    color: #303133;
    margin: 0 0 8px 0;
  }
  
  .page-description {
    color: #909399;
    margin: 0;
    font-size: 14px;
  }
}

.schedule-container {
  margin-top: 20px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}
</style>
