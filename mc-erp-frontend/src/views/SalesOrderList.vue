<template>
  <div class="mc-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">销售订单</h2>
      </div>
      <div class="page-header-right">
        <el-button type="warning" :icon="Upload" @click="triggerContractUpload">上传销售合同</el-button>
        <el-button type="warning" :icon="Upload" @click="triggerDetailsUpload">上传销售明细</el-button>
        <el-button link type="primary" @click="downloadSalesContractTemplate()">[合同模板]</el-button>
        <el-button link type="primary" @click="downloadSalesDetailsTemplate()">[明细模板]</el-button>
        <el-button type="success" :icon="Download" @click="handleExport">导出</el-button>
        <el-button type="primary" :icon="Plus" @click="handleAdd">新建订单</el-button>
        <input ref="contractFileRef" type="file" accept=".xlsx,.xls" style="display:none" @change="handleContractFile" />
        <input ref="detailsFileRef" type="file" accept=".xlsx,.xls" style="display:none" @change="handleDetailsFile" />
      </div>
    </div>

    <!-- 状态标签页 -->
    <div class="status-tabs">
      <div
        v-for="tab in statusTabs"
        :key="String(tab.value)"
        :class="['status-tab-item', { active: activeStatusTab === tab.value }]"
        @click="handleTabChange(tab.value)"
      >
        {{ tab.label }}({{ tab.count }})
      </div>
    </div>

    <!-- 搜索过滤区域 -->
    <div class="filter-bar">
      <div class="filter-inputs">
        <el-input v-model="queryParams.orderNo" placeholder="订单号" clearable class="filter-input" :prefix-icon="Search" @clear="handleQuery" @keyup.enter="handleQuery" />
        <el-autocomplete v-model="queryParams.operatorName" :fetch-suggestions="queryOperator" placeholder="操作人员" clearable class="filter-input" @select="onOperatorSelect" @clear="handleQuery" />
        <el-autocomplete v-model="queryParams.salespersonName" :fetch-suggestions="querySalesperson" placeholder="业务人员" clearable class="filter-input" @select="onSalespersonSelect" @clear="handleQuery" />
      </div>
      <div class="filter-actions">
        <el-button :icon="Search" type="primary" @click="handleQuery">查询</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="orderList"
        :header-cell-style="{ background: '#fafafa', color: '#333', fontWeight: 500 }"
        row-class-name="table-row"
        style="width: 100%"
      >
        <el-table-column type="selection" width="40" align="center" />
        <el-table-column label="订单号" prop="orderNo" min-width="150" sortable>
          <template #default="{ row }">
            <span class="link-text" @click="handleDetail(row)">{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="日期" prop="createTime" min-width="120" sortable>
          <template #default="{ row }">
            {{ row.createTime ? row.createTime.slice(0, 10) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="业务员" prop="salespersonName" width="100" />
        <el-table-column label="操作人员" prop="operatorName" width="100" />
        <el-table-column label="合同总数量" prop="contractTotalQuantity" width="110" align="right" />
        <el-table-column label="合同金额" width="150" align="right" sortable>
          <template #default="{ row }">
            <span class="currency-label">{{ row.currency }} </span>
            <span class="amount-text">{{ row.contractAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="贸易条款" prop="tradeTerm" width="90" />
        <el-table-column label="付款方式" prop="paymentMethod" width="90" />
        <el-table-column label="目的港" prop="destinationPort" width="100" align="center" />
        <el-table-column label="运输方式" prop="transportType" width="100" />
        <el-table-column label="交货期" prop="deliveryDate" width="110" align="center" />
        <el-table-column label="总收款" width="150" align="right">
          <template #default="{ row }">
            <span class="currency-label">{{ row.currency }} </span>
            <span class="amount-text">{{ row.actualAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="定金收款金额" prop="receivedAmount" width="120" align="right" />
        <el-table-column label="尾款金额" prop="finalPaymentAmount" width="110" align="right" />
        <el-table-column label="预计尾款日期" prop="expectedReceiptDays" width="120" align="center" />
        <el-table-column label="损耗" prop="loss" width="100" align="right" />
        <el-table-column label="结算总数量" prop="settlementTotalQuantity" width="110" align="right" />
        <el-table-column label="结算总金额" prop="settlementTotalAmount" width="110" align="right" />
        <el-table-column label="状态" prop="status" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getTagType(row.status)" effect="light" round>{{ getLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="160" sortable />
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="scope">
            <div class="action-btns">
              <el-button link type="primary" @click="handleDetail(scope.row)">详情</el-button>
              <el-button link type="primary" @click="handleGoDetail(scope.row)">明细</el-button>
              <el-dropdown
                v-if="getAllowedNextStatuses(scope.row.status).length > 0"
                @command="(targetCode: number) => changeStatus(scope.row.id, targetCode, getList)"
                trigger="click"
              >
                <el-button link type="warning">变更状态</el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item
                      v-for="next in getAllowedNextStatuses(scope.row.status)"
                      :key="next.code"
                      :command="next.code"
                    >{{ next.label }}</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-button link type="danger" v-if="isAdmin" @click="handleDelete(scope.row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 汇总统计栏 -->
    <div class="summary-bar">
      <span class="summary-label">总计：</span>
      <span class="summary-item">订单 <b>{{ total }}</b> 笔</span>
    </div>

    <!-- 分页 -->
    <div class="pagination-bar">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[20, 50, 100]"
        layout="total, prev, pager, next"
        small
        @current-change="getList"
        @size-change="getList"
      />
      <el-select v-model="queryParams.pageSize" class="page-size-select" @change="handleQuery">
        <el-option :value="20" label="20 条/页" />
        <el-option :value="50" label="50 条/页" />
        <el-option :value="100" label="100 条/页" />
      </el-select>
    </div>
    <!-- Detail Dialog -->
    <el-dialog v-model="detailDialogVisible" title="订单详情" width="900px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ detailData.orderNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ getLabel(detailData.status) }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ detailData.customerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="业务员">{{ detailData.salespersonName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作员">{{ detailData.operatorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="贸易条款">{{ detailData.tradeTerm || '-' }}</el-descriptions-item>
        <el-descriptions-item label="付款方式">{{ detailData.paymentMethod || '-' }}</el-descriptions-item>
        <el-descriptions-item label="币种">{{ detailData.currency || '-' }}</el-descriptions-item>
        <el-descriptions-item label="合同金额">{{ detailData.contractAmount ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="总收款">{{ detailData.actualAmount ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="定金汇率">{{ detailData.depositExchangeRate ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="尾款汇率">{{ detailData.finalExchangeRate ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="定金比例(%)">{{ detailData.depositRate ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="定金收款金额">{{ detailData.receivedAmount ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="尾款金额">{{ detailData.finalPaymentAmount ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="保险费用">{{ detailData.insuranceFee ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="保额">{{ detailData.insuranceAmount ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="预计尾款日期">{{ detailData.expectedReceiptDays ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="交货期">{{ detailData.deliveryDate ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="目的港">{{ detailData.destinationPort || '-' }}</el-descriptions-item>
        <el-descriptions-item label="运输方式">{{ detailData.transportType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="海运费(USD)">{{ detailData.seaFreight ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="港杂费">{{ detailData.portFee ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="增值税">{{ detailData.vat ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="利润">{{ detailData.profit ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="损耗">{{ detailData.loss ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="合同总数量">{{ detailData.contractTotalQuantity ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="结算总数量">{{ detailData.settlementTotalQuantity ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="结算总金额">{{ detailData.settlementTotalAmount ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailData.updateTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button v-if="detailData.status === 1 || isAdmin" type="warning" @click="() => { detailDialogVisible = false; handleEdit(detailData) }">编辑</el-button>
        <el-button type="primary" @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 导入结果弹窗 -->
    <el-dialog v-model="importResultVisible" title="导入结果" width="600px" destroy-on-close>
      <el-alert
        :title="`成功 ${importResultData.successCount} 条${importResultData.updateCount ? '，更新 ' + importResultData.updateCount + ' 条' : ''}，失败 ${importResultData.errorCount} 条`"
        :type="importResultData.errorCount > 0 ? 'warning' : 'success'"
        show-icon
        :closable="false"
        style="margin-bottom: 16px"
      />
      <div v-if="importResultData.errors?.length">
        <el-text type="danger" tag="b">错误详情：</el-text>
        <el-scrollbar max-height="350px" style="margin-top: 8px">
          <div v-for="(err, idx) in importResultData.errors" :key="idx" style="padding: 4px 0; border-bottom: 1px solid #f0f0f0; font-size: 13px; color: #f56c6c;">
            {{ err }}
          </div>
        </el-scrollbar>
      </div>
      <template #footer>
        <el-button type="primary" @click="importResultVisible = false">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Download, Refresh, Upload } from '@element-plus/icons-vue'
import { exportToCsv } from '@/utils/export'
import { getOrderPage, deleteSalesOrder, importSalesOrderContract, importSalesOrderDetails, downloadSalesContractTemplate as apiDownloadSalesContract, downloadSalesDetailsTemplate as apiDownloadSalesDetails } from '@/api/salesOrder'
import { getUserListWithRoles } from '@/api/system'
import { useOrderStatus } from '@/composables/useOrderStatus'

const { statusList, getLabel, getTagType, getAllowedNextStatuses, changeStatus } = useOrderStatus('sales')

// Status tabs
const activeStatusTab = ref<number | null>(null)
const statusCounts = reactive<Record<string, number>>({})

const statusTabs = computed(() => {
  const tabs = [{ label: '全部', value: null as number | null, count: statusCounts['all'] || 0 }]
  for (const s of statusList.value) {
    tabs.push({ label: s.label, value: s.code, count: statusCounts[String(s.code)] || 0 })
  }
  return tabs
})

const getStatusCounts = async () => {
  try {
    const baseParams = { ...queryParams, pageNum: 1, pageSize: 1 }
    const allRes = await getOrderPage({ ...baseParams, status: null })
    statusCounts['all'] = allRes.data.total || 0
    const promises = statusList.value.map(async (s: any) => {
      const res = await getOrderPage({ ...baseParams, status: s.code })
      statusCounts[String(s.code)] = res.data.total || 0
    })
    await Promise.all(promises)
  } catch { /* ignore */ }
}

const handleTabChange = (val: number | null) => {
  activeStatusTab.value = val
  queryParams.status = val
  queryParams.pageNum = 1
  getList()
}

// Admin check
const isAdmin = ref(false)
const checkAdmin = async () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const res = await getUserListWithRoles()
    const allUsers = res.data || []
    const currentUser = allUsers.find((u: any) => u.id === userInfo.userId)
    isAdmin.value = !!(currentUser && Array.isArray(currentUser.roleCodes) && currentUser.roleCodes.includes('admin'))
  } catch {
    isAdmin.value = false
  }
}

// Data definitions
const loading = ref(false)
const router = useRouter()
const orderList = ref([])
const total = ref(0)
const detailDialogVisible = ref(false)
const salespersonOptions = ref<any[]>([])
const operatorOptions = ref<any[]>([])
const allUsers = ref<any[]>([])
const businessUsers = ref<any[]>([])
const operatorUsers = ref<any[]>([])
const queryParams = reactive({
  pageNum: 1,
  pageSize: 20,
  orderNo: '',
  tradeTerm: '',
  createUserName: '',
  createId: null as number | null,
  salespersonName: '',
  salespersonId: null as number | null,
  operatorName: '',
  operatorId: null as number | null,
  status: null as number | null
})

const detailData = reactive<any>({
  id: null,
  orderNo: '',
  customerId: null,
  salespersonId: null,
  operatorId: null,
  tradeTerm: '',
  paymentMethod: '',
  currency: '',
  depositExchangeRate: null,
  finalExchangeRate: null,
  contractAmount: null,
  actualAmount: null,
  depositRate: null,
  receivedAmount: null,
  finalPaymentAmount: null,
  insuranceFee: null,
  insuranceAmount: null,
  expectedReceiptDays: null,
  deliveryDate: null,
  destinationPort: '',
  transportType: '',
  seaFreight: null,
  portFee: null,
  vat: null,
  profit: null,
  loss: null,
  contractTotalQuantity: null,
  settlementTotalQuantity: null,
  settlementTotalAmount: null,
  status: 1,
  createTime: '',
  updateTime: ''
})

// Fetch logic
const getList = async () => {
  loading.value = true
  try {
    const res = await getOrderPage(queryParams)
    orderList.value = res.data.list
    total.value = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// User autocomplete methods

const querySalesperson = (queryString: string, cb: (results: { value: string; data: any }[]) => void) => {
  const users = businessUsers.value
  const results = queryString
    ? users.filter(user => user.realName?.toLowerCase().includes(queryString.toLowerCase())).map(user => ({ value: user.realName, data: user }))
    : users.map(user => ({ value: user.realName, data: user }))
  cb(results)
}

const queryOperator = (queryString: string, cb: (results: { value: string; data: any }[]) => void) => {
  const users = operatorUsers.value
  const results = queryString
    ? users.filter(user => user.realName?.toLowerCase().includes(queryString.toLowerCase())).map(user => ({ value: user.realName, data: user }))
    : users.map(user => ({ value: user.realName, data: user }))
  cb(results)
}


const onSalespersonSelect = (item: { value: string; data: any }) => {
  queryParams.salespersonId = item.data.id
}

const onOperatorSelect = (item: { value: string; data: any }) => {
  queryParams.operatorId = item.data.id
}

// Search and reset
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}
const resetQuery = () => {
  queryParams.orderNo = ''
  queryParams.tradeTerm = ''
  queryParams.createUserName = ''
  queryParams.createId = null
  queryParams.salespersonName = ''
  queryParams.salespersonId = null
  queryParams.operatorName = ''
  queryParams.operatorId = null
  queryParams.status = null
  handleQuery()
}

const handleAdd = () => {
  router.push({ name: 'SalesOrderCreate' })
}
const handleGoDetail = (row: any) => {
  router.push({ name: 'SalesOrderDetail', params: { orderId: row.id }, query: { orderNo: row.orderNo } })
}
const handleDetail = (row: any) => {
  Object.assign(detailData, {
    id: row.id,
    orderNo: row.orderNo,
    customerId: row.customerId,
    customerName: row.customerName,
    salespersonId: row.salespersonId,
    salespersonName: row.salespersonName,
    operatorId: row.operatorId,
    operatorName: row.operatorName,
    tradeTerm: row.tradeTerm,
    paymentMethod: row.paymentMethod,
    currency: row.currency,
  depositExchangeRate: row.depositExchangeRate ?? null,
  finalExchangeRate: row.finalExchangeRate ?? null,
  contractAmount: row.contractAmount ?? null,
  actualAmount: row.actualAmount ?? null,
  depositRate: row.depositRate ?? null,
  receivedAmount: row.receivedAmount ?? null,
  finalPaymentAmount: row.finalPaymentAmount ?? null,
  insuranceFee: row.insuranceFee ?? null,
  insuranceAmount: row.insuranceAmount ?? null,
  expectedReceiptDays: row.expectedReceiptDays ?? null,
  deliveryDate: row.deliveryDate ?? null,
  destinationPort: row.destinationPort ?? '',
  transportType: row.transportType ?? '',
  seaFreight: row.seaFreight ?? null,
  portFee: row.portFee ?? null,
  vat: row.vat ?? null,
  profit: row.profit ?? null,
  loss: row.loss ?? null,
  contractTotalQuantity: row.contractTotalQuantity ?? null,
  settlementTotalQuantity: row.settlementTotalQuantity ?? null,
  settlementTotalAmount: row.settlementTotalAmount ?? null,
    status: row.status ?? 1,
    createTime: row.createTime ?? '',
    updateTime: row.updateTime ?? ''
  })
  detailDialogVisible.value = true
}
const handleEdit = async (row: any) => {
  router.push({ name: 'SalesOrderCreate', query: { id: row.id } })
}

const handleDelete = async (row: any) => {
  await ElMessageBox.confirm(`确认删除订单 "${row.orderNo}"？此操作不可恢复。`, '提示', { type: 'warning' })
  await deleteSalesOrder(row.id)
  ElMessage.success('删除成功')
  getList()
}

const handleExport = async () => {
  const res = await getOrderPage({ ...queryParams, pageNum: 1, pageSize: 10000 })
  const rows = res.data.list || []
  exportToCsv('销售订单导出', rows, [
    { label: '订单号', key: 'orderNo' },
    { label: '贸易条款', key: 'tradeTerm' },
    { label: '付款方式', key: 'paymentMethod' },
    { label: '币种', key: 'currency' },
    { label: '合同金额', key: 'contractAmount' },
    { label: '总收款', key: 'actualAmount' },
    { label: '定金比例(%)', key: 'depositRate' },
    { label: '定金收款金额', key: 'receivedAmount' },
    { label: '尾款金额', key: 'finalPaymentAmount' },
    { label: '保险费用', key: 'insuranceFee' },
    { label: '保额', key: 'insuranceAmount' },
    { label: '预计尾款日期', key: 'expectedReceiptDays' },
    { label: '目的地', key: 'destinationPort' },
    { label: '运输方式', key: 'transportType' },
    { label: '操作人员', key: 'operatorName' },
    { label: '状态', value: (r: any) => getLabel(r.status) },
    { label: '创建时间', key: 'createTime' }
  ])
}

// ---- Excel 导入 ----
const contractFileRef = ref<HTMLInputElement>()
const detailsFileRef = ref<HTMLInputElement>()

const triggerContractUpload = () => contractFileRef.value?.click()
const triggerDetailsUpload = () => detailsFileRef.value?.click()

const handleContractFile = async (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  try {
    const res = await importSalesOrderContract(file)
    const r = res.data
    showImportResult(r)
    getList()
  } catch (err) {
    ElMessage.error('导入失败')
  } finally {
    ;(e.target as HTMLInputElement).value = ''
  }
}

const handleDetailsFile = async (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  try {
    const res = await importSalesOrderDetails(file)
    const r = res.data
    showImportResult(r)
    getList()
  } catch (err) {
    ElMessage.error('导入失败')
  } finally {
    ;(e.target as HTMLInputElement).value = ''
  }
}

/** 导入结果展示 */
const importResultVisible = ref(false)
const importResultData = ref<{ successCount: number; updateCount?: number; errorCount: number; errors: string[] }>({ successCount: 0, errorCount: 0, errors: [] })

const showImportResult = (r: any) => {
  importResultData.value = r
  if (r.errors?.length) {
    importResultVisible.value = true
  } else {
    const parts = [`成功 ${r.successCount} 条`]
    if (r.updateCount) parts.push(`更新 ${r.updateCount} 条`)
    ElMessage.success(`导入完成：${parts.join('，')}`)
  }
}

const downloadTemplate = async (apiFn: () => Promise<any>, filename: string) => {
  const res = await apiFn()
  const url = URL.createObjectURL(new Blob([res.data]))
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  URL.revokeObjectURL(url)
}

const downloadSalesContractTemplate = () => downloadTemplate(apiDownloadSalesContract, '销售订单合同导入模板.xlsx')
const downloadSalesDetailsTemplate = () => downloadTemplate(apiDownloadSalesDetails, '销售订单明细导入模板.xlsx')

const loadSalespersonOptions = async () => {
  const res = await getUserListWithRoles()
  const list: any[] = res.data || []
  // 所有用户（用于创建人搜索）
  allUsers.value = list
  // 业务人员：角色名称包含"业务"的用户
  businessUsers.value = list.filter((user: any) =>
    user.roleNames?.some((name: string) => name.includes('业务'))
  ) 
  // 操作人员：角色名称包含"操作"的用户
  operatorUsers.value = list
  // 供新建/编辑表单的业务员下拉
  salespersonOptions.value = list
  // 供新建/编辑表单的操作员下拉
  operatorOptions.value = list
}

// Init
onMounted(() => {
  checkAdmin()
  loadSalespersonOptions()
  getList()
  getStatusCounts()
})
</script>

<style scoped>
.group-divider {
  margin: 8px 0 16px;
}
</style>
