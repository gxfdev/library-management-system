<template>
  <div class="audit-log-page">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>操作日志审计</span>
          <el-button type="danger" :disabled="!selectedIds?.length" @click="batchDelete">
            批量删除
          </el-button>
        </div>
      </template>

      <el-form :model="queryForm" inline class="search-form">
        <el-form-item label="操作模块">
          <el-select v-model="queryForm.module" placeholder="全部" clearable style="width: 150px;">
            <el-option label="认证" value="AUTH" />
            <el-option label="图书" value="BOOK" />
            <el-option label="借阅" value="BORROW" />
            <el-option label="用户" value="USER" />
            <el-option label="系统" value="SYSTEM" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="queryForm.username" placeholder="用户名" clearable style="width: 150px;" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px;">
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table
        :data="tableData"
        border
        stripe
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="username" label="操作人" width="120" />
        <el-table-column prop="module" label="模块" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.module }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="action" label="操作" width="100" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryForm.page"
          v-model:page-size="queryForm.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="日志详情" width="600px">
      <el-descriptions :column="2" border v-if="currentRow">
        <el-descriptions-item label="操作人">{{ currentRow.username }}</el-descriptions-item>
        <el-descriptions-item label="模块">{{ currentRow.module }}</el-descriptions-item>
        <el-descriptions-item label="操作">{{ currentRow.action }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentRow.status === 1 ? 'success' : 'danger'" size="small">
            {{ currentRow.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentRow.ip }}</el-descriptions-item>
        <el-descriptions-item label="请求方式">{{ currentRow.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="请求URL" :span="2">{{ currentRow.requestUrl }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ currentRow.description }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <pre class="json-pre">{{ formatJson(currentRow.requestParams) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2" v-if="currentRow.errorMsg">
          <span style="color: #f56c6c;">{{ currentRow.errorMsg }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ currentRow.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/api/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const selectedIds = ref<number[]>([])
const detailVisible = ref(false)
const currentRow = ref<any>(null)

const queryForm = reactive({
  module: '',
  username: '',
  status: undefined as number | undefined,
  page: 1,
  size: 10,
})

const fetchData = async () => {
  loading.value = true
  try {
    const params: any = { page: queryForm.page, size: queryForm.size }
    if (queryForm.module) params.module = queryForm.module
    if (queryForm.username) params.username = queryForm.username
    if (queryForm.status !== undefined) params.status = queryForm.status
    const res = await request.get('/operation-logs', { params })
    if (res.data?.code === 200) {
      tableData.value = res.data.data?.records || []
      total.value = res.data.data?.total || 0
    }
  } catch {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryForm.page = 1
  fetchData()
}

const resetQuery = () => {
  queryForm.module = ''
  queryForm.username = ''
  queryForm.status = undefined
  queryForm.page = 1
  fetchData()
}

const handleSelectionChange = (rows: any[]) => {
  selectedIds.value = rows.map(r => r.id)
}

const batchDelete = async () => {
  await ElMessageBox.confirm('确定删除选中的日志记录？', '提示', { type: 'warning' })
  await request.delete('/operation-logs', { data: { ids: selectedIds.value } })
  ElMessage.success('删除成功')
  fetchData()
}

const viewDetail = (row: any) => {
  currentRow.value = row
  detailVisible.value = true
}

const formatJson = (str: string) => {
  if (!str) return ''
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch {
    return str
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.audit-log-page {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.search-form {
  margin-bottom: 16px;
}
.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.json-pre {
  max-height: 200px;
  overflow: auto;
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;
}
</style>
