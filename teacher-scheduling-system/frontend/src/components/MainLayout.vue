<template>
  <el-container class="main-layout">
    <!-- 顶部导航栏 -->
    <el-header class="main-header">
      <div class="header-left">
        <h2 class="system-title">教师排课管理系统</h2>
      </div>
      <div class="header-right">
        <el-dropdown>
          <span class="user-dropdown">
            <el-icon><User /></el-icon>
            <span class="username">{{ currentUser }}</span>
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="handleProfile">个人信息</el-dropdown-item>
              <el-dropdown-item @click="handleChangePassword">修改密码</el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-container class="main-container">
      <!-- 左侧菜单栏 -->
      <el-aside width="240px" class="main-aside">
        <el-menu
          :default-active="activeMenu"
          class="main-menu"
          router
          @select="handleMenuSelect"
        >
          <!-- 排课管理 -->
          <el-menu-item index="/schedule">
            <el-icon><Calendar /></el-icon>
            <span>排课管理</span>
          </el-menu-item>

          <!-- 数据管理 -->
          <el-sub-menu index="data-management">
            <template #title>
              <el-icon><Folder /></el-icon>
              <span>数据管理</span>
            </template>
            <el-menu-item index="/timeslot">时间段管理</el-menu-item>
            <el-menu-item index="/classroom">教室管理</el-menu-item>
            <el-menu-item index="/teacher">教师管理</el-menu-item>
          </el-sub-menu>

          <!-- 数据查询 -->
          <el-sub-menu index="data-query">
            <template #title>
              <el-icon><Search /></el-icon>
              <span>数据查询</span>
            </template>
            <el-menu-item index="/data-conflict">数据异常查询</el-menu-item>
          </el-sub-menu>

          <!-- 系统管理（仅管理员可见） -->
          <el-sub-menu v-if="isAdmin" index="system-management">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/user-management">用户管理</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>

    <!-- 个人信息对话框 -->
    <el-dialog v-model="profileDialogVisible" title="个人信息" width="500px">
      <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="profileForm.username" :disabled="!isAdmin" />
        </el-form-item>
        <el-form-item label="显示名称" prop="name">
          <el-input v-model="profileForm.name" placeholder="请输入显示名称" />
        </el-form-item>
        <el-form-item label="角色">
          <el-tag>{{ getRoleText(authStore.userInfo.role) }}</el-tag>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="profileDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitProfile">保存</el-button>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="500px">
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入旧密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitPassword">确定</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { User, ArrowDown, Calendar, Folder, Search, Setting } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import request from '@/api/request'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const currentUser = computed(() => {
  return authStore.userInfo?.name || authStore.userInfo?.username || '管理员'
})

const isAdmin = computed(() => {
  return authStore.userInfo?.role === 'admin'
})

const activeMenu = computed(() => route.path)

// 个人信息对话框
const profileDialogVisible = ref(false)
const profileFormRef = ref<FormInstance>()
const profileForm = reactive({
  username: '',
  name: ''
})

const profileRules = reactive<FormRules>({
  name: [
    { required: true, message: '请输入显示名称', trigger: 'blur' }
  ]
})

// 修改密码对话框
const passwordDialogVisible = ref(false)
const passwordFormRef = ref<FormInstance>()
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码验证规则
const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = reactive<FormRules>({
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
})

const handleMenuSelect = (index: string) => {
  // 菜单选择时自动路由跳转
}

const handleProfile = () => {
  // 初始化表单数据
  profileForm.username = authStore.userInfo?.username || ''
  profileForm.name = authStore.userInfo?.name || ''
  profileDialogVisible.value = true
}

const handleSubmitProfile = async () => {
  if (!profileFormRef.value) return

  await profileFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await request.post('/auth/update-profile', {
          name: profileForm.name
        })

        // 更新本地用户信息
        if (authStore.userInfo) {
          authStore.userInfo.name = profileForm.name
          const storedUserInfo = localStorage.getItem('userInfo') || sessionStorage.getItem('userInfo')
          if (storedUserInfo) {
            const userInfo = JSON.parse(storedUserInfo)
            userInfo.name = profileForm.name
            if (localStorage.getItem('userInfo')) {
              localStorage.setItem('userInfo', JSON.stringify(userInfo))
            } else {
              sessionStorage.setItem('userInfo', JSON.stringify(userInfo))
            }
          }
        }

        ElMessage.success('个人信息修改成功')
        profileDialogVisible.value = false
      } catch (error: any) {
        ElMessage.error(error.message || '修改个人信息失败')
      }
    }
  })
}

const handleChangePassword = () => {
  passwordDialogVisible.value = true
  // 重置表单
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

const handleSubmitPassword = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await request.post('/auth/change-password', {
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })

        ElMessage.success('密码修改成功，请重新登录')
        passwordDialogVisible.value = false

        // 退出登录
        setTimeout(() => {
          authStore.logout()
          router.push('/login')
        }, 1500)
      } catch (error: any) {
        ElMessage.error(error.message || '修改密码失败')
      }
    }
  })
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    authStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch {
    // 用户取消
  }
}

const getRoleText = (role: string) => {
  const roleMap: Record<string, string> = {
    admin: '管理员',
    teacher: '教师',
    student: '学生'
  }
  return roleMap[role] || role
}

onMounted(() => {
  // 恢复用户信息
  if (!authStore.userInfo) {
    authStore.restoreUserInfo()
  }
})
</script>

<style scoped lang="scss">
.main-layout {
  height: 100vh;
  width: 100%;
}

.main-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  .header-left {
    .system-title {
      margin: 0;
      font-size: 20px;
      font-weight: 600;
    }
  }

  .header-right {
    .user-dropdown {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      padding: 8px 16px;
      border-radius: 6px;
      transition: background-color 0.3s;

      &:hover {
        background-color: rgba(255, 255, 255, 0.1);
      }

      .username {
        font-size: 14px;
        color: white;
      }
    }
  }
}

.main-container {
  height: calc(100vh - 60px);
}

.main-aside {
  background: #f5f7fa;
  border-right: 1px solid #e4e7ed;
  overflow-y: auto;

  .main-menu {
    border: none;
    background: transparent;
  }
}

.main-content {
  background: #f0f2f5;
  overflow-y: auto;
}

// Element Plus 菜单样式覆盖
:deep(.el-menu-item) {
  &.is-active {
    background-color: #ecf5ff !important;
    color: #409eff !important;
  }
}

:deep(.el-sub-menu__title) {
  &:hover {
    background-color: #ecf5ff !important;
  }
}
</style>
