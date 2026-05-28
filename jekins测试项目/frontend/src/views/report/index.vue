<template>
  <div class="report-page">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>高级报表生成器</span>
          <el-button type="primary" @click="generateReport" :loading="generating">
            生成报表
          </el-button>
        </div>
      </template>

      <el-form :model="queryForm" inline>
        <el-form-item label="报表类型">
          <el-select v-model="queryForm.reportType" placeholder="选择报表类型" style="width: 180px;">
            <el-option label="借阅统计报表" value="borrow" />
            <el-option label="图书库存报表" value="inventory" />
            <el-option label="读者活跃度报表" value="reader" />
            <el-option label="罚款统计报表" value="fine" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="queryForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="导出格式">
          <el-select v-model="queryForm.exportFormat" style="width: 120px;">
            <el-option label="PDF" value="pdf" />
            <el-option label="Excel" value="excel" />
          </el-select>
        </el-form-item>
      </el-form>

      <el-divider />

      <div v-if="reportData && reportData.length" class="report-content">
        <el-table :data="reportData" border stripe style="width: 100%">
          <el-table-column prop="metric" label="统计指标" width="200" />
          <el-table-column prop="value" label="数值" width="120" align="center" />
          <el-table-column prop="change" label="环比变化" width="120" align="center">
            <template #default="{ row }">
              <span :style="{ color: row.change >= 0 ? '#67c23a' : '#f56c6c' }">
                {{ row.change >= 0 ? '+' : '' }}{{ row.change }}%
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="说明" show-overflow-tooltip />
        </el-table>
      </div>
      <el-empty v-else description="请选择报表类型和时间范围后生成报表" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const generating = ref(false)
const reportData = ref<any[]>([])

const queryForm = reactive({
  reportType: 'borrow',
  dateRange: [] as string[],
  exportFormat: 'pdf',
})

const generateReport = async () => {
  generating.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 800))
    reportData.value = [
      { metric: '总借阅量', value: 1280, change: 12.5, description: '较上期增长12.5%' },
      { metric: '活跃读者数', value: 356, change: 8.3, description: '较上期增长8.3%' },
      { metric: '平均借阅时长', value: 14, change: -2.1, description: '较上期减少2.1天' },
      { metric: '逾期率', value: 3.2, change: -1.5, description: '较上期下降1.5%' },
      { metric: '图书流通率', value: 68.7, change: 5.4, description: '较上期提升5.4%' },
    ]
    ElMessage.success('报表生成成功')
  } finally {
    generating.value = false
  }
}
</script>

<style scoped>
.report-page {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.report-content {
  margin-top: 16px;
}
</style>
