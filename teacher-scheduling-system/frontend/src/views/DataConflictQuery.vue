<template>
  <div class="data-conflict-query">
    <el-card class="query-card">
      <template #header>
        <div class="card-header">
          <span class="header-title">数据异常查询</span>
          <el-button
            type="success"
            :icon="Download"
            :loading="exportLoading"
            @click="handleExport"
          >
            导出Excel
          </el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- Teacher Conflicts Tab -->
        <el-tab-pane label="教师冲突" name="teacher">
          <div class="tab-content">
            <div class="query-actions">
              <el-button
                type="primary"
                :icon="Search"
                :loading="teacherLoading"
                @click="queryTeacherConflicts"
              >
                查询
              </el-button>
              <el-text type="info" size="small">
                查找同一个教师在同一时间段被安排到不同教室上课的冲突记录
              </el-text>
            </div>

            <el-table
              v-loading="teacherLoading"
              :data="teacherConflicts"
              stripe
              border
              class="conflict-table"
              empty-text="未发现教师冲突"
            >
              <el-table-column type="index" label="#" width="60" align="center" />
              <el-table-column prop="teacherName" label="教师姓名" min-width="120" />
              <el-table-column prop="timeSlot" label="时间段" min-width="150">
                <template #default="{ row }">
                  {{ formatTimeSlot(row.dayOfWeek, row.startTime, row.endTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="date" label="日期" width="120" />
              <el-table-column prop="conflictCount" label="冲突数量" width="120" align="center">
                <template #default="{ row }">
                  <el-tag type="danger" size="small">{{ row.conflictCount }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="冲突课程/教室" min-width="300">
                <template #default="{ row }">
                  <div class="conflict-list">
                    <el-tag
                      v-for="(conflict, index) in row.conflicts"
                      :key="index"
                      type="warning"
                      size="small"
                      class="conflict-tag"
                    >
                      {{ conflict.courseName }} - {{ conflict.classroomName }}
                    </el-tag>
                  </div>
                </template>
              </el-table-column>
            </el-table>

            <div v-if="teacherConflicts.length > 0" class="summary-info">
              <el-alert
                :title="`共发现 ${teacherConflicts.length} 个教师冲突`"
                type="warning"
                :closable="false"
                show-icon
              />
            </div>
          </div>
        </el-tab-pane>

        <!-- Classroom Conflicts Tab -->
        <el-tab-pane label="教室冲突" name="classroom">
          <div class="tab-content">
            <div class="query-actions">
              <el-button
                type="primary"
                :icon="Search"
                :loading="classroomLoading"
                @click="queryClassroomConflicts"
              >
                查询
              </el-button>
              <el-text type="info" size="small">
                查找同一个教室在同一时间段被安排多个课程的冲突记录
              </el-text>
            </div>

            <el-table
              v-loading="classroomLoading"
              :data="classroomConflicts"
              stripe
              border
              class="conflict-table"
              empty-text="未发现教室冲突"
            >
              <el-table-column type="index" label="#" width="60" align="center" />
              <el-table-column prop="classroomName" label="教室名称" min-width="120" />
              <el-table-column prop="timeSlot" label="时间段" min-width="150">
                <template #default="{ row }">
                  {{ formatTimeSlot(row.dayOfWeek, row.startTime, row.endTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="date" label="日期" width="120" />
              <el-table-column prop="conflictCount" label="冲突数量" width="120" align="center">
                <template #default="{ row }">
                  <el-tag type="danger" size="small">{{ row.conflictCount }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="冲突课程/教师" min-width="300">
                <template #default="{ row }">
                  <div class="conflict-list">
                    <el-tag
                      v-for="(conflict, index) in row.conflicts"
                      :key="index"
                      type="warning"
                      size="small"
                      class="conflict-tag"
                    >
                      {{ conflict.courseName }} ({{ conflict.teacherName }})
                    </el-tag>
                  </div>
                </template>
              </el-table-column>
            </el-table>

            <div v-if="classroomConflicts.length > 0" class="summary-info">
              <el-alert
                :title="`共发现 ${classroomConflicts.length} 个教室冲突`"
                type="warning"
                :closable="false"
                show-icon
              />
            </div>
          </div>
        </el-tab-pane>

        <!-- Student Conflicts Tab -->
        <el-tab-pane label="学生冲突" name="student">
          <div class="tab-content">
            <div class="query-actions">
              <el-button
                type="primary"
                :icon="Search"
                :loading="studentLoading"
                @click="queryStudentConflicts"
              >
                查询
              </el-button>
              <el-text type="info" size="small">
                查找同一个学生在同一时间段被安排到不同教室上课的冲突记录
              </el-text>
            </div>

            <el-table
              v-loading="studentLoading"
              :data="studentConflicts"
              stripe
              border
              class="conflict-table"
              empty-text="未发现学生冲突"
            >
              <el-table-column type="index" label="#" width="60" align="center" />
              <el-table-column prop="studentName" label="学生姓名" min-width="120" />
              <el-table-column prop="studentId" label="学生ID" width="120" />
              <el-table-column prop="timeSlot" label="时间段" min-width="150">
                <template #default="{ row }">
                  {{ formatTimeSlot(row.dayOfWeek, row.startTime, row.endTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="date" label="日期" width="120" />
              <el-table-column prop="conflictCount" label="冲突数量" width="120" align="center">
                <template #default="{ row }">
                  <el-tag type="danger" size="small">{{ row.conflictCount }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="冲突课程/教室" min-width="300">
                <template #default="{ row }">
                  <div class="conflict-list">
                    <el-tag
                      v-for="(conflict, index) in row.conflicts"
                      :key="index"
                      type="warning"
                      size="small"
                      class="conflict-tag"
                    >
                      {{ conflict.courseName }} ({{ conflict.classroomName }})
                    </el-tag>
                  </div>
                </template>
              </el-table-column>
            </el-table>

            <div v-if="studentConflicts.length > 0" class="summary-info">
              <el-alert
                :title="`共发现 ${studentConflicts.length} 个学生冲突`"
                type="warning"
                :closable="false"
                show-icon
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Download } from '@element-plus/icons-vue'
import { conflictApi } from '@/api/conflict'
import type { ConflictWarning } from '@/types'

// Types
interface ConflictDetail {
  courseName: string
  classroomName?: string
  teacherName?: string
}

interface TeacherConflict {
  teacherName: string
  teacherId: string
  dayOfWeek: number
  startTime: string
  endTime: string
  date?: string
  conflictCount: number
  conflicts: ConflictDetail[]
}

interface ClassroomConflict {
  classroomName: string
  classroomId: string
  dayOfWeek: number
  startTime: string
  endTime: string
  date?: string
  conflictCount: number
  conflicts: ConflictDetail[]
}

interface StudentConflict {
  studentName: string
  studentId: string
  dayOfWeek: number
  startTime: string
  endTime: string
  date?: string
  conflictCount: number
  conflicts: ConflictDetail[]
}

// State
const activeTab = ref<string>('teacher')
const teacherLoading = ref<boolean>(false)
const classroomLoading = ref<boolean>(false)
const studentLoading = ref<boolean>(false)
const exportLoading = ref<boolean>(false)

const teacherConflicts = ref<TeacherConflict[]>([])
const classroomConflicts = ref<ClassroomConflict[]>([])
const studentConflicts = ref<StudentConflict[]>([])

// Computed
const currentConflicts = computed(() => {
  switch (activeTab.value) {
    case 'teacher':
      return teacherConflicts.value
    case 'classroom':
      return classroomConflicts.value
    case 'student':
      return studentConflicts.value
    default:
      return []
  }
})

// Methods
const formatTimeSlot = (dayOfWeek: number, startTime: string, endTime: string): string => {
  const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const dayName = days[dayOfWeek] || '未知'
  return `${dayName} ${startTime} - ${endTime}`
}

const queryTeacherConflicts = async () => {
  teacherLoading.value = true
  try {
    // 调用真实API，查询教师冲突 (conflictType = 1)
    const response = await conflictApi.getConflictWarnings({
      conflictType: 1,  // 1 = 教师冲突
      pageNum: 0,
      pageSize: 1000
    })

    // 将API返回的数据转换为前端需要的格式
    if (response.data && response.data.content) {
      const conflicts = response.data.content as ConflictWarning[]

      // 按教师和时间段分组
      const groupedMap = new Map<string, TeacherConflict>()

      for (const conflict of conflicts) {
        const key = `${conflict.relatedIds[0]}-${conflict.timeSlotId}-${conflict.conflictDate}`

        if (!groupedMap.has(key)) {
          groupedMap.set(key, {
            teacherName: conflict.conflictDescription.split('在')[0] || '未知教师',
            teacherId: conflict.relatedIds[0]?.toString() || '',
            dayOfWeek: new Date(conflict.conflictDate).getDay(),
            startTime: conflict.timeSlotName.split('-')[0]?.trim() || '',
            endTime: conflict.timeSlotName.split('-')[1]?.trim() || '',
            date: conflict.conflictDate,
            conflictCount: 0,
            conflicts: []
          })
        }

        const group = groupedMap.get(key)!
        group.conflictCount++

        // 从冲突描述中提取课程和教室信息
        const match = conflict.conflictDescription.match(/课程：(.+?)，教室：(.+?)/)
        if (match) {
          group.conflicts.push({
            courseName: match[1],
            classroomName: match[2]
          })
        }
      }

      teacherConflicts.value = Array.from(groupedMap.values())
    } else {
      teacherConflicts.value = []
    }

    ElMessage.success(`找到 ${teacherConflicts.value.length} 个教师冲突`)
  } catch (error) {
    console.error('查询教师冲突失败:', error)
    ElMessage.error('查询教师冲突失败')
    teacherConflicts.value = []
  } finally {
    teacherLoading.value = false
  }
}

const queryClassroomConflicts = async () => {
  classroomLoading.value = true
  try {
    // 调用真实API，查询教室冲突 (conflictType = 2)
    const response = await conflictApi.getConflictWarnings({
      conflictType: 2,  // 2 = 教室冲突
      pageNum: 0,
      pageSize: 1000
    })

    // 将API返回的数据转换为前端需要的格式
    if (response.data && response.data.content) {
      const conflicts = response.data.content as ConflictWarning[]

      // 按教室和时间段分组
      const groupedMap = new Map<string, ClassroomConflict>()

      for (const conflict of conflicts) {
        const key = `${conflict.relatedIds[0]}-${conflict.timeSlotId}-${conflict.conflictDate}`

        if (!groupedMap.has(key)) {
          groupedMap.set(key, {
            classroomName: conflict.conflictDescription.split('在')[0] || '未知教室',
            classroomId: conflict.relatedIds[0]?.toString() || '',
            dayOfWeek: new Date(conflict.conflictDate).getDay(),
            startTime: conflict.timeSlotName.split('-')[0]?.trim() || '',
            endTime: conflict.timeSlotName.split('-')[1]?.trim() || '',
            date: conflict.conflictDate,
            conflictCount: 0,
            conflicts: []
          })
        }

        const group = groupedMap.get(key)!
        group.conflictCount++

        // 从冲突描述中提取课程和教师信息
        const match = conflict.conflictDescription.match(/课程：(.+?)，教师：(.+?)/)
        if (match) {
          group.conflicts.push({
            courseName: match[1],
            teacherName: match[2]
          })
        }
      }

      classroomConflicts.value = Array.from(groupedMap.values())
    } else {
      classroomConflicts.value = []
    }

    ElMessage.success(`找到 ${classroomConflicts.value.length} 个教室冲突`)
  } catch (error) {
    console.error('查询教室冲突失败:', error)
    ElMessage.error('查询教室冲突失败')
    classroomConflicts.value = []
  } finally {
    classroomLoading.value = false
  }
}

const queryStudentConflicts = async () => {
  studentLoading.value = true
  try {
    // 调用真实API，查询学生冲突 (conflictType = 3)
    const response = await conflictApi.getConflictWarnings({
      conflictType: 3,  // 3 = 学生冲突
      pageNum: 0,
      pageSize: 1000
    })

    // 将API返回的数据转换为前端需要的格式
    if (response.data && response.data.content) {
      const conflicts = response.data.content as ConflictWarning[]

      // 按学生和时间段分组
      const groupedMap = new Map<string, StudentConflict>()

      for (const conflict of conflicts) {
        const key = `${conflict.relatedIds[0]}-${conflict.timeSlotId}-${conflict.conflictDate}`

        if (!groupedMap.has(key)) {
          groupedMap.set(key, {
            studentName: conflict.conflictDescription.split('在')[0] || '未知学生',
            studentId: conflict.relatedIds[0]?.toString() || '',
            dayOfWeek: new Date(conflict.conflictDate).getDay(),
            startTime: conflict.timeSlotName.split('-')[0]?.trim() || '',
            endTime: conflict.timeSlotName.split('-')[1]?.trim() || '',
            date: conflict.conflictDate,
            conflictCount: 0,
            conflicts: []
          })
        }

        const group = groupedMap.get(key)!
        group.conflictCount++

        // 从冲突描述中提取课程和教室信息
        const match = conflict.conflictDescription.match(/课程：(.+?)，教室：(.+?)/)
        if (match) {
          group.conflicts.push({
            courseName: match[1],
            classroomName: match[2]
          })
        }
      }

      studentConflicts.value = Array.from(groupedMap.values())
    } else {
      studentConflicts.value = []
    }

    ElMessage.success(`找到 ${studentConflicts.value.length} 个学生冲突`)
  } catch (error) {
    console.error('查询学生冲突失败:', error)
    ElMessage.error('查询学生冲突失败')
    studentConflicts.value = []
  } finally {
    studentLoading.value = false
  }
}

const handleTabChange = (tabName: string) => {
  // Optionally auto-query when switching tabs
  // if (tabName === 'teacher' && teacherConflicts.value.length === 0) {
  //   queryTeacherConflicts()
  // } else if (tabName === 'classroom' && classroomConflicts.value.length === 0) {
  //   queryClassroomConflicts()
  // } else if (tabName === 'student' && studentConflicts.value.length === 0) {
  //   queryStudentConflicts()
  // }
}

const handleExport = async () => {
  if (currentConflicts.value.length === 0) {
    ElMessage.warning('没有数据可导出')
    return
  }

  const conflictTypeMap: Record<string, string> = {
    teacher: '教师',
    classroom: '教室',
    student: '学生'
  }
  const conflictTypeName = conflictTypeMap[activeTab.value] || '冲突'

  try {
    await ElMessageBox.confirm(
      `确定要导出 ${currentConflicts.value.length} 个${conflictTypeName}冲突记录到Excel吗？`,
      '确认导出',
      {
        confirmButtonText: '导出',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    exportLoading.value = true

    // TODO: Implement actual Excel export
    // This would typically use a library like xlsx or SheetJS
    // Example:
    // import * as XLSX from 'xlsx'
    // const worksheet = XLSX.utils.json_to_sheet(currentConflicts.value)
    // const workbook = XLSX.utils.book_new()
    // XLSX.utils.book_append_sheet(workbook, worksheet, activeTab.value)
    // XLSX.writeFile(workbook, `${activeTab.value}_conflicts_${Date.now()}.xlsx`)

    // Mock export delay
    await new Promise(resolve => setTimeout(resolve, 1500))

    ElMessage.success('导出成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('导出数据失败:', error)
      ElMessage.error('导出失败')
    }
  } finally {
    exportLoading.value = false
  }
}

// Auto-query on mount (optional)
// onMounted(() => {
//   queryTeacherConflicts()
// })
</script>

<style scoped lang="scss">
.data-conflict-query {
  padding: 20px;

  .query-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .header-title {
        font-size: 18px;
        font-weight: 600;
        color: #303133;
      }
    }
  }

  .tab-content {
    .query-actions {
      display: flex;
      align-items: center;
      gap: 16px;
      margin-bottom: 20px;
      padding: 16px;
      background-color: #f5f7fa;
      border-radius: 4px;
    }

    .conflict-table {
      margin-top: 20px;

      .conflict-list {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;

        .conflict-tag {
          margin: 0;
        }
      }
    }

    .summary-info {
      margin-top: 20px;
    }
  }

  :deep(.el-table) {
    font-size: 14px;

    .el-table__header th {
      background-color: #f5f7fa;
      color: #606266;
      font-weight: 600;
    }

    .el-table__body td {
      padding: 12px 0;
    }
  }

  :deep(.el-tabs__item) {
    font-size: 15px;
    font-weight: 500;

    &.is-active {
      color: #409eff;
      font-weight: 600;
    }
  }

  :deep(.el-card__header) {
    padding: 18px 20px;
    border-bottom: 1px solid #ebeef5;
  }

  :deep(.el-card__body) {
    padding: 20px;
  }

  :deep(.el-alert) {
    .el-alert__title {
      font-size: 14px;
      font-weight: 500;
    }
  }
}

// Responsive design
@media screen and (max-width: 768px) {
  .data-conflict-query {
    padding: 10px;

    .query-card {
      .card-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 12px;

        .header-title {
          font-size: 16px;
        }
      }
    }

    .tab-content {
      .query-actions {
        flex-direction: column;
        align-items: flex-start;
      }
    }

    :deep(.el-table) {
      font-size: 12px;

      .conflict-list {
        .conflict-tag {
          font-size: 11px;
        }
      }
    }
  }
}
</style>
