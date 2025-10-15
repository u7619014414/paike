import request from './request'
import type { CourseSchedule, PageResult } from '@/types'

export const scheduleApi = {
  createSchedule: (data: any) =>
    request.post<CourseSchedule>('/schedules', data),

  updateSchedule: (id: number, data: any) =>
    request.put<CourseSchedule>(`/schedules/${id}`, data),

  getScheduleList: (params: any) =>
    request.get<PageResult<CourseSchedule>>('/schedules', { params }),

  cancelSchedule: (id: number, reason: string) =>
    request.put(`/schedules/${id}/cancel`, null, { params: { reason } }),

  deleteSchedule: (id: number) =>
    request.delete(`/schedules/${id}`),

  assignTeachers: (data: any) =>
    request.post('/schedules/assign', data)
}
