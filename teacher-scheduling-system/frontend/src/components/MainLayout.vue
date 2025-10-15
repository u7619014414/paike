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
              <el-dropdown-item @click="handleSettings">系统设置</el-dropdown-item>
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
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, ArrowDown, Calendar, Folder, Search } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const currentUser = ref('管理员')

const activeMenu = computed(() => route.path)

const handleMenuSelect = (index: string) => {
  // 菜单选择时自动路由跳转
}

const handleProfile = () => {
  ElMessage.info('个人信息功能开发中')
}

const handleSettings = () => {
  ElMessage.info('系统设置功能开发中')
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // TODO: 清除登录状态
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch {
    // 用户取消
  }
}

onMounted(() => {
  // TODO: 获取当前登录用户信息
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
