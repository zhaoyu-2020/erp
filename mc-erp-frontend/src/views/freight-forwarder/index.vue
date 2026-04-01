<template>
  <div class="mc-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">货代管理</h2>
      </div>
      <div class="page-header-right">
        <el-button type="success" icon="Download" @click="handleExport">导出</el-button>
        <el-button type="primary" icon="Plus" @click="handleAdd">新增货代</el-button>
      </div>
    </div>

    <!-- 搜索过滤区域 -->
    <div class="filter-bar">
      <div class="filter-inputs">
        <el-input v-model="queryParams.forwarderCode" placeholder="货代编码" clearable class="filter-input" @clear="handleQuery" @keyup.enter="handleQuery" />
        <el-input v-model="queryParams.name" placeholder="名称" clearable class="filter-input" @clear="handleQuery" @keyup.enter="handleQuery" />
        <el-select v-model="queryParams.freightType" clearable placeholder="货代类型" class="filter-input-sm" @change="handleQuery">
          <el-option label="集装箱" value="集装箱" />
          <el-option label="散货" value="散货" />
        </el-select>
        <el-select v-model="queryParams.marketAdvantage" clearable placeholder="优势市场" class="filter-input" @change="handleQuery">
          <el-option v-for="item in marketOptions" :key="item" :label="item" :value="item" />
        </el-select>
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
        <el-table-column label="编码" prop="forwarderCode" width="140" />
        <el-table-column label="名称" prop="name" min-width="150" />
        <el-table-column label="货代类型" prop="freightType" width="120" />
        <el-table-column label="优势市场" prop="marketAdvantage" min-width="220" show-overflow-tooltip />
        <el-table-column label="联系人" prop="contactPerson" width="120" />
        <el-table-column label="电话" prop="phone" width="150" />
        <el-table-column label="地址" prop="address" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="scope">
            <div class="action-btns">
              <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button link type="warning" @click="handleAccount(scope.row)">账户</el-button>
              <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
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
import {
  getFreightForwarderPage,
  saveFreightForwarder,
  updateFreightForwarder,
  deleteFreightForwarder
} from '@/api/freightForwarder'
import { getForwarderAccountList, saveForwarderAccount, updateForwarderAccount, deleteForwarderAccount } from '@/api/forwarderAccount'

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
  pageSize: 20,
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

// ===== 账户管理 =====
const accountDialogVisible = ref(false)
const accountDialogTitle = ref('')
const accountLoading = ref(false)
const accountList = ref<any[]>([])
const currentForwarderId = ref<number | null>(null)

const accountFormVisible = ref(false)
const accountFormTitle = ref('')
const accountSubmitLoading = ref(false)
const accountFormRef = ref<FormInstance>()

const accountForm = reactive<any>({
  id: null,
  forwarderId: null,
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
  if (!currentForwarderId.value) return
  accountLoading.value = true
  try {
    const res = await getForwarderAccountList(currentForwarderId.value)
    accountList.value = res.data || []
  } finally {
    accountLoading.value = false
  }
}

const handleAccount = async (row: any) => {
  if (!row.id) {
    ElMessage.error('货代无效')
    return
  }
  currentForwarderId.value = row.id
  accountDialogTitle.value = `账户管理 - ${row.name}`
  accountDialogVisible.value = true
  await loadAccountList()
}

const resetAccountDialog = () => {
  accountList.value = []
  currentForwarderId.value = null
}

const resetAccountForm = () => {
  accountForm.id = null
  accountForm.forwarderId = null
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
  accountForm.forwarderId = currentForwarderId.value
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
    await deleteForwarderAccount(row.id)
    ElMessage.success('删除成功')
    loadAccountList()
  })
}

const handleAccountSubmit = async () => {
  await accountFormRef.value?.validate()
  accountSubmitLoading.value = true
  try {
    if (accountForm.id) {
      await updateForwarderAccount({ ...accountForm })
    } else {
      const payload = { ...accountForm }
      delete payload.id
      await saveForwarderAccount(payload)
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
.account-toolbar { margin-bottom: 12px; }
</style>
