<template>
  <div class="finance-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">付款单</h2>
      </div>
      <div class="page-header-right">
        <el-button type="success" :icon="Download" @click="handleExport">导出</el-button>
        <el-button type="primary" :icon="Plus" @click="handleAdd">新建付款单</el-button>
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
          v-model="queryParams.paymentNo"
          placeholder="付款单号"
          clearable
          class="filter-input"
          :prefix-icon="Search"
          @clear="handleQuery"
          @keyup.enter="handleQuery"
        />
        <el-input
          v-model="queryParams.purchaseOrderNo"
          placeholder="采购订单号"
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
      </div>
      <div class="filter-actions">
        <el-button :icon="Search" type="primary" @click="handleQuery">查询</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </div>
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
        <el-table-column label="付款单号" prop="paymentNo" min-width="150" sortable>
          <template #default="{ row }">
            <span class="link-text" @click="handleDetail(row)">{{ row.paymentNo || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="采购订单号" prop="purchaseOrderNo" min-width="150">
          <template #default="{ row }">
            <span class="claim-order-no">{{ row.purchaseOrderNo || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="付款类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="bindTypeTagType(row.bindType)" size="small">
              {{ bindTypeLabel(row.bindType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="付款金额" width="160" align="right" sortable>
          <template #default="{ row }">
            <span class="currency-label">{{ row.currency || 'CNY' }}</span>
            <span class="amount-text">{{ formatAmount(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="付款日期" prop="paymentDate" width="120" align="center" sortable />
        <el-table-column label="付款银行" prop="payingBank" min-width="120" />
        <el-table-column label="收款方账户" prop="payeeAccountName" min-width="140" />
        <el-table-column label="收款方账号" prop="payeeAccountNo" min-width="150" />
        <el-table-column label="开户行" prop="payeeBankName" min-width="140" />
        <el-table-column label="行号/SWIFT" prop="payeeBankNo" min-width="130" />
        <el-table-column label="创建人" prop="creatorName" width="100" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" effect="light" round>{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核人" width="100" align="center">
          <template #default="{ row }">
            {{ row.auditByName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="审核时间" prop="auditTime" width="160" />
        <el-table-column label="创建时间" prop="createTime" width="160" sortable />
        <el-table-column label="操作" width="260" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
            <el-button link type="primary" @click="handleEdit(row)" :disabled="row.status === 2">编辑</el-button>
            <el-button v-if="isAdmin && row.status === 1" link type="success" @click="handleAudit(row, true)">审核通过</el-button>
            <el-button v-if="isAdmin && row.status === 1" link type="warning" @click="handleAudit(row, false)">驳回</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 汇总统计栏 -->
    <div class="summary-bar">
      <span class="summary-label">总计（CNY）：</span>
      <span class="summary-item">付款金额 <b>{{ summaryTotal }}</b></span>
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

    <!-- 新建/编辑付款单对话框 -->
    <el-dialog v-model="formDialogVisible" :title="formDialogTitle" width="580px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="付款单号" prop="paymentNo">
          <el-input v-model="form.paymentNo" placeholder="付款单号(可自动生成)" />
        </el-form-item>
        <el-form-item label="采购订单号" prop="purchaseOrderNo">
          <el-input v-model="form.purchaseOrderNo" placeholder="请输入采购订单号" />
        </el-form-item>
        <el-form-item label="付款类型" prop="bindType">
          <el-select v-model="form.bindType" placeholder="请选择付款类型" style="width:100%">
            <el-option label="定金" :value="1" />
            <el-option label="尾款" :value="2" />
            <el-option label="运费" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="付款金额" prop="amount">
          <el-input v-model="form.amount" placeholder="请输入付款金额(人民币)" style="width:100%"
            :disabled="form.id && form.status === 2" />
        </el-form-item>
        <el-form-item label="币种" prop="currency">
          <el-select v-model="form.currency" style="width:100%">
            <el-option label="CNY (人民币)" value="CNY" />
            <el-option label="USD (美元)" value="USD" />
          </el-select>
        </el-form-item>
        <el-form-item label="付款日期" prop="paymentDate">
          <el-date-picker v-model="form.paymentDate" type="date" value-format="YYYY-MM-DD"
            placeholder="选择付款日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="付款银行" prop="payingBank">
          <el-input v-model="form.payingBank" placeholder="请输入付款银行" />
        </el-form-item>
        <el-form-item label="收款方账户" prop="payeeAccountName">
          <el-input v-model="form.payeeAccountName" placeholder="请输入收款方账户名称" />
        </el-form-item>
        <el-form-item label="收款方账号" prop="payeeAccountNo">
          <el-input v-model="form.payeeAccountNo" placeholder="请输入收款方账号" />
        </el-form-item>
        <el-form-item label="开户行" prop="payeeBankName">
          <el-input v-model="form.payeeBankName" placeholder="请输入收款方开户行" />
        </el-form-item>
        <el-form-item label="行号/SWIFT" prop="payeeBankNo">
          <el-input v-model="form.payeeBankNo" placeholder="请输入行号或SWIFT代码" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="付款备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>

    <!-- 付款详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="付款详情"
      width="650px" @close="currentPayment = null">
      <div v-if="currentPayment" class="detail-info">
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="付款单号" :span="2">{{ currentPayment.paymentNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(currentPayment.status)">{{ currentPayment.statusLabel }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="采购订单号">{{ currentPayment.purchaseOrderNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="付款类型">
            <el-tag :type="bindTypeTagType(currentPayment.bindType)" size="small">
              {{ bindTypeLabel(currentPayment.bindType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="付款金额">
            <b>{{ currentPayment.currency || 'CNY' }}</b>&nbsp;{{ formatAmount(currentPayment.amount) }}
          </el-descriptions-item>
          <el-descriptions-item label="付款日期">{{ currentPayment.paymentDate }}</el-descriptions-item>
          <el-descriptions-item label="付款银行">{{ currentPayment.payingBank || '-' }}</el-descriptions-item>
          <el-descriptions-item label="收款方账户">{{ currentPayment.payeeAccountName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="收款方账号">{{ currentPayment.payeeAccountNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="开户行">{{ currentPayment.payeeBankName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="行号/SWIFT">{{ currentPayment.payeeBankNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建人">{{ currentPayment.creatorName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核人">{{ currentPayment.auditByName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">{{ currentPayment.auditTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentPayment.createTime || '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="currentPayment.auditRemark" label="审核备注" :span="3">
            {{ currentPayment.auditRemark }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentPayment.remark" label="备注" :span="3">
            {{ currentPayment.remark }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 审核驳回原因对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="驳回原因" width="400px">
      <el-form label-width="80px">
        <el-form-item label="驳回原因">
          <el-input v-model="rejectRemark" type="textarea" :rows="3" placeholder="请输入驳回原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="warning" :loading="auditLoading" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Download, Refresh } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import { exportToCsv } from '@/utils/export'
import {
  getFinancePaymentPage,
  getFinancePaymentById,
  saveFinancePayment,
  updateFinancePayment,
  deleteFinancePayment,
  auditFinancePayment
} from '@/api/advancedModules'
import { getUserListWithRoles } from '@/api/system'

const loading = ref(false)
const submitLoading = ref(false)
const auditLoading = ref(false)
const dataList = ref<any[]>([])
const total = ref(0)
const isAdmin = ref(false)
const activeTab = ref<number | undefined>(undefined)

// 状态计数
const statusCounts = reactive({ all: 0, pending: 0, approved: 0, rejected: 0 })

const statusTabs = computed(() => [
  { label: '全部', value: undefined, count: statusCounts.all },
  { label: '待审核', value: 1, count: statusCounts.pending },
  { label: '已审核', value: 2, count: statusCounts.approved },
  { label: '已驳回', value: 3, count: statusCounts.rejected }
])

// 汇总统计
const summaryTotal = computed(() => {
  const sum = dataList.value.reduce((acc: number, r: any) => acc + (parseFloat(String(r.amount)) || 0), 0)
  return sum.toFixed(2)
})

const formatAmount = (val: any) => {
  const n = parseFloat(String(val))
  if (isNaN(n)) return '-'
  return n.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const bindTypeLabel = (bt: number) => {
  const map: Record<number, string> = { 1: '定金', 2: '尾款', 3: '运费' }
  return map[bt] || '-'
}

const bindTypeTagType = (bt: number) => {
  const map: Record<number, string> = { 1: 'warning', 2: 'primary', 3: 'success' }
  return map[bt] || ''
}

const statusTagType = (status: number) => {
  const map: Record<number, string> = { 1: 'warning', 2: 'success', 3: 'danger' }
  return map[status] || ''
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
  paymentNo: '',
  status: undefined as number | undefined,
  purchaseOrderNo: '',
  amountMin: undefined as number | undefined,
  amountMax: undefined as number | undefined
})

// ---- 列表 ----
const getList = async () => {
  loading.value = true
  try {
    const res = await getFinancePaymentPage(queryParams)
    dataList.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const getStatusCounts = async () => {
  try {
    const baseParams = { ...queryParams, pageNum: 1, pageSize: 1 }
    const [allRes, pendingRes, approvedRes, rejectedRes] = await Promise.all([
      getFinancePaymentPage({ ...baseParams, status: undefined }),
      getFinancePaymentPage({ ...baseParams, status: 1 }),
      getFinancePaymentPage({ ...baseParams, status: 2 }),
      getFinancePaymentPage({ ...baseParams, status: 3 })
    ])
    statusCounts.all = allRes.data.total || 0
    statusCounts.pending = pendingRes.data.total || 0
    statusCounts.approved = approvedRes.data.total || 0
    statusCounts.rejected = rejectedRes.data.total || 0
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
  queryParams.paymentNo = ''
  queryParams.status = undefined
  queryParams.purchaseOrderNo = ''
  queryParams.amountMin = undefined
  queryParams.amountMax = undefined
  activeTab.value = undefined
  handleQuery()
}

// ---- 新建/编辑付款单 ----
const formDialogVisible = ref(false)
const formDialogTitle = ref('新建付款单')
const formRef = ref<FormInstance>()
const form = reactive<any>({
  id: null,
  paymentNo: '',
  purchaseOrderNo: '',
  bindType: null,
  amount: null,
  currency: 'CNY',
  paymentDate: '',
  payingBank: '',
  payeeAccountName: '',
  payeeAccountNo: '',
  payeeBankName: '',
  payeeBankNo: '',
  remark: '',
  status: null
})

const rules = {
  purchaseOrderNo: [{ required: true, message: '请输入采购订单号', trigger: 'blur' }],
  bindType: [{ required: true, message: '请选择付款类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入付款金额', trigger: 'change' }],
  paymentDate: [{ required: true, message: '请选择付款日期', trigger: 'change' }]
}

const resetForm = () => {
  Object.assign(form, {
    id: null, paymentNo: '', purchaseOrderNo: '', bindType: null,
    amount: null, currency: 'CNY', paymentDate: '', payingBank: '',
    payeeAccountName: '', payeeAccountNo: '', payeeBankName: '', payeeBankNo: '',
    remark: '', status: null
  })
  formRef.value?.clearValidate()
}

const handleAdd = () => {
  resetForm()
  formDialogTitle.value = '新建付款单'
  formDialogVisible.value = true
}

const handleEdit = (row: any) => {
  Object.assign(form, {
    id: row.id,
    paymentNo: row.paymentNo,
    purchaseOrderNo: row.purchaseOrderNo,
    bindType: row.bindType,
    amount: row.amount,
    currency: row.currency,
    paymentDate: row.paymentDate,
    payingBank: row.payingBank,
    payeeAccountName: row.payeeAccountName,
    payeeAccountNo: row.payeeAccountNo,
    payeeBankName: row.payeeBankName,
    payeeBankNo: row.payeeBankNo,
    remark: row.remark,
    status: row.status
  })
  formDialogTitle.value = '编辑付款单'
  formDialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (form.id) {
      await updateFinancePayment({ ...form })
      ElMessage.success('更新成功')
    } else {
      await saveFinancePayment({ ...form })
      ElMessage.success('创建成功')
    }
    formDialogVisible.value = false
    getList()
    getStatusCounts()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row: any) => {
  await ElMessageBox.confirm('确认删除该付款单?', '提示', { type: 'warning' })
  await deleteFinancePayment(row.id)
  ElMessage.success('删除成功')
  getList()
  getStatusCounts()
}

// ---- 审核 ----
const rejectDialogVisible = ref(false)
const rejectRemark = ref('')
let rejectingRow: any = null

const handleAudit = async (row: any, approved: boolean) => {
  if (approved) {
    await ElMessageBox.confirm('确认审核通过该付款单？审核通过后将同步付款金额到对应采购订单。', '审核确认', { type: 'info' })
    auditLoading.value = true
    try {
      await auditFinancePayment({ id: row.id, approved: true, auditRemark: '' })
      ElMessage.success('审核通过')
      getList()
      getStatusCounts()
    } finally {
      auditLoading.value = false
    }
  } else {
    rejectingRow = row
    rejectRemark.value = ''
    rejectDialogVisible.value = true
  }
}

const confirmReject = async () => {
  if (!rejectRemark.value.trim()) {
    ElMessage.warning('请输入驳回原因')
    return
  }
  auditLoading.value = true
  try {
    await auditFinancePayment({ id: rejectingRow.id, approved: false, auditRemark: rejectRemark.value })
    ElMessage.success('已驳回')
    rejectDialogVisible.value = false
    getList()
    getStatusCounts()
  } finally {
    auditLoading.value = false
  }
}

// ---- 详情 ----
const detailDialogVisible = ref(false)
const currentPayment = ref<any>(null)

const handleDetail = async (row: any) => {
  const res = await getFinancePaymentById(row.id)
  currentPayment.value = res.data
  detailDialogVisible.value = true
}

// ---- 导出 ----
const handleExport = async () => {
  const res = await getFinancePaymentPage({ ...queryParams, pageNum: 1, pageSize: 10000 })
  const rows = res.data.list || []
  exportToCsv('财务付款单导出', rows, [
    { label: '付款单号', key: 'paymentNo' },
    { label: '采购订单号', key: 'purchaseOrderNo' },
    { label: '付款类型', key: 'bindType' },
    { label: '币种', key: 'currency' },
    { label: '金额', key: 'amount' },
    { label: '付款日期', key: 'paymentDate' },
    { label: '付款银行', key: 'payingBank' },
    { label: '状态', key: 'statusLabel' },
    { label: '创建人', key: 'creatorName' },
    { label: '审核人', key: 'auditByName' },
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
.filter-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
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
