<template>
  <div class="timeslot-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>时间段管理</span>
          <el-button type="primary" @click="showCreateDialog = true">
            <el-icon><Plus /></el-icon>
            新建时间段
          </el-button>
        </div>
      </template>

      <!-- 查询表单 -->
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="星期">
          <el-select v-model="queryForm.dayOfWeek" placeholder="全部" clearable>
            <el-option label="周一" :value="1" />
            <el-option label="周二" :value="2" />
            <el-option label="周三" :value="3" />
            <el-option label="周四" :value="4" />
            <el-option label="周五" :value="5" />
            <el-option label="周六" :value="6" />
            <el-option label="周日" :value="7" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadTimeSlots">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="timeSlotList" style="width: 100%" v-loading="loading">
        <el-table-column prop="slotName" label="时间段名称" width="150" />
        <el-table-column prop="dayOfWeek" label="星期" width="100">
          <template #default="{ row }">
            {{ getDayOfWeekLabel(row.dayOfWeek) }}
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="120" />
        <el-table-column prop="endTime" label="结束时间" width="120" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadTimeSlots"
          @current-change="loadTimeSlots"
        />
      </div>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="isEditMode ? '编辑时间段' : '新建时间段'"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="时间段名称" prop="slotName">
          <el-input v-model="formData.slotName" placeholder="例如：早班" />
        </el-form-item>
        <el-form-item label="星期" prop="dayOfWeek">
          <el-select v-model="formData.dayOfWeek" placeholder="请选择星期" style="width: 100%">
            <el-option label="周一" :value="1" />
            <el-option label="周二" :value="2" />
            <el-option label="周三" :value="3" />
            <el-option label="周四" :value="4" />
            <el-option label="周五" :value="5" />
            <el-option label="周六" :value="6" />
            <el-option label="周日" :value="7" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-time-picker
            v-model="formData.startTime"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="选择开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-time-picker
            v-model="formData.endTime"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="选择结束时间"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEditMode ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { timeSlotApi } from '@/api/timeSlot'
import type { TimeSlot } from '@/types'

const loading = ref(false)
const submitting = ref(false)
const showCreateDialog = ref(false)
const isEditMode = ref(false)
const formRef = ref<FormInstance>()

const timeSlotList = ref<TimeSlot[]>([])

const queryForm = reactive({
  dayOfWeek: null as number | null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const formData = reactive({
  id: null as number | null,
  slotName: '',
  dayOfWeek: null as number | null,
  startTime: '',
  endTime: ''
})

const rules: FormRules = {
  slotName: [{ required: true, message: '请输入时间段名称', trigger: 'blur' }],
  dayOfWeek: [{ required: true, message: '请选择星期', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const getDayOfWeekLabel = (day: number) => {
  const labels = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']
  return labels[day] || ''
}

const loadTimeSlots = async () => {
  loading.value = true
  try {
    // TODO: 实现分页查询API
    const data = await timeSlotApi.getAllTimeSlots()
    let filtered = data

    if (queryForm.dayOfWeek) {
      filtered = data.filter(slot => slot.dayOfWeek === queryForm.dayOfWeek)
    }

    timeSlotList.value = filtered
    pagination.total = filtered.length
  } catch (error) {
    console.error('加载时间段失败', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryForm.dayOfWeek = null
  pagination.page = 1
  loadTimeSlots()
}

const handleEdit = (row: TimeSlot) => {
  isEditMode.value = true
  formData.id = row.id
  formData.slotName = row.slotName
  formData.dayOfWeek = row.dayOfWeek
  formData.startTime = row.startTime
  formData.endTime = row.endTime
  showCreateDialog.value = true
}

const handleDelete = async (row: TimeSlot) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除时间段"${row.slotName}"吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // TODO: 实现删除API
    ElMessage.success('删除成功')
    loadTimeSlots()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    if (isEditMode.value) {
      // TODO: 实现更新API
      ElMessage.success('更新成功')
    } else {
      // TODO: 实现创建API
      ElMessage.success('创建成功')
    }

    showCreateDialog.value = false
    loadTimeSlots()
  } catch (error) {
    console.error('提交失败', error)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadTimeSlots()
})
</script>

<style scoped lang="scss">
.timeslot-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
