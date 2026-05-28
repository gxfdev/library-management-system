<template>
  <el-dialog :model-value="visible" :title="isEdit ? '编辑分类' : (parentId ? '新增子分类' : '新增分类')" width="460px" :close-on-click-modal="false" @close="handleClose">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" size="default">
      <el-form-item v-if="parentId" label="父分类">
        <el-input :value="parentName" disabled />
      </el-form-item>
      <el-form-item label="分类名称" prop="name"><el-input v-model="form.name" placeholder="请输入分类名称" /></el-form-item>
      <el-form-item label="排序" prop="sortOrder"><el-input-number v-model="form.sortOrder" :min="0" style="width: 100%" /></el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status"><el-radio :value="1">启用</el-radio><el-radio :value="0">禁用</el-radio></el-radio-group>
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
import { createCategory, updateCategory, getCategoryById } from '@/api/category'

interface Props { visible: boolean; categoryId: number | null; parentId?: number | undefined }
const props = defineProps<Props>()
const emit = defineEmits(['update:visible', 'success'])

const formRef = ref<FormInstance>()
const loading = ref(false)
const isEdit = computed(() => !!props.categoryId)
const parentName = computed(() => {
  const names: Record<number, string> = { 1: '计算机科学', 2: '文学', 3: '自然科学', 4: '社会科学', 5: '历史地理', 11: '程序设计' }
  return props.parentId ? names[props.parentId] || `分类ID:${props.parentId}` : ''
})

const form = reactive({ name: '', sortOrder: 0, status: 1 })
const rules: FormRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }, { max: 50, message: '名称长度不超过50个字符', trigger: 'blur' }],
}

watch(() => props.visible, async (val) => {
  if (val && props.categoryId) {
    try {
      const cat = await getCategoryById(props.categoryId)
      Object.assign(form, { name: cat.name || '', sortOrder: cat.sortOrder ?? 0, status: cat.status ?? 1 })
    } catch {}
  } else if (!val) {
    resetForm()
  }
})

function resetForm() { if (formRef.value) formRef.value.resetFields(); Object.assign(form, { name: '', sortOrder: 0, status: 1 }) }
function handleClose() { emit('update:visible', false) }

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      if (isEdit.value) {
        await updateCategory({ id: props.categoryId!, ...form })
      } else {
        await createCategory({ ...form, parentId: props.parentId })
      }
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      handleClose()
      emit('success')
    } finally { loading.value = false }
  })
}
</script>
