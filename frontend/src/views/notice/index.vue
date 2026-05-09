<template>
  <div class="notice-management">
    <div class="card-container">
      <div class="table-toolbar">
        <div class="toolbar-left">
          <el-button v-if="canManage" type="primary" @click="handleAdd">新增通知</el-button>
        </div>
        <div class="toolbar-right">
          <el-select v-model="typeFilter" placeholder="类型筛选" clearable style="width: 120px" @change="fetchData">
            <el-option label="通知" value="NOTICE" />
            <el-option label="公告" value="ANNOUNCEMENT" />
            <el-option label="新闻" value="NEWS" />
          </el-select>
          <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 120px; margin-left: 8px" @change="fetchData">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已发布" value="PUBLISHED" />
          </el-select>
        </div>
      </div>

      <el-table v-loading="loading" :data="tableData" border size="default">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="typeMap[row.type]?.type || 'info'" size="small">{{ typeMap[row.type]?.label || row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'info'">{{ row.status === 'PUBLISHED' ? '已发布' : '草稿' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publisherName" label="发布人" width="100" />
        <el-table-column prop="publishTime" label="发布时间" width="170" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column v-if="canManage" label="操作" width="200" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 'DRAFT'" type="success" link size="small" @click="handlePublish(row)">发布</el-button>
            <el-button v-if="isAdmin" type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="fetchData" @current-change="fetchData" />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="editId ? '编辑通知' : '新增通知'" width="640px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" style="width: 100%">
            <el-option label="通知" value="NOTICE" />
            <el-option label="公告" value="ANNOUNCEMENT" />
            <el-option label="新闻" value="NEWS" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="8" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getNotices, createNotice, updateNotice, deleteNotice, publishNotice } from '@/api/notice'
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.isAdmin)
const canManage = computed(() => userStore.canManageBooks)

const typeMap: Record<string, { label: string; type: string }> = {
  NOTICE: { label: '通知', type: 'warning' },
  ANNOUNCEMENT: { label: '公告', type: 'success' },
  NEWS: { label: '新闻', type: 'primary' }
}

const loading = ref(false)
const tableData = ref<any[]>([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const typeFilter = ref('')
const statusFilter = ref('')

const dialogVisible = ref(false)
const editId = ref<number | null>(null)
const formRef = ref()
const form = reactive({ title: '', type: 'NOTICE', content: '', status: 'DRAFT' })
const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const res = await getNotices({ page: page.value, size: size.value, type: typeFilter.value, status: statusFilter.value })
    tableData.value = res?.records || []
    total.value = res?.total || 0
  } catch { tableData.value = [] } finally { loading.value = false }
}

function handleAdd() {
  editId.value = null
  Object.assign(form, { title: '', type: 'NOTICE', content: '', status: 'DRAFT' })
  dialogVisible.value = true
}

function handleEdit(row: any) {
  editId.value = row.id
  Object.assign(form, row)
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value?.validate()
  try {
    const data = { ...form, id: editId.value }
    if (editId.value) { await updateNotice(data); ElMessage.success('更新成功') }
    else { await createNotice(data); ElMessage.success('创建成功') }
    dialogVisible.value = false
    fetchData()
  } catch (e: any) { ElMessage.error(e.message || '操作失败') }
}

async function handlePublish(row: any) {
  try {
    await ElMessageBox.confirm('确定要发布该通知吗？', '提示', { type: 'warning' })
    await publishNotice(row.id)
    ElMessage.success('发布成功')
    fetchData()
  } catch {}
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定要删除"${row.title}"吗？`, '提示', { type: 'warning' })
    await deleteNotice(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}
</script>

<style lang="scss" scoped>
.notice-management {
  .table-toolbar { display: flex; justify-content: space-between; margin-bottom: 16px; }
  .pagination-container { display: flex; justify-content: flex-end; margin-top: 16px; }
}
</style>
