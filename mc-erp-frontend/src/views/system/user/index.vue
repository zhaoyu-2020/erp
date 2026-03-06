<template>
  <div class="app-container">
    <el-card shadow="never" class="search-wrap">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="用户名">
          <el-input v-model="queryParams.username" placeholder="输入用户名" clearable />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="queryParams.realName" placeholder="输入真实姓名" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="queryParams.phone" placeholder="输入手机号" clearable />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="queryParams.email" placeholder="输入邮箱" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-wrap">
      <div class="table-toolbar">
        <el-button type="primary" icon="Plus" @click="handleAdd">新增用户</el-button>
        <el-button type="success" icon="Download" @click="handleExport">导出</el-button>
      </div>

      <el-table v-loading="loading" :data="dataList" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="用户名" prop="username" min-width="120" />
        <el-table-column label="真实姓名" prop="realName" min-width="120" />
        <el-table-column label="手机号" prop="phone" min-width="120" />
        <el-table-column label="邮箱" prop="email" min-width="180" />
        <el-table-column label="角色" min-width="180">
          <template #default="{ row }">
            <el-tag
              v-for="rid in (row.roleIds || [])"
              :key="rid"
              style="margin-right: 6px"
            >
              {{ roleNameMap[rid] || rid }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="180" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="primary" @click="handleAssignRoles(scope.row)">分配角色</el-button>
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
        @size-change="handleQuery"
      />
    </el-card>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="输入用户名" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item v-if="!form.id" label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="输入密码" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="输入邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>

    <!-- Assign Roles Dialog -->
    <el-dialog v-model="roleDialogVisible" title="分配角色" width="520px" @close="resetRoleDialog">
      <el-form label-width="90px">
        <el-form-item label="用户">
          <el-input :model-value="currentUserName" disabled />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="selectedRoleIds" multiple filterable placeholder="选择角色" style="width: 100%">
            <el-option v-for="r in roleOptions" :key="r.id" :label="r.roleName" :value="r.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="roleSubmitLoading" @click="submitAssignRoles">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { getUserPage, saveUser, updateUser, deleteUser, getRoleList, getUserRoleIds, updateUserRoles } from '@/api/system'
import { exportToCsv } from '@/utils/export'

const loading = ref(false)
const submitLoading = ref(false)
const dataList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const roleDialogVisible = ref(false)
const roleSubmitLoading = ref(false)
const currentUserId = ref<number | null>(null)
const currentUserName = ref('')
const roleOptions = ref<any[]>([])
const roleNameMap = reactive<Record<number, string>>({})
const selectedRoleIds = ref<number[]>([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  username: '',
  realName: '',
  phone: '',
  email: ''
})

const form = reactive<any>({
  id: null,
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getUserPage(queryParams)
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
  queryParams.username = ''
  queryParams.realName = ''
  queryParams.phone = ''
  queryParams.email = ''
  handleQuery()
}

const resetForm = () => {
  form.id = null
  form.username = ''
  form.password = ''
  form.realName = ''
  form.phone = ''
  form.email = ''
  formRef.value?.clearValidate()
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增用户'
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  resetForm()
  Object.assign(form, { id: row.id, username: row.username, realName: row.realName, phone: row.phone, email: row.email })
  dialogTitle.value = '编辑用户'
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定要删除用户 "${row.username}" 吗?`, '警告', {
    type: 'warning'
  }).then(async () => {
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    getList()
  })
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (form.id) {
      await updateUser({ id: form.id, realName: form.realName, phone: form.phone, email: form.email })
    } else {
      await saveUser({ username: form.username, password: form.password, realName: form.realName, phone: form.phone, email: form.email })
    }
    ElMessage.success(form.id ? '更新成功' : '添加成功')
    dialogVisible.value = false
    getList()
  } finally {
    submitLoading.value = false
  }
}

const handleExport = async () => {
  const res = await getUserPage({ ...queryParams, pageNum: 1, pageSize: 10000 })
  const rows = res.data.list || []
  exportToCsv('用户管理导出', rows, [
    { label: '用户名', key: 'username' },
    { label: '真实姓名', key: 'realName' },
    { label: '手机号', key: 'phone' },
    { label: '邮箱', key: 'email' },
    { label: '创建时间', key: 'createTime' }
  ])
}

const loadRoleOptions = async () => {
  const res = await getRoleList()
  roleOptions.value = res.data || []
  ;(res.data || []).forEach((r: any) => {
    roleNameMap[r.id] = r.roleName
  })
}

const resetRoleDialog = () => {
  currentUserId.value = null
  currentUserName.value = ''
  selectedRoleIds.value = []
}

const handleAssignRoles = async (row: any) => {
  if (!roleOptions.value.length) await loadRoleOptions()
  currentUserId.value = row.id
  currentUserName.value = row.username
  const res = await getUserRoleIds(row.id)
  selectedRoleIds.value = (res.data || []) as number[]
  roleDialogVisible.value = true
}

const submitAssignRoles = async () => {
  if (!currentUserId.value) return
  roleSubmitLoading.value = true
  try {
    await updateUserRoles(currentUserId.value, selectedRoleIds.value)
    ElMessage.success('保存成功')
    roleDialogVisible.value = false
    getList()
  } finally {
    roleSubmitLoading.value = false
  }
}

onMounted(() => {
  getList()
  loadRoleOptions()
})
</script>

<style scoped>
.app-container { padding: 0; }
.search-wrap { margin-bottom: 16px; }
.table-toolbar { margin-bottom: 16px; }
.pagination-container { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
