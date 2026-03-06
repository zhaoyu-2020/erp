<template>
  <div class="app-container">
    <el-card shadow="never" class="search-wrap">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="客户编码">
          <el-input v-model="queryParams.customerCode" placeholder="输入编码" clearable />
        </el-form-item>
        <el-form-item label="客户名称">
          <el-input v-model="queryParams.name" placeholder="输入名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-wrap">
      <div class="table-toolbar">
        <el-button type="primary" icon="Plus" @click="handleAdd">新增客户</el-button>
        <el-button type="success" icon="Download" @click="handleExport">导出</el-button>
      </div>

      <el-table v-loading="loading" :data="dataList" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="客户编码" prop="customerCode" width="120" />
        <el-table-column label="名称" prop="name" min-width="150" />
        <el-table-column label="国家/地区" prop="country" width="120" />
        <el-table-column label="联系人" prop="contactPerson" width="120" />
        <el-table-column label="邮箱" prop="email" min-width="150" />
        <el-table-column label="电话" prop="phone" width="150" />
        <el-table-column label="级别" prop="level" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.level === 'VIP' ? 'danger' : 'info'">{{ row.level }}</el-tag>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="客户编码" prop="customerCode">
          <el-input v-model="form.customerCode" placeholder="输入客户编码" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="客户名称" prop="name">
          <el-input v-model="form.name" placeholder="输入客户名称" />
        </el-form-item>
        <el-form-item label="国家/地区" prop="country">
          <el-input v-model="form.country" placeholder="输入国家/地区" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="form.contactPerson" placeholder="输入联系人" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="输入邮箱" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="输入电话" />
        </el-form-item>
        <el-form-item label="级别" prop="level">
          <el-select v-model="form.level" placeholder="选择级别" style="width: 100%">
            <el-option label="普通" value="NORMAL" />
            <el-option label="VIP" value="VIP" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { exportToCsv } from '@/utils/export'
import { getCustomerPage, saveCustomer, updateCustomer, deleteCustomer } from '@/api/customer'

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
  customerCode: '',
  name: ''
})

const form = reactive<any>({
  id: null,
  customerCode: '',
  name: '',
  country: '',
  contactPerson: '',
  email: '',
  phone: '',
  level: 'NORMAL'
})

const rules = {
  customerCode: [{ required: true, message: '请输入客户编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入客户名称', trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getCustomerPage(queryParams)
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
  queryParams.customerCode = ''
  queryParams.name = ''
  handleQuery()
}

const resetForm = () => {
  form.id = null
  form.customerCode = ''
  form.name = ''
  form.country = ''
  form.contactPerson = ''
  form.email = ''
  form.phone = ''
  form.level = 'NORMAL'
  formRef.value?.clearValidate()
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增客户'
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  resetForm()
  Object.assign(form, {
    id: row.id,
    customerCode: row.customerCode,
    name: row.name,
    country: row.country,
    contactPerson: row.contactPerson,
    email: row.email,
    phone: row.phone,
    level: row.level || 'NORMAL'
  })
  dialogTitle.value = '编辑客户'
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定要删除客户 "${row.name}" 吗?`, '警告', { type: 'warning' }).then(async () => {
    await deleteCustomer(row.id)
    ElMessage.success('删除成功')
    getList()
  })
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (form.id) {
      await updateCustomer({ ...form })
    } else {
      const payload = { ...form }
      delete payload.id
      await saveCustomer(payload)
    }
    ElMessage.success(form.id ? '更新成功' : '添加成功')
    dialogVisible.value = false
    getList()
  } finally {
    submitLoading.value = false
  }
}

const handleExport = async () => {
  const res = await getCustomerPage({ ...queryParams, pageNum: 1, pageSize: 10000 })
  const rows = res.data.list || []
  exportToCsv('客户管理导出', rows, [
    { label: '客户编码', key: 'customerCode' },
    { label: '名称', key: 'name' },
    { label: '国家/地区', key: 'country' },
    { label: '联系人', key: 'contactPerson' },
    { label: '邮箱', key: 'email' },
    { label: '电话', key: 'phone' },
    { label: '级别', key: 'level' }
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
