<template>
  <div class="order-finance-report">
    <el-card shadow="never">
      <template #header>
        <span class="card-title">未完订单资金图</span>
      </template>
      <div v-loading="loading" class="chart-wrapper">
        <div ref="chartRef" class="chart-container"></div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { getIncompleteOrdersFinance } from '@/api/report'
import { ElMessage } from 'element-plus'

interface OrderFinanceItem {
  orderNo: string
  salesContractAmount: number
  salesReceivedAmount: number
  purchaseContractAmount: number
  purchaseDepositPaid: number
}

const chartRef = ref<HTMLElement | null>(null)
const loading = ref(false)
let chartInstance: echarts.ECharts | null = null

async function loadData() {
  loading.value = true
  try {
    const res = await getIncompleteOrdersFinance()
    renderChart(res.data || [])
  } catch {
    ElMessage.error('获取未完订单资金数据失败')
  } finally {
    loading.value = false
  }
}

function renderChart(data: OrderFinanceItem[]) {
  if (!chartRef.value) return
  const chartHeight = Math.max(400, data.length * 80 + 120)
  chartRef.value.style.height = chartHeight + 'px'
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  } else {
    chartInstance.resize()
  }

  // 1 category per order, 2 stacked bar groups side by side
  const orderNos: string[] = []
  const salesReceived: number[] = []
  const salesRemaining: number[] = []
  const purchaseDeposit: number[] = []
  const purchaseRemaining: number[] = []

  // Reverse so first order is at top
  for (let i = data.length - 1; i >= 0; i--) {
    const d = data[i]!
    const sc = Number(d.salesContractAmount) || 0
    const sr = Number(d.salesReceivedAmount) || 0
    const pc = Number(d.purchaseContractAmount) || 0
    const pd = Number(d.purchaseDepositPaid) || 0

    orderNos.push(d.orderNo)
    salesReceived.push(sr)
    salesRemaining.push(Math.max(0, sc - sr))
    purchaseDeposit.push(pd)
    purchaseRemaining.push(Math.max(0, pc - pd))
  }

  const option: echarts.EChartsOption = {
    title: {
      text: '未完订单资金图',
      left: 'center',
      textStyle: { fontSize: 18, fontWeight: 'bold', color: '#333' }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'none' },
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: '#e0e0e0',
      borderWidth: 1,
      textStyle: { color: '#333' },
      formatter: (params: any) => {
        const arr = Array.isArray(params) ? params : [params]
        const nonZero = arr.filter((p: any) => p.value > 0)
        if (!nonZero.length) return ''
        let html = `<div style="font-weight:600;margin-bottom:6px">${arr[0].name}</div>`
        nonZero.forEach((p: any) => {
          html += `<div>${p.marker}<span style="margin-right:8px">${p.seriesName}</span><b>¥${Number(p.value).toLocaleString()}</b></div>`
        })
        return html
      }
    },
    legend: {
      bottom: 8,
      itemWidth: 14,
      itemHeight: 10,
      textStyle: { color: '#555', fontSize: 12 },
      data: ['销售合同已收定金', '销售合同余款', '采购已付定金', '采购合同余款']
    },
    grid: { top: 56, bottom: 56, left: 120, right: 80 },
    xAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f0f0f0' } },
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: {
        color: '#999',
        fontSize: 11,
        formatter: (val: number) => val === 0 ? '' : val >= 10000 ? (val / 10000).toFixed(0) + '万' : String(val)
      }
    },
    yAxis: {
      type: 'category',
      data: orderNos,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { fontSize: 13, fontWeight: 'bold', color: '#444' }
    },
    series: [
      {
        name: '销售合同已收定金',
        type: 'bar',
        stack: 'sales',
        barWidth: 20,
        barGap: '0%',
        barCategoryGap: '40%',
        data: salesReceived,
        itemStyle: { color: '#1976d2' },
        label: {
          show: true, position: 'inside', color: '#fff', fontSize: 11,
          formatter: (p: any) => p.value > 0 ? '¥' + Number(p.value).toLocaleString() : ''
        }
      },
      {
        name: '销售合同余款',
        type: 'bar',
        stack: 'sales',
        barWidth: 20,
        data: salesRemaining,
        itemStyle: { color: '#bbdefb', borderRadius: [0, 4, 4, 0] },
        label: {
          show: true, position: 'inside', color: '#1565c0', fontSize: 11,
          formatter: (p: any) => p.value > 0 ? '¥' + Number(p.value).toLocaleString() : ''
        }
      },
      {
        name: '采购已付定金',
        type: 'bar',
        stack: 'purchase',
        barWidth: 20,
        barGap: '0%',
        data: purchaseDeposit,
        itemStyle: { color: '#388e3c' },
        label: {
          show: true, position: 'inside', color: '#fff', fontSize: 11,
          formatter: (p: any) => p.value > 0 ? '¥' + Number(p.value).toLocaleString() : ''
        }
      },
      {
        name: '采购合同余款',
        type: 'bar',
        stack: 'purchase',
        barWidth: 20,
        data: purchaseRemaining,
        itemStyle: { color: '#c8e6c9', borderRadius: [0, 4, 4, 0] },
        label: {
          show: true, position: 'inside', color: '#2e7d32', fontSize: 11,
          formatter: (p: any) => p.value > 0 ? '¥' + Number(p.value).toLocaleString() : ''
        }
      }
    ]
  }

  chartInstance.setOption(option)
}

function handleResize() {
  chartInstance?.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<style scoped>
.order-finance-report {
  padding: 16px;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
}
.chart-wrapper {
  min-height: 600px;
}
.chart-container {
  width: 100%;
  height: 640px;
}
</style>
