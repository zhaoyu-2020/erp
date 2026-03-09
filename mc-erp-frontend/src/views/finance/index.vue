<template>
  <div class="app-container">
    <el-card shadow="never" class="search-wrap">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="收款单号">
          <el-input v-model="queryParams.receiptNo" placeholder="输入收款单号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="选择状态" clearable>
            <el-option label="未认领" :value="1" />
            <el-option label="已认领" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-wrap">
      <div class="table-toolbar">
        <el-button type="primary" icon="Plus" @click="handleAdd">新建收款单</el-button>
        <el-button type="success" icon="Download" @click="handleExport">导出</el-button>
      </div>

      <el-table v-loading="loading" :data="dataList" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="收款单号" prop="receiptNo" width="150" />
        <el-table-column label="客户" prop="customerName" min-width="150" />
        <el-table-column label="金额" width="150" align="right">
          <template #default="{ row }">
            <b>{{ row.currency }}</b> {{ row.amount }}
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="认领时间" prop="claimTime" width="160" />
        <el-table-column label="创建时间" prop="createTime" width="160" />
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" v-if="scope.row.status === 1">认领</el-button>
            <el-button link type="primary">详情</el-button>
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
      />
    </el-card>

    <!-- Add Dialog -->
    <el-dialog v-model="dialogVisible" title="新建收款单" width="620px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="收款单号" prop="receiptNo">
          <el-input v-model="form.receiptNo" placeholder="输入收款单号" />
        </el-form-item>
        <el-form-item label="客户ID" prop="customerId">
          <el-input v-model="form.customerId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input v-model="form.amount" :min="0" :step="0.01" style="width: 100%" />
        </el-form-item>
        <el-form-item label="币种" prop="currency">
          <el-input v-model="form.currency" placeholder="例如：USD" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="选择状态" style="width: 100%">
            <el-option label="未认领" :value="1" />
            <el-option label="已认领" :value="2" />
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
import { getFinanceReceiptPage, saveFinanceReceipt } from '@/api/advancedModules'

const loading = ref(false)
const submitLoading = ref(false)
const dataList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  receiptNo: '',
  status: undefined
})

const form = reactive<any>({
  receiptNo: '',
  customerId: null,
  amount: 0,
  currency: '',
  status: 1
})

const rules = {
  receiptNo: [{ required: true, message: '请输入收款单号', trigger: 'blur' }],
  customerId: [{ required: true, message: '请输入客户ID', trigger: 'change' }],
  currency: [{ required: true, message: '请输入币种', trigger: 'blur' }]
}

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
  queryParams.receiptNo = ''
  queryParams.status = undefined
  handleQuery()
}

const getStatusLabel = (status: number) => {
  const map: Record<number, string> = { 1: '未认领', 2: '已认领' }
  return map[status] || '未知'
}
const getStatusType = (status: number) => {
  const map: Record<number, string> = { 1: 'danger', 2: 'success' }
  return map[status] || ''
}

const resetForm = () => {
  form.receiptNo = ''
  form.customerId = null
  form.amount = 0
  form.currency = ''
  form.status = 1
  formRef.value?.clearValidate()
}

const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    await saveFinanceReceipt({ ...form })
    ElMessage.success('创建成功')
    dialogVisible.value = false
    getList()
  } finally {
    submitLoading.value = false
  }
}

const handleExport = async () => {
  const res = await getFinanceReceiptPage({ ...queryParams, pageNum: 1, pageSize: 10000 })
  const rows = res.data.list || []
  exportToCsv('财务收据导出', rows, [
    { label: '收款单号', key: 'receiptNo' },
    { label: '客户', key: 'customerName' },
    { label: '客户ID', key: 'customerId' },
    { label: '币种', key: 'currency' },
    { label: '金额', key: 'amount' },
    { label: '状态', value: (r: any) => getStatusLabel(r.status) },
    { label: '认领时间', key: 'claimTime' },
    { label: '创建时间', key: 'createTime' }
  ])
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.app-container { padding: 0; }
.search-wrap { margin-bottom: 16px; }
.table-toolbar { margin-bottom: 16px; }
.pagination-container { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
