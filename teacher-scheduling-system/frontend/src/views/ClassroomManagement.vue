<template>
  <div class="classroom-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>教室管理</span>
          <el-button type="primary" @click="showCreateDialog = true">
            <el-icon><Plus /></el-icon>
            新建教室
          </el-button>
        </div>
      </template>

      <!-- 查询表单 -->
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="教室名称">
          <el-input v-model="queryForm.classroomName" placeholder="请输入教室名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadClassrooms">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="classroomList" style="width: 100%" v-loading="loading">
        <el-table-column prop="classroomName" label="教室名称" width="200" />
        <el-table-column prop="maxCapacity" label="最大容量" width="120">
          <template #default="{ row }">
            {{ row.maxCapacity }} 人
          </template>
        </el-table-column>
        <el-table-column prop="location" label="位置" />
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
          @size-change="loadClassrooms"
          @current-change="loadClassrooms"
        />
      </div>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="isEditMode ? '编辑教室' : '新建教室'"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="教室名称" prop="classroomName">
          <el-input v-model="formData.classroomName" placeholder="例如：阳光教室" />
        </el-form-item>
        <el-form-item label="最大容量" prop="maxCapacity">
          <el-input-number
            v-model="formData.maxCapacity"
            :min="1"
            :max="200"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="formData.location" placeholder="例如：一楼101室" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="handleClose">取消</el-button>
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
import { classroomApi } from '@/api/classroom'
import type { Classroom } from '@/types'

const loading = ref(false)
const submitting = ref(false)
const showCreateDialog = ref(false)
const isEditMode = ref(false)
const formRef = ref<FormInstance>()

const classroomList = ref<Classroom[]>([])

const queryForm = reactive({
  classroomName: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const formData = reactive({
  id: null as number | null,
  classroomName: '',
  maxCapacity: 30,
  location: ''
})

const rules: FormRules = {
  classroomName: [{ required: true, message: '请输入教室名称', trigger: 'blur' }],
  maxCapacity: [{ required: true, message: '请输入最大容量', trigger: 'blur' }],
  location: [{ required: true, message: '请输入位置', trigger: 'blur' }]
}

const loadClassrooms = async () => {
  loading.value = true
  try {
    const data = await classroomApi.getAllClassrooms()
    let filtered = data

    if (queryForm.classroomName) {
      filtered = data.filter(classroom =>
        classroom.classroomName.includes(queryForm.classroomName)
      )
    }

    classroomList.value = filtered
    pagination.total = filtered.length
  } catch (error) {
    console.error('加载教室失败', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryForm.classroomName = ''
  pagination.page = 1
  loadClassrooms()
}

const handleEdit = (row: Classroom) => {
  isEditMode.value = true
  formData.id = row.id
  formData.classroomName = row.classroomName
  formData.maxCapacity = row.maxCapacity
  formData.location = row.location || ''
  showCreateDialog.value = true
}

const handleDelete = async (row: Classroom) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除教室"${row.classroomName}"吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // TODO: 实现删除API
    ElMessage.success('删除成功')
    loadClassrooms()
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
    loadClassrooms()
  } catch (error) {
    console.error('提交失败', error)
  } finally {
    submitting.value = false
  }
}

const handleClose = () => {
  formRef.value?.resetFields()
  showCreateDialog.value = false
  isEditMode.value = false
}

onMounted(() => {
  loadClassrooms()
})
</script>

<style scoped lang="scss">
.classroom-management {
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
