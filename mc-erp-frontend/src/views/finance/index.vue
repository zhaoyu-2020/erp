<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-wrap">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="销售订单号">
          <el-input v-model="queryParams.salesOrderNo" placeholder="输入销售订单号" clearable style="width:160px" />
        </el-form-item>
        <el-form-item label="金额">
          <el-input v-model="queryParams.amountMin" placeholder="最小金额" clearable style="width:110px" />
          <span style="margin:0 4px;color:#999">~</span>
          <el-input v-model="queryParams.amountMax" placeholder="最大金额" clearable style="width:110px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="选择状态" clearable style="width:120px">
            <el-option label="新建" :value="1" />
            <el-option label="认领中" :value="2" />
            <el-option label="完成" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never" class="table-wrap">
      <div class="table-toolbar">
        <el-button type="primary" icon="Plus" @click="handleAdd">新建收款单</el-button>
        <el-button type="success" icon="Download" @click="handleExport">导出</el-button>
      </div>

      <el-table v-loading="loading" :data="dataList" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="流水号" prop="serialNo" min-width="160" />
        <el-table-column label="金额" width="160" align="right">
          <template #default="{ row }">
            <b>{{ row.currency }}</b>&nbsp;{{ row.amount }}
          </template>
        </el-table-column>
        <el-table-column label="收款日期" prop="receiptDate" width="120" align="center" />
        <el-table-column label="收款银行" prop="receivingBank" min-width="160" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="160" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">详情/认领</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination-container"
        @current-change="getList"
        @size-change="getList"
      />
    </el-card>

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

const checkAdmin = async () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const res = await getUserListWithRoles()
    const allUsers = res.data || []
    const currentUser = allUsers.find((u: any) => u.id === userInfo.userId)
    isAdmin.value = !!(currentUser && Array.isArray(currentUser.roleNames) && currentUser.roleNames.includes('管理员'))
  } catch {
    isAdmin.value = false
  }
}

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  serialNo: '',
  status: undefined as number | undefined,
  salesOrderNo: '',
  amountMin: undefined as number | undefined,
  amountMax: undefined as number | undefined
})

// ---- 列表 ----
const getList = async () => {
  loading.value = true
  try {
    const res = await getFinanceReceiptPage(queryParams)
    dataList.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

const resetQuery = () => {
  queryParams.serialNo = ''
  queryParams.status = undefined
  queryParams.salesOrderNo = ''
  queryParams.amountMin = undefined
  queryParams.amountMax = undefined
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
  status: null
})

const rules = {
  amount: [{ required: true, message: '请输入金额', trigger: 'change' }],
  currency: [{ required: true, message: '请选择币种', trigger: 'change' }],
  receiptDate: [{ required: true, message: '请选择收款日期', trigger: 'change' }]
}

const resetForm = () => {
  Object.assign(form, { id: null, serialNo: '', amount: 0, currency: 'USD', receiptDate: '', receivingBank: '', status: null })
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
const detailForm = reactive({ salesOrderNo: '', bindType: null as number | null, boundAmount: null as number | null })
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
      details: (currentReceipt.value.details || []).map((d: any) => ({
        salesOrderNo: d.salesOrderNo,
        bindType: d.bindType,
        boundAmount: d.boundAmount,
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
  checkAdmin()
})
</script>

<style scoped>
.app-container { padding: 0; }
.search-wrap { margin-bottom: 16px; }
.table-toolbar { margin-bottom: 16px; }
.pagination-container { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
