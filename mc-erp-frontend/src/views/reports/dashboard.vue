<template>
  <div class="dashboard-container">
    <!-- 核心指标卡片 -->
    <el-row :gutter="16" class="kpi-row">
      <el-col :span="6">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-content">
            <div class="kpi-icon sales">
              <el-icon :size="40"><TrendCharts /></el-icon>
            </div>
            <div class="kpi-info">
              <div class="kpi-label">本月销售额</div>
              <div class="kpi-value">¥{{ formatNumber(dashboard.currentMonthSales) }}</div>
              <div class="kpi-trend positive">
                <el-icon><CaretTop /></el-icon>
                {{ dashboard.salesGrowthRate }}%
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-content">
            <div class="kpi-icon profit">
              <el-icon :size="40"><Money /></el-icon>
            </div>
            <div class="kpi-info">
              <div class="kpi-label">本月利润</div>
              <div class="kpi-value">¥{{ formatNumber(dashboard.currentMonthProfit) }}</div>
              <div class="kpi-trend" :class="dashboard.profitGrowthRate >= 0 ? 'positive' : 'negative'">
                <el-icon><CaretTop v-if="dashboard.profitGrowthRate >= 0" /><CaretBottom v-else /></el-icon>
                {{ Math.abs(dashboard.profitGrowthRate) }}%
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-content">
            <div class="kpi-icon receivable">
              <el-icon :size="40"><Wallet /></el-icon>
            </div>
            <div class="kpi-info">
              <div class="kpi-label">应收总额</div>
              <div class="kpi-value">¥{{ formatNumber(dashboard.totalReceivables) }}</div>
              <div class="kpi-note">逾期: ¥{{ formatNumber(dashboard.overdueReceivables) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-content">
            <div class="kpi-icon payable">
              <el-icon :size="40"><CreditCard /></el-icon>
            </div>
            <div class="kpi-info">
              <div class="kpi-label">应付总额</div>
              <div class="kpi-value">¥{{ formatNumber(dashboard.totalPayables) }}</div>
              <div class="kpi-note">毛利率: {{ dashboard.grossMargin }}%</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 订单统计和预警 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="12">
        <el-card shadow="never" class="stats-card">
          <template #header>
            <div class="card-header">
              <span class="title">订单统计</span>
            </div>
          </template>
          <div class="order-stats">
            <div class="stat-item">
              <div class="stat-label">订单总数</div>
              <div class="stat-value">{{ dashboard.totalOrders }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">待处理</div>
              <div class="stat-value warning">{{ dashboard.pendingOrders }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">已发货</div>
              <div class="stat-value success">{{ dashboard.shippedOrders }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">已完成</div>
              <div class="stat-value info">{{ dashboard.completedOrders }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="stats-card">
          <template #header>
            <div class="card-header">
              <span class="title">异常预警</span>
            </div>
          </template>
          <div class="alert-list">
            <el-alert 
              v-if="dashboard.overdueReceivableCount > 0"
              :title="`超期应收 ${dashboard.overdueReceivableCount} 笔`" 
              type="error" 
              :closable="false"
              show-icon
            />
            <el-alert 
              v-if="dashboard.highReceivableCount > 0"
              :title="`应收过高 ${dashboard.highReceivableCount} 笔`" 
              type="warning" 
              :closable="false"
              show-icon
            />
            <el-alert 
              v-if="dashboard.highPayableCount > 0"
              :title="`应付过高 ${dashboard.highPayableCount} 笔`" 
              type="warning" 
              :closable="false"
              show-icon
            />
            <el-empty v-if="!dashboard.overdueReceivableCount && !dashboard.highReceivableCount && !dashboard.highPayableCount" 
              description="暂无预警" :image-size="80" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 趋势图表 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span class="title">销售趋势（最近12个月）</span>
            </div>
          </template>
          <div class="chart-container" ref="salesChartRef" style="height: 350px;"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span class="title">客户排行 TOP10</span>
            </div>
          </template>
          <div class="ranking-list">
            <div v-for="item in dashboard.customerRanking" :key="item.id" class="ranking-item">
              <div class="rank-badge" :class="'rank-' + item.rank">{{ item.rank }}</div>
              <div class="rank-name">{{ item.name }}</div>
              <div class="rank-amount">¥{{ formatNumber(item.amount) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 年度对比 -->
    <el-row :gutter="16" class="summary-row">
      <el-col :span="24">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span class="title">本年累计</span>
            </div>
          </template>
          <el-descriptions :column="4" border>
            <el-descriptions-item label="本年销售额">¥{{ formatNumber(dashboard.currentYearSales) }}</el-descriptions-item>
            <el-descriptions-item label="本年利润">¥{{ formatNumber(dashboard.currentYearProfit) }}</el-descriptions-item>
            <el-descriptions-item label="毛利率">{{ dashboard.grossMargin }}%</el-descriptions-item>
            <el-descriptions-item label="订单总数">{{ dashboard.totalOrders }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { getDashboard } from '@/api/report'
import * as echarts from 'echarts'

const dashboard = ref<any>({
  currentMonthSales: 0,
  currentYearSales: 0,
  currentMonthProfit: 0,
  currentYearProfit: 0,
  grossMargin: 0,
  totalOrders: 0,
  pendingOrders: 0,
  shippedOrders: 0,
  completedOrders: 0,
  totalReceivables: 0,
  totalPayables: 0,
  overdueReceivables: 0,
  salesGrowthRate: 0,
  profitGrowthRate: 0,
  overdueReceivableCount: 0,
  highReceivableCount: 0,
  highPayableCount: 0,
  salesTrend: [],
  customerRanking: []
})

const salesChartRef = ref<HTMLElement>()

const formatNumber = (value: any) => {
  if (!value) return '0.00'
  return Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const loadDashboard = async () => {
  try {
    const res = await getDashboard()
    dashboard.value = res.data
    await nextTick()
    initSalesChart()
  } catch (error) {
    console.error('加载驾驶舱数据失败:', error)
  }
}

const initSalesChart = () => {
  if (!salesChartRef.value) return
  
  const chart = echarts.init(salesChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dashboard.value.salesTrend?.map((item: any) => item.period) || [],
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      name: '销售额（元）'
    },
    series: [
      {
        name: '销售额',
        type: 'bar',
        data: dashboard.value.salesTrend?.map((item: any) => item.value) || [],
        barWidth: '60%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        }
      }
    ]
  }
  chart.setOption(option)
}

onMounted(() => {
  loadDashboard()
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  background: #f0f2f5;
  min-height: calc(100vh - 60px);
}

.kpi-row, .stats-row, .chart-row, .summary-row {
  margin-bottom: 16px;
}

.kpi-card {
  border-radius: 8px;
  transition: transform 0.3s;
}

.kpi-card:hover {
  transform: translateY(-4px);
}

.kpi-content {
  display: flex;
  align-items: center;
}

.kpi-icon {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.kpi-icon.sales {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.kpi-icon.profit {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.kpi-icon.receivable {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.kpi-icon.payable {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: white;
}

.kpi-info {
  flex: 1;
}

.kpi-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.kpi-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.kpi-trend {
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.kpi-trend.positive {
  color: #67c23a;
}

.kpi-trend.negative {
  color: #f56c6c;
}

.kpi-note {
  font-size: 13px;
  color: #909399;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header .title {
  font-size: 16px;
  font-weight: bold;
}

.stats-card {
  height: 200px;
}

.order-stats {
  display: flex;
  justify-content: space-around;
  padding: 20px 0;
}

.stat-item {
  text-align: center;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 12px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-value.warning {
  color: #e6a23c;
}

.stat-value.success {
  color: #67c23a;
}

.stat-value.info {
  color: #409eff;
}

.alert-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ranking-list {
  max-height: 350px;
  overflow-y: auto;
}

.ranking-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #ebeef5;
}

.ranking-item:last-child {
  border-bottom: none;
}

.rank-badge {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  color: white;
  margin-right: 12px;
  flex-shrink: 0;
}

.rank-badge.rank-1 {
  background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
}

.rank-badge.rank-2 {
  background: linear-gradient(135deg, #c0c0c0 0%, #e8e8e8 100%);
}

.rank-badge.rank-3 {
  background: linear-gradient(135deg, #cd7f32 0%, #d4a679 100%);
}

.rank-badge.rank-4, .rank-badge.rank-5, .rank-badge.rank-6, 
.rank-badge.rank-7, .rank-badge.rank-8, .rank-badge.rank-9, .rank-badge.rank-10 {
  background: linear-gradient(135deg, #909399 0%, #b3b6bb 100%);
}

.rank-name {
  flex: 1;
  font-size: 14px;
  color: #303133;
}

.rank-amount {
  font-size: 15px;
  font-weight: bold;
  color: #409eff;
}
</style>
