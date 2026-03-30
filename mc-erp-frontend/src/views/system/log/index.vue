<template>
  <div class="log-container">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="queryForm" inline>
        <el-form-item label="日志类型">
          <el-select v-model="queryForm.logType" placeholder="全部" clearable style="width: 130px">
            <el-option label="操作日志" value="OPERATION" />
            <el-option label="登录日志" value="LOGIN" />
            <el-option label="异常日志" value="EXCEPTION" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作模块">
          <el-input v-model="queryForm.module" placeholder="请输入模块名" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="queryForm.operationType" placeholder="全部" clearable style="width: 130px">
            <el-option label="登录" value="LOGIN" />
            <el-option label="退出" value="LOGOUT" />
            <el-option label="新增" value="ADD" />
            <el-option label="修改" value="MODIFY" />
            <el-option label="删除" value="DELETE" />
            <el-option label="查询" value="QUERY" />
            <el-option label="导出" value="EXPORT" />
            <el-option label="导入" value="IMPORT" />
            <el-option label="权限变更" value="AUTH_CHANGE" />
            <el-option label="状态变更" value="STATUS_CHANGE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 100px">
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作用户">
          <el-input v-model="queryForm.operatorName" placeholder="请输入用户名" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="操作时间">
          <el-date-picker
            v-model="timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 360px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
          <el-button type="success" @click="handleExport">
            <el-icon><Download /></el-icon> 导出
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表区域 -->
    <el-card shadow="never" style="margin-top: 12px">
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        border
        style="width: 100%"
        @row-click="handleRowClick"
        highlight-current-row
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="logType" label="日志类型" width="100">
          <template #default="{ row }">
            <el-tag :type="logTypeTag(row.logType)" size="small">{{ logTypeLabel(row.logType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="操作模块" width="120" />
        <el-table-column prop="operationType" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="opTypeTag(row.operationType)" size="small" effect="plain">{{ opTypeLabel(row.operationType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="操作描述" min-width="160" show-overflow-tooltip />
        <el-table-column prop="requestMethod" label="请求方法" width="90">
          <template #default="{ row }">
            <el-tag :type="methodTag(row.requestMethod)" size="small" effect="plain">{{ row.requestMethod }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requestUrl" label="请求URL" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '成功' : '失败' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作用户" width="100" />
        <el-table-column prop="operatorIp" label="操作IP" width="140" />
        <el-table-column prop="elapsedTime" label="耗时(ms)" width="100" align="right" />
        <el-table-column prop="createTime" label="操作时间" width="180" />
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryForm.pageNum"
          v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </el-card>

    <!-- 日志详情弹窗 -->
    <el-dialog v-model="detailVisible" title="日志详情" width="700px" destroy-on-close>
      <el-descriptions :column="2" border v-if="currentLog">
        <el-descriptions-item label="日志ID">{{ currentLog.id }}</el-descriptions-item>
        <el-descriptions-item label="日志类型">
          <el-tag :type="logTypeTag(currentLog.logType)" size="small">{{ logTypeLabel(currentLog.logType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作模块">{{ currentLog.module }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ opTypeLabel(currentLog.operationType) }}</el-descriptions-item>
        <el-descriptions-item label="操作描述" :span="2">{{ currentLog.description }}</el-descriptions-item>
        <el-descriptions-item label="请求方法">{{ currentLog.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="请求URL">{{ currentLog.requestUrl }}</el-descriptions-item>
        <el-descriptions-item label="操作状态">
          <el-tag :type="currentLog.status === 1 ? 'success' : 'danger'" size="small">{{ currentLog.status === 1 ? '成功' : '失败' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="耗时(ms)">{{ currentLog.elapsedTime }}</el-descriptions-item>
        <el-descriptions-item label="操作用户">{{ currentLog.operatorName }}</el-descriptions-item>
        <el-descriptions-item label="操作IP">{{ currentLog.operatorIp }}</el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ currentLog.createTime }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2" v-if="currentLog.requestParams">
          <el-input type="textarea" :model-value="formatJson(currentLog.requestParams)" :rows="4" readonly />
        </el-descriptions-item>
        <el-descriptions-item label="响应结果" :span="2" v-if="currentLog.responseResult">
          <el-input type="textarea" :model-value="formatJson(currentLog.responseResult)" :rows="4" readonly />
        </el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2" v-if="currentLog.errorMsg">
          <el-input type="textarea" :model-value="currentLog.errorMsg" :rows="4" readonly style="color: #f56c6c" />
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getOperationLogPage, getOperationLogDetail, exportOperationLogs } from '@/api/operationLog'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const detailVisible = ref(false)
const currentLog = ref<any>(null)
const timeRange = ref<string[]>([])

const queryForm = reactive({
  logType: '',
  module: '',
  operationType: '',
  status: undefined as number | undefined,
  operatorName: '',
  operatorIp: '',
  startTime: '',
  endTime: '',
  pageNum: 1,
  pageSize: 20
})

// ---- 查询 ----
const handleSearch = async () => {
  loading.value = true
  // 时间范围
  if (timeRange.value && timeRange.value.length === 2) {
    queryForm.startTime = timeRange.value[0] ?? ''
    queryForm.endTime = timeRange.value[1] ?? ''
  } else {
    queryForm.startTime = ''
    queryForm.endTime = ''
  }
  try {
    const res: any = await getOperationLogPage(queryForm)
    tableData.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryForm.logType = ''
  queryForm.module = ''
  queryForm.operationType = ''
  queryForm.status = undefined
  queryForm.operatorName = ''
  queryForm.operatorIp = ''
  queryForm.startTime = ''
  queryForm.endTime = ''
  queryForm.pageNum = 1
  timeRange.value = []
  handleSearch()
}

// ---- 详情 ----
const handleRowClick = async (row: any) => {
  const res: any = await getOperationLogDetail(row.id)
  currentLog.value = res.data
  detailVisible.value = true
}

// ---- 导出 ----
const handleExport = async () => {
  if (timeRange.value && timeRange.value.length === 2) {
    queryForm.startTime = timeRange.value[0] ?? ''
    queryForm.endTime = timeRange.value[1] ?? ''
  }
  try {
    const res: any = await exportOperationLogs(queryForm)
    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = '操作日志.xlsx'
    a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  }
}

// ---- 工具函数 ----
const logTypeLabel = (type: string) => {
  const map: Record<string, string> = { OPERATION: '操作日志', LOGIN: '登录日志', EXCEPTION: '异常日志' }
  return map[type] || type
}
const logTypeTag = (type: string) => {
  const map: Record<string, string> = { OPERATION: '', LOGIN: 'success', EXCEPTION: 'danger' }
  return map[type] || 'info'
}

const opTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    LOGIN: '登录', LOGOUT: '退出', ADD: '新增', MODIFY: '修改', DELETE: '删除',
    QUERY: '查询', EXPORT: '导出', IMPORT: '导入', AUTH_CHANGE: '权限变更',
    STATUS_CHANGE: '状态变更', OTHER: '其他'
  }
  return map[type] || type
}
const opTypeTag = (type: string) => {
  const map: Record<string, string> = {
    LOGIN: 'success', LOGOUT: 'info', ADD: 'success', MODIFY: 'warning',
    DELETE: 'danger', QUERY: 'info', EXPORT: '', IMPORT: '',
    AUTH_CHANGE: 'warning', STATUS_CHANGE: 'warning', OTHER: 'info'
  }
  return map[type] || 'info'
}

const methodTag = (method: string) => {
  const map: Record<string, string> = {
    GET: 'info', POST: 'success', PUT: 'warning', PATCH: 'warning', DELETE: 'danger'
  }
  return map[method] || 'info'
}

const formatJson = (str: string) => {
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch {
    return str
  }
}

onMounted(() => {
  handleSearch()
})
</script>

<style scoped>
.log-container {
  padding: 16px;
}
.search-card {
  margin-bottom: 0;
}
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
