<template>
  <div class="cashflow-container">
    <el-card shadow="never">
      <div ref="chartRef" class="chart-box"></div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { getHalfMonthCashFlow } from '@/api/report'

interface CashFlowItem {
  periodLabel: string
  periodStart: string
  periodEnd: string
  expectedCollection: number
  expectedPayment: number
  collectionOrderNos: string[]
  paymentOrderNos: string[]
}

const chartRef = ref<HTMLDivElement>()
let chartInstance: echarts.ECharts | null = null

onMounted(async () => {
  try {
    const res = await getHalfMonthCashFlow()
    renderChart(res.data || [])
    window.addEventListener('resize', handleResize)
  } catch (e) {
    console.error('加载资金收付预期数据失败', e)
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})

function handleResize() {
  chartInstance?.resize()
}

function renderChart(data: CashFlowItem[]) {
  if (!chartRef.value) return
  chartRef.value.style.height = '600px'

  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  } else {
    chartInstance.resize()
  }

  const xLabels = data.map(d => d.periodLabel)
  const collectionData = data.map(d => Number(d.expectedCollection) || 0)
  const paymentData = data.map(d => Number(d.expectedPayment) || 0)

  const option: echarts.EChartsOption = {
    title: {
      text: '资金收付预期半月图',
      left: 'center',
      textStyle: { fontSize: 18, fontWeight: 'bold', color: '#333' }
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.96)',
      borderColor: '#e0e0e0',
      borderWidth: 1,
      textStyle: { color: '#333', fontSize: 13 },
      formatter: (params: any) => {
        const arr = Array.isArray(params) ? params : [params]
        if (!arr.length) return ''
        const idx = arr[0].dataIndex
        const item = data[idx]
        if (!item) return ''

        let html = `<div style="font-weight:600;margin-bottom:8px;font-size:14px">${item.periodLabel}（${item.periodStart.slice(5)} ~ ${item.periodEnd.slice(5)}）</div>`

        // 预计收款
        const cAmt = Number(item.expectedCollection) || 0
        html += `<div style="margin-bottom:4px">${arr[0]?.marker || ''}<span style="font-weight:500">预计收款：</span><b style="color:#1976d2">$${cAmt.toLocaleString()}</b></div>`
        if (item.collectionOrderNos?.length) {
          html += `<div style="color:#666;font-size:12px;padding-left:16px;margin-bottom:6px">订单：${item.collectionOrderNos.join('、')}</div>`
        }

        // 预计付款
        const pAmt = Number(item.expectedPayment) || 0
        html += `<div style="margin-bottom:4px">${arr[1]?.marker || ''}<span style="font-weight:500">预计付款：</span><b style="color:#e53935">¥${pAmt.toLocaleString()}</b></div>`
        if (item.paymentOrderNos?.length) {
          html += `<div style="color:#666;font-size:12px;padding-left:16px">订单：${item.paymentOrderNos.join('、')}</div>`
        }

        return html
      }
    },
    legend: {
      bottom: 8,
      itemWidth: 20,
      itemHeight: 10,
      textStyle: { color: '#555', fontSize: 13 },
      data: ['预计收款', '预计付款']
    },
    grid: { top: 60, bottom: 60, left: 60, right: 40, containLabel: true },
    xAxis: {
      type: 'category',
      data: xLabels,
      boundaryGap: false,
      axisLine: { lineStyle: { color: '#ddd' } },
      axisTick: { show: false },
      axisLabel: {
        color: '#666',
        fontSize: 12,
        rotate: xLabels.length > 12 ? 30 : 0
      }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f0f0f0' } },
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: {
        color: '#999',
        fontSize: 11,
        formatter: (val: number) => {
          if (val === 0) return ''
          if (val >= 10000) return (val / 10000).toFixed(0) + '万'
          return String(val)
        }
      }
    },
    series: [
      {
        name: '预计收款',
        type: 'line',
        data: collectionData,
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: { width: 3, color: '#1976d2' },
        itemStyle: { color: '#1976d2' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(25,118,210,0.25)' },
            { offset: 1, color: 'rgba(25,118,210,0.02)' }
          ])
        }
      },
      {
        name: '预计付款',
        type: 'line',
        data: paymentData,
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: { width: 3, color: '#e53935' },
        itemStyle: { color: '#e53935' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(229,57,53,0.20)' },
            { offset: 1, color: 'rgba(229,57,53,0.02)' }
          ])
        }
      }
    ]
  }

  chartInstance.setOption(option)
}
</script>

<style scoped>
.cashflow-container {
  padding: 20px;
}
.chart-box {
  width: 100%;
  min-height: 600px;
}
</style>
