<template>
  <div class="dashboard">
    <div class="welcome-bar animate-fade-in">
      <div class="welcome-left">
        <div class="welcome-icon">
          <svg width="40" height="40" viewBox="0 0 44 44" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect width="44" height="44" rx="12" fill="#2c3e50"/>
            <rect x="10" y="12" width="11" height="22" rx="1.5" fill="#fff" opacity="0.95"/>
            <rect x="23" y="14" width="11" height="20" rx="1.5" fill="#fff" opacity="0.6"/>
            <line x1="13" y1="17" x2="18" y2="17" stroke="#2c3e50" stroke-width="2" stroke-linecap="round"/>
            <line x1="13" y1="21" x2="18" y2="21" stroke="#2c3e50" stroke-width="1.6" stroke-linecap="round"/>
            <line x1="26" y1="19" x2="31" y2="19" stroke="#7f8c8d" stroke-width="1.8" stroke-linecap="round"/>
            <line x1="26" y1="23" x2="29" y2="23" stroke="#95a5a6" stroke-width="1.4" stroke-linecap="round"/>
          </svg>
        </div>
        <div class="welcome-text">
          <h2>{{ greeting }}，{{ userInfo?.realName || userInfo?.username || '用户' }}</h2>
          <p>{{ todayStr }}</p>
        </div>
      </div>
      <div class="welcome-actions" v-if="isReader">
        <button class="action-btn" @click="$router.push('/book-catalog')">
          <el-icon :size="16"><Reading /></el-icon>去借书
        </button>
      </div>
    </div>

    <div class="stat-grid">
      <div v-for="(card, i) in statCards" :key="i"
           class="stat-card animate-fade-in"
           :style="{ '--delay': (i * 0.06) + 's' }"
           @mouseenter="card.hovered = true" @mouseleave="card.hovered = false">
        <div class="stat-top">
          <span class="stat-label">{{ card.label }}</span>
          <div class="stat-icon-wrap" :class="{ hovered: card.hovered, [card.type]: true }">
            <el-icon :size="18"><component :is="card.icon" /></el-icon>
          </div>
        </div>
        <div class="stat-value">{{ card.value }}</div>
        <div class="stat-trend" v-if="card.trend !== null">
          <span :class="{ up: card.trend > 0, down: card.trend < 0 }">
            {{ card.trend > 0 ? '↑' : card.trend < 0 ? '↓' : '' }}{{ Math.abs(card.trend) }}%
          </span>
          较上月
        </div>
      </div>
    </div>

    <div class="chart-row">
      <div class="panel-card main-panel animate-fade-in">
        <div class="panel-header">
          <h3>借阅趋势</h3>
          <span>近7天</span>
        </div>
        <div class="bar-chart" v-if="chartData.length">
          <div v-for="(bar, i) in chartData" :key="i" class="bar-col">
            <span class="bar-val">{{ bar.display }}</span>
            <div class="bar-track">
              <div class="bar-fill" :style="{ height: bar.pct + '%' }"></div>
            </div>
            <span class="bar-day">{{ bar.label }}</span>
          </div>
        </div>
        <el-empty v-else description="暂无数据" :image-size="56" />
      </div>

      <div class="panel-card side-panel animate-fade-in">
        <div class="panel-header">
          <h3>分类分布</h3>
          <span>图书占比</span>
        </div>
        <div class="cat-list" v-if="categoryStats.length">
          <div v-for="(cat, i) in categoryStats" :key="i" class="cat-row">
            <span class="cat-dot" :style="{ background: cat.color }"></span>
            <span class="cat-name">{{ cat.name }}</span>
            <div class="cat-track"><div class="cat-fill" :style="{ width: cat.percentage + '%', background: cat.color }"></div></div>
            <span class="cat-pct">{{ cat.percentage }}%</span>
          </div>
        </div>
        <el-empty v-else description="暂无数据" :image-size="56" />
      </div>
    </div>

    <div class="bottom-row">
      <div class="panel-card hot-panel animate-fade-in">
        <div class="panel-header">
          <h3>热门图书</h3>
          <span>借阅排行</span>
        </div>
        <div v-if="hotBooks.length" class="hot-list">
          <div v-for="(book, i) in hotBooks" :key="i" class="hot-item">
            <span class="rank-num" :class="{ top: i < 3 }" :data-rk="i">{{ i + 1 }}</span>
            <div class="hot-info">
              <span class="hot-title" @click="goToBook(book.title)">{{ book.title }}</span>
              <div class="hot-bar"><div class="hot-fill" :style="{ width: (hotBooks[0]?.borrowCount ? book.borrowCount / hotBooks[0].borrowCount * 100 : 0) + '%' }"></div></div>
            </div>
            <span class="hot-count">{{ book.borrowCount }}次</span>
          </div>
        </div>
        <el-empty v-else description="暂无数据" :image-size="56" />
      </div>

      <div class="panel-card recent-panel animate-fade-in">
        <div class="panel-header">
          <h3>最近借阅</h3>
          <span>最新动态</span>
        </div>
        <el-table :data="recentBorrows" size="small" stripe>
          <el-table-column prop="userName" label="读者" width="80" />
          <el-table-column prop="bookTitle" label="图书" show-overflow-tooltip />
          <el-table-column prop="borrowDate" label="日期" width="90" />
          <el-table-column prop="status" label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-tag
                :type="row.status === 'BORROWING' ? 'primary' : row.status === 'OVERDUE' ? 'danger' : 'success'"
                size="small" round effect="light"
              >
                {{ row.status === 'BORROWING' ? '借阅中' : row.status === 'OVERDUE' ? '已逾期' : '已归还' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import { getDashboardStats, getBorrowTrend, getCategoryDistribution, getHotBooks } from '@/api/dashboard'
import { getBorrowPage, getMyBorrows } from '@/api/borrow'
import { Reading } from '@element-plus/icons-vue'
import type { BorrowRecord } from '@/types'

const router = useRouter()
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)
const isReader = computed(() => userStore.isReader)

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '凌晨好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const todayStr = computed(() => {
  const d = new Date()
  const weekDays = ['日', '一', '二', '三', '四', '五', '六']
  return `${d.getMonth() + 1}月${d.getDate()}日 星期${weekDays[d.getDay()]}`
})

interface StatCardDef {
  icon: string; label: string; value: string; trend: number | null; hovered: boolean; type: string;
}

const statCards = ref<StatCardDef[]>([
  { icon: 'Reading', label: '图书总量', value: '-', trend: null, hovered: false, type: 'primary' },
  { icon: 'User', label: '注册读者', value: '-', trend: 3.2, hovered: false, type: 'success' },
  { icon: 'Tickets', label: '今日借阅', value: '-', trend: -1.5, hovered: false, type: 'warning' },
  { icon: 'Box', label: '逾期未还', value: '-', trend: null, hovered: false, type: 'danger' },
])

const chartData = ref<Array<{ label: string; pct: number; display: number }>>([])

const categoryStats = ref<Array<{ name: string; percentage: number; color: string }>>([])

const recentBorrows = ref<BorrowRecord[]>([])
const hotBooks = ref<{ title: string; borrowCount: number }[]>([])

let aborted = false

onMounted(async () => {
  const results = await Promise.allSettled([
    getDashboardStats(),
    getBorrowTrend(),
    getCategoryDistribution(),
    getHotBooks(),
  ])

  if (aborted) return

  if (results[0].status === 'fulfilled') {
    const data = results[0].value
    statCards.value[0].value = String(data.totalBooks ?? '-')
    statCards.value[1].value = String(data.totalUsers ?? '-')
    statCards.value[2].value = String(data.todayBorrows ?? '-')
    statCards.value[3].value = String(data.overdueCount ?? '-')
  }

  if (results[1].status === 'fulfilled') {
    const trend = results[1].value
    if (trend && trend.length) {
      const maxVal = Math.max(...trend.map(t => t.value), 1)
      chartData.value = trend.map(t => ({
        label: t.label,
        pct: Math.round((t.value / maxVal) * 100),
        display: t.value
      }))
    }
  }

  if (results[2].status === 'fulfilled') {
    const cats = results[2].value
    if (cats && cats.length) {
      categoryStats.value = cats
    }
  }

  if (results[3].status === 'fulfilled') {
    const data = results[3].value
    hotBooks.value = data || []
  }

  try {
    const apiFn = isReader.value ? getMyBorrows : getBorrowPage
    const res = await apiFn({ page: 1, size: 100 })
    if (aborted) return
    const records: BorrowRecord[] = res?.records || []
    recentBorrows.value = records.slice(0, 6).map((r: BorrowRecord) => ({
      id: r.id,
      userId: r.userId,
      userName: r.userName || '-',
      bookId: r.bookId,
      bookTitle: r.bookTitle || '-',
      borrowDate: r.borrowDate?.split('T')[0] || '-',
      dueDate: r.dueDate || '',
      returnDate: r.returnDate || '',
      renewCount: r.renewCount || 0,
      status: r.status === 'BORROWING' ? 'BORROWING' : r.status === 'RETURNED' ? 'RETURNED' : 'OVERDUE',
      fineAmount: r.fineAmount || 0,
      remark: r.remark || '',
    }))
  } catch {}
})

onUnmounted(() => {
  aborted = true
})

function goToBook(title: string) { router.push({ path: '/book-catalog', query: { keyword: title } }) }
</script>

<style lang="scss" scoped>
.dashboard { max-width: 1400px; }

.welcome-bar {
  margin-bottom: 24px;
  padding: 24px 28px;
  border-radius: 18px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  box-shadow: 0 8px 24px rgba(44,62,80,0.15);

  .welcome-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .welcome-icon {
      flex-shrink: 0;
      animation: iconFloat 3s ease-in-out infinite;
    }

    h2 { font-size: 20px; font-weight: 700; color: #fff; margin-bottom: 4px; letter-spacing: 0.4px; }
    p { font-size: 13.5px; color: rgba(255,255,255,0.6); }
  }

  @keyframes iconFloat {
    0%, 100% { transform: translateY(0); }
    50% { transform: translateY(-4px); }
  }

  .welcome-actions {
    .action-btn {
      padding: 10px 22px;
      border-radius: 12px;
      font-size: 14px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
      display: inline-flex;
      align-items: center;
      gap: 8px;
      background: #fff;
      color: #2c3e50;
      border: none;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 20px rgba(0,0,0,0.15);
      }
    }
  }
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
  margin-bottom: 24px;

  .stat-card {
    background: #fff;
    border-radius: 16px;
    padding: 22px 24px;
    transition: all 0.28s cubic-bezier(0.4, 0, 0.2, 1);
    border: 1px solid #e0e6ed;
    animation-delay: var(--delay);
    box-shadow: 0 2px 8px rgba(0,0,0,0.04);

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 12px 32px rgba(0,0,0,0.08);
    }

    .stat-top {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 14px;

      .stat-label { font-size: 13px; color: #7f8c8d; font-weight: 500; }

      .stat-icon-wrap {
        width: 42px; height: 42px;
        border-radius: 12px;
        background: #ecf0f1;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #7f8c8d;
        transition: all 0.25s ease;

        &.primary { background: #ecf0f1; color: #2c3e50; }
        &.success { background: #d4edda; color: #27ae60; }
        &.warning { background: #fff3cd; color: #f39c12; }
        &.danger { background: #f8d7da; color: #e74c3c; }

        &.hovered {
          transform: scale(1.08) rotate(-3deg);
        }
      }
    }

    .stat-value {
      font-size: 30px;
      font-weight: 800;
      color: #2c3e50;
      line-height: 1.1;
      margin-bottom: 6px;
    }

    .stat-trend {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 12px;
      font-weight: 500;

      span.up { color: #27ae60; }
      span.down { color: #e74c3c; }
      & > span:last-child { color: #95a5a6; font-weight: 400; }
    }
  }
}

.chart-row {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 18px;
  margin-bottom: 18px;
}

.bottom-row {
  display: grid;
  grid-template-columns: 1fr 1.3fr;
  gap: 18px;
}

.panel-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  border: 1px solid #e0e6ed;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);

  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: baseline;
    margin-bottom: 20px;

    h3 { font-size: 16px; font-weight: 700; color: #2c3e50; }
    span { font-size: 12px; color: #95a5a6; }
  }
}

.bar-chart {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  height: 220px;
  padding-top: 8px;

  .bar-col {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    height: 100%;

    .bar-val { font-size: 11px; color: #95a5a6; margin-bottom: 6px; font-weight: 600; }

    .bar-track {
      flex: 1;
      width: 100%;
      display: flex;
      align-items: flex-end;
      justify-content: center;

      .bar-fill {
        width: 34px;
        border-radius: 6px 6px 3px 3px;
        background: linear-gradient(180deg, #2c3e50 0%, #34495e 100%);
        transition: height 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
        min-height: 6px;
      }
    }

    .bar-day { margin-top: 10px; font-size: 12px; color: #95a5a6; font-weight: 500; }
  }
}

.cat-list {
  .cat-row {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 0;
    border-bottom: 1px solid #f5f7fa;

    &:last-child { border-bottom: none; }

    .cat-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
    .cat-name { width: 72px; font-size: 13px; color: #34495e; flex-shrink: 0; font-weight: 500; }

    .cat-track {
      flex: 1; height: 6px;
      background: #f5f7fa; border-radius: 3px; overflow: hidden;

      .cat-fill { height: 100%; border-radius: 3px; transition: width 0.5s cubic-bezier(0.34, 1.56, 0.64, 1); }
    }

    .cat-pct { width: 40px; text-align: right; font-size: 12px; color: #7f8c8d; flex-shrink: 0; font-weight: 600; }
  }
}

.hot-list {
  .hot-item {
    display: flex;
    align-items: center;
    padding: 11px 0;
    border-bottom: 1px solid #f5f7fa;
    gap: 12px;
    cursor: default;

    &:last-child { border-bottom: none; }

    .rank-num {
      width: 26px; height: 26px;
      border-radius: 8px;
      background: #ecf0f1;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 12px;
      font-weight: 750;
      color: #7f8c8d;
      flex-shrink: 0;
      transition: all 0.22s;

      &.top[data-rk="0"] { background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%); color: #fff; }
      &.top[data-rk="1"] { background: linear-gradient(135deg, #f39c12 0%, #e67e22 100%); color: #fff; }
      &.top[data-rk="2"] { background: linear-gradient(135deg, #27ae60 0%, #16a085 100%); color: #fff; }
    }

    .hot-info {
      flex: 1; min-width: 0;

      .hot-title {
        font-size: 13.5px; color: #34495e;
        overflow: hidden; text-overflow: ellipsis;
        white-space: nowrap; cursor: pointer;
        transition: color 0.2s; display: block; margin-bottom: 4px;
        &:hover { color: #2c3e50; }
      }

      .hot-bar {
        height: 4px; background: #f5f7fa;
        border-radius: 2px; overflow: hidden;

        .hot-fill { height: 100%; border-radius: 2px; background: linear-gradient(90deg, #2c3e50 0%, #34495e 100%); transition: width 0.4s ease; }
      }
    }

    .hot-count { font-size: 12px; color: #95a5a6; margin-left: 8px; font-weight: 500; flex-shrink: 0; }
  }
}

@media (max-width: 1200px) {
  .stat-grid { grid-template-columns: repeat(2, 1fr); }
  .chart-row, .bottom-row { grid-template-columns: 1fr; }
}
@media (max-width: 768px) {
  .stat-grid { grid-template-columns: 1fr; }
  .welcome-bar { flex-direction: column; gap: 16px; text-align: center; padding: 20px; }
  .welcome-left { flex-direction: column; }
}
</style>
