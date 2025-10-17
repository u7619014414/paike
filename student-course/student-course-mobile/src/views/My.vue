<template>
  <div class="my-page">
    <!-- 顶部导航栏 -->
    <van-nav-bar title="我的" fixed placeholder />

    <!-- 用户信息卡片 -->
    <div class="user-card">
      <div class="user-avatar">
        <van-icon name="user-circle-o" size="60" />
      </div>
      <div v-if="userStore.student" class="user-info">
        <h3 class="user-name">{{ userStore.student.studentName }}</h3>
        <p class="user-code">学号: {{ userStore.student.studentCode }}</p>
      </div>
    </div>

    <!-- 我的课程统计 -->
    <div class="course-stats">
      <div class="stat-item">
        <div class="stat-value">{{ enrolledCount }}</div>
        <div class="stat-label">已选课程</div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <div class="stat-value">{{ getAgeGroupLabel(userStore.student?.ageGroup) }}</div>
        <div class="stat-label">年龄组</div>
      </div>
    </div>

    <!-- 菜单列表 -->
    <van-cell-group inset>
      <van-cell title="我的信息" icon="user-o" is-link @click="showInfo = true" />
      <van-cell title="我的课程" icon="orders-o" is-link @click="showMyCourses = true" />
      <van-cell title="家长信息" icon="friends-o" is-link @click="showParentInfo = true" />
    </van-cell-group>

    <!-- 设置选项 -->
    <van-cell-group inset>
      <van-cell title="关于系统" icon="info-o" is-link @click="showAbout = true" />
      <van-cell title="退出登录" icon="revoke" is-link @click="handleLogout" />
    </van-cell-group>

    <!-- 版本信息 -->
    <div class="version-info">
      <p>学生选课系统 v1.0.0</p>
      <p>移动端版本</p>
    </div>

    <!-- 底部导航 -->
    <van-tabbar v-model="activeTab" fixed placeholder>
      <van-tabbar-item icon="orders-o" to="/course-selection">选课</van-tabbar-item>
      <van-tabbar-item icon="contact" to="/student-management">学生</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/my">我的</van-tabbar-item>
    </van-tabbar>

    <!-- 我的信息弹窗 -->
    <van-popup v-model:show="showInfo" position="bottom" round :style="{ height: '60%' }">
      <div v-if="userStore.student" class="popup-content">
        <div class="popup-header">
          <h3>我的信息</h3>
          <van-icon name="cross" @click="showInfo = false" />
        </div>
        <van-cell-group inset>
          <van-cell title="学号" :value="userStore.student.studentCode" />
          <van-cell title="姓名" :value="userStore.student.studentName" />
          <van-cell title="年龄" :value="`${userStore.student.age}岁`" />
          <van-cell title="年龄组" :value="getAgeGroupLabel(userStore.student.ageGroup)" />
          <van-cell title="状态">
            <template #value>
              <van-tag :type="userStore.student.isActive ? 'success' : 'default'">
                {{ userStore.student.isActive ? '在读' : '停用' }}
              </van-tag>
            </template>
          </van-cell>
        </van-cell-group>
      </div>
    </van-popup>

    <!-- 家长信息弹窗 -->
    <van-popup v-model:show="showParentInfo" position="bottom" round :style="{ height: '50%' }">
      <div v-if="userStore.student" class="popup-content">
        <div class="popup-header">
          <h3>家长信息</h3>
          <van-icon name="cross" @click="showParentInfo = false" />
        </div>
        <van-cell-group inset>
          <van-cell title="家长姓名" :value="userStore.student.parentName" />
          <van-cell title="联系电话" :value="userStore.student.parentPhone">
            <template #value>
              <a :href="`tel:${userStore.student.parentPhone}`" class="phone-link">
                {{ userStore.student.parentPhone }}
              </a>
            </template>
          </van-cell>
          <van-cell
            v-if="userStore.student.parentEmail"
            title="邮箱地址"
            :value="userStore.student.parentEmail"
          />
        </van-cell-group>
      </div>
    </van-popup>

    <!-- 我的课程弹窗 -->
    <van-popup v-model:show="showMyCourses" position="bottom" round :style="{ height: '70%' }">
      <div class="popup-content">
        <div class="popup-header">
          <h3>我的课程</h3>
          <van-icon name="cross" @click="showMyCourses = false" />
        </div>

        <van-pull-refresh v-model="refreshing" @refresh="loadMyCourses">
          <div class="my-courses-list">
            <van-empty v-if="!loading && myCourses.length === 0" description="暂无已选课程" />

            <div v-for="course in myCourses" :key="course.scheduleId" class="course-item">
              <div class="course-name">{{ course.courseName }}</div>
              <div class="course-detail">
                <van-tag type="primary" size="small">{{ course.classroomName }}</van-tag>
                <span class="course-time">
                  {{ course.dayName }} {{ course.startTime }}-{{ course.endTime }}
                </span>
              </div>
            </div>
          </div>
        </van-pull-refresh>
      </div>
    </van-popup>

    <!-- 关于系统弹窗 -->
    <van-popup v-model:show="showAbout" position="center" round :style="{ width: '80%' }">
      <div class="about-content">
        <van-icon name="info-o" size="60" color="#667eea" />
        <h3>学生选课系统</h3>
        <p>移动端版本 v1.0.0</p>
        <p class="about-desc">
          这是一个简约易用的学生选课管理系统,支持课程浏览、在线选课、学生管理等功能。
        </p>
        <van-button type="primary" round block @click="showAbout = false">
          知道了
        </van-button>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showSuccessToast } from 'vant'
import dayjs from 'dayjs'
import { courseApi, enrollmentApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { AGE_GROUPS, WEEK_DAYS } from '@/types'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref(2)
const loading = ref(false)
const refreshing = ref(false)

const showInfo = ref(false)
const showParentInfo = ref(false)
const showMyCourses = ref(false)
const showAbout = ref(false)

const enrolledCount = ref(0)
const myCourses = ref<any[]>([])

// 获取年龄组标签
const getAgeGroupLabel = (ageGroup?: number) => {
  if (!ageGroup) return ''
  const group = AGE_GROUPS.find(g => g.value === ageGroup)
  return group?.label || ''
}

// 加载我的课程
const loadMyCourses = async () => {
  if (!userStore.student) return

  loading.value = true
  try {
    const enrolledIds = await enrollmentApi.getStudentEnrollments(userStore.student.id)
    enrolledCount.value = enrolledIds.length

    // 获取课程详情
    const startDate = dayjs().startOf('week').format('YYYY-MM-DD')
    const schedules = await courseApi.getScheduleView(userStore.student.ageGroup, startDate)

    // 提取已选课程
    const courses: any[] = []
    schedules.forEach(daySchedule => {
      daySchedule.timeSlots.forEach(timeSlot => {
        timeSlot.courses.forEach(course => {
          if (enrolledIds.includes(course.scheduleId)) {
            courses.push({
              ...course,
              dayName: daySchedule.dayName,
              startTime: timeSlot.startTime,
              endTime: timeSlot.endTime
            })
          }
        })
      })
    })

    myCourses.value = courses
  } catch (error) {
    console.error('加载我的课程失败:', error)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

// 退出登录
const handleLogout = async () => {
  try {
    await showConfirmDialog({
      title: '确认退出',
      message: '确定要退出登录吗?'
    })

    userStore.logout()
    showSuccessToast('已退出登录')
    router.push('/login')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('退出登录失败:', error)
    }
  }
}

onMounted(() => {
  loadMyCourses()
})
</script>

<style lang="scss" scoped>
.my-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 60px;
}

.user-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 30px 20px;
  display: flex;
  align-items: center;
  gap: 20px;

  .user-avatar {
    .van-icon {
      color: white;
    }
  }

  .user-info {
    color: white;

    .user-name {
      font-size: 22px;
      font-weight: bold;
      margin: 0 0 8px 0;
    }

    .user-code {
      font-size: 14px;
      opacity: 0.9;
      margin: 0;
    }
  }
}

.course-stats {
  background: white;
  margin: -20px 16px 16px;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-around;

  .stat-item {
    text-align: center;

    .stat-value {
      font-size: 24px;
      font-weight: bold;
      color: #667eea;
      margin-bottom: 8px;
    }

    .stat-label {
      font-size: 13px;
      color: #969799;
    }
  }

  .stat-divider {
    width: 1px;
    height: 40px;
    background: #ebedf0;
  }
}

.van-cell-group {
  margin-bottom: 12px;
}

.version-info {
  text-align: center;
  padding: 30px 20px;
  color: #969799;
  font-size: 12px;

  p {
    margin: 4px 0;
  }
}

.popup-content {
  padding: 20px;
  height: 100%;
  overflow-y: auto;

  .popup-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20px;

    h3 {
      font-size: 18px;
      font-weight: bold;
      margin: 0;
    }

    .van-icon {
      font-size: 20px;
      cursor: pointer;
    }
  }

  .phone-link {
    color: #667eea;
    text-decoration: none;

    &:active {
      opacity: 0.7;
    }
  }
}

.my-courses-list {
  .course-item {
    background: #f7f8fa;
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 12px;

    .course-name {
      font-size: 16px;
      font-weight: bold;
      color: #323233;
      margin-bottom: 8px;
    }

    .course-detail {
      display: flex;
      align-items: center;
      gap: 8px;

      .course-time {
        font-size: 13px;
        color: #646566;
      }
    }
  }
}

.about-content {
  padding: 30px 20px;
  text-align: center;

  .van-icon {
    margin-bottom: 16px;
  }

  h3 {
    font-size: 20px;
    font-weight: bold;
    margin: 0 0 8px 0;
  }

  p {
    margin: 4px 0;
    color: #969799;
    font-size: 14px;
  }

  .about-desc {
    margin: 16px 0 24px;
    color: #646566;
    line-height: 1.6;
  }

  .van-button {
    margin-top: 20px;
  }
}
</style>
