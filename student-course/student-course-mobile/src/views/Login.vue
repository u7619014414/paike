<template>
  <div class="login-page">
    <div class="login-header">
      <h1 class="login-title">学生选课系统</h1>
      <p class="login-subtitle">欢迎登录</p>
    </div>

    <div class="login-form">
      <van-form @submit="handleLogin">
        <van-cell-group inset>
          <van-field
            v-model="loginForm.studentId"
            name="学号"
            label="学号"
            placeholder="请输入学号"
            :rules="[{ required: true, message: '请输入学号' }]"
            clearable
          />
          <van-field
            v-model="loginForm.password"
            type="password"
            name="密码"
            label="密码"
            placeholder="请输入密码"
            :rules="[{ required: true, message: '请输入密码' }]"
            clearable
          />
        </van-cell-group>

        <div class="login-actions">
          <van-button
            round
            block
            type="primary"
            native-type="submit"
            :loading="loading"
          >
            登录
          </van-button>
        </div>
      </van-form>

      <div class="register-link">
        <van-button
          type="default"
          size="small"
          plain
          @click="showRegister = true"
        >
          没有账号?立即注册
        </van-button>
      </div>
    </div>

    <!-- 注册弹窗 -->
    <van-popup
      v-model:show="showRegister"
      position="bottom"
      round
      :style="{ height: '70%' }"
    >
      <div class="register-popup">
        <div class="popup-header">
          <h3>学生注册</h3>
          <van-icon name="cross" @click="showRegister = false" />
        </div>

        <van-form @submit="handleRegister">
          <van-cell-group inset>
            <van-field
              v-model="registerForm.studentName"
              name="学生姓名"
              label="学生姓名"
              placeholder="请输入学生姓名"
              :rules="[{ required: true, message: '请输入学生姓名' }]"
              clearable
            />
            <van-field
              v-model.number="registerForm.age"
              type="number"
              name="年龄"
              label="年龄"
              placeholder="请输入年龄"
              :rules="[{ required: true, message: '请输入年龄' }]"
              clearable
            />
            <van-field
              v-model="registerForm.parentName"
              name="家长姓名"
              label="家长姓名"
              placeholder="请输入家长姓名"
              :rules="[{ required: true, message: '请输入家长姓名' }]"
              clearable
            />
            <van-field
              v-model="registerForm.parentPhone"
              type="tel"
              name="家长电话"
              label="家长电话"
              placeholder="请输入家长电话"
              :rules="[
                { required: true, message: '请输入家长电话' },
                { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' }
              ]"
              clearable
            />
            <van-field
              v-model="registerForm.parentEmail"
              type="email"
              name="家长邮箱"
              label="家长邮箱"
              placeholder="请输入家长邮箱(可选)"
              clearable
            />
          </van-cell-group>

          <div class="register-actions">
            <van-button
              round
              block
              type="primary"
              native-type="submit"
              :loading="registerLoading"
            >
              注册
            </van-button>
          </div>
        </van-form>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showSuccessToast } from 'vant'
import { authApi, studentApi } from '@/api'
import { useUserStore } from '@/stores/user'
import type { StudentRegistration } from '@/types'

const router = useRouter()
const userStore = useUserStore()

const loginForm = ref({
  studentId: '',
  password: ''
})

const registerForm = ref<StudentRegistration>({
  studentName: '',
  age: 0,
  parentName: '',
  parentPhone: '',
  parentEmail: ''
})

const loading = ref(false)
const registerLoading = ref(false)
const showRegister = ref(false)

// 登录
const handleLogin = async () => {
  if (!loginForm.value.studentId || !loginForm.value.password) {
    showToast('请填写完整信息')
    return
  }

  loading.value = true
  try {
    const result = await authApi.login({
      studentId: loginForm.value.studentId,
      password: loginForm.value.password
    })

    // 保存登录信息
    userStore.login(result.student, result.token)

    showSuccessToast('登录成功')
    router.push('/course-selection')
  } catch (error: any) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}

// 注册
const handleRegister = async () => {
  registerLoading.value = true
  try {
    const student = await studentApi.register(registerForm.value)

    showSuccessToast({
      message: `注册成功!您的学号是: ${student.studentCode}`,
      duration: 3000
    })

    // 关闭弹窗,清空表单
    showRegister.value = false
    registerForm.value = {
      studentName: '',
      age: 0,
      parentName: '',
      parentPhone: '',
      parentEmail: ''
    }
  } catch (error: any) {
    console.error('注册失败:', error)
  } finally {
    registerLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-header {
  text-align: center;
  color: white;
  margin-bottom: 40px;

  .login-title {
    font-size: 32px;
    font-weight: bold;
    margin-bottom: 10px;
  }

  .login-subtitle {
    font-size: 16px;
    opacity: 0.9;
  }
}

.login-form {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);

  :deep(.van-cell-group) {
    margin-bottom: 20px;
  }
}

.login-actions {
  margin-top: 30px;

  :deep(.van-button) {
    height: 44px;
    font-size: 16px;
  }
}

.register-link {
  margin-top: 20px;
  text-align: center;
}

.register-popup {
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
    }

    .van-icon {
      font-size: 20px;
      cursor: pointer;
    }
  }

  :deep(.van-cell-group) {
    margin-bottom: 20px;
  }

  .register-actions {
    margin-top: 30px;

    :deep(.van-button) {
      height: 44px;
      font-size: 16px;
    }
  }
}
</style>
