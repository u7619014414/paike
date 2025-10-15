import request from './request'
import type { Student, StudentRegistration, CourseScheduleView } from '@/types'

// 认证相关API
export const authApi = {
  // 学生登录
  login: (data: { studentId: string; password: string }) =>
    request.post<{ token: string; student: Student }>('/auth/login', data),
  // 退出登录
  logout: () =>
    request.post('/auth/logout'),
  // 验证token
  validateToken: () =>
    request.get<Student>('/auth/validate')
}

// 学生相关API
export const studentApi = {
  // 学生注册
  register: (data: StudentRegistration) =>
    request.post<Student>('/students/register', data),
  // 获取所有学生
  getAllStudents: () =>
    request.get<Student[]>('/students'),
  // 根据年龄组获取学生
  getStudentsByAgeGroup: (ageGroup: number) =>
    request.get<Student[]>(`/students/age-group/${ageGroup}`),
  // 获取学生详情
  getStudent: (id: number) =>
    request.get<Student>(`/students/${id}`),
  // 更新学生信息
  updateStudent: (id: number, data: Partial<Student>) =>
    request.put<Student>(`/students/${id}`, data)
}

// 课程相关API
export const courseApi = {
  // 获取课程表视图
  getScheduleView: (studentAgeGroup: number, startDate: string) =>
    request.get<CourseScheduleView[]>('/courses/schedule', {
      params: { studentAgeGroup, startDate }
    })
}

// 选课相关API
export const enrollmentApi = {
  // 学生选课
  enrollCourse: (studentId: number, courseScheduleId: number) =>
    request.post('/enrollments', null, {
      params: { studentId, courseScheduleId }
    }),
  // 取消选课
  cancelEnrollment: (studentId: number, courseScheduleId: number) =>
    request.delete('/enrollments', {
      params: { studentId, courseScheduleId }
    })
}
