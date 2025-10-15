<template>
  <div class="schedule-management" :class="{ 'fullscreen-mode': isFullscreen }">
    <el-card :body-style="isFullscreen ? { padding: '10px' } : {}">
      <template #header>
        <div class="card-header">
          <span>排课管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="showCreateDialog = true">
              <el-icon><Plus /></el-icon>
              新建课程安排
            </el-button>
            <el-button
              :type="viewMode === 'grid' ? 'primary' : 'default'"
              @click="toggleViewMode"
            >
              <el-icon><Grid v-if="viewMode === 'list'" /><List v-else /></el-icon>
              {{ viewMode === 'list' ? '卡表式' : '列表式' }}
            </el-button>
            <el-button
              type="default"
              @click="toggleFullscreen"
              :title="isFullscreen ? '退出全屏' : '全屏显示'"
            >
              {{ isFullscreen ? '》《' : '《》' }}
            </el-button>
          </div>
        </div>
      </template>

      <!-- 列表式查询表单 -->
      <el-form
        v-if="viewMode === 'list'"
        :inline="true"
        :model="queryForm"
        class="query-form"
      >
        <el-form-item label="开始日期">
          <el-date-picker
            v-model="queryForm.startDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker
            v-model="queryForm.endDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable>
            <el-option
              v-for="item in SCHEDULE_STATUS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadSchedules">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 列表视图 -->
      <el-table
        v-if="viewMode === 'list'"
        :data="scheduleList"
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="courseName" label="课程名称" width="150" />
        <el-table-column prop="scheduleDate" label="上课日期" width="120" />
        <el-table-column prop="timeSlotName" label="时间段" width="150">
          <template #default="{ row }">
            {{ row.timeSlotName }} ({{ row.startTime }}-{{ row.endTime }})
          </template>
        </el-table-column>
        <el-table-column prop="classroomName" label="教室" width="120" />
        <el-table-column prop="teacherNames" label="教师">
          <template #default="{ row }">
            <el-tag
              v-for="(name, index) in row.teacherNames"
              :key="index"
              size="small"
              style="margin-right: 5px"
            >
              {{ name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleEdit(row)"
              v-if="row.status === 1"
            >
              修改
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(row)"
              v-if="row.status === 1"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="viewMode === 'list'" class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadSchedules"
          @current-change="loadSchedules"
        />
      </div>

      <!-- 卡表视图 -->
      <ScheduleGridView
        v-else
        :schedules="allSchedules"
        :time-slots="timeSlots"
        :loading="loading"
        @add-schedule="handleAddScheduleFromGrid"
        @assign-teachers="handleAssignTeachers"
        @cancel-schedule="handleCancel"
        @schedule-click="handleScheduleClick"
      />
    </el-card>

    <!-- 创建/编辑课程安排对话框 -->
    <CreateScheduleDialog
      v-model:visible="showCreateDialog"
      :prefilled-date="prefilledDate"
      :prefilled-time-slot="prefilledTimeSlot"
      :time-slots="timeSlots"
      :editing-schedule="editingSchedule"
      :is-edit-mode="isEditMode"
      @success="handleCreateSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Grid, List, Plus } from '@element-plus/icons-vue'
import { scheduleApi } from '@/api/schedule'
import { timeSlotApi } from '@/api/timeSlot'
import { SCHEDULE_STATUS } from '@/types'
import type { CourseSchedule, TimeSlot } from '@/types'
import ScheduleGridView from '@/components/ScheduleGridView.vue'
import CreateScheduleDialog from '@/components/CreateScheduleDialog.vue'

const loading = ref(false)
const showCreateDialog = ref(false)
const scheduleList = ref<CourseSchedule[]>([])
const allSchedules = ref<CourseSchedule[]>([])
const timeSlots = ref<TimeSlot[]>([])
const viewMode = ref<'list' | 'grid'>('list')
const prefilledDate = ref<string>('')
const prefilledTimeSlot = ref<TimeSlot | undefined>(undefined)
const editingSchedule = ref<CourseSchedule | null>(null)
const isEditMode = ref(false)
const isFullscreen = ref(false)

const queryForm = reactive({
  startDate: '',
  endDate: '',
  status: null as number | null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const loadSchedules = async () => {
  loading.value = true
  try {
    const params = {
      ...queryForm,
      pageNum: pagination.page - 1,
      pageSize: pagination.size
    }
    const data = await scheduleApi.getScheduleList(params)
    scheduleList.value = data.content
    pagination.total = data.totalElements
  } catch (error) {
    console.error('加载课程安排失败', error)
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryForm.startDate = ''
  queryForm.endDate = ''
  queryForm.status = null
  pagination.page = 1
  loadSchedules()
}

const loadAllSchedules = async () => {
  loading.value = true
  try {
    const params = {
      startDate: '',
      endDate: '',
      status: null,
      pageNum: 0,
      pageSize: 1000
    }
    const data = await scheduleApi.getScheduleList(params)
    allSchedules.value = data.content
  } catch (error) {
    console.error('加载所有课程安排失败', error)
  } finally {
    loading.value = false
  }
}

const loadTimeSlots = async () => {
  try {
    const data = await timeSlotApi.getAllTimeSlots()
    timeSlots.value = data
  } catch (error) {
    console.error('加载时间段失败', error)
  }
}

const toggleViewMode = () => {
  viewMode.value = viewMode.value === 'list' ? 'grid' : 'list'
  if (viewMode.value === 'grid') {
    loadAllSchedules()
    loadTimeSlots()
  } else {
    loadSchedules()
  }
}

const handleAddScheduleFromGrid = (dayOfWeek: number, timeSlot: TimeSlot, date: string) => {
  prefilledDate.value = date
  prefilledTimeSlot.value = timeSlot
  showCreateDialog.value = true
}

const handleScheduleClick = (schedule: CourseSchedule) => {
  ElMessage.info(`查看课程详情: ${schedule.courseName}`)
}

const handleAssignTeachers = (schedule: CourseSchedule) => {
  ElMessage.info(`分配教师功能开发中: ${schedule.courseName}`)
}

const handleCancel = async (schedule: CourseSchedule) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消课程"${schedule.courseName}"的排课吗？`,
      '取消确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await scheduleApi.deleteSchedule(schedule.id)
    ElMessage.success('取消成功')

    if (viewMode.value === 'list') {
      loadSchedules()
    } else {
      loadAllSchedules()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消失败', error)
      ElMessage.error('取消失败')
    }
  }
}

const handleCreateSuccess = () => {
  if (viewMode.value === 'list') {
    loadSchedules()
  } else {
    loadAllSchedules()
  }
  prefilledDate.value = ''
  prefilledTimeSlot.value = undefined
  editingSchedule.value = null
  isEditMode.value = false
}

const handleEdit = (row: CourseSchedule) => {
  editingSchedule.value = row
  isEditMode.value = true
  showCreateDialog.value = true
}

const handleDelete = async (row: CourseSchedule) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除课程"${row.courseName}"的排课吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await scheduleApi.deleteSchedule(row.id)
    ElMessage.success('删除成功')

    if (viewMode.value === 'list') {
      loadSchedules()
    } else {
      loadAllSchedules()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
      ElMessage.error('删除失败')
    }
  }
}

const getStatusLabel = (status: number) => {
  return SCHEDULE_STATUS.find(s => s.value === status)?.label || '未知'
}

const getStatusType = (status: number) => {
  const map: any = { 1: 'success', 2: 'danger', 3: 'warning' }
  return map[status] || 'info'
}

const toggleFullscreen = () => {
  const mainLayout = document.querySelector('.main-layout') as HTMLElement
  const mainAside = document.querySelector('.main-aside') as HTMLElement
  const mainHeader = document.querySelector('.main-header') as HTMLElement
  const mainContent = document.querySelector('.main-content') as HTMLElement
  const scheduleManagement = document.querySelector('.schedule-management') as HTMLElement

  if (!isFullscreen.value) {
    // 进入全屏模式
    if (mainAside) mainAside.style.display = 'none'
    if (mainHeader) mainHeader.style.display = 'none'
    if (mainContent) {
      mainContent.style.padding = '0'
      mainContent.style.margin = '0'
    }
    if (scheduleManagement) {
      scheduleManagement.style.position = 'fixed'
      scheduleManagement.style.top = '0'
      scheduleManagement.style.left = '0'
      scheduleManagement.style.right = '0'
      scheduleManagement.style.bottom = '0'
      scheduleManagement.style.padding = '0'
      scheduleManagement.style.margin = '0'
      scheduleManagement.style.zIndex = '9999'
      scheduleManagement.style.background = 'white'
      scheduleManagement.style.overflow = 'auto'
    }
    isFullscreen.value = true
  } else {
    // 退出全屏模式
    if (mainAside) mainAside.style.display = 'block'
    if (mainHeader) mainHeader.style.display = 'flex'
    if (mainContent) {
      mainContent.style.padding = ''
      mainContent.style.margin = ''
    }
    if (scheduleManagement) {
      scheduleManagement.style.position = ''
      scheduleManagement.style.top = ''
      scheduleManagement.style.left = ''
      scheduleManagement.style.right = ''
      scheduleManagement.style.bottom = ''
      scheduleManagement.style.padding = '20px'
      scheduleManagement.style.margin = ''
      scheduleManagement.style.zIndex = ''
      scheduleManagement.style.background = ''
      scheduleManagement.style.overflow = ''
    }
    isFullscreen.value = false
  }
}

// 监听ESC键退出全屏
const handleEscKey = (event: KeyboardEvent) => {
  if (event.key === 'Escape' && isFullscreen.value) {
    toggleFullscreen()
  }
}

onMounted(() => {
  loadSchedules()
  document.addEventListener('keydown', handleEscKey)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleEscKey)
})
</script>

<style scoped lang="scss">
.schedule-management {
  padding: 20px;

  &.fullscreen-mode {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 9999;
    background: white;
    padding: 0;
    margin: 0;
    overflow: auto;

    :deep(.el-card) {
      border: none;
      box-shadow: none;
      height: 100%;
      margin: 0;
    }

    :deep(.el-card__header) {
      padding: 15px 20px;
      border-bottom: 1px solid #e4e7ed;
    }
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.query-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
