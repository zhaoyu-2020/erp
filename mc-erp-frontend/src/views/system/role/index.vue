<template>
  <div class="app-container">
    <el-card shadow="never" class="search-wrap">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="角色编码">
          <el-input v-model="queryParams.roleCode" placeholder="输入角色编码" clearable />
        </el-form-item>
        <el-form-item label="角色名称">
          <el-input v-model="queryParams.roleName" placeholder="输入角色名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-wrap">
      <div class="table-toolbar">
        <el-button type="primary" icon="Plus" @click="handleAdd">新增角色</el-button>
        <el-button type="success" icon="Download" @click="handleExport">导出</el-button>
      </div>

      <el-table v-loading="loading" :data="dataList" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="角色编码" prop="roleCode" width="150" />
        <el-table-column label="角色名称" prop="roleName" width="150" />
        <el-table-column label="描述" prop="description" min-width="200" show-overflow-tooltip />
        <el-table-column label="创建时间" prop="createTime" width="180" />
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="warning" @click="handleAssignMenus(scope.row)">分配权限</el-button>
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

    <!-- 新增/编辑 Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="输入角色编码" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="输入角色名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限 Dialog -->
    <el-dialog
      v-model="permDialogVisible"
      :title="`分配权限 — ${currentRoleName}`"
      width="480px"
      @open="loadPermTree"
    >
      <div v-loading="permLoading" class="perm-tree-wrap">
        <div class="perm-tree-actions">
          <el-button size="small" @click="checkAll">全选</el-button>
          <el-button size="small" @click="uncheckAll">清空</el-button>
        </div>
        <el-tree
          ref="menuTreeRef"
          :data="menuTreeData"
          show-checkbox
          node-key="id"
          :default-checked-keys="checkedMenuIds"
          :default-expand-all="true"
          :props="treeProps"
        >
          <template #default="{ node, data }">
            <span class="tree-node-label">
              {{ node.label }}
              <el-tag v-if="data.permission" size="small" type="info" class="perm-tag">
                {{ data.permission }}
              </el-tag>
            </span>
          </template>
        </el-tree>
      </div>
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="permSaving" @click="handleSaveMenus">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import type { ElTree } from 'element-plus'
import {
  getRolePage, saveRole, updateRole, deleteRole,
  getMenuTree, getRoleMenuIds, updateRoleMenus
} from '@/api/system'
import { exportToCsv } from '@/utils/export'

// ── 列表 ─────────────────────────────────────────────
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
  roleCode: '',
  roleName: ''
})

const form = reactive<any>({
  id: null,
  roleCode: '',
  roleName: '',
  description: ''
})

const rules = {
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getRolePage(queryParams)
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
  queryParams.roleCode = ''
  queryParams.roleName = ''
  handleQuery()
}

const resetForm = () => {
  form.id = null
  form.roleCode = ''
  form.roleName = ''
  form.description = ''
  formRef.value?.clearValidate()
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增角色'
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  resetForm()
  Object.assign(form, { id: row.id, roleCode: row.roleCode, roleName: row.roleName, description: row.description })
  dialogTitle.value = '编辑角色'
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定要删除角色 "${row.roleName}" 吗?`, '警告', {
    type: 'warning'
  }).then(async () => {
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    getList()
  })
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (form.id) {
      await updateRole({ id: form.id, roleCode: form.roleCode, roleName: form.roleName, description: form.description })
    } else {
      await saveRole({ roleCode: form.roleCode, roleName: form.roleName, description: form.description })
    }
    ElMessage.success(form.id ? '更新成功' : '添加成功')
    dialogVisible.value = false
    getList()
  } finally {
    submitLoading.value = false
  }
}

const handleExport = async () => {
  const res = await getRolePage({ ...queryParams, pageNum: 1, pageSize: 10000 })
  const rows = res.data.list || []
  exportToCsv('角色管理导出', rows, [
    { label: '角色编码', key: 'roleCode' },
    { label: '角色名称', key: 'roleName' },
    { label: '描述', key: 'description' },
    { label: '创建时间', key: 'createTime' }
  ])
}

// ── 分配权限 ──────────────────────────────────────────
const permDialogVisible = ref(false)
const permLoading = ref(false)
const permSaving = ref(false)
const currentRoleId = ref<number | null>(null)
const currentRoleName = ref('')
const menuTreeData = ref<any[]>([])
const checkedMenuIds = ref<number[]>([])
const menuTreeRef = ref<InstanceType<typeof ElTree>>()

const treeProps = { label: 'menuName', children: 'children' }

/** 将后端返回的平铺菜单列表转为树结构 */
function buildMenuTree(flatList: any[]): any[] {
  const map = new Map<number, any>()
  flatList.forEach(item => map.set(item.id, { ...item, children: [] }))
  const roots: any[] = []
  flatList.forEach(item => {
    const node = map.get(item.id)!
    if (item.parentId && map.has(item.parentId)) {
      map.get(item.parentId)!.children.push(node)
    } else {
      roots.push(node)
    }
  })
  return roots
}

const handleAssignMenus = (row: any) => {
  currentRoleId.value = row.id
  currentRoleName.value = row.roleName
  checkedMenuIds.value = []
  menuTreeData.value = []
  permDialogVisible.value = true
}

/** 弹窗打开后并行加载菜单树 + 该角色已勾选的菜单 */
const loadPermTree = async () => {
  if (!currentRoleId.value) return
  permLoading.value = true
  try {
    const [treeRes, checkedRes] = await Promise.all([
      getMenuTree(),
      getRoleMenuIds(currentRoleId.value)
    ])
    menuTreeData.value = buildMenuTree(treeRes.data || [])
    checkedMenuIds.value = checkedRes.data || []
  } finally {
    permLoading.value = false
  }
}

const checkAll = () => {
  menuTreeRef.value?.setCheckedNodes(getAllNodes(menuTreeData.value))
}

const uncheckAll = () => {
  menuTreeRef.value?.setCheckedKeys([])
}

function getAllNodes(nodes: any[]): any[] {
  const result: any[] = []
  nodes.forEach(n => {
    result.push(n)
    if (n.children?.length) result.push(...getAllNodes(n.children))
  })
  return result
}

const handleSaveMenus = async () => {
  if (!currentRoleId.value) return
  permSaving.value = true
  try {
    const checkedKeys = (menuTreeRef.value?.getCheckedKeys() as number[]) || []
    const halfCheckedKeys = (menuTreeRef.value?.getHalfCheckedKeys() as number[]) || []
    // 全选节点 + 半选父节点一并保存，确保父目录页面也被记录
    const allKeys = [...new Set([...checkedKeys, ...halfCheckedKeys])]
    await updateRoleMenus(currentRoleId.value, allKeys)
    ElMessage.success('权限保存成功')
    permDialogVisible.value = false
  } finally {
    permSaving.value = false
  }
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
.perm-tree-wrap { max-height: 480px; overflow-y: auto; }
.perm-tree-actions { margin-bottom: 8px; }
.tree-node-label { display: flex; align-items: center; gap: 6px; }
.perm-tag { font-size: 11px; font-family: monospace; }
</style>
