<template>
  <div id="app">
    <el-container v-if="$route.path !== '/login'">
      <el-header height="60px">
        <div class="header">
          <h1>学生选课系统</h1>
          <nav class="nav">
            <el-button
              type="text"
              @click="$router.push('/course-selection')"
              :class="{ active: $route.path === '/course-selection' }"
            >
              学生选课
            </el-button>
            <el-button
              type="text"
              @click="$router.push('/student-management')"
              :class="{ active: $route.path === '/student-management' }"
            >
              学生管理
            </el-button>
            <el-button
              type="text"
              @click="handleLogout"
              class="logout-btn"
            >
              退出登录
            </el-button>
          </nav>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
    <router-view v-else />
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'

const router = useRouter()

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    localStorage.removeItem('token')
    localStorage.removeItem('studentInfo')
    router.push('/login')
  } catch {
    // 用户取消退出
  }
}
</script>
<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  padding: 0 20px;
  background: #409eff;
  color: white;
}
.header h1 {
  margin: 0;
  font-size: 20px;
}
.nav .el-button {
  color: white;
  margin-left: 20px;
}
.nav .el-button.active,
.nav .el-button:hover {
  color: #67c23a;
}
.logout-btn:hover {
  color: #f56c6c !important;
}
</style>
