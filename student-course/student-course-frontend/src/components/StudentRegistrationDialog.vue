<template>
  <el-dialog
    v-model="dialogVisible"
    title="学生注册"
    width="500px"
    :before-close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="80px"
      @submit.prevent="handleSubmit"
    >
      <el-form-item label="学生姓名" prop="studentName">
        <el-input
          v-model="form.studentName"
          placeholder="请输入学生姓名"
          maxlength="20"
          show-word-limit
        />
      </el-form-item>
      <el-form-item label="学生年龄" prop="age">
        <el-input-number
          v-model="form.age"
          :min="1"
          :max="12"
          style="width: 100%"
        />
        <div class="age-hint">
          <span>年龄将自动分配到对应班级：</span>
          <div class="age-groups">
            <el-tag size="small">学前班 1-3岁</el-tag>
            <el-tag size="small">小班 3-6岁</el-tag>
            <el-tag size="small">中班 6-9岁</el-tag>
            <el-tag size="small">大班 9-12岁</el-tag>
          </div>
        </div>
      </el-form-item>
      <el-form-item label="家长姓名" prop="parentName">
        <el-input
          v-model="form.parentName"
          placeholder="请输入家长姓名"
          maxlength="20"
          show-word-limit
        />
      </el-form-item>
      <el-form-item label="家长电话" prop="parentPhone">
        <el-input
          v-model="form.parentPhone"
          placeholder="请输入家长电话"
          maxlength="11"
        />
      </el-form-item>
      <el-form-item label="家长邮箱" prop="parentEmail">
        <el-input
          v-model="form.parentEmail"
          placeholder="请输入家长邮箱（可选）"
          type="email"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleSubmit"
          :loading="submitting"
        >
          提交注册
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>
<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useStudentStore } from '@/stores/student'
import type { StudentRegistration, Student } from '@/types'
interface Props {
  modelValue: boolean
}
const props = defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'register-success': [student: Student]
}>()
const studentStore = useStudentStore()
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const submitting = ref(false)
const form = ref<StudentRegistration>({
  studentName: '',
  age: 3,
  parentName: '',
  parentPhone: '',
  parentEmail: ''
})
const rules: FormRules = {
  studentName: [
    { required: true, message: '请输入学生姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度应在2-20个字符', trigger: 'blur' }
  ],
  age: [
    { required: true, message: '请选择学生年龄', trigger: 'change' },
    { type: 'number', min: 1, max: 12, message: '年龄应在1-12岁之间', trigger: 'change' }
  ],
  parentName: [
    { required: true, message: '请输入家长姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度应在2-20个字符', trigger: 'blur' }
  ],
  parentPhone: [
    { required: true, message: '请输入家长电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  parentEmail: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}
const handleSubmit = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const student = await studentStore.registerStudent(form.value)
    emit('register-success', student)
    resetForm()
  } catch (error) {
    // 错误信息由拦截器处理
  } finally {
    submitting.value = false
  }
}
const handleClose = () => {
  emit('update:modelValue', false)
  resetForm()
}
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  form.value = {
    studentName: '',
    age: 3,
    parentName: '',
    parentPhone: '',
    parentEmail: ''
  }
}
watch(() => props.modelValue, (newVal) => {
  dialogVisible.value = newVal
})
watch(() => dialogVisible.value, (newVal) => {
  emit('update:modelValue', newVal)
})
</script>
<style lang="scss" scoped>
.age-hint {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  .age-groups {
    margin-top: 4px;
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
  }
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
