<template>
  <div class="borrow-code-page">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="生成借阅码" name="generate">
        <div class="card-container">
          <div class="section-header">
            <h3>生成借阅码</h3>
            <p class="sub-text">选择图书后生成6位数字借阅码，有效期30分钟</p>
          </div>
          <el-form label-width="100px" style="max-width: 500px; margin-top: 20px">
            <el-form-item label="选择图书">
              <el-select
                v-model="selectedBookId"
                filterable
                remote
                placeholder="搜索图书名称/ISBN"
                style="width: 100%"
                :remote-method="searchBooks"
                :loading="bookLoading"
                value-key="id"
              >
                <el-option
                  v-for="b in bookOptions"
                  :key="b.id"
                  :label="b.title"
                  :value="b.id"
                >
                  <div style="display: flex; justify-content: space-between; align-items: center;">
                    <span>{{ b.title }}</span>
                    <span style="color: #999; font-size: 12px;">库存:{{ b.stockAvailable || b.stockTotal }}</span>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="generating" :disabled="!selectedBookId" @click="handleGenerate">生成借阅码</el-button>
            </el-form-item>
          </el-form>

          <el-result v-if="generatedCode" icon="success" title="借阅码生成成功" style="margin-top: 20px">
            <template #extra>
              <div class="code-display">
                <div class="code-value">{{ generatedCode.code }}</div>
                <div class="code-info">
                  <p>图书：{{ generatedCode.bookTitle }}</p>
                  <p>有效期至：{{ formatTime(generatedCode.expireTime) }}</p>
                  <p>状态：{{ statusLabel(generatedCode.status) }}</p>
                </div>
              </div>
            </template>
          </el-result>
        </div>
      </el-tab-pane>

      <el-tab-pane label="使用借阅码" name="verify">
        <div class="card-container">
          <div class="section-header">
            <h3>验证借阅码</h3>
            <p class="sub-text">输入6位借阅码完成图书借阅</p>
          </div>
          <el-form label-width="100px" style="max-width: 500px; margin-top: 20px">
            <el-form-item label="借阅码">
              <el-input v-model="codeInput" placeholder="请输入6位借阅码" maxlength="8" style="width: 100%" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="verifying" @click="handleVerify">验证并借阅</el-button>
            </el-form-item>
          </el-form>

          <el-result v-if="verifyResult" icon="success" title="借阅成功" style="margin-top: 20px">
            <template #extra>
              <div class="code-info">
                <p>图书：{{ verifyResult.borrowCode?.bookTitle || '-' }}</p>
                <p>借阅码：{{ verifyResult.borrowCode?.code }}</p>
                <p v-if="verifyResult.borrowCode?.usedTime">使用时间：{{ formatTime(verifyResult.borrowCode.usedTime) }}</p>
              </div>
            </template>
          </el-result>
        </div>
      </el-tab-pane>

      <el-tab-pane label="我的借阅码" name="my-codes">
        <div class="card-container">
          <div class="section-header">
            <div class="header-row">
              <h3>我的借阅码记录</h3>
              <el-button @click="loadMyCodes">刷新</el-button>
            </div>
          </div>
          <el-table :data="myCodes" border stripe v-loading="myLoading">
            <el-table-column prop="code" label="借阅码" width="120" />
            <el-table-column prop="bookTitle" label="图书" min-width="200" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="expireTime" label="过期时间" width="180">
              <template #default="{ row }">{{ formatTime(row.expireTime) }}</template>
            </el-table-column>
            <el-table-column prop="usedTime" label="使用时间" width="180">
              <template #default="{ row }">{{ formatTime(row.usedTime) }}</template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180">
              <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap">
            <el-pagination v-model:current-page="myPage" v-model:page-size="mySize" :total="myTotal"
              layout="total, prev, pager, next" @current-change="loadMyCodes" />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { generateBorrowCode, verifyBorrowCode, getMyBorrowCodes } from '@/api/borrowCode'
import { getBookPage } from '@/api/book'

const route = useRoute()

const activeTab = ref('generate')
const selectedBookId = ref<number | undefined>(undefined)
const bookLoading = ref(false)
const bookOptions = ref<any[]>([])
const generating = ref(false)
const generatedCode = ref<any>(null)

const codeInput = ref('')
const verifying = ref(false)
const verifyResult = ref<any>(null)

const myCodes = ref<any[]>([])
const myLoading = ref(false)
const myPage = ref(1)
const mySize = ref(10)
const myTotal = ref(0)

onMounted(() => {
  loadMyCodes()
  if (route.query.bookId) {
    const bookId = Number(route.query.bookId)
    if (!isNaN(bookId)) {
      selectedBookId.value = bookId
      const bookTitle = route.query.bookTitle as string
      if (bookTitle) {
        bookOptions.value = [{ id: bookId, title: bookTitle }]
      } else {
        searchBooks(String(bookId))
      }
    }
  }
})

async function searchBooks(query: string) {
  if (!query || query.length < 1) return
  bookLoading.value = true
  try {
    const res = await getBookPage({ page: 1, size: 20, keyword: query })
    bookOptions.value = res?.records || []
  } catch { bookOptions.value = [] } finally { bookLoading.value = false }
}

async function handleGenerate() {
  if (!selectedBookId.value) { ElMessage.warning('请先选择图书'); return }
  generating.value = true
  try {
    generatedCode.value = await generateBorrowCode(selectedBookId.value)
    ElMessage.success('借阅码生成成功')
    loadMyCodes()
  } catch { ElMessage.error('生成失败') } finally { generating.value = false }
}

async function handleVerify() {
  if (!codeInput.value.trim()) { ElMessage.warning('请输入借阅码'); return }
  verifying.value = true
  try {
    verifyResult.value = await verifyBorrowCode(codeInput.value.trim())
    ElMessage.success('借阅成功')
    loadMyCodes()
  } catch { ElMessage.error('验证失败') } finally { verifying.value = false }
}

async function loadMyCodes() {
  myLoading.value = true
  try {
    const res = await getMyBorrowCodes({ page: myPage.value, size: mySize.value })
    myCodes.value = res?.records || []
    myTotal.value = res?.total || 0
  } catch {} finally { myLoading.value = false }
}

function formatTime(t?: string): string {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 19)
}

function statusLabel(s: string) {
  const m: Record<string, string> = { VALID: '有效', USED: '已使用', EXPIRED: '已过期', INVALID: '已作废' }
  return m[s] || s
}
function statusType(s: string) {
  const m: Record<string, string> = { VALID: 'success', USED: 'info', EXPIRED: 'warning', INVALID: 'danger' }
  return m[s] || 'info'
}
</script>

<style lang="scss" scoped>
.borrow-code-page {
  .section-header {
    margin-bottom: 16px;
    h3 { margin: 0 0 4px; font-size: 16px; }
    .sub-text { color: #8c8c8c; font-size: 13px; margin: 0; }
    .header-row { display: flex; align-items: center; justify-content: space-between; }
  }
  .code-display {
    text-align: center;
    .code-value {
      font-size: 48px; font-weight: 700; letter-spacing: 12px;
      color: #409eff; margin: 16px 0;
    }
  }
  .code-info { text-align: left; p { margin: 6px 0; } }
  .pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
}
</style>
