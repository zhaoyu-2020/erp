<template>
  <div class="app-container">
    <el-card shadow="never" class="table-wrap">
      <div class="table-toolbar">
        <el-button type="primary" icon="Plus" @click="handleAdd(null)">新增菜单</el-button>
        <el-button icon="Sort" @click="toggleExpandAll">展开/折叠</el-button>
        <el-button type="success" icon="Download" @click="handleExport">导出</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="menuList"
        row-key="id"
        border
        :default-expand-all="expandAll"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      >
        <el-table-column prop="menuName" label="菜单名称" min-width="200" />
        <el-table-column prop="icon" label="图标" align="center" width="80">
          <template #default="{ row }">
            <el-icon v-if="row.icon"><component :is="row.icon" /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.type === 1">目录</el-tag>
            <el-tag type="success" v-else-if="row.type === 2">菜单</el-tag>
            <el-tag type="info" v-else-if="row.type === 3">按钮</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由地址" min-width="150" show-overflow-tooltip />
        <el-table-column prop="component" label="组件路径" min-width="150" show-overflow-tooltip />
        <el-table-column prop="permission" label="权限标识" min-width="150" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="70" align="center" />
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="handleAdd(scope.row)">新增下级</el-button>
            <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="form.parentId"
            :data="menuTreeOptions"
            :props="{ label: 'menuName', value: 'id', children: 'children' }"
            placeholder="选择上级菜单 (不选则为顶级)"
            clearable
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" placeholder="输入菜单名称" />
        </el-form-item>
        <el-form-item label="菜单类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :label="1">目录</el-radio>
            <el-radio :label="2">菜单</el-radio>
            <el-radio :label="3">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="Element Plus 图标名称" />
        </el-form-item>
        <el-form-item label="路由地址" v-if="form.type !== 3">
          <el-input v-model="form.path" placeholder="路由路径, 例如: /system/user" />
        </el-form-item>
        <el-form-item label="组件路径" v-if="form.type === 2">
          <el-input v-model="form.component" placeholder="组件路径, 例如: system/user/index" />
        </el-form-item>
        <el-form-item label="权限标识" v-if="form.type === 3">
          <el-input v-model="form.permission" placeholder="权限标识, 例如: sys:user:add" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { getMenuTree, saveMenu, updateMenu, deleteMenu } from '@/api/system'
import { exportToCsv } from '@/utils/export'

const loading = ref(false)
const submitLoading = ref(false)
const menuList = ref<any[]>([])
const menuTreeOptions = ref<any[]>([])
const expandAll = ref(true)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()

const form = ref<any>({
  id: null,
  parentId: null,
  menuName: '',
  type: 1,
  icon: '',
  path: '',
  component: '',
  permission: '',
  sort: 0
})

const rules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择菜单类型', trigger: 'change' }]
}

/** Build tree from flat list */
const buildTree = (list: any[]): any[] => {
  const map: Record<number, any> = {}
  const roots: any[] = []
  list.forEach(item => { map[item.id] = { ...item, children: [] } })
  list.forEach(item => {
    if (item.parentId && map[item.parentId]) {
      map[item.parentId].children.push(map[item.id])
    } else {
      roots.push(map[item.id])
    }
  })
  return roots
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getMenuTree()
    // Backend returns flat list; build tree on frontend
    const flat: any[] = res.data || []
    menuList.value = buildTree(flat)
    menuTreeOptions.value = buildTree(flat)
  } finally {
    loading.value = false
  }
}

const toggleExpandAll = () => {
  expandAll.value = !expandAll.value
}

const resetForm = () => {
  form.value = { id: null, parentId: null, menuName: '', type: 1, icon: '', path: '', component: '', permission: '', sort: 0 }
  formRef.value?.clearValidate()
}

const handleAdd = (parent: any) => {
  resetForm()
  if (parent) form.value.parentId = parent.id
  dialogTitle.value = parent ? `新增下级菜单 ("${parent.menuName}")` : '新增菜单'
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  resetForm()
  form.value = { ...row, children: undefined }
  dialogTitle.value = '编辑菜单'
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定要删除菜单 "${row.menuName}" 吗? 包含的下级菜单也会受到影响。`, '警告', {
    type: 'warning'
  }).then(async () => {
    await deleteMenu(row.id)
    ElMessage.success('删除成功')
    getList()
  })
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    const payload = { ...form.value, children: undefined }
    if (payload.id) {
      await updateMenu(payload)
    } else {
      await saveMenu(payload)
    }
    ElMessage.success(payload.id ? '更新成功' : '添加成功')
    dialogVisible.value = false
    getList()
  } finally {
    submitLoading.value = false
  }
}

const flattenTree = (nodes: any[], out: any[] = [], parentName = '') => {
  nodes.forEach(n => {
    out.push({
      id: n.id,
      parentId: n.parentId,
      parentName,
      menuName: n.menuName,
      type: n.type,
      path: n.path,
      component: n.component,
      permission: n.permission,
      sort: n.sort,
      icon: n.icon
    })
    if (n.children && n.children.length) flattenTree(n.children, out, n.menuName)
  })
  return out
}

const handleExport = () => {
  const rows = flattenTree(menuList.value || [])
  exportToCsv('菜单管理导出', rows, [
    { label: 'ID', key: 'id' },
    { label: '上级ID', key: 'parentId' },
    { label: '上级名称', key: 'parentName' },
    { label: '菜单名称', key: 'menuName' },
    { label: '类型', value: (r: any) => (r.type === 1 ? '目录' : r.type === 2 ? '菜单' : '按钮') },
    { label: '路由地址', key: 'path' },
    { label: '组件路径', key: 'component' },
    { label: '权限标识', key: 'permission' },
    { label: '排序', key: 'sort' },
    { label: '图标', key: 'icon' }
  ])
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.app-container { padding: 0; }
.table-toolbar { margin-bottom: 16px; }
</style>
