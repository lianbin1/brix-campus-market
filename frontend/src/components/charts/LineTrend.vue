<template>
  <div ref="chartRef" class="line-trend"></div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  // [{ date: '2026-10-23', count: 3 }]
  data: { type: Array, default: () => [] },
  height: { type: Number, default: 280 }
})

const chartRef = ref(null)
let chart = null
let observer = null

/** 渲染折线图（空数据时叠加「暂无数据」占位） */
function render() {
  if (!chartRef.value) return
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }
  const points = props.data || []
  const option = {
    grid: { left: 40, right: 20, top: 30, bottom: 30 },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: points.map((item) => item.date?.slice(5) || ''),
      axisLine: { lineStyle: { color: '#d9dde3' } },
      axisLabel: { color: '#646a73' }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { color: '#646a73' },
      splitLine: { lineStyle: { color: '#eef0f3' } }
    },
    series: [
      {
        name: '订单数',
        type: 'line',
        smooth: true,
        data: points.map((item) => Number(item.count || 0)),
        itemStyle: { color: '#0FA968' },
        lineStyle: { color: '#0FA968', width: 2.5 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(15,169,104,0.25)' },
            { offset: 1, color: 'rgba(15,169,104,0)' }
          ])
        }
      }
    ],
    graphic: points.length
      ? []
      : [
          {
            type: 'text',
            left: 'center',
            top: 'middle',
            style: { text: '暂无数据', fill: '#8f959e', fontSize: 14 }
          }
        ]
  }
  chart.setOption(option, true)
}

onMounted(() => {
  render()
  observer = new ResizeObserver(() => chart && chart.resize())
  observer.observe(chartRef.value)
})

watch(
  () => props.data,
  () => render(),
  { deep: true }
)

onBeforeUnmount(() => {
  if (observer) observer.disconnect()
  if (chart) {
    chart.dispose()
    chart = null
  }
})
</script>

<style scoped>
.line-trend {
  width: 100%;
  height: v-bind("props.height + 'px'");
}
</style>
