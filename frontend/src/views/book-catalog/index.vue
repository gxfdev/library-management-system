<template>
  <div class="book-catalog">
    <div class="catalog-header animate-fade-in">
      <div class="header-search">
        <el-input
          v-model="queryParams.keyword"
          placeholder="搜索书名、作者、ISBN..."
          size="large"
          clearable
          :prefix-icon="Search"
          @keyup.enter="handleSearch"
        />
      </div>
      <div class="header-filters">
        <el-select v-model="queryParams.categoryId" placeholder="全部分类" clearable style="width: 150px" @change="handleSearch">
          <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
        </el-select>
        <el-radio-group v-model="sortBy" size="default" @change="handleSearch" style="margin-left: 8px;">
          <el-radio-button value="default">默认</el-radio-button>
          <el-radio-button value="hot">热门</el-radio-button>
          <el-radio-button value="new">最新</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <div v-loading="loading" class="book-grid" :class="{ 'grid-empty': !bookList.length && !loading }">
      <transition-group name="book-card">
        <div v-for="(book, idx) in bookList" :key="book.id"
             class="book-card" :style="{ '--card-idx': idx }"
             @click="handleBookClick(book)">
          <div class="cover-area">
            <img
              v-if="book.coverImage"
              :src="getImageUrl(book.coverImage)"
              :alt="book.title"
              loading="lazy"
              @error="handleImageError($event)"
            />
            <div v-else class="cover-placeholder" :style="{ '--hue': getHue(book.title) }">
              <svg viewBox="0 0 160 200" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect width="160" height="200" rx="4" fill="url(#cp)"/>
                <rect x="10" y="12" width="140" height="176" rx="2" stroke="rgba(255,255,255,0.1)" stroke-width="1"/>
                <line x1="20" y1="50" x2="90" y2="50" stroke="rgba(255,255,255,0.18)" stroke-width="3" stroke-linecap="round"/>
                <line x1="20" y1="66" x2="75" y2="66" stroke="rgba(255,255,255,0.12)" stroke-width="2.5" stroke-linecap="round"/>
                <line x1="20" y1="80" x2="85" y2="80" stroke="rgba(255,255,255,0.1)" stroke-width="2" stroke-linecap="round"/>
                <text x="18" y="155" fill="rgba(255,255,255,0.7)" font-size="28" font-weight="700">{{ book.title.charAt(0) }}</text>
                <defs><linearGradient id="cp" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" :style="`stop-color:hsl(${getHue(book.title)},25%,35%)`"/>
                  <stop offset="100%" :style="`stop-color:hsl(${getHue(book.title)},22%,26%)`"/>
                </linearGradient></defs>
              </svg>
            </div>

            <div class="cover-actions">
              <button class="action-chip" @click.stop="handleBorrow(book)" :disabled="book.stockAvailable <= 0">
                {{ book.stockAvailable > 0 ? '借阅' : '已借完' }}
              </button>
            </div>

            <span v-if="book.stockAvailable <= 0" class="stock-tag tag-empty">已借完</span>
            <span v-else-if="book.stockAvailable <= 3" class="stock-tag tag-low">仅剩{{ book.stockAvailable }}本</span>
            <span v-else-if="book.stockAvailable >= 10" class="stock-tag tag-hot">热销</span>
          </div>

          <div class="card-body">
            <h3 class="book-title">{{ book.title }}</h3>
            <p class="book-meta">
              <span>{{ book.author || '未知作者' }}</span>
              <span v-if="book.categoryName" class="cat-label">{{ book.categoryName }}</span>
            </p>
            <div class="card-footer">
              <span class="avail"><svg width="13" height="13" viewBox="0 0 16 16" fill="none"><path d="M8 2L2 6v7a1 1 0 001 1h10a1 1 0 001-1V6L8 2z" stroke="currentColor" stroke-width="1.3" fill="none"/></svg> 可借 {{ book.stockAvailable }}</span>
              <span v-if="book.price" class="price">¥{{ (book.price || 0).toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </transition-group>

      <div v-if="!loading && !bookList.length" class="empty-state">
        <el-empty description="暂无图书数据" :image-size="120">
          <template #image>
            <svg width="120" height="100" viewBox="0 0 120 100" fill="none">
              <rect x="14" y="10" width="42" height="60" rx="3" fill="#f5f5f4" stroke="#e7e5e4" stroke-width="1.2"/>
              <path d="M21 24h28M21 34h24M21 44h18M21 54h22" stroke="#d6d3d1" stroke-width="1.8" stroke-linecap="round"/>
              <rect x="40" y="18" width="48" height="68" rx="3" fill="#fafaf9" stroke="#e7e5e4" stroke-width="1.2"/>
              <path d="M50 34h30M50 44h26M50 54h20M50 64h28" stroke="#d6d3d1" stroke-width="1.8" stroke-linecap="round"/>
              <circle cx="76" cy="78" r="9" fill="#f5f5f4" stroke="#e7e5e4" stroke-width="1"/>
              <path d="M73 78l2 2 4-4" stroke="#a8a29e" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </template>
        </el-empty>
      </div>
    </div>

    <div v-if="total > queryParams.size" class="pagination-wrap">
      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        :total="total"
        :page-sizes="[12, 24, 36]"
        layout="prev, pager, next"
        @current-change="fetchBooks"
        background
      />
    </div>

    <BookDetailDialog
      v-model:visible="detailVisible"
      :book-id="selectedBookId"
      @borrowed="fetchBooks"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getBookPage, type BookPageParams } from '@/api/book'
import { getCategoryList } from '@/api/category'
import { borrowBook as apiBorrowBook } from '@/api/borrow'
import { generateBorrowCode } from '@/api/borrowCode'
import type { Book, BookCategory } from '@/types'
import BookDetailDialog from './components/BookDetailDialog.vue'

const loading = ref(false)
const bookList = ref<Book[]>([])
const total = ref(0)
const categories = ref<BookCategory[]>([])
const sortBy = ref('default')
const detailVisible = ref(false)
const selectedBookId = ref<number | null>(null)

const queryParams = reactive({ page: 1, size: 12, keyword: '', categoryId: undefined as number | undefined })

onMounted(() => {
  fetchCategories()
  fetchBooks()
})

async function fetchCategories() {
  try { categories.value = await getCategoryList() || [] } catch {}
}

async function fetchBooks() {
  loading.value = true
  try {
    const params: BookPageParams = { page: queryParams.page, size: queryParams.size }
    if (queryParams.keyword) params.keyword = queryParams.keyword.trim()
    if (queryParams.categoryId) params.categoryId = queryParams.categoryId
    const res = await getBookPage(params)
    let books: Book[] = res?.records || []
    if (sortBy.value === 'hot') books.sort((a, b) => (b.stockTotal - b.stockAvailable) - (a.stockTotal - a.stockAvailable))
    else if (sortBy.value === 'new') books.sort((a, b) => new Date(b.createTime).getTime() - new Date(a.createTime).getTime())
    bookList.value = books
    total.value = res?.total || 0
  } catch { bookList.value = []; total.value = 0 }
  finally { loading.value = false }
}

function handleSearch() { queryParams.page = 1; fetchBooks() }

function getImageUrl(url: string) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  if (url.startsWith('/')) return url
  return '/' + url
}

function handleImageError(e: Event) { (e.target as HTMLImageElement).style.display = 'none' }

function getHue(title: string): number {
  let hash = 0
  for (let i = 0; i < title.length; i++) hash = title.charCodeAt(i) + ((hash << 5) - hash)
  return Math.abs(hash % 360)
}

function handleBookClick(book: Book) { selectedBookId.value = book.id; detailVisible.value = true }

async function handleBorrow(book: Book) {
  if (book.stockAvailable <= 0) { ElMessage.warning('该图书暂时无库存'); return }
  try {
    await ElMessageBox.confirm(`确定要借阅《${book.title}》吗？系统将为您生成借阅码。`, '确认借阅', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'info',
    })
    try {
      await apiBorrowBook({ bookId: book.id })
      const codeRes = await generateBorrowCode(book.id)
      const borrowCode = codeRes?.code || '未知'
      ElMessageBox.alert(
        `您已成功借阅《${book.title}》！\n\n您的借阅码为：${borrowCode}\n有效期30分钟，请妥善保管。`,
        '借阅成功',
        { confirmButtonText: '我知道了', type: 'success', dangerouslyUseHTMLString: false }
      )
      fetchBooks()
    } catch (error: any) { ElMessage.error(error.message || '借阅失败') }
  } catch {}
}
</script>

<style lang="scss" scoped>
.book-catalog { max-width: 1360px; }

.catalog-header {
  display: flex;
  gap: 16px;
  margin-bottom: 28px;
  align-items: center;

  .header-search {
    flex: 1;
    max-width: 480px;

    :deep(.el-input__wrapper) {
      border-radius: 11px;
      padding-left: 18px;
      box-shadow: 0 0 0 1px #e7e5e4 inset;
      transition: all 0.28s cubic-bezier(0.4, 0, 0.2, 1);
      background: #fff;

      &:hover, &.is-focus {
        box-shadow: 0 0 0 1.5px var(--primary-color) inset;
      }

      .el-input__inner { height: 44px; font-size: 14px; letter-spacing: 0.2px; }
    }
  }

  .header-filters { display: flex; align-items: center; gap: 8px; }
}

.book-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
  min-height: 300px;
}

.book-card {
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.32s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #f5f5f4;
  animation: cardIn 0.45s ease-out both;
  animation-delay: calc(var(--card-idx, 0) * 0.05s);

  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 12px 32px rgba(28,25,23,0.08), 0 0 0 1px #e7e5e4;

    .cover-actions { opacity: 1; transform: translateY(0); }
    .cover-area img,
    .cover-placeholder { transform: scale(1.03); }
    .card-body .book-title { color: var(--text-primary); }
  }

  .cover-area {
    position: relative;
    height: 210px;
    overflow: hidden;
    background: linear-gradient(145deg, #f5f5f4 0%, #e7e5e4 100%);
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 16px;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      border-radius: 6px;
      transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    }

    .cover-placeholder {
      width: 100%;
      height: 100%;
      border-radius: 6px;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);

      svg { width: 75%; height: auto; opacity: 0.95; }
    }

    .cover-actions {
      position: absolute;
      inset: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(28,25,23,0.45);
      backdrop-filter: blur(4px);
      opacity: 0;
      transform: translateY(8px);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      z-index: 2;
      border-radius: 6px;
      margin: 16px;

      .action-chip {
        padding: 10px 28px;
        border-radius: 20px;
        background: #fff;
        color: var(--text-primary);
        font-weight: 600;
        font-size: 13.5px;
        cursor: pointer;
        border: none;
        transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
        box-shadow: 0 4px 14px rgba(0,0,0,0.12);

        &:hover:not(:disabled) {
          background: var(--text-primary);
          color: #fff;
          transform: scale(1.04);
          box-shadow: 0 6px 18px rgba(28,25,23,0.2);
        }

        &:disabled {
          background: rgba(255,255,255,0.88);
          color: #a8a29e;
          cursor: not-allowed;
          box-shadow: none;
        }
      }
    }

    .stock-tag {
      position: absolute;
      top: 12px;
      right: 12px;
      padding: 3px 9px;
      border-radius: 6px;
      font-size: 11px;
      font-weight: 650;
      letter-spacing: 0.3px;
      z-index: 3;

      &.tag-empty { background: rgba(220,38,38,0.88); color: #fff; }
      &.tag-low { background: rgba(161,98,7,0.88); color: #fff; }
      &.tag-hot { background: rgba(22,101,52,0.86); color: #fff; }
    }
  }

  .card-body {
    padding: 14px 16px 16px;

    .book-title {
      font-size: 14.5px;
      font-weight: 650;
      color: #292524;
      line-height: 1.4;
      margin-bottom: 4px;
      display: -webkit-box;
      -webkit-line-clamp: 1;
      line-clamp: 1;
      -webkit-box-orient: vertical;
      overflow: hidden;
      transition: color 0.25s ease;
    }

    .book-meta {
      display: flex;
      align-items: center;
      justify-content: space-between;
      font-size: 12.5px;
      color: #a8a29e;
      margin-bottom: 10px;

      .cat-label {
        font-size: 11px;
        padding: 2px 8px;
        border-radius: 5px;
        background: var(--primary-light);
        color: var(--primary-color);
        font-weight: 500;
      }
    }

    .card-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: 10px;
      border-top: 1px solid #f5f5f4;

      .avail {
        font-size: 12px;
        color: var(--success-color);
        display: flex;
        align-items: center;
        gap: 4px;
        font-weight: 550;
      }

      .price {
        font-size: 13px;
        color: #b91c1c;
        font-weight: 700;
      }
    }
  }
}

@keyframes cardIn {
  from { opacity: 0; transform: translateY(20px) scale(0.97); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.grid-empty { display: flex; align-items: center; justify-content: center; }
.empty-state { grid-column: 1 / -1; padding: 64px 0; }

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding-bottom: 16px;
}

.book-card-enter-active { transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1); }
.book-card-leave-active { transition: all 0.28s ease-in; position: absolute; }
.book-card-enter-from { opacity: 0; transform: translateY(20px) scale(0.97); }
.book-card-leave-to { opacity: 0; transform: translateX(-20px); }

@media (max-width: 768px) {
  .catalog-header { flex-direction: column; align-items: stretch; }
  .book-grid { grid-template-columns: repeat(auto-fill, minmax(165px, 1fr)); gap: 14px; }
  .book-card .cover-area { height: 180px; padding: 12px; }
  .book-card .card-body { padding: 12px 14px 14px; }
}
</style>
