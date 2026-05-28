<template>
  <div class="borrow-management">
    <div class="page-header">
      <h2>借阅管理</h2>
      <p>管理所有读者的借阅记录，支持办理借阅、续借和归还操作</p>
    </div>

    <div class="search-card">
      <el-form :model="queryParams" inline size="default" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="搜索读者/图书名称/ISBN" clearable style="width: 240px" :prefix-icon="Search" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option label="借阅中" value="BORROWING" />
            <el-option label="已逾期" value="OVERDUE" />
            <el-option label="已归还" value="RETURNED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="card-container">
      <div class="table-toolbar">
        <div class="toolbar-left">
          <el-button type="primary" @click="handleBorrow"><el-icon><Plus /></el-icon> 办理借阅</el-button>
          <el-dropdown @command="handleExport" style="margin-left: 8px;">
            <el-button>
              导出数据 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="all">导出全部记录</el-dropdown-item>
                <el-dropdown-item command="BORROWING">导出借阅中</el-dropdown-item>
                <el-dropdown-item command="OVERDUE">导出已逾期</el-dropdown-item>
                <el-dropdown-item command="RETURNED">导出已归还</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <div class="toolbar-right">
          <el-tag type="info" effect="plain">共 {{ total }} 条记录</el-tag>
        </div>
      </div>

      <el-table v-loading="loading" :data="borrowList" stripe border size="default" row-class-name="borrow-row" style="width: 100%">
        <el-table-column prop="id" label="ID" width="65" align="center" />
        <el-table-column label="读者信息" min-width="140">
          <template #default="{ row }">
            <div class="user-cell">
              <strong>{{ row.userName }}</strong>
              <span class="sub">{{ row.userRealName || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="图书信息" min-width="200">
          <template #default="{ row }">
            <div class="book-cell">
              <div class="book-cover-mini" v-if="row.bookCoverImage">
                <img :src="getCoverUrl(row.bookCoverImage)" alt="" @error="(e: Event) => (e.target as HTMLImageElement).style.display = 'none'" />
              </div>
              <div class="book-text">
                <strong>{{ row.bookTitle }}</strong>
                <span class="isbn">{{ row.bookIsbn || '-' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="borrowDate" label="借阅日期" width="110">
          <template #default="{ row }">{{ formatDate(row.borrowDate) }}</template>
        </el-table-column>
        <el-table-column prop="dueDate" label="应还日期" width="120">
          <template #default="{ row }">
            <span :class="{ 'text-danger': isOverdue(row), 'text-warning': isDueSoon(row) }">
              {{ formatDate(row.dueDate) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="returnDate" label="归还日期" width="110">
          <template #default="{ row }">
            <span>{{ formatDate(row.returnDate) || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="renewCount" label="续借" width="60" align="center">
          <template #default="{ row }">{{ row.renewCount }}/2</template>
        </el-table-column>
        <el-table-column prop="fineAmount" label="罚款" width="80" align="right">
          <template #default="{ row }">
            <span :class="{ 'text-danger': row.fineAmount > 0 }">¥{{ (row.fineAmount || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="95" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small" effect="dark">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'BORROWING' || row.status === 'OVERDUE'">
              <el-button type="danger" link size="small" @click="handleReturn(row)">归还</el-button>
              <el-button
                type="primary"
                link
                size="small"
                :disabled="row.renewCount >= 2"
                @click="handleRenew(row)"
              >续借</el-button>
            </template>
            <span v-else class="no-action">-</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @change="fetchBorrowList"
        />
      </div>
    </div>

    <BorrowDialog v-model:visible="borrowDialogVisible" @success="fetchBorrowList" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, ArrowDown } from '@element-plus/icons-vue'
import { getBorrowPage, returnBook, renewBook } from '@/api/borrow'
import { exportBorrowsExcel, downloadBlob } from '@/api/export'
import type { BorrowRecord } from '@/types'
import BorrowDialog from './components/BorrowDialog.vue'

const loading = ref(false)
const borrowList = ref<BorrowRecord[]>([])
const total = ref(0)
const borrowDialogVisible = ref(false)

const queryParams = reactive({ page: 1, size: 10, keyword: '', status: undefined as string | undefined })

onMounted(() => fetchBorrowList())

async function fetchBorrowList() {
  loading.value = true
  try {
    const res = await getBorrowPage(queryParams)
    borrowList.value = res?.records || []
    total.value = res?.total || 0
  } catch {
    borrowList.value = []
    total.value = 0
  } finally { loading.value = false }
}

function handleSearch() { queryParams.page = 1; fetchBorrowList() }
function resetQuery() { Object.assign(queryParams, { page: 1, keyword: '', status: undefined }); fetchBorrowList() }

function handleBorrow() { borrowDialogVisible.value = true }

async function handleReturn(record: BorrowRecord) {
  try {
    await ElMessageBox.confirm(
      `确认归还《${record.bookTitle}》？\n读者：${record.userName}\n${isOverdue(record) ? '⚠️ 该书已逾期' : ''}`,
      '办理归还',
      { confirmButtonText: '确认归还', cancelButtonText: '取消', type: 'info', dangerouslyUseHTMLString: true },
    )
    await returnBook(record.id)
    ElMessage.success(`《${record.bookTitle}》归还成功`)
    fetchBorrowList()
  } catch {}
}

async function handleRenew(record: BorrowRecord) {
  if (record.renewCount >= 2) { ElMessage.warning('续借次数已达上限'); return }
  try {
    await ElMessageBox.confirm(`为"${record.userName}"续借《${record.bookTitle}》？`, '确认续借')
    await renewBook(record.id)
    ElMessage.success('续借成功')
    fetchBorrowList()
  } catch {}
}

function formatDate(dateStr?: string): string { if (!dateStr) return '-'; return dateStr.split('T')[0] }

function isOverdue(record: BorrowRecord): boolean {
  return record.status === 'OVERDUE' || (record.status === 'BORROWING' && new Date(record.dueDate) < new Date())
}

function isDueSoon(record: BorrowRecord): boolean {
  if (record.status !== 'BORROWING') return false
  const diff = new Date(record.dueDate).getTime() - new Date().getTime()
  return diff > 0 && diff <= 3 * 24 * 60 * 60 * 1000
}

function getStatusType(status: string): string {
  const map: Record<string, string> = { BORROWING: '', RETURNED: 'success', OVERDUE: 'danger' }
  return map[status] || ''
}

function getStatusText(status: string): string {
  const map: Record<string, string> = { BORROWING: '借阅中', RETURNED: '已归还', OVERDUE: '逾期' }
  return map[status] || status
}

function handleExport(command: string) {
  const status = command === 'all' ? undefined : command
  const label = command === 'all' ? '全部' : getStatusText(command)
  exportBorrowsExcel(status).then((blob: any) => {
    downloadBlob(blob, `借阅记录_${label}_${new Date().toISOString().slice(0, 10)}.xlsx`)
    ElMessage.success('导出成功')
  }).catch(() => ElMessage.error('导出失败'))
}

function getCoverUrl(url: string) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  if (url.startsWith('/')) return url
  return '/' + url
}
</script>

<style lang="scss" scoped>
.borrow-management {
  .page-header {
    margin-bottom: 20px;
    h2 { font-size: 20px; font-weight: 700; color: #1c1917; margin-bottom: 4px; }
    p { font-size: 13px; color: #78716c; }
  }

  .search-card {
    background: #fff;
    border-radius: 14px;
    padding: 18px 22px;
    margin-bottom: 16px;
    box-shadow: var(--shadow-sm);
    border: 1px solid var(--border-light);
  }

  .search-form {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
  }

  .card-container {
    background: #fff;
    border-radius: 14px;
    padding: 20px;
    box-shadow: var(--shadow-sm);
    border: 1px solid var(--border-light);
  }

  .table-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    .toolbar-left { display: flex; gap: 8px; }
  }

  .user-cell {
    display: flex;
    flex-direction: column;
    strong { font-size: 13px; color: #1c1917; }
    .sub { font-size: 11px; color: #a8a29e; }
  }

  .book-cell {
    display: flex;
    align-items: center;
    gap: 8px;

    .book-cover-mini {
      width: 32px;
      height: 42px;
      border-radius: 3px;
      overflow: hidden;
      background: #f5f5f4;
      flex-shrink: 0;
      img { width: 100%; height: 100%; object-fit: cover; }
    }

    .book-text {
      display: flex;
      flex-direction: column;
      strong { font-size: 13px; color: #1c1917; line-height: 1.3; }
      .isbn { font-size: 11px; color: #a8a29e; }
    }
  }

  .text-danger { color: var(--danger-color); font-weight: 600; }
  .text-warning { color: var(--warning-color); font-weight: 500; }
  .no-action { color: #d6d3d1; }

  .pagination-wrapper {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
