<template>
  <div class="borrow-trend-chart" v-loading="loading">
    <v-chart :option="chartOption" :autoresize="true" style="height: 350px;" />
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent
} from 'echarts/components'
import type { ComposeOption } from 'echarts/core'
import type { BarSeriesOption } from 'echarts/charts'
import type {
  TitleComponentOption,
  TooltipComponentOption,
  GridComponentOption,
  LegendComponentOption
} from 'echarts/components'

use([
  CanvasRenderer,
  BarChart,
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent
])

type ECOption = ComposeOption<
  | BarSeriesOption
  | TitleComponentOption
  | TooltipComponentOption
  | GridComponentOption
  | LegendComponentOption
>

const props = defineProps<{
  data?: Array<{ label: string; value: number }>
  loading?: boolean
}>()

const defaultData = ref<Array<{ label: string; value: number }>>([])

function createGradient(startColor: string, endColor: string) {
  return {
    type: 'linear',
    x: 0, y: 0, x2: 0, y2: 1,
    colorStops: [
      { offset: 0, color: startColor },
      { offset: 1, color: endColor }
    ]
  }
}

const chartOption = ref<ECOption>({
  tooltip: {
    trigger: 'axis',
    axisPointer: { type: 'shadow' },
    formatter: (params: any) => {
      const item = params[0]
      if (!item) return ''
      return `${item.axisValue}<br/>借阅次数: <b>${item.value}</b>`
    }
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    top: '10%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    data: [],
    axisLabel: { color: '#666', fontSize: 11 },
    axisLine: { lineStyle: { color: '#ddd' } }
  },
  yAxis: {
    type: 'value',
    name: '借阅次数',
    nameTextStyle: { color: '#999', fontSize: 11 },
    axisLabel: { color: '#666', fontSize: 11 },
    splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' as const } }
  },
  series: [{
    type: 'bar',
    data: [],
    barWidth: '50%',
    itemStyle: {
      borderRadius: [6, 6, 0, 0],
      color: createGradient('#1890ff', '#36cfc9')
    },
    emphasis: {
      itemStyle: {
        color: createGradient('#40a9ff', '#5cdbd3')
      }
    },
    animationDelay: (idx: number) => idx * 100
  }],
  animationEasing: 'elasticOut' as const,
  animationDelayUpdate: (idx: number) => idx * 5
})

watch(() => props.data, (newData) => {
  if (newData && newData.length) {
    updateChart(newData)
  }
}, { immediate: true, deep: true })

onMounted(() => {
  if (!props.data || !props.data.length) {
    updateChart(defaultData.value)
  }
})

function updateChart(data: Array<{ label: string; value: number }>) {
  chartOption.value = {
    ...chartOption.value,
    xAxis: { ...chartOption.value.xAxis as any, data: data.map(d => d.label) },
    series: [{ ...(chartOption.value.series?.[0] as any), data: data.map(d => d.value) }]
  }
}

defineExpose({ updateChart })
</script>

<style lang="scss" scoped>
.borrow-trend-chart {
  width: 100%;
}
</style>
