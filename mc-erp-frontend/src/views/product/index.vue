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
        <el-form-item label="类型">
          <el-select v-model="queryParams.type" placeholder="选择类型" clearable style="width: 180px">
            <el-option
              v-for="item in PRODUCT_TYPE_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
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
        <el-button type="primary" icon="Plus" @click="handleAdd">新增产品</el-button>
        <el-button type="success" icon="Download" @click="handleExport">导出</el-button>
      </div>

      <el-table v-loading="loading" :data="dataList" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="SPU编码" prop="spuCode" width="120" />
        <el-table-column label="中文名称" prop="nameCn" min-width="150" />
        <el-table-column label="英文名称" prop="nameEn" min-width="150" />
        <el-table-column label="类型" prop="type" width="120" />
        <el-table-column label="规格" prop="spec" width="140" />
        <el-table-column label="材质" prop="material" width="120" />
        <el-table-column label="长度" prop="length" width="100" />
        <el-table-column label="米重" prop="meterWeight" width="100" />
        <el-table-column label="公差" prop="tolerance" width="100" />
        <el-table-column label="单位" prop="unit" width="80" align="center" />
        <el-table-column label="hscode" prop="hsCode" width="130" />
        <el-table-column label="退税率" prop="taxRefundRate" width="140" align="right">
          <template #default="{ row }">
            {{ (row.taxRefundRate * 100).toFixed(2) }}%
          </template>
        </el-table-column>
        <el-table-column label="申报要素" prop="declaration" min-width="180" show-overflow-tooltip />
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="680px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="SPU编码" prop="spuCode">
          <el-input v-model="form.spuCode" placeholder="输入SPU编码" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="中文名称" prop="nameCn">
          <el-input v-model="form.nameCn" placeholder="输入中文名称" />
        </el-form-item>
        <el-form-item label="英文名称" prop="nameEn">
          <el-input v-model="form.nameEn" placeholder="输入英文名称" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="选择类型" style="width: 100%">
            <el-option
              v-for="item in PRODUCT_TYPE_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="规格" prop="spec">
          <el-input v-model="form.spec" placeholder="输入规格" />
        </el-form-item>
        <el-form-item label="材质" prop="material">
          <el-input v-model="form.material" placeholder="输入材质" />
        </el-form-item>
        <el-form-item label="长度" prop="length">
          <el-input v-model="form.length" placeholder="输入长度" />
        </el-form-item>
        <el-form-item label="米重" prop="meterWeight">
          <el-input v-model="form.meterWeight" placeholder="输入米重" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="form.unit" placeholder="例如：吨、米、PCS" />
        </el-form-item>
        <el-form-item label="公差" prop="tolerance">
          <el-input v-model="form.tolerance" placeholder="输入公差" />
        </el-form-item>
        <el-form-item label="hscode" prop="hsCode">
          <el-input v-model="form.hsCode" placeholder="输入hscode" />
        </el-form-item>
        <el-form-item label="退税率" prop="taxRefundRate">
          <el-input-number v-model="form.taxRefundRate" :min="0" :max="1" :step="0.01" style="width: 100%" />
        </el-form-item>
        <el-form-item label="申报要素" prop="declaration">
          <el-input v-model="form.declaration" type="textarea" :rows="3" placeholder="输入申报要素" />
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

const PRODUCT_TYPE_OPTIONS = [
  { label: 'H型钢', value: 'H型钢' },
  { label: 'I型钢', value: 'I型钢' },
  { label: '热轧卷', value: '热轧卷' },
  { label: '冷轧卷', value: '冷轧卷' },
  { label: '角钢', value: '角钢' }
]

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
  nameCn: '',
  type: ''
})

const form = reactive<any>({
  id: null,
  spuCode: '',
  nameCn: '',
  nameEn: '',
  type: '',
  spec: '',
  material: '',
  length: '',
  meterWeight: '',
  unit: '',
  tolerance: '',
  hsCode: '',
  taxRefundRate: 0,
  declaration: ''
})

const rules = {
  spuCode: [{ required: true, message: '请输入SPU编码', trigger: 'blur' }],
  nameCn: [{ required: true, message: '请输入中文名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  spec: [{ required: true, message: '请输入规格', trigger: 'blur' }],
  material: [{ required: true, message: '请输入材质', trigger: 'blur' }],
  length: [{ required: true, message: '请输入长度', trigger: 'blur' }],
  meterWeight: [{ required: true, message: '请输入米重', trigger: 'blur' }],
  unit: [{ required: true, message: '请输入单位', trigger: 'blur' }],
  tolerance: [{ required: true, message: '请输入公差', trigger: 'blur' }],
  declaration: [{ required: true, message: '请输入申报要素', trigger: 'blur' }]
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
  queryParams.type = ''
  handleQuery()
}

const resetForm = () => {
  form.id = null
  form.spuCode = ''
  form.nameCn = ''
  form.nameEn = ''
  form.type = ''
  form.spec = ''
  form.material = ''
  form.length = ''
  form.meterWeight = ''
  form.tolerance = ''
  form.hsCode = ''
  form.unit = ''
  form.taxRefundRate = 0
  form.declaration = ''
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
    nameCn: row.nameCn,
    nameEn: row.nameEn,
    type: row.type,
    spec: row.spec,
    material: row.material,
    length: row.length,
    meterWeight: row.meterWeight,
    tolerance: row.tolerance,
    hsCode: row.hsCode,
    unit: row.unit,
    taxRefundRate: row.taxRefundRate ?? 0,
    declaration: row.declaration
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
    { label: '中文名称', key: 'nameCn' },
    { label: '英文名称', key: 'nameEn' },
    { label: '类型', key: 'type' },
    { label: '规格', key: 'spec' },
    { label: '材质', key: 'material' },
    { label: '长度', key: 'length' },
    { label: '米重', key: 'meterWeight' },
    { label: '单位', key: 'unit' },
    { label: '公差', key: 'tolerance' },
    { label: 'hscode', key: 'hsCode' },
    { label: '退税率', value: (r: any) => (r.taxRefundRate == null ? '' : `${(Number(r.taxRefundRate) * 100).toFixed(2)}%`) },
    { label: '申报要素', key: 'declaration' }
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
