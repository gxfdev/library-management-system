<template>
  <div class="statistics-page">
    <el-row :gutter="20">
      <el-col :xs="24" :lg="16">
        <el-card shadow="hover" v-loading="loading">
          <template #header>借阅趋势分析</template>
          <div class="chart-container" style="height: 350px;">
            <div class="mock-chart">
              <div class="chart-bars">
                <div v-for="(bar, i) in trendData" :key="i" class="chart-bar-wrapper">
                  <div class="chart-bar" :style="{ height: bar.value + '%' }"></div>
                  <span class="chart-label">{{ bar.label }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="8">
        <el-card shadow="hover"><template #header>分类占比</template>
          <div style="padding: 16px;">
            <div v-for="(cat, i) in categoryData" :key="i" class="cat-item">
              <span class="cat-name">{{ cat.name }}</span><el-progress :percentage="cat.percentage" :color="cat.color" />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover"><template #header>热门图书 TOP10</template>
          <div v-if="hotBooks.length">
            <div v-for="(book, i) in hotBooks" :key="i" class="hot-item">
              <span class="rank" :class="{ top3: i < 3 }">{{ i + 1 }}</span>
              <span class="title">{{ book.title }}</span>
              <span class="count">{{ book.borrowCount }}次</span>
            </div>
          </div>
          <el-empty v-else description="暂无借阅数据" />
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="12">
        <el-card shadow="hover"><template #header>统计数据汇总</template>
          <el-table :data="summaryData" border stripe size="default">
            <el-table-column prop="metric" label="统计指标" width="160" />
            <el-table-column prop="value" label="数值" width="100" align="center" />
            <el-table-column prop="description" label="说明" show-overflow-tooltip />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getDashboardStats, getBorrowTrend, getCategoryDistribution, getHotBooks } from '@/api/dashboard'

const loading = ref(false)

const trendData = ref<Array<{ label: string; value: number }>>([])

const categoryData = ref<Array<{ name: string; percentage: number; color: string }>>([])

const hotBooks = ref<{ title: string; borrowCount: number }[]>([])

const summaryData = ref<{ metric: string; value: string; description: string }[]>([])

onMounted(async () => {
  await Promise.all([fetchDashboardStats(), fetchBorrowTrend(), fetchCategoryDistribution(), fetchHotBooks()])
})

async function fetchDashboardStats() {
  loading.value = true
  try {
    const d = await getDashboardStats()
    summaryData.value = [
      { metric: '图书总量', value: String(d.totalBooks ?? 0), description: '系统中所有在馆图书总数' },
      { metric: '注册用户', value: String(d.totalUsers ?? 0), description: '已注册的读者和管理员数量' },
      { metric: '今日借阅', value: String(d.todayBorrows ?? 0), description: '今日新产生的借阅记录数' },
      { metric: '逾期未还', value: String(d.overdueCount ?? 0), description: '超过应还日期未归还的记录' },
      { metric: '可借库存', value: String(d.availableBooks ?? 0), description: '当前可借阅的图书数量' },
    ]
  } catch {} finally { loading.value = false }
}

async function fetchBorrowTrend() {
  try {
    const data = await getBorrowTrend()
    if (data && data.length) {
      const maxVal = Math.max(...data.map(d => d.value), 1)
      trendData.value = data.map(d => ({ label: d.label, value: Math.round((d.value / maxVal) * 100) }))
    }
  } catch {}
}

async function fetchCategoryDistribution() {
  try {
    const data = await getCategoryDistribution()
    if (data && data.length) {
      categoryData.value = data
    }
  } catch {}
}

async function fetchHotBooks() {
  try {
    const data = await getHotBooks()
    hotBooks.value = data || []
  } catch {}
}
</script>

<style lang="scss" scoped>
.statistics-page {
  .mock-chart {
    height: 100%;
    .chart-bars {
      display: flex; justify-content: space-around; align-items: flex-end;
      height: 280px; gap: 6px;
      .chart-bar-wrapper {
        flex: 1; display: flex; flex-direction: column; align-items: center;
        .chart-bar {
          width: 100%; background: linear-gradient(180deg, #1890ff, #36cfc9);
          border-radius: 3px 3px 0 0; transition: all 0.3s ease;
          &:hover { opacity: 0.85; transform: scaleY(1.05); }
        }
        .chart-label { margin-top: 6px; font-size: 10px; color: var(--text-secondary); }
      }
    }
  }

  .cat-item { margin-bottom: 14px; &:last-child { margin-bottom: 0; }
    .cat-name { display: inline-block; width: 80px; font-size: 13px; color: var(--text-regular); }
  }

  .hot-item {
    display: flex; align-items: center; padding: 10px 0;
    border-bottom: 1px solid var(--border-light);
    &:last-child { border-bottom: none; }
    .rank {
      width: 24px; height: 24px; border-radius: 50%; background: var(--bg-page);
      text-align: center; line-height: 24px; font-size: 12px; font-weight: 600;
      margin-right: 10px; color: var(--text-secondary);
      &.top3 { background: var(--primary-color); color: white; }
    }
    .title { flex: 1; font-size: 14px; color: var(--text-primary); }
    .count { color: var(--text-secondary); font-size: 13px; }
  }
}
</style>
