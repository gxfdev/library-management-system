<template>
  <div class="book-management">
    <div class="search-form">
      <el-form :model="queryParams" inline size="default">
        <el-form-item label="关键词">
          <el-input
            v-model="queryParams.keyword"
            placeholder="书名/作者/ISBN"
            clearable
            style="width: 240px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="queryParams.categoryId" placeholder="全部分类" clearable style="width: 160px">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="在馆" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="card-container">
      <div class="table-toolbar">
        <div class="toolbar-left">
          <el-button type="primary" @click="handleAdd">新增图书</el-button>
          <el-button @click="handleExport">导出数据</el-button>
        </div>
        <div class="toolbar-right"><span>共 {{ total }} 条记录</span></div>
      </div>

      <el-table v-loading="loading" :data="bookList" stripe border size="default">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column label="封面" width="70" align="center">
          <template #default="{ row }">
            <el-image v-if="row.coverImage" :src="getImageUrl(row.coverImage)" fit="cover" style="width: 40px; height: 52px;" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="书名" min-width="180" show-overflow-tooltip />
        <el-table-column prop="author" label="作者" width="120" show-overflow-tooltip />
        <el-table-column prop="isbn" label="ISBN" width="140" show-overflow-tooltip />
        <el-table-column prop="publisher" label="出版社" width="140" show-overflow-tooltip />
        <el-table-column prop="stockTotal" label="库存" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '在馆' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link size="small" @click="handleBorrow(row)">借阅</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
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
          @change="fetchBookList"
        />
      </div>
    </div>

    <BookDialog v-model:visible="dialogVisible" :book-id="editBookId" @success="fetchBookList" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import BookDialog from './components/BookDialog.vue'
import { getBookPage, deleteBook } from '@/api/book'
import { getCategoryList } from '@/api/category'
import type { Book, BookCategory } from '@/types'

const router = useRouter()

const loading = ref(false)
const bookList = ref<Book[]>([])
const total = ref(0)
const categories = ref<BookCategory[]>([])
const dialogVisible = ref(false)
const editBookId = ref<number | null>(null)

const queryParams = reactive({ page: 1, size: 10, keyword: '', categoryId: undefined as number | undefined, status: null as number | null })

onMounted(() => {
  fetchBookList()
  fetchCategories()
})

async function fetchCategories() {
  try {
    categories.value = await getCategoryList() || []
  } catch {}
}

async function fetchBookList() {
  loading.value = true
  try {
    const res = await getBookPage(queryParams)
    bookList.value = res?.records || []
    total.value = res?.total || 0
  } catch {
    bookList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() { queryParams.page = 1; fetchBookList() }
function resetQuery() { Object.assign(queryParams, { page: 1, keyword: '', categoryId: undefined, status: null }); fetchBookList() }
function handleAdd() { editBookId.value = null; dialogVisible.value = true }
function handleEdit(row: Book) { editBookId.value = row.id; dialogVisible.value = true }
function handleBorrow(row: Book) { router.push({ path: '/borrow/code', query: { bookId: String(row.id), bookTitle: row.title } }) }
function getImageUrl(url: string) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  if (url.startsWith('/')) return url
  return '/' + url
}
function handleExport() {
  if (bookList.value.length === 0) {
    ElMessage.warning('暂无数据可导出')
    return
  }
  const headers = ['ID', 'ISBN', '书名', '作者', '出版社', '库存', '状态']
  const rows = bookList.value.map((row: Book) => [
    row.id,
    row.isbn,
    row.title,
    row.author || '',
    row.publisher || '',
    row.stockTotal || 0,
    row.status === 1 ? '在馆' : '下架',
  ])
  const csvContent = [headers, ...rows].map(r => r.join(',')).join('\n')
  const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `图书数据_${new Date().toISOString().slice(0, 10)}.csv`
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}
async function handleDelete(row: Book) {
  try {
    await ElMessageBox.confirm(`确定要删除图书"${row.title}"吗？`, '警告', { type: 'warning' })
    await deleteBook(row.id)
    ElMessage.success('删除成功')
    fetchBookList()
  } catch {}
}
</script>

<style lang="scss" scoped>
.book-management .toolbar-left { display: flex; gap: 8px; }
</style>
