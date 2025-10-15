import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { CourseScheduleView } from '@/types'
import { courseApi, enrollmentApi } from '@/api'
export const useCourseStore = defineStore('course', () => {
  const scheduleData = ref<CourseScheduleView[]>([])
  const loading = ref(false)
  // 获取课程表数据
  const fetchScheduleView = async (studentAgeGroup: number, startDate: string) => {
    loading.value = true
    try {
      scheduleData.value = await courseApi.getScheduleView(studentAgeGroup, startDate)
    } catch (error) {
      console.error('获取课程表失败:', error)
    } finally {
      loading.value = false
    }
  }
  // 学生选课
  const enrollCourse = async (studentId: number, courseScheduleId: number) => {
    try {
      await enrollmentApi.enrollCourse(studentId, courseScheduleId)
      return true
    } catch (error) {
      throw error
    }
  }
  // 取消选课
  const cancelEnrollment = async (studentId: number, courseScheduleId: number) => {
    try {
      await enrollmentApi.cancelEnrollment(studentId, courseScheduleId)
      return true
    } catch (error) {
      throw error
    }
  }
  return {
    scheduleData,
    loading,
    fetchScheduleView,
    enrollCourse,
    cancelEnrollment
  }
})
