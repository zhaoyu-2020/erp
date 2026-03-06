<template>
  <div class="app-container">
    <el-card shadow="never" class="search-wrap">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="SPU编码">
          <el-input v-model="queryParams.spuCode" placeholder="输入SPU编码" clearable />
        </el-form-item>
        <el-form-item label="产品名称">
          <el-input v-model="queryParams.nameCn" placeholder="输入产品中文名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-wrap">
      <div class="table-toolbar">
        <el-button type="primary" icon="Plus" @click="handleAdd">新增产品</el-button>
        <el-button type="success" icon="Download" @click="handleExport">导出</el-button>
      </div>

      <el-table v-loading="loading" :data="dataList" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="SPU编码" prop="spuCode" width="120" />
        <el-table-column label="HS海关编码" prop="hsCode" width="120" />
        <el-table-column label="中文名称" prop="nameCn" min-width="150" />
        <el-table-column label="英文名称" prop="nameEn" min-width="150" />
        <el-table-column label="单位" prop="unit" width="80" align="center" />
        <el-table-column label="退税率" prop="taxRefundRate" width="140" align="right">
          <template #default="{ row }">
            {{ (row.taxRefundRate * 100).toFixed(2) }}%
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
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
        <el-form-item label="SPU编码" prop="spuCode">
          <el-input v-model="form.spuCode" placeholder="输入SPU编码" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="HS海关编码" prop="hsCode">
          <el-input v-model="form.hsCode" placeholder="输入HS海关编码" />
        </el-form-item>
        <el-form-item label="中文名称" prop="nameCn">
          <el-input v-model="form.nameCn" placeholder="输入中文名称" />
        </el-form-item>
        <el-form-item label="英文名称" prop="nameEn">
          <el-input v-model="form.nameEn" placeholder="输入英文名称" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="form.unit" placeholder="例如：PCS" />
        </el-form-item>
        <el-form-item label="退税率" prop="taxRefundRate">
          <el-input-number v-model="form.taxRefundRate" :min="0" :max="1" :step="0.01" style="width: 100%" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { exportToCsv } from '@/utils/export'
import { getProductPage, saveProduct, updateProduct, deleteProduct } from '@/api/product'

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
  spuCode: '',
  nameCn: ''
})

const form = reactive<any>({
  id: null,
  spuCode: '',
  hsCode: '',
  nameCn: '',
  nameEn: '',
  unit: '',
  taxRefundRate: 0
})

const rules = {
  spuCode: [{ required: true, message: '请输入SPU编码', trigger: 'blur' }],
  nameCn: [{ required: true, message: '请输入中文名称', trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getProductPage(queryParams)
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
  queryParams.spuCode = ''
  queryParams.nameCn = ''
  handleQuery()
}

const resetForm = () => {
  form.id = null
  form.spuCode = ''
  form.hsCode = ''
  form.nameCn = ''
  form.nameEn = ''
  form.unit = ''
  form.taxRefundRate = 0
  formRef.value?.clearValidate()
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增产品'
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  resetForm()
  Object.assign(form, {
    id: row.id,
    spuCode: row.spuCode,
    hsCode: row.hsCode,
    nameCn: row.nameCn,
    nameEn: row.nameEn,
    unit: row.unit,
    taxRefundRate: row.taxRefundRate ?? 0
  })
  dialogTitle.value = '编辑产品'
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定要删除产品 "${row.nameCn}" 吗?`, '警告', { type: 'warning' }).then(async () => {
    await deleteProduct(row.id)
    ElMessage.success('删除成功')
    getList()
  })
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (form.id) {
      await updateProduct({ ...form })
    } else {
      const payload = { ...form }
      delete payload.id
      await saveProduct(payload)
    }
    ElMessage.success(form.id ? '更新成功' : '添加成功')
    dialogVisible.value = false
    getList()
  } finally {
    submitLoading.value = false
  }
}

const handleExport = async () => {
  const res = await getProductPage({ ...queryParams, pageNum: 1, pageSize: 10000 })
  const rows = res.data.list || []
  exportToCsv('产品管理导出', rows, [
    { label: 'SPU编码', key: 'spuCode' },
    { label: 'HS海关编码', key: 'hsCode' },
    { label: '中文名称', key: 'nameCn' },
    { label: '英文名称', key: 'nameEn' },
    { label: '单位', key: 'unit' },
    { label: '退税率', value: (r: any) => (r.taxRefundRate == null ? '' : `${(Number(r.taxRefundRate) * 100).toFixed(2)}%`) }
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
