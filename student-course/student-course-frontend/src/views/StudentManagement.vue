<template>
  <div class="student-management">
    <div class="page-header">
      <h1>学生管理</h1>
      <el-button type="primary" @click="showAddDialog = true" :icon="Plus">
        添加学生
      </el-button>
    </div>
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-card>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="searchForm.keyword"
              placeholder="搜索学生姓名或家长姓名"
              clearable
              @clear="handleSearch"
              @keyup.enter="handleSearch"
            />
          </el-col>
          <el-col :span="4">
            <el-select
              v-model="searchForm.ageGroup"
              placeholder="年龄组"
              clearable
              @change="handleSearch"
            >
              <el-option
                v-for="group in AGE_GROUPS"
                :key="group.value"
                :label="group.label"
                :value="group.value"
              />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-select
              v-model="searchForm.status"
              placeholder="注册状态"
              clearable
              @change="handleSearch"
            >
              <el-option label="待审核" :value="1" />
              <el-option label="已通过" :value="2" />
              <el-option label="已拒绝" :value="3" />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-button type="primary" @click="handleSearch" :icon="Search">
              搜索
            </el-button>
            <el-button @click="handleReset" :icon="Refresh">
              重置
            </el-button>
          </el-col>
        </el-row>
      </el-card>
    </div>
    <!-- 学生列表 -->
    <el-card>
      <el-table
        :data="filteredStudents"
        v-loading="studentStore.loading"
        style="width: 100%"
        stripe
      >
        <el-table-column prop="studentCode" label="学生编号" width="120" />
        <el-table-column prop="studentName" label="学生姓名" width="100" />
        <el-table-column prop="age" label="年龄" width="80" />
        <el-table-column label="年龄组" width="120">
          <template #default="{ row }">
            <el-tag>{{ getAgeGroupLabel(row.ageGroup) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="parentName" label="家长姓名" width="100" />
        <el-table-column prop="parentPhone" label="家长电话" width="120" />
        <el-table-column label="注册状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="getStatusType(row.registrationStatus)"
            >
              {{ getStatusText(row.registrationStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleEdit(row)"
              :icon="Edit"
            >
              编辑
            </el-button>
            <el-button
              v-if="row.registrationStatus === 1"
              type="success"
              size="small"
              @click="handleApprove(row)"
              :icon="Check"
            >
              通过
            </el-button>
            <el-button
              v-if="row.registrationStatus === 1"
              type="danger"
              size="small"
              @click="handleReject(row)"
              :icon="Close"
            >
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <!-- 添加/编辑学生对话框 -->
    <StudentRegistrationDialog
      v-model="showAddDialog"
      @register-success="handleAddSuccess"
    />
    <!-- 编辑学生对话框 -->
    <el-dialog
      v-model="showEditDialog"
      title="编辑学生信息"
      width="500px"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="80px"
      >
        <el-form-item label="学生姓名" prop="studentName">
          <el-input v-model="editForm.studentName" />
        </el-form-item>
        <el-form-item label="学生年龄" prop="age">
          <el-input-number v-model="editForm.age" :min="1" :max="12" />
        </el-form-item>
        <el-form-item label="家长姓名" prop="parentName">
          <el-input v-model="editForm.parentName" />
        </el-form-item>
        <el-form-item label="家长电话" prop="parentPhone">
          <el-input v-model="editForm.parentPhone" />
        </el-form-item>
        <el-form-item label="家长邮箱" prop="parentEmail">
          <el-input v-model="editForm.parentEmail" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit" :loading="editing">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Search, Refresh, Edit, Check, Close } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import StudentRegistrationDialog from '@/components/StudentRegistrationDialog.vue'
import { useStudentStore } from '@/stores/student'
import { studentApi } from '@/api'
import type { Student } from '@/types'
import { AGE_GROUPS } from '@/types'
const studentStore = useStudentStore()
const showAddDialog = ref(false)
const showEditDialog = ref(false)
const editing = ref(false)
const editFormRef = ref<FormInstance>()
const searchForm = ref({
  keyword: '',
  ageGroup: null,
  status: null
})
const editForm = ref({
  id: null,
  studentName: '',
  age: 1,
  parentName: '',
  parentPhone: '',
  parentEmail: ''
})
const editRules: FormRules = {
  studentName: [
    { required: true, message: '请输入学生姓名', trigger: 'blur' }
  ],
  age: [
    { required: true, message: '请输入年龄', trigger: 'blur' }
  ],
  parentName: [
    { required: true, message: '请输入家长姓名', trigger: 'blur' }
  ],
  parentPhone: [
    { required: true, message: '请输入家长电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}
const students = computed(() => studentStore.students)
const filteredStudents = computed(() => {
  let result = students.value
  if (searchForm.value.keyword) {
    result = result.filter(student => 
      student.studentName.includes(searchForm.value.keyword) ||
      student.parentName.includes(searchForm.value.keyword)
    )
  }
  if (searchForm.value.ageGroup) {
    result = result.filter(student => student.ageGroup === searchForm.value.ageGroup)
  }
  if (searchForm.value.status) {
    result = result.filter(student => student.registrationStatus === searchForm.value.status)
  }
  return result
})
const loadStudents = async () => {
  await studentStore.fetchStudents()
}
const handleSearch = () => {}
const handleReset = () => {
  searchForm.value = {
    keyword: '',
    ageGroup: null,
    status: null
  }
}
const handleAddSuccess = () => {
  showAddDialog.value = false
  ElMessage.success('学生添加成功！')
  loadStudents()
}
const handleEdit = (student: Student) => {
  editForm.value = {
    id: student.id,
    studentName: student.studentName,
    age: student.age,
    parentName: student.parentName,
    parentPhone: student.parentPhone,
    parentEmail: student.parentEmail || ''
  }
  showEditDialog.value = true
}
const handleSaveEdit = async () => {
  if (!editFormRef.value) return
  const valid = await editFormRef.value.validate().catch(() => false)
  if (!valid) return
  editing.value = true
  try {
    await studentApi.updateStudent(editForm.value.id!, editForm.value)
    ElMessage.success('学生信息更新成功！')
    showEditDialog.value = false
    loadStudents()
  } catch (error) {
    // 错误信息由拦截器处理
  } finally {
    editing.value = false
  }
}
const handleApprove = async (student: Student) => {
  try {
    await ElMessageBox.confirm('确认通过该学生的注册申请？', '确认操作', {
      type: 'warning'
    })
    await studentApi.updateStudent(student.id, { registrationStatus: 2 })
    ElMessage.success('审核通过！')
    loadStudents()
  } catch (error) {
    if (error !== 'cancel') {
      // 错误信息由拦截器处理
    }
  }
}
const handleReject = async (student: Student) => {
  try {
    await ElMessageBox.confirm('确认拒绝该学生的注册申请？', '确认操作', {
      type: 'warning'
    })
    await studentApi.updateStudent(student.id, { registrationStatus: 3 })
    ElMessage.success('已拒绝申请！')
    loadStudents()
  } catch (error) {
    if (error !== 'cancel') {
      // 错误信息由拦截器处理
    }
  }
}
const getAgeGroupLabel = (ageGroup: number): string => {
  return AGE_GROUPS.find(g => g.value === ageGroup)?.label || ''
}
const getStatusType = (status: number) => {
  const types = { 1: 'warning', 2: 'success', 3: 'danger' }
  return types[status] || 'info'
}
const getStatusText = (status: number): string => {
  const texts = { 1: '待审核', 2: '已通过', 3: '已拒绝' }
  return texts[status] || '未知'
}
const formatDate = (dateStr: string): string => {
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm')
}
onMounted(() => {
  loadStudents()
})
</script>
<style lang="scss" scoped>
.student-management {
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  h1 {
    color: #303133;
    margin: 0;
  }
}
.search-bar {
  margin-bottom: 20px;
}
</style>
