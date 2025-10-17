import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Student } from '@/types'

export const useUserStore = defineStore('user', () => {
  const student = ref<Student | null>(null)
  const token = ref<string>('')

  // 从本地存储恢复状态
  const init = () => {
    const savedToken = localStorage.getItem('token')
    const savedStudent = localStorage.getItem('student')

    if (savedToken) {
      token.value = savedToken
    }

    if (savedStudent) {
      try {
        student.value = JSON.parse(savedStudent)
      } catch (e) {
        console.error('Failed to parse student data:', e)
      }
    }
  }

  // 登录
  const login = (studentData: Student, tokenData: string) => {
    student.value = studentData
    token.value = tokenData
    localStorage.setItem('token', tokenData)
    localStorage.setItem('student', JSON.stringify(studentData))
  }

  // 登出
  const logout = () => {
    student.value = null
    token.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('student')
  }

  // 更新学生信息
  const updateStudent = (studentData: Student) => {
    student.value = studentData
    localStorage.setItem('student', JSON.stringify(studentData))
  }

  // 是否已登录
  const isLoggedIn = computed(() => !!token.value && !!student.value)

  return {
    student,
    token,
    isLoggedIn,
    init,
    login,
    logout,
    updateStudent
  }
})
