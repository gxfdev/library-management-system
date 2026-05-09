<template>
  <div class="my-borrows">
    <div class="page-header">
      <h2>我的借阅</h2>
      <p>管理您的借阅记录，支持续借和自助归还</p>
    </div>

    <el-tabs type="border-card" class="borrow-tabs">
      <el-tab-pane name="borrowing">
        <template #label>
          <span class="tab-label">当前借阅</span>
          <el-badge :value="currentBorrows.length" :max="99" type="primary" v-if="currentBorrows.length > 0" />
        </template>

        <el-table :data="currentBorrows" stripe size="default" v-loading="loading" row-class-name="borrow-row">
          <el-table-column label="图书信息" min-width="240">
            <template #default="{ row }">
              <div class="book-cell">
                <div class="book-cover-mini" v-if="row.bookCoverImage">
                  <img :src="row.bookCoverImage" alt="" @error="(e: Event) => (e.target as HTMLImageElement).style.display = 'none'" />
                </div>
                <div class="book-text">
                  <strong>{{ row.bookTitle }}</strong>
                  <span class="isbn">{{ row.bookIsbn || '-' }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="borrowDate" label="借阅日期" width="110"><template #default="{ row }">{{ formatDate(row.borrowDate) }}</template></el-table-column>
          <el-table-column prop="dueDate" label="应还日期" width="120">
            <template #default="{ row }">
              <span :class="{ 'text-danger': isOverdue(row), 'text-warning': isDueSoon(row) }">
                {{ formatDate(row.dueDate) }}
                <el-tag v-if="isOverdue(row)" size="small" type="danger" effect="dark" style="margin-left: 4px;">逾期</el-tag>
                <el-tag v-else-if="isDueSoon(row)" size="small" type="warning" effect="light" style="margin-left: 4px;">即将到期</el-tag>
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="renewCount" label="续借" width="70" align="center">
            <template #default="{ row }">
              <span>{{ row.renewCount }}/2</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" align="center">
            <template #default="{ row }">
              <div class="action-btns">
                <el-button
                  type="success"
                  link
                  size="small"
                  :disabled="row.renewCount >= 2 || row.status === 'OVERDUE'"
                  @click="handleRenew(row)"
                >
                  续借
                </el-button>
                <el-button
                  type="danger"
                  link
                  size="small"
                  v-if="row.status === 'BORROWING' || row.status === 'OVERDUE'"
                  :loading="returningId === row.id"
                  @click="handleReturn(row)"
                >
                  归还
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="!loading && currentBorrows.length === 0" class="empty-area">
          <el-empty description="暂无借阅中的图书，去书架看看吧~" :image-size="100">
            <el-button type="primary" @click="$router.push('/book-catalog')">浏览图书</el-button>
          </el-empty>
        </div>
      </el-tab-pane>

      <el-tab-pane label="历史记录" name="history">
        <el-table :data="historyBorrows" stripe size="default" v-loading="loading">
          <el-table-column prop="bookTitle" label="图书名称" min-width="200" show-overflow-tooltip />
          <el-table-column prop="borrowDate" label="借阅日期" width="110"><template #default="{ row }">{{ formatDate(row.borrowDate) }}</template></el-table-column>
          <el-table-column prop="returnDate" label="归还日期" width="110"><template #default="{ row }">{{ formatDate(row.returnDate) || '-' }}</template></el-table-column>
          <el-table-column prop="fineAmount" label="罚款(元)" width="90" align="right">
            <template #default="{ row }"><span :class="{ 'text-danger': (row.fineAmount || 0) > 0, 'text-success': (row.fineAmount || 0) === 0 }">{{ (row.fineAmount || 0).toFixed(2) }}</span></template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="90" align="center">
            <template #default="{ row }"><el-tag :type="getStatusType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag></template>
          </el-table-column>
        </el-table>

        <div v-if="!loading && historyBorrows.length === 0" class="empty-area">
          <el-empty description="暂无历史借阅记录" :image-size="100" />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyBorrows, renewBook, selfReturnBook } from '@/api/borrow'
import type { BorrowRecord } from '@/types'

const router = useRouter()
const loading = ref(false)
const returningId = ref<number | null>(null)
const currentBorrows = ref<BorrowRecord[]>([])
const historyBorrows = ref<BorrowRecord[]>([])

onMounted(async () => {
  await Promise.all([fetchCurrentBorrows(), fetchHistoryBorrows()])
})

async function fetchCurrentBorrows() {
  loading.value = true
  try {
    const res = await getMyBorrows({ page: 1, size: 100, status: 'BORROWING' })
    currentBorrows.value = (res?.records || []).filter((r: BorrowRecord) => r.status === 'BORROWING' || r.status === 'OVERDUE')
  } catch {
    currentBorrows.value = []
  } finally { loading.value = false }
}

async function fetchHistoryBorrows() {
  try {
    const res = await getMyBorrows({ page: 1, size: 100, status: 'RETURNED' })
    historyBorrows.value = res?.records || []
  } catch {
    historyBorrows.value = []
  }
}

async function handleRenew(record: BorrowRecord) {
  if (record.renewCount >= 2) { ElMessage.warning('续借次数已达上限'); return }
  try {
    await ElMessageBox.confirm(`确定要续借《${record.bookTitle}》吗？`, '确认续借')
    await renewBook(record.id)
    ElMessage.success('续借成功，已延长30天')
    record.renewCount++
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') ElMessage.error('续借失败')
  }
}

async function handleReturn(record: BorrowRecord) {
  try {
    await ElMessageBox.confirm(
      `确定要归还《${record.bookTitle}》吗？\n${isOverdue(record) ? '⚠️ 该书已逾期，可能产生罚款。' : ''}`,
      '确认归还',
      { confirmButtonText: '确认归还', cancelButtonText: '取消', type: 'info', dangerouslyUseHTMLString: true },
    )
    returningId.value = record.id
    await selfReturnBook(record.id)
    ElMessage.success('归还成功！感谢您的使用')
    await fetchCurrentBorrows()
    await fetchHistoryBorrows()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') ElMessage.error(e?.message || '归还失败')
  } finally { returningId.value = null }
}

function formatDate(dateStr?: string): string { if (!dateStr) return '-'; return dateStr.split('T')[0] }

function isOverdue(record: BorrowRecord): boolean {
  return record.status === 'OVERDUE' || (record.status === 'BORROWING' && new Date(record.dueDate) < new Date())
}

function isDueSoon(record: BorrowRecord): boolean {
  if (record.status !== 'BORROWING') return false
  const due = new Date(record.dueDate)
  const now = new Date()
  const diff = due.getTime() - now.getTime()
  return diff > 0 && diff <= 3 * 24 * 60 * 60 * 1000
}

function getStatusType(status: string): string { const map: Record<string, string> = { RETURNED: 'success', OVERDUE: 'danger' }; return map[status] || '' }
function getStatusText(status: string): string { const map: Record<string, string> = { RETURNED: '已归还', OVERDUE: '逾期' }; return map[status] || status }
</script>

<style lang="scss" scoped>
.my-borrows {
  .page-header {
    margin-bottom: 20px;
    h2 { font-size: 20px; font-weight: 700; color: #1c1917; margin-bottom: 4px; }
    p { font-size: 13px; color: #78716c; }
  }

  .borrow-tabs {
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  }

  .tab-label { display: inline-flex; align-items: center; gap: 6px; }

  .book-cell {
    display: flex;
    align-items: center;
    gap: 10px;

    .book-cover-mini {
      width: 36px;
      height: 48px;
      border-radius: 4px;
      overflow: hidden;
      background: #f5f5f4;
      flex-shrink: 0;
      img { width: 100%; height: 100%; object-fit: cover; }
    }

    .book-text {
      display: flex;
      flex-direction: column;
      strong { font-size: 13.5px; color: #1c1917; line-height: 1.3; }
      .isbn { font-size: 11px; color: #a8a29e; margin-top: 2px; }
    }
  }

  .action-btns {
    display: flex;
    justify-content: center;
    gap: 4px;
  }

  .text-danger { color: var(--danger-color); font-weight: 600; }
  .text-warning { color: var(--warning-color); font-weight: 500; }
  .text-success { color: var(--success-color); }

  .empty-area { padding: 40px 20px; text-align: center; }
}
</style>
