<template>
  <div class="finance-report-container">
    <el-card shadow="never" class="search-wrap">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="开始日期">
          <el-date-picker v-model="queryParams.startDate" type="date" placeholder="选择开始日期" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="queryParams.endDate" type="date" placeholder="选择结束日期" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          <el-button type="success" icon="Download" @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never" class="summary-card">
          <template #header>
            <span class="card-title">资金概况</span>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="总收入">¥{{ formatNumber(summary.totalIncome) }}</el-descriptions-item>
            <el-descriptions-item label="总支出">¥{{ formatNumber(summary.totalExpense) }}</el-descriptions-item>
            <el-descriptions-item label="净利润">
              <span :class="summary.netProfit >= 0 ? 'positive' : 'negative'">
                ¥{{ formatNumber(summary.netProfit) }}
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="资金余额">¥{{ formatNumber(summary.balance) }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="summary-card">
          <template #header>
            <span class="card-title">账龄分析</span>
          </template>
          <div class="aging-analysis">
            <div class="aging-item">
              <div class="aging-label">0-30天应收</div>
              <div class="aging-value good">¥{{ formatNumber(summary.receivable0to30) }}</div>
            </div>
            <div class="aging-item">
              <div class="aging-label">30-60天应收</div>
              <div class="aging-value normal">¥{{ formatNumber(summary.receivable30to60) }}</div>
            </div>
            <div class="aging-item">
              <div class="aging-label">60-90天应收</div>
              <div class="aging-value warning">¥{{ formatNumber(summary.receivable60to90) }}</div>
            </div>
            <div class="aging-item">
              <div class="aging-label">90天以上应收</div>
              <div class="aging-value danger">¥{{ formatNumber(summary.receivableOver90) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="table-wrap" style="margin-top: 16px;">
      <template #header>
        <span class="card-title">资金流水明细</span>
      </template>
      <el-table v-loading="loading" :data="reportList" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="日期" prop="date" width="120" />
        <el-table-column label="收入" prop="income" width="150" align="right">
          <template #default="{ row }">
            <span class="amount-text positive">+¥{{ formatNumber(row.income) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="支出" prop="expense" width="150" align="right">
          <template #default="{ row }">
            <span class="amount-text negative">-¥{{ formatNumber(row.expense) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="余额" prop="balance" width="150" align="right">
          <template #default="{ row }">
            <span class="amount-text">¥{{ formatNumber(row.balance) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" min-width="200" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { getFinanceReport } from '@/api/report'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const reportList = ref<any[]>([])
const summary = reactive({
  totalIncome: 0,
  totalExpense: 0,
  netProfit: 0,
  balance: 0,
  receivable0to30: 0,
  receivable30to60: 0,
  receivable60to90: 0,
  receivableOver90: 0
})

const queryParams = reactive({
  startDate: null,
  endDate: null
})

const formatNumber = (value: any) => {
  if (!value) return '0.00'
  return Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const loadReport = async () => {
  loading.value = true
  try {
    const res = await getFinanceReport(queryParams)
    reportList.value = res.data || []
    
    // 计算汇总数据
    if (reportList.value.length > 0) {
      summary.totalIncome = reportList.value.reduce((sum, item) => sum + (item.income || 0), 0)
      summary.totalExpense = reportList.value.reduce((sum, item) => sum + (item.expense || 0), 0)
      summary.netProfit = summary.totalIncome - summary.totalExpense
      summary.balance = reportList.value[reportList.value.length - 1]?.balance || 0
    }
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
  loadReport()
}

const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

loadReport()
</script>

<style scoped>
.finance-report-container {
  padding: 20px;
}

.search-wrap {
  margin-bottom: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
}

.summary-card {
  height: 240px;
}

.aging-analysis {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  padding: 16px 0;
}

.aging-item {
  text-align: center;
  padding: 16px;
  border-radius: 8px;
  background: #f5f7fa;
}

.aging-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.aging-value {
  font-size: 20px;
  font-weight: bold;
  font-family: 'Courier New', monospace;
}

.aging-value.good {
  color: #67c23a;
}

.aging-value.normal {
  color: #409eff;
}

.aging-value.warning {
  color: #e6a23c;
}

.aging-value.danger {
  color: #f56c6c;
}

.amount-text {
  font-family: 'Courier New', monospace;
  font-weight: 500;
}

.amount-text.positive {
  color: #67c23a;
}

.amount-text.negative {
  color: #f56c6c;
}

.positive {
  color: #67c23a;
}

.negative {
  color: #f56c6c;
}
</style>
