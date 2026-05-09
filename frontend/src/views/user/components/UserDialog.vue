<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑用户' : '新增用户'"
    width="520px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" size="default">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username" placeholder="请输入用户名" :disabled="isEdit" />
      </el-form-item>
      
      <el-form-item v-if="!isEdit" label="密码" prop="password">
        <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
      </el-form-item>

      <el-form-item label="真实姓名" prop="realName">
        <el-input v-model="form.realName" placeholder="请输入真实姓名" />
      </el-form-item>

      <el-form-item label="角色" prop="role">
        <el-select v-model="form.role" placeholder="选择角色" style="width: 100%">
          <el-option label="管理员" value="ADMIN" />
          <el-option label="图书管理员" value="LIBRARIAN" />
          <el-option label="读者" value="READER" />
        </el-select>
      </el-form-item>

      <el-form-item label="手机号" prop="phone">
        <el-input v-model="form.phone" placeholder="请输入手机号" />
      </el-form-item>

      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" placeholder="请输入邮箱" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { createUser, updateUser, getUserById } from '@/api/user'

interface Props {
  visible: boolean
  userId: number | null
}

const props = defineProps<Props>()
const emit = defineEmits(['update:visible', 'success'])

const formRef = ref<FormInstance>()
const loading = ref(false)

const isEdit = computed(() => !!props.userId)

const form = reactive({
  username: '',
  password: '',
  realName: '',
  role: 'READER',
  phone: '',
  email: '',
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '3-20个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '至少6位字符', trigger: 'blur' },
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
}

watch(() => props.visible, (val) => {
  if (val && props.userId) {
    loadUserDetail()
  } else if (!val) {
    resetForm()
  }
})

async function loadUserDetail() {
  try {
    const user = await getUserById(props.userId!)
    Object.assign(form, {
      username: user.username || '',
      password: '',
      realName: user.realName || '',
      role: user.role || 'READER',
      phone: user.phone || '',
      email: user.email || '',
    })
  } catch {}
}

function resetForm() {
  if (formRef.value) formRef.value.resetFields()
  Object.assign(form, { username: '', password: '', realName: '', role: 'READER', phone: '', email: '' })
}

function handleClose() {
  emit('update:visible', false)
}

async function handleSubmit() {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      if (isEdit.value) {
        await updateUser({ id: props.userId!, ...form })
      } else {
        await createUser(form)
      }
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      handleClose()
      emit('success')
    } finally {
      loading.value = false
    }
  })
}
</script>
