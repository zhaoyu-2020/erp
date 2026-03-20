<template>
  <div class="order-status-report">
    <!-- 未完订单状态进度表 -->
    <el-card shadow="never" style="margin-bottom: 20px;">
      <template #header>
        <span class="card-title">未完成订单状态一览</span>
      </template>
      <div v-loading="tableLoading">
        <el-table
          :data="incompleteOrders"
          border
          stripe
          style="width: 100%"
          empty-text="暂无未完成订单"
        >
          <el-table-column prop="orderNo" label="订单号" width="130" fixed />
          <el-table-column
            v-for="s in allStatuses"
            :key="s.code"
            :label="s.label"
            align="center"
            min-width="90"
          >
            <template #default="{ row }">
              <div
                v-if="row.statusCode === s.code"
                class="status-cell"
                :style="{ backgroundColor: statusColor(s.code) }"
              >
                {{ s.label }}
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 横向柱状图 -->
    <el-card shadow="never">
      <template #header>
        <span class="card-title">未完订单状态图</span>
      </template>
      <div v-loading="chartLoading" class="chart-wrapper">
        <div ref="chartRef" class="chart-container"></div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { getOrderStatusReport, getIncompleteOrdersStatus } from '@/api/report'
import { ElMessage } from 'element-plus'

interface OrderStatusItem {
  statusCode: number
  statusLabel: string
  orderCount: number
  isCompleted: boolean
}

interface IncompleteOrder {
  orderNo: string
  statusCode: number
  statusLabel: string
}

// 所有状态定义（与后端枚举对应）
const allStatuses = [
  { code: 1, label: '新建' },
  { code: 2, label: '已收定金' },
  { code: 3, label: '已采购' },
  { code: 4, label: '待发运' },
  { code: 5, label: '已发运' },
  { code: 6, label: '已收款' },
  { code: 7, label: '已完成' },
]

// 每个状态对应的高亮颜色
const STATUS_COLORS: Record<number, string> = {
  1: '#fff59d',  // 新建 - 黄
  2: '#ffd54f',  // 已收定金 - 金黄
  3: '#a5d6a7',  // 已采购 - 绿
  4: '#80deea',  // 待发运 - 青
  5: '#f48fb1',  // 已发运 - 粉
  6: '#ffcc80',  // 已收款 - 橙
  7: '#90caf9',  // 已完成 - 蓝
}

function statusColor(code: number): string {
  return STATUS_COLORS[code] ?? '#e0e0e0'
}

const chartRef = ref<HTMLElement | null>(null)
const chartLoading = ref(false)
const tableLoading = ref(false)
const incompleteOrders = ref<IncompleteOrder[]>([])
let chartInstance: echarts.ECharts | null = null

async function loadChartData() {
  chartLoading.value = true
  try {
    const res = await getOrderStatusReport()
    const data: OrderStatusItem[] = res.data || []
    renderChart(data)
  } catch {
    ElMessage.error('获取订单状态数据失败')
  } finally {
    chartLoading.value = false
  }
}

async function loadTableData() {
  tableLoading.value = true
  try {
    const res = await getIncompleteOrdersStatus()
    incompleteOrders.value = res.data || []
  } catch {
    ElMessage.error('获取未完成订单列表失败')
  } finally {
    tableLoading.value = false
  }
}

function renderChart(data: OrderStatusItem[]) {
  if (!chartRef.value) return
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }

  const labels = data.map(item => item.statusLabel)
  const counts = data.map(item => item.orderCount)
  const colors = data.map(item => (item.isCompleted ? '#5470c6' : '#cccccc'))

  const option: echarts.EChartsOption = {
    title: {
      text: '未完订单状态图',
      left: 'center',
      textStyle: { fontSize: 18 }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params: any) => {
        const p = Array.isArray(params) ? params[0] : params
        return `${p.name}<br/>订单数量：${p.value}`
      }
    },
    grid: { top: 60, bottom: 60, left: 100, right: 40 },
    xAxis: { type: 'value', name: '订单数量', minInterval: 1 },
    yAxis: { type: 'category', data: labels, axisLabel: { fontSize: 13 } },
    series: [
      {
        type: 'bar',
        data: counts.map((val, idx) => ({
          value: val,
          itemStyle: { color: colors[idx] }
        })),
        label: { show: true, position: 'right', formatter: '{c}' },
        barMaxWidth: 40
      }
    ]
  }

  chartInstance.setOption(option)
}

function handleResize() {
  chartInstance?.resize()
}

onMounted(() => {
  loadChartData()
  loadTableData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<style scoped>
.order-status-report {
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
  height: 620px;
}
.status-cell {
  display: inline-block;
  width: 100%;
  padding: 4px 0;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  text-align: center;
  color: #333;
}
</style>

