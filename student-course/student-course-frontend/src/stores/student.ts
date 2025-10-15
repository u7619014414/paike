import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Student } from '@/types'
import { studentApi } from '@/api'
export const useStudentStore = defineStore('student', () => {
  const students = ref<Student[]>([])
  const currentStudent = ref<Student | null>(null)
  const loading = ref(false)
  // 获取所有学生
  const fetchStudents = async () => {
    loading.value = true
    try {
      students.value = await studentApi.getAllStudents()
    } catch (error) {
      console.error('获取学生列表失败:', error)
    } finally {
      loading.value = false
    }
  }
  // 设置当前学生
  const setCurrentStudent = (student: Student | null) => {
    currentStudent.value = student
  }
  // 注册学生
  const registerStudent = async (data: any) => {
    try {
      const newStudent = await studentApi.register(data)
      students.value.push(newStudent)
      return newStudent
    } catch (error) {
      throw error
    }
  }
  return {
    students,
    currentStudent,
    loading,
    fetchStudents,
    setCurrentStudent,
    registerStudent
  }
})
