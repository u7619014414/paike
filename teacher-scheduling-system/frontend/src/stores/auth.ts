import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authApi } from '@/api/auth'

interface LoginParams {
  username: string
  password: string
  rememberMe?: boolean
}

interface UserInfo {
  id: number
  username: string
  name: string
  role: string
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(null)
  const userInfo = ref<UserInfo | null>(null)

  // 登录
  const login = async (params: LoginParams) => {
    const data = await authApi.login(params)

    token.value = data.token
    userInfo.value = data.userInfo

    // 保存token到localStorage
    if (params.rememberMe) {
      localStorage.setItem('token', data.token)
      localStorage.setItem('userInfo', JSON.stringify(data.userInfo))
    } else {
      sessionStorage.setItem('token', data.token)
      sessionStorage.setItem('userInfo', JSON.stringify(data.userInfo))
    }
  }

  // 登出
  const logout = () => {
    token.value = null
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('userInfo')
  }

  // 检查是否已登录
  const isAuthenticated = () => {
    return !!token.value
  }

  // 从本地存储恢复用户信息
  const restoreUserInfo = () => {
    const storedToken = localStorage.getItem('token') || sessionStorage.getItem('token')
    const storedUserInfo = localStorage.getItem('userInfo') || sessionStorage.getItem('userInfo')

    if (storedToken && storedUserInfo) {
      token.value = storedToken
      userInfo.value = JSON.parse(storedUserInfo)
    }
  }

  return {
    token,
    userInfo,
    login,
    logout,
    isAuthenticated,
    restoreUserInfo
  }
})
