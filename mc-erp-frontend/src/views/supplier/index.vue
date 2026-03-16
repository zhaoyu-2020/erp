<template>
  <div class="app-container">
    <el-card shadow="never" class="search-wrap">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="供应商编码">
          <el-input v-model="queryParams.supplierCode" placeholder="输入编码" clearable />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="queryParams.name" placeholder="输入名称" clearable />
        </el-form-item>
        <el-form-item label="产品类型">
          <el-select
            v-model="queryParams.productType"
            filterable
            allow-create
            clearable
            placeholder="输入或选择产品类型"
            style="width: 180px"
          >
            <el-option
              v-for="item in productTypeList"
              :key="item.id"
              :label="item.typeName"
              :value="item.typeName"
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
        <el-button type="primary" icon="Plus" @click="handleAdd">新增供应商</el-button>
        <el-button type="success" icon="Download" @click="handleExport">导出</el-button>
      </div>

      <el-table v-loading="loading" :data="dataList" border stripe>
  <el-table-column type="index" label="序号" width="60" align="center" />
  <el-table-column label="编码" prop="supplierCode" width="120" />
  <el-table-column label="名称" prop="name" min-width="150" />
  <el-table-column label="产品类型" prop="productType" width="120" />
  <el-table-column label="联系人" prop="contactPerson" width="120" />
  <el-table-column label="电话" prop="phone" width="150" />
  <el-table-column label="地址" prop="address" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="warning" @click="handleAccount(scope.row)">账户</el-button>
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
        <el-form-item label="供应商编码" prop="supplierCode">
          <el-input v-model="form.supplierCode" placeholder="输入供应商编码" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="输入名称" />
        </el-form-item>
        <el-form-item label="产品类型" prop="productType">
          <el-select
            v-model="form.productTypeArr"
            filterable
            allow-create
            clearable
            multiple
            collapse-tags
            placeholder="输入或选择产品类型"
          >
            <el-option
              v-for="item in productTypeList"
              :key="item.id"
              :label="item.typeName"
              :value="item.typeName"
            />
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

    <!-- Account Dialog -->
    <el-dialog v-model="accountDialogVisible" :title="accountDialogTitle" width="800px" destroy-on-close @close="resetAccountDialog">
      <div class="account-toolbar">
        <el-button type="primary" icon="Plus" size="small" @click="handleAccountAdd">新增账户</el-button>
      </div>
      <el-table :data="accountList" border stripe size="small" v-loading="accountLoading">
        <el-table-column type="index" label="序号" width="55" align="center" />
        <el-table-column label="开户银行" prop="bankName" min-width="130" />
        <el-table-column label="账户名称" prop="accountName" min-width="120" />
        <el-table-column label="银行账号" prop="accountNo" min-width="160" />
        <el-table-column label="币种" prop="currency" width="80" />
        <el-table-column label="SWIFT码" prop="swiftCode" min-width="120" />
        <el-table-column label="备注" prop="remark" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="130" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="handleAccountEdit(scope.row)">编辑</el-button>
            <el-button link type="danger" @click="handleAccountDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- Account Add/Edit Dialog -->
    <el-dialog v-model="accountFormVisible" :title="accountFormTitle" width="480px" destroy-on-close @close="resetAccountForm">
      <el-form ref="accountFormRef" :model="accountForm" :rules="accountRules" label-width="100px">
        <el-form-item label="开户银行" prop="bankName">
          <el-input v-model="accountForm.bankName" placeholder="输入开户银行" />
        </el-form-item>
        <el-form-item label="账户名称" prop="accountName">
          <el-input v-model="accountForm.accountName" placeholder="输入账户名称" />
        </el-form-item>
        <el-form-item label="银行账号" prop="accountNo">
          <el-input v-model="accountForm.accountNo" placeholder="输入银行账号" />
        </el-form-item>
        <el-form-item label="币种" prop="currency">
          <el-select v-model="accountForm.currency" style="width:100%">
            <el-option label="CNY - 人民币" value="CNY" />
            <el-option label="USD - 美元" value="USD" />
            <el-option label="EUR - 欧元" value="EUR" />
            <el-option label="HKD - 港元" value="HKD" />
            <el-option label="GBP - 英镑" value="GBP" />
          </el-select>
        </el-form-item>
        <el-form-item label="SWIFT码">
          <el-input v-model="accountForm.swiftCode" placeholder="输入SWIFT码（可选）" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="accountForm.remark" placeholder="备注（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="accountFormVisible = false">取消</el-button>
        <el-button type="primary" :loading="accountSubmitLoading" @click="handleAccountSubmit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { exportToCsv } from '@/utils/export'
import { getSupplierPage, saveSupplier, updateSupplier, deleteSupplier } from '@/api/supplier'
import { getSupplierAccountList, saveSupplierAccount, updateSupplierAccount, deleteSupplierAccount } from '@/api/supplierAccount'
import { getProductTypeList } from '@/api/productType'

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
  supplierCode: '',
  name: '',
  productType: ''
})

const form = reactive<any>({
  id: null,
  supplierCode: '',
  name: '',
  productType: '', // 用于提交和回显
  productTypeArr: [], // 用于多选控件
  contactPerson: '',
  phone: '',
  address: ''
})

const rules = {
  supplierCode: [{ required: true, message: '请输入供应商编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getSupplierPage(queryParams)
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
  queryParams.supplierCode = ''
  queryParams.name = ''
  handleQuery()
}

const resetForm = () => {
  form.id = null
  form.supplierCode = ''
  form.name = ''
  form.productType = ''
  form.productTypeArr = []
  form.contactPerson = ''
  form.phone = ''
  form.address = ''
  formRef.value?.clearValidate()
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增供应商'
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  resetForm()
  Object.assign(form, {
    id: row.id,
    supplierCode: row.supplierCode,
    name: row.name,
    productType: row.productType,
    productTypeArr: row.productType ? row.productType.split(',') : [],
    contactPerson: row.contactPerson,
    phone: row.phone,
    address: row.address
  })
  dialogTitle.value = '编辑供应商'
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定要删除供应商 "${row.name}" 吗?`, '警告', { type: 'warning' }).then(async () => {
    await deleteSupplier(row.id)
    ElMessage.success('删除成功')
    getList()
  })
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
  } catch (err: any) {
    // Element Plus 校验失败时会抛出错误对象，直接友好提示
    if (err && err.length) {
      const first = err[0]
      ElMessage.warning(first.message || '请完善必填项')
    } else {
      ElMessage.warning('请完善必填项')
    }
    return
  }
  // 多选转字符串
  form.productType = (form.productTypeArr || []).join(',')
  submitLoading.value = true
  try {
    if (form.id) {
      await updateSupplier({ ...form })
    } else {
      const payload = { ...form }
      delete payload.id
      await saveSupplier(payload)
    }
    ElMessage.success(form.id ? '更新成功' : '添加成功')
    dialogVisible.value = false
    getList()
  } finally {
    submitLoading.value = false
  }
}

const handleExport = async () => {
  const res = await getSupplierPage({ ...queryParams, pageNum: 1, pageSize: 10000 })
  const rows = res.data.list || []
  exportToCsv('供应商管理导出', rows, [
    { label: '编码', key: 'supplierCode' },
    { label: '名称', key: 'name' },
    { label: '产品类型', key: 'productType' },
    { label: '联系人', key: 'contactPerson' },
    { label: '电话', key: 'phone' },
    { label: '地址', key: 'address' }
  ])
}

const productTypeList = ref<any[]>([])
const loadProductTypeList = async () => {
  const res = await getProductTypeList()
  productTypeList.value = res.data || []
}
onMounted(() => {
  loadProductTypeList()
  getList()
})

// ===== 账户管理 =====
const accountDialogVisible = ref(false)
const accountDialogTitle = ref('')
const accountLoading = ref(false)
const accountList = ref<any[]>([])
const currentSupplierId = ref<number | null>(null)

const accountFormVisible = ref(false)
const accountFormTitle = ref('')
const accountSubmitLoading = ref(false)
const accountFormRef = ref<FormInstance>()

const accountForm = reactive<any>({
  id: null,
  supplierId: null,
  bankName: '',
  accountName: '',
  accountNo: '',
  currency: 'CNY',
  swiftCode: '',
  remark: ''
})

const accountRules = {
  bankName: [{ required: true, message: '请输入开户银行', trigger: 'blur' }],
  accountName: [{ required: true, message: '请输入账户名称', trigger: 'blur' }],
  accountNo: [{ required: true, message: '请输入银行账号', trigger: 'blur' }],
  currency: [{ required: true, message: '请选择币种', trigger: 'change' }]
}

const loadAccountList = async () => {
  if (!currentSupplierId.value) return
  accountLoading.value = true
  try {
    const res = await getSupplierAccountList(currentSupplierId.value)
    accountList.value = res.data || []
  } finally {
    accountLoading.value = false
  }
}

const handleAccount = async (row: any) => {
  if (!row.id) {
    ElMessage.error('供应商无效')
    return
  }
  currentSupplierId.value = row.id
  accountDialogTitle.value = `账户管理 - ${row.name}`
  accountDialogVisible.value = true
  await loadAccountList()
}

const resetAccountDialog = () => {
  accountList.value = []
  currentSupplierId.value = null
}

const resetAccountForm = () => {
  accountForm.id = null
  accountForm.supplierId = null
  accountForm.bankName = ''
  accountForm.accountName = ''
  accountForm.accountNo = ''
  accountForm.currency = 'CNY'
  accountForm.swiftCode = ''
  accountForm.remark = ''
  accountFormRef.value?.clearValidate()
}

const handleAccountAdd = () => {
  resetAccountForm()
  accountForm.supplierId = currentSupplierId.value
  accountFormTitle.value = '新增账户'
  accountFormVisible.value = true
}

const handleAccountEdit = (row: any) => {
  resetAccountForm()
  Object.assign(accountForm, { ...row })
  accountFormTitle.value = '编辑账户'
  accountFormVisible.value = true
}

const handleAccountDelete = (row: any) => {
  ElMessageBox.confirm(`确定要删除该账户信息吗？`, '警告', { type: 'warning' }).then(async () => {
    await deleteSupplierAccount(row.id)
    ElMessage.success('删除成功')
    loadAccountList()
  })
}

const handleAccountSubmit = async () => {
  await accountFormRef.value?.validate()
  accountSubmitLoading.value = true
  try {
    if (accountForm.id) {
      await updateSupplierAccount({ ...accountForm })
    } else {
      const payload = { ...accountForm }
      delete payload.id
      await saveSupplierAccount(payload)
    }
    ElMessage.success(accountForm.id ? '更新成功' : '添加成功')
    accountFormVisible.value = false
    loadAccountList()
  } finally {
    accountSubmitLoading.value = false
  }
}
</script>

<style scoped>
.app-container { padding: 0; }
.search-wrap { margin-bottom: 16px; }
.table-toolbar { margin-bottom: 16px; }
.pagination-container { margin-top: 16px; display: flex; justify-content: flex-end; }
.account-toolbar { margin-bottom: 12px; }
</style>
