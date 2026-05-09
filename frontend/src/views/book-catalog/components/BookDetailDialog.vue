<template>
  <el-dialog
    :model-value="visible"
    :title="null"
    width="680px"
    destroy-on-close
    @update:model-value="(val: boolean) => emit('update:visible', val)"
    class="book-detail-dialog"
  >
    <div v-if="loading" v-loading="true" style="min-height: 300px"></div>

    <template v-else-if="book">
      <div class="detail-body">
        <div class="detail-cover">
          <img v-if="book.coverImage" :src="getCoverUrl(book.coverImage)" :alt="book.title" />
          <div v-else class="cover-placeholder-lg">
            <span>{{ book.title.charAt(0) }}</span>
          </div>
          <div class="cover-actions">
            <el-button type="primary" size="large" round @click="handleBorrow" :disabled="book.stockAvailable <= 0">
              {{ book.stockAvailable > 0 ? '立即借阅' : '暂无库存' }}
            </el-button>
          </div>
        </div>

        <div class="detail-info">
          <h2 class="detail-title">{{ book.title }}</h2>
          <p class="detail-author">{{ book.author || '未知作者' }}<span v-if="book.publisher"> · {{ book.publisher }}</span></p>

          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">ISBN</span>
              <span class="info-value">{{ book.isbn || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">分类</span>
              <span class="info-value">{{ book.categoryName || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">页数</span>
              <span class="info-value">{{ book.pages || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">价格</span>
              <span class="info-value price">{{ book.price ? `¥${book.price.toFixed(2)}` : '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">出版日期</span>
              <span class="info-value">{{ book.publishDate?.split('T')[0] || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">馆藏位置</span>
              <span class="info-value">{{ book.location || '未分配' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">可借数量</span>
              <span class="info-value" :class="{ 'stock-low': book.stockAvailable <= 3, 'stock-out': book.stockAvailable <= 0 }">
                {{ book.available ?? book.stockAvailable }}
              </span>
            </div>
            <div class="info-item">
              <span class="info-label">总库存</span>
              <span class="info-value">{{ book.stockTotal }}</span>
            </div>
          </div>

          <div v-if="book.description" class="detail-desc">
            <h4>内容简介</h4>
            <p>{{ book.description }}</p>
          </div>
        </div>
      </div>
    </template>

    <template #footer>
      <el-button @click="emit('update:visible', false)">关闭</el-button>
      <el-button type="primary" @click="handleBorrow" :disabled="!book || book.stockAvailable <= 0">确认借阅</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBookById } from '@/api/book'
import { borrowBook as apiBorrowBook } from '@/api/borrow'
import { generateBorrowCode } from '@/api/borrowCode'
import type { Book } from '@/types'

const props = defineProps<{ visible: boolean; bookId: number | null }>()
const emit = defineEmits<{ (e: 'update:visible', val: boolean): void; (e: 'borrowed'): void }>()

const loading = ref(false)
const book = ref<Book | null>(null)

watch(() => props.visible, (val) => {
  if (val && props.bookId) {
    fetchDetail()
  }
})

async function fetchDetail() {
  if (!props.bookId) return
  loading.value = true
  try {
    book.value = await getBookById(props.bookId)
  } catch {
    book.value = null
  } finally {
    loading.value = false
  }
}

async function handleBorrow() {
  if (!book.value || book.value.stockAvailable <= 0) return
  try {
    await apiBorrowBook({ bookId: book.value.id })
    const codeRes = await generateBorrowCode(book.value.id)
    const borrowCode = codeRes?.code || '未知'
    ElMessageBox.alert(
      `您已成功借阅《${book.value.title}》！\n\n您的借阅码为：${borrowCode}\n有效期30分钟，请妥善保管。`,
      '借阅成功',
      { confirmButtonText: '我知道了', type: 'success' }
    )
    emit('update:visible', false)
    emit('borrowed')
  } catch (error: any) {
    ElMessage.error(error.message || '借阅失败')
  }
}

function getCoverUrl(url: string) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  if (url.startsWith('/')) return url
  return '/' + url
}
</script>

<style lang="scss" scoped>
.detail-body {
  display: flex;
  gap: 28px;
}

.detail-cover {
  flex-shrink: 0;
  width: 200px;

  img {
    width: 100%;
    height: 280px;
    object-fit: cover;
    border-radius: 10px;
    box-shadow: 0 4px 16px rgba(0,0,0,0.1);
  }

  .cover-placeholder-lg {
    width: 100%;
    height: 280px;
    border-radius: 10px;
    background: linear-gradient(145deg, #e8ecf1, #d5dbe6);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 72px;
    font-weight: 700;
    color: rgba(24,144,255,0.12);
    box-shadow: 0 4px 16px rgba(0,0,0,0.08);
  }

  .cover-actions {
    margin-top: 16px;
    text-align: center;
  }
}

.detail-info {
  flex: 1;
  min-width: 0;

  .detail-title {
    font-size: 22px;
    font-weight: 700;
    color: #1c1917;
    line-height: 1.3;
    margin-bottom: 6px;
  }

  .detail-author {
    font-size: 14px;
    color: #78716c;
    margin-bottom: 20px;
  }

  .info-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px 24px;
    margin-bottom: 20px;
  }

  .info-item {
    display: flex;
    flex-direction: column;
    gap: 3px;

    .info-label {
      font-size: 12px;
      color: #8c8c8c;
    }

    .info-value {
      font-size: 14px;
      color: #262626;
      font-weight: 500;

      &.price { color: #ff7875; }
      &.stock-low { color: #faad14; font-weight: 700; }
      &.stock-out { color: #ff4d4f; font-weight: 700; }
    }
  }

  .detail-desc {
    h4 {
      font-size: 14px;
      font-weight: 600;
      color: #262626;
      margin-bottom: 8px;
    }

    p {
      font-size: 13px;
      color: #595959;
      line-height: 1.7;
      max-height: 120px;
      overflow-y: auto;
    }
  }
}
</style>

<style lang="scss">
.book-detail-dialog {
  .el-dialog__header {
    display: none;
  }
  .el-dialog__body {
    padding: 28px 24px 16px;
  }
}
</style>
