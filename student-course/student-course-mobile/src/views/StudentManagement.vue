<template>
  <div class="student-management-page">
    <!-- 顶部导航栏 -->
    <van-nav-bar title="学生管理" fixed placeholder />

    <!-- 搜索栏 -->
    <van-search
      v-model="searchKeyword"
      placeholder="搜索学生姓名或学号"
      @search="handleSearch"
      @clear="handleSearch"
    />

    <!-- 筛选器 -->
    <div class="filter-bar">
      <van-dropdown-menu>
        <van-dropdown-item v-model="selectedAgeGroup" :options="ageGroupOptions" @change="handleSearch" />
      </van-dropdown-menu>
    </div>

    <!-- 学生列表 -->
    <van-pull-refresh v-model="refreshing" @refresh="loadStudents">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="loadStudents"
      >
        <div class="student-list">
          <van-empty v-if="!loading && students.length === 0" description="暂无学生数据" />

          <div
            v-for="student in students"
            :key="student.id"
            class="student-card"
            @click="handleStudentClick(student)"
          >
            <div class="student-header">
              <div class="student-name">{{ student.studentName }}</div>
              <van-tag :type="student.isActive ? 'success' : 'default'" size="small">
                {{ student.isActive ? '在读' : '停用' }}
              </van-tag>
            </div>

            <div class="student-info">
              <div class="info-row">
                <span class="label">学号:</span>
                <span class="value">{{ student.studentCode }}</span>
              </div>
              <div class="info-row">
                <span class="label">年龄:</span>
                <span class="value">{{ student.age }}岁</span>
              </div>
              <div class="info-row">
                <span class="label">年龄组:</span>
                <van-tag type="primary" size="small">
                  {{ getAgeGroupLabel(student.ageGroup) }}
                </van-tag>
              </div>
            </div>

            <div class="student-contact">
              <div class="contact-item">
                <van-icon name="user-o" />
                <span>{{ student.parentName }}</span>
              </div>
              <div class="contact-item">
                <van-icon name="phone-o" />
                <span>{{ student.parentPhone }}</span>
              </div>
            </div>
          </div>
        </div>
      </van-list>
    </van-pull-refresh>

    <!-- 底部导航 -->
    <van-tabbar v-model="activeTab" fixed placeholder>
      <van-tabbar-item icon="orders-o" to="/course-selection">选课</van-tabbar-item>
      <van-tabbar-item icon="contact" to="/student-management">学生</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/my">我的</van-tabbar-item>
    </van-tabbar>

    <!-- 学生详情弹窗 -->
    <van-popup
      v-model:show="showDetail"
      position="bottom"
      round
      :style="{ height: '60%' }"
    >
      <div v-if="selectedStudent" class="student-detail">
        <div class="detail-header">
          <h3>学生详情</h3>
          <van-icon name="cross" @click="showDetail = false" />
        </div>

        <van-cell-group inset>
          <van-cell title="学号" :value="selectedStudent.studentCode" />
          <van-cell title="姓名" :value="selectedStudent.studentName" />
          <van-cell title="年龄" :value="`${selectedStudent.age}岁`" />
          <van-cell title="年龄组" :value="getAgeGroupLabel(selectedStudent.ageGroup)" />
          <van-cell title="家长姓名" :value="selectedStudent.parentName" />
          <van-cell title="家长电话" :value="selectedStudent.parentPhone" />
          <van-cell
            v-if="selectedStudent.parentEmail"
            title="家长邮箱"
            :value="selectedStudent.parentEmail"
          />
          <van-cell title="状态">
            <template #value>
              <van-tag :type="selectedStudent.isActive ? 'success' : 'default'">
                {{ selectedStudent.isActive ? '在读' : '停用' }}
              </van-tag>
            </template>
          </van-cell>
          <van-cell title="注册时间" :value="formatDate(selectedStudent.createdAt)" />
        </van-cell-group>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import dayjs from 'dayjs'
import { studentApi } from '@/api'
import { AGE_GROUPS } from '@/types'
import type { Student } from '@/types'

const activeTab = ref(1)
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)

const students = ref<Student[]>([])
const searchKeyword = ref('')
const selectedAgeGroup = ref(0)
const selectedStudent = ref<Student | null>(null)
const showDetail = ref(false)

// 年龄组选项
const ageGroupOptions = computed(() => [
  { text: '全部年龄组', value: 0 },
  ...AGE_GROUPS.map(g => ({ text: g.label, value: g.value }))
])

// 获取年龄组标签
const getAgeGroupLabel = (ageGroup: number) => {
  const group = AGE_GROUPS.find(g => g.value === ageGroup)
  return group?.label || ''
}

// 格式化日期
const formatDate = (dateStr: string) => {
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm')
}

// 加载学生列表
const loadStudents = async () => {
  loading.value = true
  try {
    let result: Student[]

    if (selectedAgeGroup.value > 0) {
      result = await studentApi.getStudentsByAgeGroup(selectedAgeGroup.value)
    } else {
      result = await studentApi.getAllStudents()
    }

    // 应用搜索过滤
    if (searchKeyword.value) {
      const keyword = searchKeyword.value.toLowerCase()
      result = result.filter(s =>
        s.studentName.toLowerCase().includes(keyword) ||
        s.studentCode.toLowerCase().includes(keyword)
      )
    }

    students.value = result
    finished.value = true
  } catch (error) {
    console.error('加载学生列表失败:', error)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  finished.value = false
  students.value = []
  loadStudents()
}

// 学生点击处理
const handleStudentClick = (student: Student) => {
  selectedStudent.value = student
  showDetail.value = true
}

onMounted(() => {
  loadStudents()
})
</script>

<style lang="scss" scoped>
.student-management-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 60px;
}

.filter-bar {
  background: white;
  margin-bottom: 8px;
}

.student-list {
  padding: 12px;
}

.student-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;

  &:active {
    transform: scale(0.98);
  }

  .student-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    padding-bottom: 12px;
    border-bottom: 1px solid #f0f0f0;

    .student-name {
      font-size: 18px;
      font-weight: bold;
      color: #323233;
    }
  }

  .student-info {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 12px;

    .info-row {
      flex: 1 1 calc(50% - 4px);
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      color: #646566;

      .label {
        font-weight: 500;
      }

      .value {
        color: #323233;
      }
    }
  }

  .student-contact {
    display: flex;
    flex-direction: column;
    gap: 6px;

    .contact-item {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 13px;
      color: #646566;

      .van-icon {
        font-size: 16px;
        color: #969799;
      }
    }
  }
}

.student-detail {
  padding: 20px;
  height: 100%;
  overflow-y: auto;

  .detail-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20px;

    h3 {
      font-size: 18px;
      font-weight: bold;
    }

    .van-icon {
      font-size: 20px;
      cursor: pointer;
    }
  }
}
</style>
