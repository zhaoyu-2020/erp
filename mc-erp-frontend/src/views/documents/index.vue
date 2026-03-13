<template>
  <div class="app-container">
    <el-card shadow="never" class="search-wrap">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="单证号">
          <el-input v-model="queryParams.docNo" placeholder="输入单证号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="选择状态" clearable>
            <el-option label="待处理" :value="1" />
            <el-option label="申报中" :value="2" />
            <el-option label="已放行" :value="3" />
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
        <el-button type="primary" icon="Plus" @click="handleAdd">新建报关单</el-button>
        <el-button type="success" icon="Download" @click="handleExport">导出</el-button>
      </div>

      <el-table v-loading="loading" :data="dataList" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="单证号" prop="docNo" width="150" />
        <el-table-column label="关联销售单" prop="salesOrderNo" width="150" />
        <el-table-column label="申报日期" prop="declareDate" width="120" />
        <el-table-column label="状态" prop="status" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="160" />
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary">打印</el-button>
            <el-button link type="primary" v-if="scope.row.status === 1" @click="handleEdit(scope.row)">编辑</el-button>
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

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="620px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="单证号" prop="docNo">
          <el-input v-model="form.docNo" placeholder="输入单证号" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="销售单ID" prop="salesOrderId">
          <el-input-number v-model="form.salesOrderId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="申报日期" prop="declareDate">
          <el-date-picker v-model="form.declareDate" type="date" placeholder="选择日期" format="YYYY-MM-DD" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="选择状态" style="width: 100%">
            <el-option label="待处理" :value="1" />
            <el-option label="申报中" :value="2" />
            <el-option label="已放行" :value="3" />
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
import { getCustomsDocPage, saveCustomsDoc, updateCustomsDoc } from '@/api/advancedModules'

const loading = ref(false)
const submitLoading = ref(false)
const dataList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  docNo: '',
  status: undefined
})

const form = reactive<any>({
  id: null,
  docNo: '',
  salesOrderId: null,
  declareDate: '',
  status: 1
})

const rules = {
  docNo: [{ required: true, message: '请输入单证号', trigger: 'blur' }],
  salesOrderId: [{ required: true, message: '请输入销售单ID', trigger: 'change' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getCustomsDocPage(queryParams)
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
  queryParams.docNo = ''
  queryParams.status = undefined
  handleQuery()
}

const getStatusLabel = (status: number) => {
  const map: Record<number, string> = { 1: '待处理', 2: '申报中', 3: '已放行' }
  return map[status] || '未知'
}
const getStatusType = (status: number) => {
  const map: Record<number, string> = { 1: 'info', 2: 'warning', 3: 'success' }
  return map[status] || ''
}

const resetForm = () => {
  form.id = null
  form.docNo = ''
  form.salesOrderId = null
  form.declareDate = ''
  form.status = 1
  formRef.value?.clearValidate()
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新建报关单'
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  resetForm()
  Object.assign(form, {
    id: row.id,
    docNo: row.docNo,
    salesOrderId: row.salesOrderId,
    declareDate: row.declareDate,
    status: row.status ?? 1
  })
  dialogTitle.value = '编辑报关单'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (form.id) {
      await updateCustomsDoc({ ...form })
    } else {
      const payload = { ...form }
      delete payload.id
      await saveCustomsDoc(payload)
    }
    ElMessage.success(form.id ? '更新成功' : '创建成功')
    dialogVisible.value = false
    getList()
  } finally {
    submitLoading.value = false
  }
}

const handleExport = async () => {
  const res = await getCustomsDocPage({ ...queryParams, pageNum: 1, pageSize: 10000 })
  const rows = res.data.list || []
  exportToCsv('报关单证导出', rows, [
    { label: '单证号', key: 'docNo' },
    { label: '销售单ID', key: 'salesOrderId' },
    { label: '关联销售单', key: 'salesOrderNo' },
    { label: '申报日期', key: 'declareDate' },
    { label: '状态', value: (r: any) => getStatusLabel(r.status) },
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
