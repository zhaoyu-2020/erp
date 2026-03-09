<template>
  <div class="sales-report-container">
    <el-card shadow="never" class="search-wrap">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="开始日期">
          <el-date-picker v-model="queryParams.startDate" type="date" placeholder="选择开始日期" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="queryParams.endDate" type="date" placeholder="选择结束日期" />
        </el-form-item>
        <el-form-item label="周期">
          <el-select v-model="queryParams.period" placeholder="选择周期" clearable>
            <el-option label="按日" value="day" />
            <el-option label="按月" value="month" />
            <el-option label="按季" value="quarter" />
            <el-option label="按年" value="year" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          <el-button type="success" icon="Download" @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-wrap">
      <el-table v-loading="loading" :data="reportList" border stripe :summary-method="getSummaries" show-summary>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="订单号" prop="orderNo" width="140" />
        <el-table-column label="客户" prop="customerName" width="160" />
        <el-table-column label="业务员" prop="salespersonName" width="100" />
        <el-table-column label="销售额" prop="salesAmount" width="140" align="right">
          <template #default="{ row }">
            <span class="amount-text">¥{{ formatNumber(row.salesAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="成本" prop="costAmount" width="140" align="right">
          <template #default="{ row }">
            <span class="amount-text">¥{{ formatNumber(row.costAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="利润" prop="profit" width="140" align="right">
          <template #default="{ row }">
            <span class="profit-text" :class="row.profit >= 0 ? 'positive' : 'negative'">
              ¥{{ formatNumber(row.profit) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="利润率" prop="profitMargin" width="100" align="right">
          <template #default="{ row }">
            <el-tag :type="getProfitMarginType(row.profitMargin)">
              {{ row.profitMargin }}%
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="已收款" prop="receivedAmount" width="140" align="right">
          <template #default="{ row }">
            <span class="amount-text">¥{{ formatNumber(row.receivedAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="应收款" prop="receivableAmount" width="140" align="right">
          <template #default="{ row }">
            <span class="amount-text">¥{{ formatNumber(row.receivableAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="160" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { getSalesReport } from '@/api/report'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const reportList = ref<any[]>([])

const queryParams = reactive({
  startDate: null,
  endDate: null,
  period: 'month',
  salespersonId: null,
  customerId: null
})

const formatNumber = (value: any) => {
  if (!value) return '0.00'
  return Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const getProfitMarginType = (margin: number) => {
  if (margin >= 30) return 'success'
  if (margin >= 15) return ''
  if (margin >= 0) return 'warning'
  return 'danger'
}

const loadReport = async () => {
  loading.value = true
  try {
    const res = await getSalesReport(queryParams)
    reportList.value = res.data || []
  } catch (error) {
    ElMessage.error('加载报表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  loadReport()
}

const resetQuery = () => {
  queryParams.startDate = null
  queryParams.endDate = null
  queryParams.period = 'month'
  loadReport()
}

const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

const getSummaries = (param: any) => {
  const { columns, data } = param
  const sums: any[] = []
  columns.forEach((column: any, index: number) => {
    if (index === 0) {
      sums[index] = '合计'
      return
    }
    if (['salesAmount', 'costAmount', 'profit', 'receivedAmount', 'receivableAmount'].includes(column.property)) {
      const values = data.map((item: any) => Number(item[column.property]) || 0)
      const sum = values.reduce((prev: number, curr: number) => prev + curr, 0)
      sums[index] = '¥' + formatNumber(sum)
    } else {
      sums[index] = ''
    }
  })
  return sums
}

loadReport()
</script>

<style scoped>
.sales-report-container {
  padding: 20px;
}

.search-wrap {
  margin-bottom: 16px;
}

.amount-text {
  font-family: 'Courier New', monospace;
  font-weight: 500;
}

.profit-text {
  font-family: 'Courier New', monospace;
  font-weight: bold;
}

.profit-text.positive {
  color: #67c23a;
}

.profit-text.negative {
  color: #f56c6c;
}
</style>
