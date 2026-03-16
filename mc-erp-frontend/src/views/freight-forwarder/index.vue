<template>
  <div class="app-container">
    <el-card shadow="never" class="search-wrap">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="货代编码">
          <el-input v-model="queryParams.forwarderCode" placeholder="输入编码" clearable />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="queryParams.name" placeholder="输入名称" clearable />
        </el-form-item>
        <el-form-item label="货代类型">
          <el-select v-model="queryParams.freightType" clearable placeholder="全部" style="width: 160px">
            <el-option label="集装箱" value="集装箱" />
            <el-option label="散货" value="散货" />
          </el-select>
        </el-form-item>
        <el-form-item label="优势市场">
          <el-select v-model="queryParams.marketAdvantage" clearable placeholder="全部" style="width: 180px">
            <el-option v-for="item in marketOptions" :key="item" :label="item" :value="item" />
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
        <el-button type="primary" icon="Plus" @click="handleAdd">新增货代</el-button>
        <el-button type="success" icon="Download" @click="handleExport">导出</el-button>
      </div>

      <el-table v-loading="loading" :data="dataList" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="编码" prop="forwarderCode" width="140" />
        <el-table-column label="名称" prop="name" min-width="150" />
        <el-table-column label="货代类型" prop="freightType" width="120" />
        <el-table-column label="优势市场" prop="marketAdvantage" min-width="220" show-overflow-tooltip />
        <el-table-column label="联系人" prop="contactPerson" width="120" />
        <el-table-column label="电话" prop="phone" width="150" />
        <el-table-column label="地址" prop="address" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="140" fixed="right" align="center">
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="货代编码" prop="forwarderCode">
          <el-input v-model="form.forwarderCode" placeholder="输入货代编码" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="输入名称" />
        </el-form-item>
        <el-form-item label="货代类型" prop="freightType">
          <el-select v-model="form.freightType" placeholder="选择货代类型" style="width: 100%">
            <el-option label="集装箱" value="集装箱" />
            <el-option label="散货" value="散货" />
          </el-select>
        </el-form-item>
        <el-form-item label="优势市场" prop="marketAdvantageArr">
          <el-select
            v-model="form.marketAdvantageArr"
            multiple
            collapse-tags
            clearable
            placeholder="可多选"
            style="width: 100%"
          >
            <el-option v-for="item in marketOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="form.contactPerson" placeholder="输入联系人" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="输入电话" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="输入地址" />
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
import {
  getFreightForwarderPage,
  saveFreightForwarder,
  updateFreightForwarder,
  deleteFreightForwarder
} from '@/api/freightForwarder'

const marketOptions = ['东南亚', '中东', '西非', '东非', '中美', '南美', '北美']

const loading = ref(false)
const submitLoading = ref(false)
const dataList = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  forwarderCode: '',
  name: '',
  freightType: '',
  marketAdvantage: ''
})

const form = reactive<any>({
  id: null,
  forwarderCode: '',
  name: '',
  freightType: '',
  marketAdvantage: '',
  marketAdvantageArr: [],
  contactPerson: '',
  phone: '',
  address: ''
})

const rules = {
  forwarderCode: [{ required: true, message: '请输入货代编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  freightType: [{ required: true, message: '请选择货代类型', trigger: 'change' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getFreightForwarderPage(queryParams)
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
  queryParams.forwarderCode = ''
  queryParams.name = ''
  queryParams.freightType = ''
  queryParams.marketAdvantage = ''
  handleQuery()
}

const resetForm = () => {
  form.id = null
  form.forwarderCode = ''
  form.name = ''
  form.freightType = ''
  form.marketAdvantage = ''
  form.marketAdvantageArr = []
  form.contactPerson = ''
  form.phone = ''
  form.address = ''
  formRef.value?.clearValidate()
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增货代'
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  resetForm()
  Object.assign(form, {
    id: row.id,
    forwarderCode: row.forwarderCode,
    name: row.name,
    freightType: row.freightType,
    marketAdvantage: row.marketAdvantage,
    marketAdvantageArr: row.marketAdvantage ? row.marketAdvantage.split(',') : [],
    contactPerson: row.contactPerson,
    phone: row.phone,
    address: row.address
  })
  dialogTitle.value = '编辑货代'
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定要删除货代 \"${row.name}\" 吗?`, '警告', { type: 'warning' }).then(async () => {
    await deleteFreightForwarder(row.id)
    ElMessage.success('删除成功')
    getList()
  })
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    ElMessage.warning('请完善必填项')
    return
  }
  form.marketAdvantage = (form.marketAdvantageArr || []).join(',')
  submitLoading.value = true
  try {
    if (form.id) {
      await updateFreightForwarder({ ...form })
    } else {
      const payload = { ...form }
      delete payload.id
      await saveFreightForwarder(payload)
    }
    ElMessage.success(form.id ? '更新成功' : '添加成功')
    dialogVisible.value = false
    getList()
  } finally {
    submitLoading.value = false
  }
}

const handleExport = async () => {
  const res = await getFreightForwarderPage({ ...queryParams, pageNum: 1, pageSize: 10000 })
  const rows = res.data.list || []
  exportToCsv('货代管理导出', rows, [
    { label: '编码', key: 'forwarderCode' },
    { label: '名称', key: 'name' },
    { label: '货代类型', key: 'freightType' },
    { label: '优势市场', key: 'marketAdvantage' },
    { label: '联系人', key: 'contactPerson' },
    { label: '电话', key: 'phone' },
    { label: '地址', key: 'address' }
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
