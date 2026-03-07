<template>
  <div class="app-container">
    <!-- 1. Search Form -->
    <el-card shadow="never" class="search-wrap">
      <el-form :inline="true" :model="queryParams" ref="queryRef">
        <el-form-item label="订单号" prop="orderNo">
          <el-input v-model="queryParams.orderNo" placeholder="输入订单号" clearable />
        </el-form-item>
        <el-form-item label="贸易条款" prop="tradeTerm">
          <el-select v-model="queryParams.tradeTerm" placeholder="选择贸易条款" clearable>
            <el-option label="FOB" value="FOB" />
            <el-option label="CIF" value="CIF" />
            <el-option label="EXW" value="EXW" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 2. Toolbar & Table -->
    <el-card shadow="never" class="table-wrap">
      <div class="table-toolbar">
        <el-button type="primary" icon="Plus" @click="handleAdd">新建订单</el-button>
        <el-button type="success" icon="Download" @click="handleExport">导出</el-button>
      </div>

      <el-table v-loading="loading" :data="orderList" border stripe>
          <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="订单号" prop="orderNo" min-width="150" />
        <el-table-column label="贸易条款" prop="tradeTerm" width="100" />
        <el-table-column label="币种" prop="currency" width="100" />
        <el-table-column label="合同金额" width="160" align="right">
          <template #default="{ row }">
            <span class="currency-text">{{ row.currency }} </span>
            <span>{{ row.contractAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="实际金额" width="160" align="right">
          <template #default="{ row }">
            <span class="currency-text">{{ row.currency }} </span>
            <span>{{ row.actualAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="收款金额" prop="receivedAmount" width="120" align="right" />
        <el-table-column label="预计收尾款(天)" prop="expectedReceiptDays" width="140" align="center" />
        <el-table-column label="运输方式" prop="transportType" width="120" />
        <el-table-column label="状态" prop="status" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="160" />
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" @click="handleDetail(scope.row)">详情</el-button>
            <el-button link type="primary" v-if="scope.row.status === 1" @click="handleEdit(scope.row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 3. Pagination -->
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination-container"
        @current-change="getList"
      />
    </el-card>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="900px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="订单号" prop="orderNo">
              <el-input v-model="form.orderNo" placeholder="输入订单号" :disabled="!!form.id" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户ID" prop="customerId">
              <el-input v-model="form.customerId" placeholder="输入客户ID" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="业务员ID" prop="salespersonId">
              <el-input v-model="form.salespersonId" placeholder="输入业务员ID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="贸易条款" prop="tradeTerm">
              <el-select v-model="form.tradeTerm" placeholder="选择贸易条款">
                <el-option label="FOB" value="FOB" />
                <el-option label="CIF" value="CIF" />
                <el-option label="EXW" value="EXW" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="币种" prop="currency">
              <el-input v-model="form.currency" placeholder="例如：USD" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="定金汇率" prop="depositExchangeRate">
              <el-input v-model="form.depositExchangeRate" placeholder="输入定金汇率" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="尾款汇率" prop="finalExchangeRate">
              <el-input v-model="form.finalExchangeRate" placeholder="输入尾款汇率" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合同金额" prop="contractAmount">
              <el-input v-model="form.contractAmount" placeholder="输入合同金额" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="实际金额" prop="actualAmount">
              <el-input v-model="form.actualAmount" placeholder="输入实际金额" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="定金比例(%)" prop="depositRate">
              <el-input v-model="form.depositRate" placeholder="输入定金比例" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="收款金额" prop="receivedAmount">
              <el-input v-model="form.receivedAmount" placeholder="输入收款金额" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预计收尾款天数" prop="expectedReceiptDays">
              <el-input v-model="form.expectedReceiptDays" placeholder="输入天数" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="运输方式" prop="transportType">
              <el-select v-model="form.transportType" placeholder="选择运输方式">
                <el-option label="集装箱" value="集装箱" />
                <el-option label="散货" value="散货" />
                <el-option label="铁路" value="铁路" />
                <el-option label="汽运" value="汽运" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="海运费" prop="seaFreight">
              <el-input v-model="form.seaFreight" placeholder="输入海运费" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="港杂费" prop="portFee">
              <el-input v-model="form.portFee" placeholder="输入港杂费" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="增值税" prop="vat">
              <el-input v-model="form.vat" placeholder="输入增值税" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="利润" prop="profit">
              <el-input v-model="form.profit" placeholder="输入利润" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="选择状态">
                <el-option label="待处理" :value="1" />
                <el-option label="采购中" :value="2" />
                <el-option label="生产中" :value="3" />
                <el-option label="已发货" :value="4" />
                <el-option label="已完成" :value="5" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { exportToCsv } from '@/utils/export'
import { getOrderPage, saveSalesOrder, updateSalesOrder } from '@/api/salesOrder'

// Data definitions
const loading = ref(false)
const submitLoading = ref(false)
const orderList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  orderNo: '',
  tradeTerm: ''
})

const form = reactive<any>({
  id: null,
  orderNo: '',
  customerId: null,
  salespersonId: null,
  tradeTerm: '',
  currency: '',
  depositExchangeRate: 0,
  finalExchangeRate: 0,
  contractAmount: 0,
  actualAmount: 0,
  depositRate: 0,
  receivedAmount: 0,
  expectedReceiptDays: 0,
  transportType: '',
  seaFreight: 0,
  portFee: 0,
  vat: 0,
  profit: 0,
  status: 1
})

const rules = {
  orderNo: [{ required: true, message: '请输入订单号', trigger: 'blur' }],
  customerId: [{ required: true, message: '请输入客户ID', trigger: 'change' }],
  salespersonId: [{ required: true, message: '请输入业务员ID', trigger: 'change' }],
  currency: [{ required: true, message: '请输入币种', trigger: 'blur' }],
  depositExchangeRate: [{ required: true, message: '请输入定金汇率', trigger: 'blur' }],
  finalExchangeRate: [{ required: true, message: '请输入尾款汇率', trigger: 'blur' }],
  contractAmount: [{ required: true, message: '请输入合同金额', trigger: 'blur' }],
  depositRate: [{ required: true, message: '请输入定金比例', trigger: 'blur' }],
  receivedAmount: [{ required: true, message: '请输入收款金额', trigger: 'blur' }],
  expectedReceiptDays: [{ required: true, message: '请输入预计收尾款天数', trigger: 'blur' }]
}

// Fetch logic
const getList = async () => {
  loading.value = true
  try {
    const res = await getOrderPage(queryParams)
    orderList.value = res.data.list
    total.value = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// Search and reset
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}
const resetQuery = () => {
  queryParams.orderNo = ''
  queryParams.tradeTerm = ''
  handleQuery()
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新建订单'
  dialogVisible.value = true
}
const handleDetail = (row: any) => {
  console.log('Detail', row)
}
const handleEdit = (row: any) => {
  resetForm()
  Object.assign(form, {
    id: row.id,
    orderNo: row.orderNo,
    customerId: row.customerId,
    salespersonId: row.salespersonId,
    tradeTerm: row.tradeTerm,
    currency: row.currency,
    depositExchangeRate: row.depositExchangeRate ?? 0,
    finalExchangeRate: row.finalExchangeRate ?? 0,
    contractAmount: row.contractAmount ?? 0,
    actualAmount: row.actualAmount ?? 0,
    depositRate: row.depositRate ?? 0,
    receivedAmount: row.receivedAmount ?? 0,
    expectedReceiptDays: row.expectedReceiptDays ?? 0,
    transportType: row.transportType ?? '',
    seaFreight: row.seaFreight ?? 0,
    portFee: row.portFee ?? 0,
    vat: row.vat ?? 0,
    profit: row.profit ?? 0,
    status: row.status ?? 1
  })
  dialogTitle.value = '编辑订单'
  dialogVisible.value = true
}

const resetForm = () => {
  form.id = null
  form.orderNo = ''
  form.customerId = null
  form.salespersonId = null
  form.tradeTerm = ''
  form.currency = ''
  form.depositExchangeRate = 0
  form.finalExchangeRate = 0
  form.contractAmount = 0
  form.actualAmount = 0
  form.depositRate = 0
  form.receivedAmount = 0
  form.expectedReceiptDays = 0
  form.transportType = ''
  form.seaFreight = 0
  form.portFee = 0
  form.vat = 0
  form.profit = 0
  form.status = 1
  formRef.value?.clearValidate()
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (form.id) {
      await updateSalesOrder({ ...form })
    } else {
      const payload = { ...form }
      delete payload.id
      await saveSalesOrder(payload)
    }
    ElMessage.success(form.id ? '更新成功' : '创建成功')
    dialogVisible.value = false
    getList()
  } finally {
    submitLoading.value = false
  }
}

const handleExport = async () => {
  const res = await getOrderPage({ ...queryParams, pageNum: 1, pageSize: 10000 })
  const rows = res.data.list || []
  exportToCsv('销售订单导出', rows, [
    { label: '订单号', key: 'orderNo' },
    { label: '贸易条款', key: 'tradeTerm' },
    { label: '币种', key: 'currency' },
    { label: '合同金额', key: 'contractAmount' },
    { label: '实际金额', key: 'actualAmount' },
    { label: '定金比例(%)', key: 'depositRate' },
    { label: '收款金额', key: 'receivedAmount' },
    { label: '预计收尾款(天)', key: 'expectedReceiptDays' },
    { label: '运输方式', key: 'transportType' },
    { label: '状态', value: (r: any) => getStatusLabel(r.status) },
    { label: '创建时间', key: 'createTime' }
  ])
}

// Status dictionary
const getStatusLabel = (status: number) => {
  const map: Record<number, string> = { 1: '待处理', 2: '采购中', 3: '生产中', 4: '已发货', 5: '已完成' }
  return map[status] || '未知'
}
const getStatusType = (status: number) => {
  const map: Record<number, string> = { 1: 'warning', 2: 'primary', 3: 'info', 4: 'success', 5: 'info' }
  return map[status] || ''
}

// Init
onMounted(() => {
  getList()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.search-wrap {
  margin-bottom: 16px;
}
.table-toolbar {
  margin-bottom: 16px;
}
.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.currency-text {
  font-weight: bold;
  color: #606266;
}
</style>
