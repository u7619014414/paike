<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEditMode ? '修改课程安排' : '创建课程安排'"
    width="700px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="100px"
      v-loading="loading"
    >
      <el-form-item label="课程" prop="courseId">
        <el-select
          v-model="formData.courseId"
          placeholder="请选择课程"
          filterable
          style="width: 100%"
          @change="handleCourseChange"
        >
          <el-option
            v-for="course in courses"
            :key="course.id"
            :label="`${course.courseName} (${course.ageGroupName})`"
            :value="course.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="上课日期" prop="scheduleDate">
        <el-date-picker
          v-model="formData.scheduleDate"
          type="date"
          placeholder="选择日期"
          value-format="YYYY-MM-DD"
          style="width: 100%"
          :disabled-date="disabledDate"
        />
      </el-form-item>

      <el-form-item label="时间段" prop="timeSlotId">
        <el-select
          v-model="formData.timeSlotId"
          placeholder="请选择时间段"
          style="width: 100%"
          @change="handleTimeSlotChange"
        >
          <el-option
            v-for="slot in timeSlots"
            :key="slot.id"
            :label="`${slot.slotName} (${slot.startTime}-${slot.endTime})`"
            :value="slot.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="教室" prop="classroomId">
        <el-select
          v-model="formData.classroomId"
          placeholder="请选择教室"
          filterable
          style="width: 100%"
          @change="checkClassroomConflict"
        >
          <el-option
            v-for="classroom in classrooms"
            :key="classroom.id"
            :label="`${classroom.classroomName} (容量:${classroom.maxCapacity}人)`"
            :value="classroom.id"
          />
        </el-select>
      </el-form-item>

      <!-- 冲突警告 -->
      <el-alert
        v-if="classroomConflict.hasConflicts"
        type="error"
        :title="classroomConflict.conflictDescription"
        :closable="false"
        style="margin-bottom: 15px"
      />

      <el-divider content-position="left">教师分配</el-divider>

      <el-form-item label="主讲教师" prop="mainTeacherId" required>
        <el-select
          v-model="formData.mainTeacherId"
          placeholder="请选择主讲教师（必选）"
          filterable
          style="width: 100%"
          @change="handleMainTeacherChange"
        >
          <el-option
            v-for="teacher in availableTeachers"
            :key="teacher.id"
            :label="`${teacher.teacherName} (${teacher.teacherCode})`"
            :value="teacher.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="助教">
        <el-select
          v-model="formData.assistantTeacherId"
          placeholder="请选择助教（可选）"
          filterable
          clearable
          style="width: 100%"
          @change="checkTeacherConflicts"
        >
          <el-option
            v-for="teacher in availableAssistants"
            :key="teacher.id"
            :label="`${teacher.teacherName} (${teacher.teacherCode})`"
            :value="teacher.id"
            :disabled="teacher.id === formData.mainTeacherId || teacher.id === formData.backupTeacherId"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="备用教师">
        <el-select
          v-model="formData.backupTeacherId"
          placeholder="请选择备用教师（可选）"
          filterable
          clearable
          style="width: 100%"
          @change="checkTeacherConflicts"
        >
          <el-option
            v-for="teacher in availableBackups"
            :key="teacher.id"
            :label="`${teacher.teacherName} (${teacher.teacherCode})`"
            :value="teacher.id"
            :disabled="teacher.id === formData.mainTeacherId || teacher.id === formData.assistantTeacherId"
          />
        </el-select>
      </el-form-item>

      <!-- 教师冲突警告 -->
      <el-alert
        v-if="teacherConflict.hasConflicts"
        type="warning"
        :title="teacherConflict.conflictDescription"
        :closable="false"
        style="margin-bottom: 15px"
      />

      <!-- 选中的教师列表 -->
      <el-form-item label="已选教师" v-if="selectedTeachers.length > 0">
        <el-tag
          v-for="teacher in selectedTeachers"
          :key="teacher.id"
          :type="getTeacherTagType(teacher.id)"
          style="margin-right: 10px"
        >
          {{ teacher.teacherName }}
          <span v-if="teacher.id === formData.mainTeacherId"> (主讲)</span>
          <span v-else-if="teacher.id === formData.assistantTeacherId"> (助教)</span>
          <span v-else-if="teacher.id === formData.backupTeacherId"> (备用)</span>
        </el-tag>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button
          type="primary"
          @click="handleSubmit"
          :loading="submitting"
          :disabled="classroomConflict.hasConflicts"
        >
          {{ isEditMode ? '保存修改' : '创建课程安排' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { courseApi } from '@/api/course'
import { classroomApi } from '@/api/classroom'
import { teacherApi } from '@/api/teacher'
import { scheduleApi } from '@/api/schedule'
import request from '@/api/request'
import type { Course, Classroom, Teacher, TimeSlot, CourseSchedule } from '@/types'

interface Props {
  visible: boolean
  prefilledDate?: string
  prefilledTimeSlot?: TimeSlot
  timeSlots: TimeSlot[]
  editingSchedule?: CourseSchedule | null
  isEditMode?: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  success: []
}>()

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const isEditMode = computed(() => props.isEditMode || false)

const formRef = ref<FormInstance>()
const loading = ref(false)
const submitting = ref(false)

const formData = reactive({
  courseId: null as number | null,
  scheduleDate: props.prefilledDate || '',
  timeSlotId: props.prefilledTimeSlot?.id || null as number | null,
  classroomId: null as number | null,
  mainTeacherId: null as number | null,
  assistantTeacherId: null as number | null,
  backupTeacherId: null as number | null
})

const courses = ref<Course[]>([])
const classrooms = ref<Classroom[]>([])
const availableTeachers = ref<Teacher[]>([])
const selectedCourse = ref<Course | null>(null)

const classroomConflict = reactive({
  hasConflicts: false,
  conflictDescription: ''
})

const teacherConflict = reactive({
  hasConflicts: false,
  conflictDescription: ''
})

const rules: FormRules = {
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  scheduleDate: [{ required: true, message: '请选择上课日期', trigger: 'change' }],
  timeSlotId: [{ required: true, message: '请选择时间段', trigger: 'change' }],
  classroomId: [{ required: true, message: '请选择教室', trigger: 'change' }],
  mainTeacherId: [{ required: true, message: '请选择主讲教师', trigger: 'change' }]
}

// 过滤掉已选教师的助教列表
const availableAssistants = computed(() => {
  return availableTeachers.value.filter(t =>
    t.id !== formData.mainTeacherId && t.id !== formData.backupTeacherId
  )
})

// 过滤掉已选教师的备用教师列表
const availableBackups = computed(() => {
  return availableTeachers.value.filter(t =>
    t.id !== formData.mainTeacherId && t.id !== formData.assistantTeacherId
  )
})

// 已选教师列表
const selectedTeachers = computed(() => {
  const teachers: Teacher[] = []
  if (formData.mainTeacherId) {
    const teacher = availableTeachers.value.find(t => t.id === formData.mainTeacherId)
    if (teacher) teachers.push(teacher)
  }
  if (formData.assistantTeacherId) {
    const teacher = availableTeachers.value.find(t => t.id === formData.assistantTeacherId)
    if (teacher) teachers.push(teacher)
  }
  if (formData.backupTeacherId) {
    const teacher = availableTeachers.value.find(t => t.id === formData.backupTeacherId)
    if (teacher) teachers.push(teacher)
  }
  return teachers
})

const disabledDate = (date: Date) => {
  // 禁用过去的日期
  return date.getTime() < Date.now() - 24 * 60 * 60 * 1000
}

const getTeacherTagType = (teacherId: number) => {
  if (teacherId === formData.mainTeacherId) return 'danger'
  if (teacherId === formData.assistantTeacherId) return 'success'
  if (teacherId === formData.backupTeacherId) return 'warning'
  return 'info'
}

const loadCourses = async () => {
  try {
    console.log('开始加载课程列表...')
    const result = await courseApi.getActiveCourses()
    console.log('课程API返回结果:', result)
    courses.value = result
    console.log('courses.value 已设置为:', courses.value)
  } catch (error) {
    console.error('加载课程列表失败', error)
  }
}

const loadClassrooms = async () => {
  try {
    console.log('开始加载教室列表...')
    const result = await classroomApi.getAllClassrooms()
    console.log('教室API返回结果:', result)
    classrooms.value = result
    console.log('classrooms.value 已设置为:', classrooms.value)
  } catch (error) {
    console.error('加载教室列表失败', error)
  }
}

const handleCourseChange = async (courseId: number) => {
  selectedCourse.value = courses.value.find(c => c.id === courseId) || null
  if (selectedCourse.value) {
    await loadAvailableTeachers()
  }
}

const loadAvailableTeachers = async () => {
  if (!selectedCourse.value || !formData.scheduleDate || !formData.timeSlotId) {
    return
  }

  try {
    loading.value = true
    availableTeachers.value = await teacherApi.getAvailableTeachers({
      ageGroup: selectedCourse.value.ageGroup,
      date: formData.scheduleDate,
      timeSlotId: formData.timeSlotId
    })
  } catch (error) {
    console.error('加载可用教师失败', error)
    availableTeachers.value = []
  } finally {
    loading.value = false
  }
}

const handleTimeSlotChange = () => {
  loadAvailableTeachers()
  checkClassroomConflict()
}

const checkClassroomConflict = async () => {
  if (!formData.classroomId || !formData.scheduleDate || !formData.timeSlotId) {
    classroomConflict.hasConflicts = false
    classroomConflict.conflictDescription = ''
    return
  }

  try {
    const result = await request.get<any>('/conflicts/check-classroom', {
      params: {
        classroomId: formData.classroomId,
        date: formData.scheduleDate,
        timeSlotId: formData.timeSlotId
      }
    })

    classroomConflict.hasConflicts = result.hasConflicts || false
    classroomConflict.conflictDescription = result.conflictDescription || ''
  } catch (error) {
    console.error('检查教室冲突失败', error)
  }
}

const handleMainTeacherChange = () => {
  // 清除与主讲教师相同的助教和备用教师
  if (formData.assistantTeacherId === formData.mainTeacherId) {
    formData.assistantTeacherId = null
  }
  if (formData.backupTeacherId === formData.mainTeacherId) {
    formData.backupTeacherId = null
  }
  checkTeacherConflicts()
}

const checkTeacherConflicts = async () => {
  const teacherIds = [
    formData.mainTeacherId,
    formData.assistantTeacherId,
    formData.backupTeacherId
  ].filter(id => id !== null) as number[]

  if (teacherIds.length === 0 || !formData.scheduleDate || !formData.timeSlotId) {
    teacherConflict.hasConflicts = false
    teacherConflict.conflictDescription = ''
    return
  }

  try {
    const result = await request.get<any>('/conflicts/check-teachers', {
      params: {
        teacherIds: teacherIds.join(','),
        date: formData.scheduleDate,
        timeSlotId: formData.timeSlotId
      }
    })

    teacherConflict.hasConflicts = result.hasConflicts || false
    teacherConflict.conflictDescription = result.conflictDescription || ''
  } catch (error) {
    console.error('检查教师冲突失败', error)
  }
}

const handleSubmit = async () => {
  if (!formRef.value) {
    return
  }

  try {
    // 验证表单
    await formRef.value.validate()

    if (classroomConflict.hasConflicts) {
      ElMessage.error('存在教室冲突，请更换教室或时间段')
      return
    }

    if (!formData.mainTeacherId) {
      ElMessage.error('主讲教师为必选项')
      return
    }

    submitting.value = true

    if (isEditMode.value && props.editingSchedule) {
      // 编辑模式：更新课程安排
      const updateData = {
        courseId: formData.courseId,
        scheduleDate: formData.scheduleDate,
        timeSlotId: formData.timeSlotId,
        classroomId: formData.classroomId
      }

      await scheduleApi.updateSchedule(props.editingSchedule.id, updateData)

      // 更新教师分配
      const teacherIds = [
        formData.mainTeacherId,
        formData.assistantTeacherId,
        formData.backupTeacherId
      ].filter(id => id !== null) as number[]

      const assignmentData = {
        courseScheduleId: props.editingSchedule.id,
        teacherIds: teacherIds,
        mainTeacherId: formData.mainTeacherId
      }

      await scheduleApi.assignTeachers(assignmentData)

      ElMessage.success('课程安排修改成功')
    } else {
      // 创建模式
      const scheduleData = {
        courseId: formData.courseId,
        scheduleDate: formData.scheduleDate,
        timeSlotId: formData.timeSlotId,
        classroomId: formData.classroomId
      }

      const schedule = await scheduleApi.createSchedule(scheduleData)

      // 分配教师
      const teacherIds = [
        formData.mainTeacherId,
        formData.assistantTeacherId,
        formData.backupTeacherId
      ].filter(id => id !== null) as number[]

      const assignmentData = {
        courseScheduleId: schedule.id,
        teacherIds: teacherIds,
        mainTeacherId: formData.mainTeacherId
      }

      await scheduleApi.assignTeachers(assignmentData)

      ElMessage.success('课程安排创建成功')
    }

    emit('success')
    handleClose()
  } catch (error: any) {
    console.error(isEditMode.value ? '修改课程安排失败' : '创建课程安排失败', error)
    ElMessage.error(error.response?.data?.message || (isEditMode.value ? '修改失败' : '创建失败'))
  } finally {
    submitting.value = false
  }
}

const handleClose = () => {
  formRef.value?.resetFields()
  formData.mainTeacherId = null
  formData.assistantTeacherId = null
  formData.backupTeacherId = null
  classroomConflict.hasConflicts = false
  classroomConflict.conflictDescription = ''
  teacherConflict.hasConflicts = false
  teacherConflict.conflictDescription = ''
  dialogVisible.value = false
}

// 监听对话框打开
watch(() => props.visible, async (newVal) => {
  if (newVal) {
    console.log('对话框打开，开始加载数据...')
    console.log('传入的 timeSlots:', props.timeSlots)

    loadCourses()
    loadClassrooms()

    if (isEditMode.value && props.editingSchedule) {
      // 编辑模式：加载现有数据
      formData.courseId = props.editingSchedule.courseId
      formData.scheduleDate = props.editingSchedule.scheduleDate
      formData.timeSlotId = props.editingSchedule.timeSlotId
      formData.classroomId = props.editingSchedule.classroomId

      // 加载教师数据 - 需要从后端获取
      // TODO: 获取已分配的教师信息
    } else {
      // 创建模式：使用预填充数据
      if (props.prefilledDate) {
        formData.scheduleDate = props.prefilledDate
      }
      if (props.prefilledTimeSlot) {
        formData.timeSlotId = props.prefilledTimeSlot.id
      }
    }
  }
})

// 监听日期和时间段变化，重新加载可用教师
watch([() => formData.scheduleDate, () => formData.timeSlotId], () => {
  loadAvailableTeachers()
  checkClassroomConflict()
})
</script>

<style scoped lang="scss">
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}
</style>
