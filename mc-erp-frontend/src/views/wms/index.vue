<template>
  <div class="app-container">
    <el-card shadow="never" class="search-wrap">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="产品ID">
          <el-input v-model="queryParams.productId" placeholder="输入产品ID" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-wrap">
      <div class="table-toolbar">
        <el-button type="primary" icon="Plus" @click="handleAdd">出入库</el-button>
        <el-button type="success" icon="Download" @click="handleExport">导出</el-button>
      </div>

      <el-table v-loading="loading" :data="dataList" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="产品编码" prop="productCode" width="150" />
        <el-table-column label="产品名称" prop="productName" min-width="150" />
        <el-table-column label="当前库存" prop="currentQty" width="120" align="right" />
        <el-table-column label="可用库存" prop="availableQty" width="120" align="right" />
        <el-table-column label="锁定库存" prop="lockedQty" width="120" align="right" />
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default>
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

    <!-- In/Out (Create/Adjust) Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="620px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="产品ID" prop="productId">
          <el-input-number v-model="form.productId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="当前库存" prop="currentQty">
          <el-input-number v-model="form.currentQty" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="可用库存" prop="availableQty">
          <el-input-number v-model="form.availableQty" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="锁定库存" prop="lockedQty">
          <el-input-number v-model="form.lockedQty" :min="0" style="width: 100%" />
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
import { getStockPage, saveStock } from '@/api/advancedModules'

const loading = ref(false)
const submitLoading = ref(false)
const dataList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('出入库')
const formRef = ref<FormInstance>()
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  productId: undefined
})

const form = reactive<any>({
  productId: null,
  currentQty: 0,
  availableQty: 0,
  lockedQty: 0
})

const rules = {
  productId: [{ required: true, message: '请输入产品ID', trigger: 'change' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getStockPage(queryParams)
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
  queryParams.productId = undefined
  handleQuery()
}

const resetForm = () => {
  form.productId = null
  form.currentQty = 0
  form.availableQty = 0
  form.lockedQty = 0
  formRef.value?.clearValidate()
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '出入库'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    await saveStock({ ...form })
    ElMessage.success('操作成功')
    dialogVisible.value = false
    getList()
  } finally {
    submitLoading.value = false
  }
}

const handleExport = async () => {
  const res = await getStockPage({ ...queryParams, pageNum: 1, pageSize: 10000 })
  const rows = res.data.list || []
  exportToCsv('仓储库存导出', rows, [
    { label: '产品编码', key: 'productCode' },
    { label: '产品名称', key: 'productName' },
    { label: '产品ID', key: 'productId' },
    { label: '当前库存', key: 'currentQty' },
    { label: '可用库存', key: 'availableQty' },
    { label: '锁定库存', key: 'lockedQty' }
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
