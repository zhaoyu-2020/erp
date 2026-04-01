<template>
  <div class="finance-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">收款单</h2>
      </div>
      <div class="page-header-right">
        <el-button type="success" :icon="Download" @click="handleExport">导出</el-button>
        <el-button type="primary" :icon="Plus" @click="handleAdd">新建收款单</el-button>
      </div>
    </div>

    <!-- 状态标签页 -->
    <div class="status-tabs">
      <div
        v-for="tab in statusTabs"
        :key="tab.value"
        :class="['status-tab-item', { active: activeTab === tab.value }]"
        @click="handleTabChange(tab.value)"
      >
        {{ tab.label }}({{ tab.count }})
      </div>
    </div>

    <!-- 搜索过滤区域 -->
    <div class="filter-bar">
      <div class="filter-inputs">
        <el-input
          v-model="queryParams.serialNo"
          placeholder="流水号"
          clearable
          class="filter-input"
          :prefix-icon="Search"
          @clear="handleQuery"
          @keyup.enter="handleQuery"
        />
        <el-input
          v-model="queryParams.salesOrderNo"
          placeholder="销售订单号"
          clearable
          class="filter-input"
          :prefix-icon="Search"
          @clear="handleQuery"
          @keyup.enter="handleQuery"
        />
        <el-input
          v-model="queryParams.amountMin"
          placeholder="最小金额"
          clearable
          class="filter-input-sm"
          @clear="handleQuery"
          @keyup.enter="handleQuery"
        />
        <span class="filter-separator">~</span>
        <el-input
          v-model="queryParams.amountMax"
          placeholder="最大金额"
          clearable
          class="filter-input-sm"
          @clear="handleQuery"
          @keyup.enter="handleQuery"
        />
        <el-date-picker
          v-model="queryParams.receiptDateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="收款开始日期"
          end-placeholder="收款结束日期"
          value-format="YYYY-MM-DD"
          class="filter-date"
          @change="handleQuery"
        />
        <el-tooltip content="更多筛选" placement="top">
          <el-button :icon="Filter" class="filter-btn" @click="showAdvancedFilter = !showAdvancedFilter" />
        </el-tooltip>
      </div>
      <div class="filter-actions">
        <el-button :icon="Search" type="primary" @click="handleQuery">查询</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </div>
    </div>

    <!-- 高级筛选 -->
    <div v-show="showAdvancedFilter" class="advanced-filter">
      <el-input
        v-model="queryParams.receivingBank"
        placeholder="收款银行"
        clearable
        class="filter-input"
        :prefix-icon="Search"
        @clear="handleQuery"
        @keyup.enter="handleQuery"
      />
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="dataList"
        :header-cell-style="{ background: '#fafafa', color: '#333', fontWeight: 500 }"
        row-class-name="table-row"
        style="width: 100%"
      >
        <el-table-column type="selection" width="40" align="center" />
        <el-table-column label="流水号" prop="serialNo" min-width="160" sortable>
          <template #default="{ row }">
            <span class="link-text" @click="handleDetail(row)">{{ row.serialNo || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="收款金额" width="160" align="right" sortable>
          <template #default="{ row }">
            <span class="currency-label">{{ row.currency }}</span>
            <span class="amount-text">{{ formatAmount(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="汇率" prop="exchangeRate" width="100" align="right">
          <template #default="{ row }">
            {{ row.exchangeRate ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column label="认领明细" min-width="260">
          <template #default="{ row }">
            <template v-if="row.details && row.details.length > 0">
              <div v-for="(d, idx) in row.details" :key="idx" class="claim-detail-row">
                <span class="claim-order-no">{{ d.salesOrderNo }}</span>
                <el-tag :type="d.bindType === 1 ? 'warning' : 'primary'" size="small" class="claim-tag">
                  {{ d.bindType === 1 ? '定金' : d.bindType === 2 ? '尾款' : '-' }}
                </el-tag>
                <span class="claim-amount">{{ row.currency }} {{ formatAmount(d.boundAmount) }}</span>
              </div>
            </template>
            <span v-else class="empty-text">暂无认领</span>
          </template>
        </el-table-column>
        <el-table-column label="收款日期" prop="receiptDate" width="120" align="center" sortable />
        <el-table-column label="收款银行" prop="receivingBank" min-width="150" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" effect="light" round>{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="160" sortable />
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 汇总统计栏 -->
    <div class="summary-bar">
      <span class="summary-label">总计（{{ summaryCurrency }}）：</span>
      <span class="summary-item">收款金额 <b>{{ summaryTotal }}</b></span>
      <span class="summary-item">已认领金额 <b>{{ summaryBound }}</b></span>
      <span class="summary-item">未认领金额 <b>{{ summaryUnbound }}</b></span>
      <span class="summary-item">笔数 <b>{{ total }}</b></span>
    </div>

    <!-- 分页 -->
    <div class="pagination-bar">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[20, 50, 100]"
        layout="total, prev, pager, next"
        small
        @current-change="getList"
        @size-change="getList"
      />
      <el-select v-model="queryParams.pageSize" class="page-size-select" @change="handleQuery">
        <el-option :value="20" label="20 条/页" />
        <el-option :value="50" label="50 条/页" />
        <el-option :value="100" label="100 条/页" />
      </el-select>
    </div>

    <!-- 新建/编辑收款单对话框 -->
    <el-dialog v-model="formDialogVisible" :title="formDialogTitle" width="580px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="流水号" prop="serialNo">
          <el-input v-model="form.serialNo" placeholder="银行流水号" />
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input v-model="form.amount" placeholder="请输入金额" style="width:100%"
            :disabled="form.id && (form.status === 2 || form.status === 3)" />
        </el-form-item>
        <el-form-item label="币种" prop="currency">
          <el-select v-model="form.currency" style="width:100%">
            <el-option label="USD" value="USD" />
            <el-option label="CNY" value="CNY" />
          </el-select>
        </el-form-item>
        <el-form-item label="收款日期" prop="receiptDate">
          <el-date-picker v-model="form.receiptDate" type="date" value-format="YYYY-MM-DD"
            placeholder="选择收款日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="收款银行" prop="receivingBank">
          <el-input v-model="form.receivingBank" placeholder="请输入收款银行" />
        </el-form-item>
        <el-form-item label="汇率" prop="exchangeRate">
          <el-input v-model="form.exchangeRate" placeholder="请输入收款汇率（如 7.2500）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>

    <!-- 收款明细对话框 -->
    <el-dialog v-model="detailDialogVisible" title="收款明细"
      width="800px" @close="currentReceipt = null">
      <div v-if="currentReceipt" class="detail-info">
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="流水号" :span="3">{{ currentReceipt.serialNo }}</el-descriptions-item>
          <el-descriptions-item label="收款银行" :span="3">{{ currentReceipt.receivingBank }}</el-descriptions-item>
          <el-descriptions-item label="收款金额">
            <b>{{ currentReceipt.currency }}</b>&nbsp;{{ currentReceipt.amount }}
          </el-descriptions-item>
          <el-descriptions-item label="汇率">{{ currentReceipt.exchangeRate ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="收款日期">{{ currentReceipt.receiptDate }}</el-descriptions-item>
          
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(currentReceipt.status)">{{ currentReceipt.statusLabel }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="detail-toolbar" style="margin-top:16px;margin-bottom:8px">
        <span style="font-weight:600;font-size:14px">收款明细</span>
        <el-button type="primary" size="small" icon="Plus" style="margin-left:12px"
          @click="handleAddDetail">新增认领</el-button>
      </div>

      <el-table :data="currentReceipt?.details || []" border stripe size="small">
        <el-table-column type="index" label="序号" width="55" align="center" />
        <el-table-column label="销售订单号" prop="salesOrderNo" min-width="150" />
        <el-table-column label="绑定类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.bindType === 1 ? 'warning' : 'primary'" size="small">
              {{ row.bindType === 1 ? '定金' : row.bindType === 2 ? '尾款' : '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="绑定金额" prop="boundAmount" width="140" align="right">
          <template #default="{ row }">
            {{ currentReceipt?.currency }} {{ row.boundAmount }}
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="160" />
        <el-table-column label="操作" width="80" align="center">
          <template #default="{ row }">
            <el-button
              link type="danger"
              :disabled="currentReceipt?.status === 3 && !isAdmin"
              @click="handleRemoveDetail(row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top:10px;text-align:right;color:#666;font-size:13px">
        已绑定合计: <b>{{ currentReceipt?.currency }} {{ boundTotal }}</b>
        &nbsp;/&nbsp;
        收款总额: <b>{{ currentReceipt?.currency }} {{ currentReceipt?.amount }}</b>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" :loading="detailSubmitLoading"
          :disabled="currentReceipt?.status === 3"
          @click="handleSaveDetails">保存认领</el-button>
      </template>
    </el-dialog>

    <!-- 新增认领明细行对话框 -->
    <el-dialog v-model="addDetailVisible" title="新增认领" width="420px">
      <el-form ref="detailFormRef" :model="detailForm" :rules="detailRules" label-width="110px">
        <el-form-item label="销售订单号" prop="salesOrderNo">
          <el-input v-model="detailForm.salesOrderNo" placeholder="请输入销售订单号" />
        </el-form-item>
        <el-form-item label="绑定类型" prop="bindType">
          <el-select v-model="detailForm.bindType" placeholder="请选择绑定类型" style="width:100%">
            <el-option label="定金" :value="1" />
            <el-option label="尾款" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="绑定金额" prop="boundAmount">
          <el-input v-model="detailForm.boundAmount"
            :placeholder="`最多可认领 ${remainingAmount}`" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDetailVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAddDetail">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Download, Refresh, Filter } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import { exportToCsv } from '@/utils/export'
import {
  getFinanceReceiptPage,
  getFinanceReceiptById,
  saveFinanceReceipt,
  updateFinanceReceipt,
  deleteFinanceReceipt
} from '@/api/advancedModules'
import { getUserListWithRoles } from '@/api/system'

const loading = ref(false)
const submitLoading = ref(false)
const detailSubmitLoading = ref(false)
const dataList = ref<any[]>([])
const total = ref(0)
const isAdmin = ref(false)
const showAdvancedFilter = ref(false)
const activeTab = ref<number | undefined>(undefined)

// 状态计数
const statusCounts = reactive({ all: 0, new: 0, claiming: 0, done: 0 })

const statusTabs = computed(() => [
  { label: '全部', value: undefined, count: statusCounts.all },
  { label: '新建', value: 1, count: statusCounts.new },
  { label: '认领中', value: 2, count: statusCounts.claiming },
  { label: '完成', value: 3, count: statusCounts.done }
])

// 汇总统计
const summaryCurrency = ref('USD')
const summaryTotal = computed(() => {
  const sum = dataList.value.reduce((acc: number, r: any) => acc + (parseFloat(String(r.amount)) || 0), 0)
  return sum.toFixed(2)
})
const summaryBound = computed(() => {
  let sum = 0
  for (const r of dataList.value) {
    if (r.details) {
      for (const d of r.details) {
        sum += parseFloat(String(d.boundAmount)) || 0
      }
    }
  }
  return sum.toFixed(2)
})
const summaryUnbound = computed(() => {
  return (parseFloat(summaryTotal.value) - parseFloat(summaryBound.value)).toFixed(2)
})

const formatAmount = (val: any) => {
  const n = parseFloat(String(val))
  if (isNaN(n)) return '-'
  return n.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const checkAdmin = async () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const res = await getUserListWithRoles()
    const allUsers = res.data || []
    const currentUser = allUsers.find((u: any) => u.id === userInfo.userId)
    isAdmin.value = !!(currentUser && Array.isArray(currentUser.roleCodes) && currentUser.roleCodes.includes('admin'))
  } catch {
    isAdmin.value = false
  }
}

const queryParams = reactive({
  pageNum: 1,
  pageSize: 20,
  serialNo: '',
  status: undefined as number | undefined,
  salesOrderNo: '',
  amountMin: undefined as number | undefined,
  amountMax: undefined as number | undefined,
  receiptDateRange: undefined as string[] | undefined,
  receivingBank: ''
})

// ---- 列表 ----
const getList = async () => {
  loading.value = true
  try {
    const res = await getFinanceReceiptPage(queryParams)
    dataList.value = res.data.list
    total.value = res.data.total
    // 根据返回数据设置汇总币种
    if (dataList.value.length > 0) {
      summaryCurrency.value = dataList.value[0].currency || 'USD'
    }
  } finally {
    loading.value = false
  }
}

const getStatusCounts = async () => {
  try {
    const baseParams = { ...queryParams, pageNum: 1, pageSize: 1 }
    // Fetch all counts in parallel
    const [allRes, newRes, claimRes, doneRes] = await Promise.all([
      getFinanceReceiptPage({ ...baseParams, status: undefined }),
      getFinanceReceiptPage({ ...baseParams, status: 1 }),
      getFinanceReceiptPage({ ...baseParams, status: 2 }),
      getFinanceReceiptPage({ ...baseParams, status: 3 })
    ])
    statusCounts.all = allRes.data.total || 0
    statusCounts.new = newRes.data.total || 0
    statusCounts.claiming = claimRes.data.total || 0
    statusCounts.done = doneRes.data.total || 0
  } catch {
    // ignore
  }
}

const handleTabChange = (val: number | undefined) => {
  activeTab.value = val
  queryParams.status = val
  queryParams.pageNum = 1
  getList()
}

const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
  getStatusCounts()
}

const resetQuery = () => {
  queryParams.serialNo = ''
  queryParams.status = undefined
  queryParams.salesOrderNo = ''
  queryParams.amountMin = undefined
  queryParams.amountMax = undefined
  queryParams.receiptDateRange = undefined
  queryParams.receivingBank = ''
  activeTab.value = undefined
  handleQuery()
}

const statusTagType = (status: number) => {
  const map: Record<number, string> = { 1: 'info', 2: 'warning', 3: 'success' }
  return map[status] || ''
}

// ---- 新建/编辑收款单 ----
const formDialogVisible = ref(false)
const formDialogTitle = ref('新建收款单')
const formRef = ref<FormInstance>()
const form = reactive<any>({
  id: null,
  serialNo: '',
  amount: 0,
  currency: 'USD',
  receiptDate: '',
  receivingBank: '',
  exchangeRate: null,
  status: null
})

const rules = {
  amount: [{ required: true, message: '请输入金额', trigger: 'change' }],
  currency: [{ required: true, message: '请选择币种', trigger: 'change' }],
  receiptDate: [{ required: true, message: '请选择收款日期', trigger: 'change' }]
}

const resetForm = () => {
  Object.assign(form, { id: null, serialNo: '', amount: 0, currency: 'USD', receiptDate: '', receivingBank: '', exchangeRate: null, status: null })
  formRef.value?.clearValidate()
}

const handleAdd = () => {
  resetForm()
  formDialogTitle.value = '新建收款单'
  formDialogVisible.value = true
}

const handleEdit = (row: any) => {
  Object.assign(form, {
    id: row.id,
    serialNo: row.serialNo,
    amount: row.amount,
    currency: row.currency,
    receiptDate: row.receiptDate,
    receivingBank: row.receivingBank,
    exchangeRate: row.exchangeRate,
    status: row.status
  })
  formDialogTitle.value = '编辑收款单'
  formDialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (form.id) {
      await updateFinanceReceipt({ ...form })
      ElMessage.success('更新成功')
    } else {
      await saveFinanceReceipt({ ...form })
      ElMessage.success('创建成功')
    }
    formDialogVisible.value = false
    getList()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row: any) => {
  await ElMessageBox.confirm(`确认删除该收款单?`, '提示', { type: 'warning' })
  await deleteFinanceReceipt(row.id)
  ElMessage.success('删除成功')
  getList()
}

// ---- 收款明细 ----
const detailDialogVisible = ref(false)
const currentReceipt = ref<any>(null)

const boundTotal = computed(() => {
  if (!currentReceipt.value?.details) return '0.00'
  const sum = currentReceipt.value.details.reduce((acc: number, d: any) => acc + (parseFloat(String(d.boundAmount)) || 0), 0)
  return sum.toFixed(2)
})

const remainingAmount = computed(() => {
  const total = parseFloat(String(currentReceipt.value?.amount ?? 0)) || 0
  const bound = parseFloat(boundTotal.value) || 0
  return (total - bound).toFixed(2)
})

const handleDetail = async (row: any) => {
  const res = await getFinanceReceiptById(row.id)
  currentReceipt.value = res.data
  detailDialogVisible.value = true
}

const handleRemoveDetail = (row: any) => {
  if (currentReceipt.value?.status === 3 && !isAdmin.value) {
    ElMessage.warning('收款已完成，只有管理员可以删除明细')
    return
  }
  const details = currentReceipt.value.details
  const idx = details.indexOf(row)
  if (idx !== -1) details.splice(idx, 1)
}

// ---- 新增认领明细行 ----
const addDetailVisible = ref(false)
const detailFormRef = ref<FormInstance>()
const detailForm = reactive({ salesOrderNo: '', bindType: null as number | null, boundAmount: null as number | null, exchangeRate: null as number | null })
const detailRules = {
  salesOrderNo: [{ required: true, message: '请输入销售订单号', trigger: 'blur' }],
  bindType: [{ required: true, message: '请选择绑定类型', trigger: 'change' }],
  boundAmount: [
    { required: true, message: '请输入绑定金额', trigger: 'change' },
    {
      validator: (_rule: any, value: any, callback: any) => {
        const v = Number(value)
        if (isNaN(v) || v <= 0) {
          callback(new Error('绑定金额须大于0'))
          return
        }
        const remaining = parseFloat(remainingAmount.value)
        if (v > remaining) {
          callback(new Error(`超出收款金额，最多还可认领 ${remainingAmount.value}`))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

const handleAddDetail = () => {
  detailForm.salesOrderNo = ''
  detailForm.bindType = null
  detailForm.boundAmount = null
  detailForm.exchangeRate = null
  detailFormRef.value?.clearValidate()
  addDetailVisible.value = true
}

const confirmAddDetail = async () => {
  await detailFormRef.value?.validate()
  if (!currentReceipt.value.details) currentReceipt.value.details = []
  currentReceipt.value.details.push({
    salesOrderNo: detailForm.salesOrderNo,
    bindType: detailForm.bindType,
    boundAmount: detailForm.boundAmount
  })
  addDetailVisible.value = false
}

const handleSaveDetails = async () => {
  detailSubmitLoading.value = true
  try {
    const payload = {
      id: currentReceipt.value.id,
      serialNo: currentReceipt.value.serialNo,
      amount: currentReceipt.value.amount,
      currency: currentReceipt.value.currency,
      receiptDate: currentReceipt.value.receiptDate,
      receivingBank: currentReceipt.value.receivingBank,
      exchangeRate: currentReceipt.value.exchangeRate,
      details: (currentReceipt.value.details || []).map((d: any) => ({
        salesOrderNo: d.salesOrderNo,
        bindType: d.bindType,
        boundAmount: d.boundAmount,
        exchangeRate: d.exchangeRate ?? null,
        salesOrderId: d.salesOrderId || 0
      }))
    }
    await updateFinanceReceipt(payload)
    ElMessage.success('认领保存成功')
    // Refresh receipt data
    const res = await getFinanceReceiptById(currentReceipt.value.id)
    currentReceipt.value = res.data
    getList()
  } finally {
    detailSubmitLoading.value = false
  }
}

// ---- 导出 ----
const handleExport = async () => {
  const res = await getFinanceReceiptPage({ ...queryParams, pageNum: 1, pageSize: 10000 })
  const rows = res.data.list || []
  exportToCsv('财务收款单导出', rows, [
    { label: '流水号', key: 'serialNo' },
    { label: '币种', key: 'currency' },
    { label: '金额', key: 'amount' },
    { label: '收款日期', key: 'receiptDate' },
    { label: '收款银行', key: 'receivingBank' },
    { label: '状态', key: 'statusLabel' },
    { label: '创建时间', key: 'createTime' }
  ])
}

onMounted(() => {
  getList()
  getStatusCounts()
  checkAdmin()
})
</script>

<style scoped>
.finance-page {
  padding: 0;
  background: #fff;
  min-height: 100%;
  display: flex;
  flex-direction: column;
}

/* ---- 页面头部 ---- */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px 12px;
  border-bottom: 1px solid #f0f0f0;
}
.page-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}
.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}
.page-header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* ---- 状态标签页 ---- */
.status-tabs {
  display: flex;
  align-items: center;
  gap: 0;
  padding: 0 20px;
  border-bottom: 1px solid #f0f0f0;
  background: #fff;
}
.status-tab-item {
  padding: 12px 16px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
  white-space: nowrap;
}
.status-tab-item:hover {
  color: #4066e0;
}
.status-tab-item.active {
  color: #4066e0;
  font-weight: 500;
  border-bottom-color: #4066e0;
}

/* ---- 过滤器区域 ---- */
.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  gap: 12px;
  border-bottom: 1px solid #f5f5f5;
}
.filter-inputs {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.filter-input {
  width: 160px;
}
.filter-input-sm {
  width: 110px;
}
.filter-separator {
  color: #ccc;
  margin: 0 2px;
}
.filter-date {
  width: 260px;
}
.filter-btn {
  border: 1px solid #dcdfe6;
  color: #666;
}
.filter-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
.advanced-filter {
  padding: 8px 20px 12px;
  display: flex;
  gap: 8px;
  border-bottom: 1px solid #f5f5f5;
}

/* ---- 表格 ---- */
.table-container {
  flex: 1;
  padding: 0;
}
.table-container :deep(.el-table) {
  border: none;
}
.table-container :deep(.el-table th.el-table__cell) {
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
  font-weight: 500;
  color: #333;
  font-size: 13px;
  padding: 10px 0;
}
.table-container :deep(.el-table td.el-table__cell) {
  border-bottom: 1px solid #f5f5f5;
  padding: 10px 0;
  font-size: 13px;
}
.table-container :deep(.el-table--border .el-table__inner-wrapper::after),
.table-container :deep(.el-table--border::before),
.table-container :deep(.el-table--border::after) {
  display: none;
}
.table-container :deep(.el-table__border-left-patch) {
  display: none;
}
.table-container :deep(.el-table .table-row:hover > td) {
  background: #f8f9ff;
}

.link-text {
  color: #4066e0;
  cursor: pointer;
  font-weight: 500;
}
.link-text:hover {
  text-decoration: underline;
}
.currency-label {
  color: #999;
  font-size: 12px;
  margin-right: 2px;
}
.amount-text {
  font-weight: 500;
  color: #333;
}
.claim-detail-row {
  font-size: 13px;
  color: #555;
  line-height: 2;
  display: flex;
  align-items: center;
  gap: 4px;
}
.claim-order-no {
  color: #4066e0;
  font-weight: 500;
}
.claim-tag {
  margin: 0 2px;
}
.claim-amount {
  color: #666;
}
.empty-text {
  color: #ccc;
  font-size: 13px;
}

/* ---- 汇总栏 ---- */
.summary-bar {
  display: flex;
  align-items: center;
  padding: 10px 20px;
  background: #f8f9ff;
  border-top: 1px solid #f0f0f0;
  font-size: 13px;
  color: #4066e0;
  gap: 8px;
  flex-wrap: wrap;
}
.summary-label {
  font-weight: 500;
}
.summary-item {
  margin-right: 16px;
}
.summary-item b {
  color: #333;
  font-weight: 600;
}

/* ---- 分页 ---- */
.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 10px 20px;
  gap: 12px;
  border-top: 1px solid #f0f0f0;
}
.page-size-select {
  width: 110px;
}
</style>
