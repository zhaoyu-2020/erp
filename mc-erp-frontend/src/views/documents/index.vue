<template>
  <div class="mc-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">报关单证</h2>
      </div>
      <div class="page-header-right">
        <el-button type="success" icon="Download" @click="handleExport">导出</el-button>
        <el-button type="primary" icon="Plus" @click="handleAdd">新建报关单</el-button>
      </div>
    </div>

    <!-- 状态标签页 -->
    <div class="status-tabs">
      <div
        v-for="tab in statusTabs"
        :key="String(tab.value)"
        :class="['status-tab-item', { active: activeStatusTab === tab.value }]"
        @click="handleTabChange(tab.value)"
      >
        {{ tab.label }}({{ tab.count }})
      </div>
    </div>

    <!-- 搜索过滤区域 -->
    <div class="filter-bar">
      <div class="filter-inputs">
        <el-input v-model="queryParams.docNo" placeholder="单证号" clearable class="filter-input" @clear="handleQuery" @keyup.enter="handleQuery" />
      </div>
      <div class="filter-actions">
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </div>
    </div>

    <!-- 表格 -->
    <div class="table-container">
      <el-table v-loading="loading" :data="dataList" stripe>
        <el-table-column type="index" label="#" width="50" align="center" />
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
            <div class="action-btns">
              <el-button link type="primary">打印</el-button>
              <el-button link type="primary" v-if="scope.row.status === 1" @click="handleEdit(scope.row)">编辑</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-bar">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="getList"
      />
    </div>

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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { exportToCsv } from '@/utils/export'
import { getCustomsDocPage, saveCustomsDoc, updateCustomsDoc } from '@/api/advancedModules'

// ============ 状态映射 ============
const docStatusMap: Record<number, { label: string; type: string }> = {
  1: { label: '待处理', type: 'info' },
  2: { label: '申报中', type: 'warning' },
  3: { label: '已放行', type: 'success' }
}

// ============ 状态标签页 ============
const activeStatusTab = ref<number | undefined>(undefined)
const statusCounts = reactive<Record<string, number>>({})

const statusTabs = computed(() => {
  const tabs = [{ label: '全部', value: undefined as number | undefined, count: statusCounts['all'] || 0 }]
  for (const [code, info] of Object.entries(docStatusMap)) {
    tabs.push({ label: info.label, value: Number(code), count: statusCounts[code] || 0 })
  }
  return tabs
})

const getStatusCounts = async () => {
  try {
    const baseParams = { ...queryParams, pageNum: 1, pageSize: 1 }
    const allRes = await getCustomsDocPage({ ...baseParams, status: undefined })
    statusCounts['all'] = allRes.data.total || 0
    const promises = Object.keys(docStatusMap).map(async (code) => {
      const res = await getCustomsDocPage({ ...baseParams, status: Number(code) })
      statusCounts[code] = res.data.total || 0
    })
    await Promise.all(promises)
  } catch { /* ignore */ }
}

const handleTabChange = (val: number | undefined) => {
  activeStatusTab.value = val
  queryParams.status = val
  queryParams.pageNum = 1
  getList()
}

const loading = ref(false)
const submitLoading = ref(false)
const dataList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const queryParams = reactive({
  pageNum: 1,
  pageSize: 20,
  docNo: '',
  status: undefined as number | undefined
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
  getStatusCounts()
})
</script>

<style scoped>
</style>
