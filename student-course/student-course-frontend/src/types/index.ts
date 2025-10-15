export interface Student {
  id: number
  studentCode: string
  studentName: string
  age: number
  ageGroup: number
  parentName: string
  parentPhone: string
  parentEmail?: string
  registrationStatus: number
  isActive: boolean
  createdAt: string
}

export interface StudentRegistration {
  studentName: string
  age: number
  parentName: string
  parentPhone: string
  parentEmail?: string
}

export interface CourseScheduleView {
  dayOfWeek: number
  dayName: string
  timeSlots: TimeSlotView[]
}

export interface TimeSlotView {
  timeSlotId: number
  slotName: string
  startTime: string
  endTime: string
  courses: CourseInfo[]
}

export interface CourseInfo {
  courseId: number
  scheduleId: number
  courseName: string
  classroomName: string
  teacherNames: string[]
  maxStudents: number
  currentStudents: number
  status: 'FULL' | 'AVAILABLE' | 'EMPTY' | 'DISABLED'
  ageGroup: number
  canEnroll: boolean
}

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

export const AGE_GROUPS = [
  { value: 1, label: '学前班(1-3岁)' },
  { value: 2, label: '小班(3-6岁)' },
  { value: 3, label: '中班(6-9岁)' },
  { value: 4, label: '大班(9-12岁)' }
]

export const WEEK_DAYS = [
  '周一', '周二', '周三', '周四', '周五', '周六', '周日'
]
