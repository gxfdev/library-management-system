<template>
  <div class="category-pie-chart" v-loading="loading">
    <v-chart :option="chartOption" :autoresize="true" style="height: 300px;" />
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent
} from 'echarts/components'
import type { ComposeOption } from 'echarts/core'
import type { PieSeriesOption } from 'echarts/charts'
import type {
  TitleComponentOption,
  TooltipComponentOption,
  LegendComponentOption
} from 'echarts/components'

use([
  CanvasRenderer,
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent
])

type ECOption = ComposeOption<
  | PieSeriesOption
  | TitleComponentOption
  | TooltipComponentOption
  | LegendComponentOption
>

const props = defineProps<{
  data?: Array<{ name: string; count: number; percentage: number; color: string }>
  loading?: boolean
}>()

const COLORS = ['#1890ff', '#2fc25b', '#facc14', '#f04864', '#8543e0', '#13c2c2', '#fa8c16', '#eb2f96']

const chartOption = ref<ECOption>({
  tooltip: {
    trigger: 'item',
    formatter: '{b}: {c} ({d}%)'
  },
  legend: {
    orient: 'vertical',
    right: '5%',
    top: 'center',
    textStyle: { fontSize: 11, color: '#666' },
    itemWidth: 12,
    itemHeight: 12,
    itemGap: 10
  },
  series: [{
    type: 'pie',
    radius: ['40%', '65%'],
    center: ['40%', '50%'],
    avoidLabelOverlap: true,
    itemStyle: {
      borderRadius: 6,
      borderColor: '#fff',
      borderWidth: 2
    },
    label: { show: false },
    emphasis: {
      label: { show: true, fontSize: 14, fontWeight: 'bold' }
    },
    labelLine: { show: false },
    data: [],
    animationType: 'scale' as const,
    animationEasing: 'elasticOut' as const
  }]
})

watch(() => props.data, (newData) => {
  if (newData && newData.length) {
    updateChart(newData)
  }
}, { immediate: true, deep: true })

onMounted(() => {
  if (!props.data || !props.data.length) {
    updateChart([])
  }
})

function updateChart(data: Array<{ name: string; count: number; percentage: number; color: string }>) {
  chartOption.value = {
    ...chartOption.value,
    series: [{
      ...(chartOption.value.series?.[0] as any),
      data: data.map((d, i) => ({
        name: d.name,
        value: d.count,
        itemStyle: { color: d.color || COLORS[i % COLORS.length] }
      }))
    }]
  }
}

defineExpose({ updateChart })
</script>

<style lang="scss" scoped>
.category-pie-chart {
  width: 100%;
}
</style>
