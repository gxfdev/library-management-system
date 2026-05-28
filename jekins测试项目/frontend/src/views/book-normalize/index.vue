<template>
  <div class="normalize-page">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="图书审核" name="review">
        <div class="card-container">
          <div class="section-header">
            <div class="header-row">
              <h3>图书名称规范化审核</h3>
              <div>
                <el-button type="primary" @click="handleBatchNormalize" :loading="batchLoading" :disabled="!hasIssues">
                  一键修正所有问题
                </el-button>
                <el-button @click="loadPending">刷新</el-button>
              </div>
            </div>
          </div>

          <el-table :data="pendingBooks" border stripe v-loading="loading" row-key="id">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column label="书名" min-width="200">
              <template #default="{ row }">
                <div>
                  <span>{{ row.title }}</span>
                  <el-tag v-if="row.suggestedTitle !== row.title" type="warning" size="small" style="margin-left: 8px">
                    建议: {{ row.suggestedTitle }}
                  </el-tag>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="作者" min-width="160">
              <template #default="{ row }">
                <div>
                  <span>{{ row.author }}</span>
                  <el-tag v-if="row.suggestedAuthor !== row.author" type="warning" size="small" style="margin-left: 8px">
                    建议: {{ row.suggestedAuthor }}
                  </el-tag>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="isbn" label="ISBN" width="150" />
            <el-table-column label="问题" width="200">
              <template #default="{ row }">
                <template v-if="row.issues && row.issues.length > 0">
                  <el-tag v-for="issue in row.issues" :key="issue" type="danger" size="small" style="margin: 2px">
                    {{ issue }}
                  </el-tag>
                </template>
                <el-tag v-else type="success" size="small">合规</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleEdit(row)">修正</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="对比报表" name="report">
        <div class="card-container">
          <div class="section-header"><h3>规范化变更记录</h3></div>
          <el-table :data="reportData" border stripe v-loading="reportLoading">
            <el-table-column prop="createTime" label="时间" width="180" />
            <el-table-column prop="operatorName" label="操作人" width="120" />
            <el-table-column label="书名变更" min-width="250">
              <template #default="{ row }">
                <span class="old-val">{{ row.oldTitle }}</span>
                <span class="arrow">→</span>
                <span class="new-val">{{ row.newTitle }}</span>
              </template>
            </el-table-column>
            <el-table-column label="作者变更" min-width="200">
              <template #default="{ row }">
                <span class="old-val">{{ row.oldAuthor }}</span>
                <span class="arrow">→</span>
                <span class="new-val">{{ row.newAuthor }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="row.status === 'BATCH_APPROVED' ? 'warning' : 'success'" size="small">
                  {{ row.status === 'BATCH_APPROVED' ? '批量修正' : '审核通过' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap">
            <el-pagination v-model:current-page="reportPage" v-model:page-size="reportSize" :total="reportTotal"
              layout="total, prev, pager, next" @current-change="loadReport" />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="editVisible" title="修正图书信息" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="书名">
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="editForm.author" />
        </el-form-item>
        <el-form-item label="ISBN">
          <el-input v-model="editForm.isbn" />
        </el-form-item>
        <el-form-item label="出版社">
          <el-input v-model="editForm.publisher" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="editLoading">确认修正</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPendingBooks, normalizeBook, batchNormalize, getNormalizeReport } from '@/api/bookNormalize'

const activeTab = ref('review')
const loading = ref(false)
const pendingBooks = ref<any[]>([])

const reportData = ref<any[]>([])
const reportLoading = ref(false)
const reportPage = ref(1)
const reportSize = ref(10)
const reportTotal = ref(0)

const editVisible = ref(false)
const editLoading = ref(false)
const editForm = ref<Record<string, string>>({})

const batchLoading = ref(false)

const hasIssues = computed(() => pendingBooks.value.some((b: any) => b.issues && b.issues.length > 0))

onMounted(() => {
  loadPending()
  loadReport()
})

async function loadPending() {
  loading.value = true
  try {
    pendingBooks.value = await getPendingBooks() || []
  } catch {} finally { loading.value = false }
}

async function loadReport() {
  reportLoading.value = true
  try {
    const res = await getNormalizeReport({ page: reportPage.value, size: reportSize.value })
    reportData.value = res?.records || []
    reportTotal.value = res?.total || 0
  } catch {} finally { reportLoading.value = false }
}

function handleEdit(row: any) {
  editForm.value = { id: row.id, title: row.suggestedTitle || row.title, author: row.suggestedAuthor || row.author, isbn: row.isbn, publisher: row.publisher }
  editVisible.value = true
}

async function submitEdit() {
  editLoading.value = true
  try {
    await normalizeBook(Number(editForm.value.id), editForm.value)
    ElMessage.success('修正成功')
    editVisible.value = false
    loadPending()
    loadReport()
  } catch {} finally { editLoading.value = false }
}

async function handleBatchNormalize() {
  batchLoading.value = true
  try {
    const items = pendingBooks.value
      .filter((b: any) => b.issues && b.issues.length > 0)
      .map((b: any) => ({ id: b.id, title: b.suggestedTitle || b.title, author: b.suggestedAuthor || b.author, isbn: b.isbn, publisher: b.publisher }))
    if (items.length === 0) { ElMessage.info('没有需要修正的图书'); return }
    ElMessage.success(`批量修正完成，共处理 ${await batchNormalize(items)} 本图书`)
    loadPending()
    loadReport()
  } catch {} finally { batchLoading.value = false }
}
</script>

<style lang="scss" scoped>
.normalize-page {
  .section-header {
    margin-bottom: 16px;
    h3 { margin: 0; font-size: 16px; }
    .header-row { display: flex; align-items: center; justify-content: space-between; }
  }
  .pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
  .old-val { color: var(--danger-color); text-decoration: line-through; }
  .new-val { color: var(--success-color); font-weight: 500; }
  .arrow { margin: 0 8px; color: var(--text-placeholder); }
}
</style>
