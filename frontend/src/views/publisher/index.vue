<template>
  <div class="publisher-management">
    <div class="card-container">
      <div class="table-toolbar">
        <div class="toolbar-left">
          <el-button type="primary" @click="handleAdd">新增出版社</el-button>
        </div>
        <div class="toolbar-right">
          <el-input v-model="keyword" placeholder="搜索出版社名称/地址" clearable style="width: 240px" @clear="fetchData" @keyup.enter="fetchData">
            <template #append><el-button @click="fetchData"><el-icon><Search /></el-icon></el-button></template>
          </el-input>
        </div>
      </div>

      <el-table v-loading="loading" :data="tableData" border size="default">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="name" label="出版社名称" min-width="160" />
        <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
        <el-table-column prop="contactPhone" label="联系电话" width="140" />
        <el-table-column prop="contactEmail" label="邮箱" width="180" show-overflow-tooltip />
        <el-table-column prop="website" label="官网" width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="fetchData" @current-change="fetchData" />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="editId ? '编辑出版社' : '新增出版社'" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入出版社名称" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="邮箱" prop="contactEmail">
          <el-input v-model="form.contactEmail" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="官网" prop="website">
          <el-input v-model="form.website" placeholder="请输入官网地址" />
        </el-form-item>
        <el-form-item label="简介" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入简介" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getPublishers, createPublisher, updatePublisher, deletePublisher } from '@/api/publisher'

const loading = ref(false)
const tableData = ref<any[]>([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const keyword = ref('')

const dialogVisible = ref(false)
const editId = ref<number | null>(null)
const formRef = ref()
const form = reactive({
  name: '', address: '', contactPhone: '', contactEmail: '', website: '', description: '', status: 1
})
const rules = {
  name: [{ required: true, message: '请输入出版社名称', trigger: 'blur' }]
}

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const res = await getPublishers({ page: page.value, size: size.value, keyword: keyword.value })
    tableData.value = res?.records || []
    total.value = res?.total || 0
  } catch { tableData.value = [] } finally { loading.value = false }
}

function handleAdd() {
  editId.value = null
  Object.assign(form, { name: '', address: '', contactPhone: '', contactEmail: '', website: '', description: '', status: 1 })
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
    if (editId.value) { await updatePublisher(data); ElMessage.success('更新成功') }
    else { await createPublisher(data); ElMessage.success('创建成功') }
    dialogVisible.value = false
    fetchData()
  } catch (e: any) { ElMessage.error(e.message || '操作失败') }
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定要删除出版社"${row.name}"吗？`, '提示', { type: 'warning' })
    await deletePublisher(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}
</script>

<style lang="scss" scoped>
.publisher-management {
  .table-toolbar { display: flex; justify-content: space-between; margin-bottom: 16px; }
  .pagination-container { display: flex; justify-content: flex-end; margin-top: 16px; }
}
</style>
