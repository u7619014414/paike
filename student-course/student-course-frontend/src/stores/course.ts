import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { CourseScheduleView } from '@/types'
import { courseApi, enrollmentApi } from '@/api'
export const useCourseStore = defineStore('course', () => {
  const scheduleData = ref<CourseScheduleView[]>([])
  const loading = ref(false)
  const enrolledScheduleIds = ref<Set<number>>(new Set())
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
  // 获取学生已选课程列表
  const fetchStudentEnrollments = async (studentId: number) => {
    try {
      const ids = await enrollmentApi.getStudentEnrollments(studentId)
      enrolledScheduleIds.value = new Set(ids)
    } catch (error) {
      console.error('获取学生已选课程失败:', error)
      enrolledScheduleIds.value = new Set()
    }
  }
  // 检查课程是否已选
  const isEnrolled = (scheduleId: number): boolean => {
    return enrolledScheduleIds.value.has(scheduleId)
  }
  return {
    scheduleData,
    loading,
    enrolledScheduleIds,
    fetchScheduleView,
    enrollCourse,
    cancelEnrollment,
    fetchStudentEnrollments,
    isEnrolled
  }
})
