import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import { getDashboardStats, getBorrowTrend, getCategoryDistribution, getHotBooks } from '@/api/dashboard'
import { getBorrowPage } from '@/api/borrow'
import { Reading } from '@element-plus/icons-vue'
import BorrowTrendChart from '@/components/charts/BorrowTrendChart.vue'
import CategoryPieChart from '@/components/charts/CategoryPieChart.vue'

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

const echartsTrendData = ref<Array<{ label: string; value: number }>>([])
const categoryStats = ref<Array<{ name: string; count: number; percentage: number; color: string }>>([])
const hotBooks = ref<Array<{ title: string; borrowCount: number }>>([])
const recentBorrows = ref<Array<{ userName: string; bookTitle: string; borrowDate: string; status: string }>>([])

let refreshTimer: ReturnType<typeof setInterval> | null = null

onMounted(async () => {
  await Promise.allSettled([
    fetchDashboardData(),
    fetchChartData(),
    fetchHotBooks(),
    fetchRecentBorrows(),
  ])
  
  refreshTimer = setInterval(async () => {
    await Promise.allSettled([fetchDashboardData(), fetchRecentBorrows()])
  }, 60000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
})

async function fetchDashboardData() {
  try {
    const stats = await getDashboardStats()
    if (stats) {
      statCards.value[0].value = String(stats.totalBooks ?? '-')
      statCards.value[1].value = String(stats.totalUsers ?? '-')
      statCards.value[2].value = String(stats.todayBorrows ?? '-')
      statCards.value[3].value = String(stats.overdueCount ?? '-')
    }
  } catch (error) {
    console.error('获取仪表盘数据失败:', error)
  }
}

async function fetchChartData() {
  try {
    const [trendRes, categoryRes] = await Promise.allSettled([
      getBorrowTrend(),
      getCategoryDistribution(),
    ])
    
    if (trendRes.status === 'fulfilled' && trendRes.value) {
      echartsTrendData.value = trendRes.value
    }
    
    if (categoryRes.status === 'fulfilled' && categoryRes.value) {
      categoryStats.value = categoryRes.value
    }
  } catch (error) {
    console.error('获取图表数据失败:', error)
  }
}

async function fetchHotBooks() {
  try {
    const books = await getHotBooks()
    if (books && Array.isArray(books)) {
      hotBooks.value = books.slice(0, 10)
    }
  } catch (error) {
    console.error('获取热门图书失败:', error)
  }
}

async function fetchRecentBorrows() {
  try {
    const res = await getBorrowPage({ page: 1, size: 5 })
    if (res?.records) {
      recentBorrows.value = res.records.map((record: Record<string, unknown>) => ({
        userName: (record.userName as string) || '-',
        bookTitle: (record.bookTitle as string) || '-',
        borrowDate: (record.borrowDate as string)?.split('T')[0] || '-',
        status: (record.status as string) || 'BORROWING',
      }))
    }
  } catch (error) {
    console.error('获取最近借阅失败:', error)
  }
}

function goToBook(title: string) {
  if (title) {
    router.push({ path: '/book-catalog', query: { keyword: title } })
  }
}
