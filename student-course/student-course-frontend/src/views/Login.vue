<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>学生登录</h2>
          <p class="subtitle">欢迎使用学生选课系统</p>
        </div>
      </template>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="studentId">
          <el-input
            v-model="loginForm.studentId"
            placeholder="请输入学号"
            size="large"
            clearable
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            clearable
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-button"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>

        <el-form-item>
          <div class="footer-links">
            <el-link type="primary" @click="handleRegister">注册新账号</el-link>
            <el-link type="info">忘记密码?</el-link>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { authApi } from '@/api'

const router = useRouter()
const loginFormRef = ref<FormInstance>()
const loading = ref(false)

interface LoginForm {
  studentId: string
  password: string
  rememberMe: boolean
}

const loginForm = reactive<LoginForm>({
  studentId: '',
  password: '',
  rememberMe: false
})

const loginRules: FormRules<LoginForm> = {
  studentId: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { min: 3, max: 20, message: '学号长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate()
    loading.value = true

    const response = await authApi.login({
      studentId: loginForm.studentId,
      password: loginForm.password
    })

    // 直接使用 response.token，不需要 .data
    localStorage.setItem('token', response.token)
    localStorage.setItem('studentInfo', JSON.stringify(response.student))

    if (loginForm.rememberMe) {
      localStorage.setItem('rememberMe', 'true')
      localStorage.setItem('savedStudentId', loginForm.studentId)
    } else {
      localStorage.removeItem('rememberMe')
      localStorage.removeItem('savedStudentId')
    }

    ElMessage.success('登录成功')
    router.push('/course-selection')
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '登录失败，请检查学号和密码')
  } finally {
    loading.value = false
  }
}

const handleRegister = () => {
  router.push('/student-management')
}

// 加载记住的学号
const loadRememberedCredentials = () => {
  const rememberMe = localStorage.getItem('rememberMe')
  if (rememberMe === 'true') {
    const savedStudentId = localStorage.getItem('savedStudentId')
    if (savedStudentId) {
      loginForm.studentId = savedStudentId
      loginForm.rememberMe = true
    }
  }
}

loadRememberedCredentials()
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 420px;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.card-header {
  text-align: center;
  padding: 10px 0;
}

.card-header h2 {
  margin: 0;
  color: #303133;
  font-size: 28px;
  font-weight: 600;
}

.subtitle {
  margin: 8px 0 0;
  color: #909399;
  font-size: 14px;
}

.login-form {
  padding: 20px 0;
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
}

.footer-links {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

:deep(.el-card__header) {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-card__body) {
  padding: 30px;
}
</style>
