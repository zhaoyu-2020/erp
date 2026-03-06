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
        <el-table-column label="总金额" width="150" align="right">
          <template #default="{ row }">
            <span class="currency-text">{{ row.currency }} </span>
            <span>{{ row.totalAmount }}</span>
          </template>
        </el-table-column>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="订单号" prop="orderNo">
          <el-input v-model="form.orderNo" placeholder="输入订单号" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="客户ID" prop="customerId">
          <el-input-number v-model="form.customerId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="业务员ID" prop="salespersonId">
          <el-input-number v-model="form.salespersonId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="贸易条款" prop="tradeTerm">
          <el-select v-model="form.tradeTerm" placeholder="选择贸易条款" style="width: 100%">
            <el-option label="FOB" value="FOB" />
            <el-option label="CIF" value="CIF" />
            <el-option label="EXW" value="EXW" />
          </el-select>
        </el-form-item>
        <el-form-item label="币种" prop="currency">
          <el-input v-model="form.currency" placeholder="例如：USD" />
        </el-form-item>
        <el-form-item label="汇率" prop="exchangeRate">
          <el-input-number v-model="form.exchangeRate" :min="0" :step="0.0001" style="width: 100%" />
        </el-form-item>
        <el-form-item label="总金额" prop="totalAmount">
          <el-input-number v-model="form.totalAmount" :min="0" :step="0.01" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="选择状态" style="width: 100%">
            <el-option label="待处理" :value="1" />
            <el-option label="采购中" :value="2" />
            <el-option label="生产中" :value="3" />
            <el-option label="已发货" :value="4" />
            <el-option label="已完成" :value="5" />
          </el-select>
        </el-form-item>
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
  exchangeRate: 0,
  totalAmount: 0,
  status: 1
})

const rules = {
  orderNo: [{ required: true, message: '请输入订单号', trigger: 'blur' }],
  customerId: [{ required: true, message: '请输入客户ID', trigger: 'change' }],
  tradeTerm: [{ required: true, message: '请选择贸易条款', trigger: 'change' }],
  currency: [{ required: true, message: '请输入币种', trigger: 'blur' }]
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
    exchangeRate: row.exchangeRate ?? 0,
    totalAmount: row.totalAmount ?? 0,
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
  form.exchangeRate = 0
  form.totalAmount = 0
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
    { label: '总金额', key: 'totalAmount' },
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
