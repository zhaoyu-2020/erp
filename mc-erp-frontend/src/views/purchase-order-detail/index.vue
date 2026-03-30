<template>
  <div class="app-container">
    <el-card shadow="never" class="search-wrap">
      <el-form :inline="true" :model="queryParams" ref="queryRef">
        <el-form-item label="采购单号" prop="poNo">
          <el-input v-model="queryParams.poNo" placeholder="输入采购单号" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="产品规格" prop="spec">
          <el-input v-model="queryParams.spec" placeholder="输入产品规格" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="产品类型" prop="productType">
          <el-input v-model="queryParams.productType" placeholder="输入产品类型" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="材质" prop="material">
          <el-input v-model="queryParams.material" placeholder="输入材质" clearable style="width: 120px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-wrap">
      <div class="table-toolbar">
        <el-button type="primary" icon="Plus" @click="handleAdd">新增明细</el-button>
        <el-button type="success" icon="Download" @click="handleExport">导出</el-button>
      </div>

      <el-table v-loading="loading" :data="detailList" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="采购单号" prop="poNo" width="160" />
        <el-table-column label="产品规格" prop="spec" min-width="120" />
        <el-table-column label="产品类型" prop="productType" width="120" />
        <el-table-column label="材质" prop="material" width="100" />
        <el-table-column label="长度(m)" prop="length" width="100" align="center" />
        <el-table-column label="公差" prop="tolerance" width="100" />
        <el-table-column label="订货数量" prop="orderedQuantity" width="100" align="right" />
        <el-table-column label="结算价格" prop="settlementPrice" width="110" align="right" />
        <el-table-column label="合同金额小计" prop="priceTotal" width="120" align="right">
          <template #default="{ row }">
            <span class="price-total">{{ row.priceTotal ?? '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="结算金额小计" prop="settlementAmount" width="120" align="right">
          <template #default="{ row }">
            <span class="price-total">{{ row.settlementAmount ?? '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="实际数量" prop="actualQuantity" width="100" align="right" />
        <el-table-column label="捆数" prop="bundleCount" width="80" align="right" />
        <el-table-column label="净重" prop="netWeight" width="100" align="right" />
        <el-table-column label="毛重" prop="grossWeight" width="100" align="right" />
        <el-table-column label="实际理论重量" prop="actualTheoreticalWeight" width="120" align="right" />
        <el-table-column label="体积" prop="volume" width="100" align="right" />
        <el-table-column label="货源地" prop="originPlace" width="120" show-overflow-tooltip />

        <el-table-column label="包装重量" prop="packagingWeight" width="110" align="right" />
        <el-table-column label="包装" prop="packaging" width="120" />
        <el-table-column label="卷内径(mm)" prop="coilInnerDiameter" width="110" align="center" />
        <el-table-column label="加工项" prop="processingItems" min-width="150" show-overflow-tooltip />
        <el-table-column label="备注" prop="remark" min-width="150" show-overflow-tooltip />
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
        @size-change="getList"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="860px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-divider content-position="left" class="group-divider">关联采购订单</el-divider>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="采购单号" prop="poNo">
              <el-autocomplete
                v-model="form.poNo"
                :fetch-suggestions="queryPoNo"
                placeholder="输入或选择采购单号"
                clearable
                style="width: 100%"
                @select="onPoNoSelect"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left" class="group-divider">产品信息（五项唯一确定产品）</el-divider>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="产品规格" prop="spec">
              <el-input v-model="form.spec" placeholder="输入产品规格" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品类型" prop="productType">
              <el-autocomplete
                v-model="form.productType"
                :fetch-suggestions="queryProductType"
                placeholder="输入或选择产品类型"
                clearable
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="材质" prop="material">
              <el-input v-model="form.material" placeholder="输入材质" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="长度(米)" prop="length">
              <el-input v-model="form.length" placeholder="输入长度，单位：米(卷材输入0)" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="公差" prop="tolerance">
              <el-input v-model="form.tolerance" placeholder="输入公差（卷材输入0）" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left" class="group-divider">数量明细 / 重量 / 体积</el-divider>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="订货数量" prop="orderedQuantity">
              <el-input v-model="form.orderedQuantity" placeholder="输入订货数量" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="实际数量" prop="actualQuantity">
              <el-input v-model="form.actualQuantity" placeholder="输入实际数量" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="捆数（卷数）" prop="bundleCount">
              <el-input v-model="form.bundleCount" placeholder="输入捆数" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="净重" prop="netWeight">
              <el-input v-model="form.netWeight" placeholder="输入净重" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="毛重" prop="grossWeight">
              <el-input v-model="form.grossWeight" placeholder="输入毛重" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="实际理论重量" prop="actualTheoreticalWeight">
              <el-input v-model="form.actualTheoreticalWeight" placeholder="输入实际理论重量" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="体积" prop="volume">
              <el-input v-model="form.volume" placeholder="输入体积" />
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="货源地" prop="originPlace">
              <el-input v-model="form.originPlace" placeholder="输入货源地" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left" class="group-divider">价格</el-divider>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="结算价格" prop="settlementPrice">
              <el-input v-model="form.settlementPrice" placeholder="输入结算价格" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="合同金额小计">
              <el-input :model-value="computedPriceTotal" disabled placeholder="结算价格 × 订货数量（自动计算）" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结算金额小计">
              <el-input :model-value="computedSettlementAmount" disabled placeholder="结算价格 × 实际数量（自动计算）" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left" class="group-divider">包装 / 其他</el-divider>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="包装重量" prop="packagingWeight">
              <el-input v-model="form.packagingWeight" placeholder="输入包装重量" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="包装方式" prop="packaging">
              <el-input v-model="form.packaging" placeholder="输入包装方式" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="卷内径(mm)" prop="coilInnerDiameter">
              <el-input v-model="form.coilInnerDiameter" placeholder="输入卷内径，单位：mm（508/610）" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="加工项" prop="processingItems">
              <el-input v-model="form.processingItems" placeholder="输入加工项" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { exportToCsv } from '@/utils/export'
import {
  getPurchaseDetailPage,
  savePurchaseOrderDetail,
  updatePurchaseOrderDetail,
  deletePurchaseOrderDetail
} from '@/api/purchaseOrderDetail'
import { getProductTypeList } from '@/api/productType'
import { getPurchaseOrderPage } from '@/api/purchaseOrder'

const loading = ref(false)
const submitLoading = ref(false)
const detailList = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const productTypeOptions = ref<string[]>([])
const poNoOptions = ref<string[]>([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  poNo: '',
  spec: '',
  productType: '',
  material: ''
})

const form = reactive<any>({
  id: null,
  purchaseOrderId: null,
  poNo: '',
  spec: '',
  productType: '',
  material: '',
  length: '',
  tolerance: '',
  orderedQuantity: null,
  actualQuantity: null,
  bundleCount: null,
  netWeight: null,
  grossWeight: null,
  actualTheoreticalWeight: null,
  volume: null,
  originPlace: '',
  settlementPrice: null,
  priceTotal: null,
  settlementAmount: null,
  packagingWeight: null,
  packaging: '',
  coilInnerDiameter: '',
  processingItems: '',
  remark: '',
  detailSeq: null
})

const rules = {
  poNo: [{ required: true, message: '请选择采购单号', trigger: 'blur' }],
  spec: [{ required: true, message: '请输入产品规格', trigger: 'blur' }],
  productType: [{ required: true, message: '请输入产品类型', trigger: 'blur' }],
  material: [{ required: true, message: '请输入材质', trigger: 'blur' }],
  length: [{ required: true, message: '请输入长度', trigger: 'blur' }],
  tolerance: [{ required: true, message: '请输入公差', trigger: 'blur' }]
}

const computedPriceTotal = computed(() => {
  const price = parseFloat(form.settlementPrice)
  const qty = parseFloat(form.orderedQuantity)
  if (!isNaN(price) && !isNaN(qty)) {
    return (price * qty).toFixed(2)
  }
  return ''
})

const computedSettlementAmount = computed(() => {
  const price = parseFloat(form.settlementPrice)
  const qty = parseFloat(form.actualQuantity)
  if (!isNaN(price) && !isNaN(qty)) {
    return (price * qty).toFixed(2)
  }
  return ''
})

const poNoIdMap = ref<Record<string, number>>({})

const getList = async () => {
  loading.value = true
  try {
    const res = await getPurchaseDetailPage(queryParams)
    detailList.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const loadOptions = async () => {
  const [ptRes, poRes] = await Promise.all([
    getProductTypeList(),
    getPurchaseOrderPage({ pageNum: 1, pageSize: 1000 })
  ])
  productTypeOptions.value = (ptRes.data || []).map((pt: any) => pt.typeName)
  const orders: any[] = poRes.data?.list || []
  poNoOptions.value = orders.map((o: any) => o.poNo)
  orders.forEach((o: any) => { poNoIdMap.value[o.poNo] = o.id })
}

const queryProductType = (queryString: string, cb: (results: { value: string }[]) => void) => {
  const opts = productTypeOptions.value
  const results = queryString
    ? opts.filter(t => t.toLowerCase().includes(queryString.toLowerCase())).map(t => ({ value: t }))
    : opts.map(t => ({ value: t }))
  cb(results)
}

const queryPoNo = (queryString: string, cb: (results: { value: string }[]) => void) => {
  const opts = poNoOptions.value
  const results = queryString
    ? opts.filter(o => o.toLowerCase().includes(queryString.toLowerCase())).map(o => ({ value: o }))
    : opts.map(o => ({ value: o }))
  cb(results)
}

const onPoNoSelect = (item: { value: string }) => {
  form.poNo = item.value
  form.purchaseOrderId = poNoIdMap.value[item.value] ?? null
}

const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

const resetQuery = () => {
  queryParams.poNo = ''
  queryParams.spec = ''
  queryParams.productType = ''
  queryParams.material = ''
  handleQuery()
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增明细'
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  resetForm()
  Object.assign(form, {
    id: row.id,
    purchaseOrderId: row.purchaseOrderId,
    poNo: row.poNo ?? '',
    spec: row.spec ?? '',
    productType: row.productType ?? '',
    material: row.material ?? '',
    length: row.length ?? '',
    tolerance: row.tolerance ?? '',
    orderedQuantity: row.orderedQuantity ?? null,
    actualQuantity: row.actualQuantity ?? null,
    bundleCount: row.bundleCount ?? null,
    netWeight: row.netWeight ?? null,
    grossWeight: row.grossWeight ?? null,
    actualTheoreticalWeight: row.actualTheoreticalWeight ?? null,
    volume: row.volume ?? null,
    originPlace: row.originPlace ?? '',
    settlementPrice: row.settlementPrice ?? null,
    priceTotal: row.priceTotal ?? null,
    settlementAmount: row.settlementAmount ?? null,
    packagingWeight: row.packagingWeight ?? null,
    packaging: row.packaging ?? '',
    coilInnerDiameter: row.coilInnerDiameter ?? '',
    processingItems: row.processingItems ?? '',
    remark: row.remark ?? '',
    detailSeq: row.detailSeq ?? null
  })
  dialogTitle.value = '编辑明细'
  dialogVisible.value = true
}

const handleDelete = async (row: any) => {
  await ElMessageBox.confirm('确认删除该明细？', '提示', { type: 'warning' })
  await deletePurchaseOrderDetail(row.id)
  ElMessage.success('删除成功')
  getList()
}

const resetForm = () => {
  form.id = null
  form.purchaseOrderId = null
  form.poNo = ''
  form.spec = ''
  form.productType = ''
  form.material = ''
  form.length = ''
  form.tolerance = ''
  form.orderedQuantity = null
  form.actualQuantity = null
  form.bundleCount = null
  form.netWeight = null
  form.grossWeight = null
  form.actualTheoreticalWeight = null
  form.volume = null
  form.originPlace = ''
  form.packagingWeight = null
  form.priceTotal = null
  form.settlementAmount = null
  form.packaging = ''
  form.coilInnerDiameter = ''
  form.processingItems = ''
  form.remark = ''
  form.detailSeq = null
  formRef.value?.clearValidate()
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  if (!form.purchaseOrderId && form.poNo) {
    form.purchaseOrderId = poNoIdMap.value[form.poNo] ?? null
  }
  if (!form.purchaseOrderId) {
    ElMessage.error('无效的采购单号，请从列表中选择')
    return
  }
  submitLoading.value = true
  try {
    const { poNo: _poNo, ...payload } = { ...form }
    payload.priceTotal = computedPriceTotal.value ? parseFloat(computedPriceTotal.value) : null
    payload.settlementAmount = computedSettlementAmount.value ? parseFloat(computedSettlementAmount.value) : null
    if (payload.id) {
      await updatePurchaseOrderDetail(payload)
    } else {
      delete payload.id
      await savePurchaseOrderDetail(payload)
    }
    ElMessage.success(form.id ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadOptions()
    getList()
  } finally {
    submitLoading.value = false
  }
}

const handleExport = async () => {
  const res = await getPurchaseDetailPage({ ...queryParams, pageNum: 1, pageSize: 10000 })
  exportToCsv('采购订单明细导出', res.data.list || [], [
    { label: '采购单号', key: 'poNo' },
    { label: '产品规格', key: 'spec' },
    { label: '产品类型', key: 'productType' },
    { label: '材质', key: 'material' },
    { label: '长度(m)', key: 'length' },
    { label: '公差', key: 'tolerance' },
    { label: '订货数量', key: 'orderedQuantity' },
    { label: '实际数量', key: 'actualQuantity' },
    { label: '捆数', key: 'bundleCount' },
    { label: '净重', key: 'netWeight' },
    { label: '毛重', key: 'grossWeight' },
    { label: '实际理论重量', key: 'actualTheoreticalWeight' },
    { label: '体积', key: 'volume' },
    { label: '货源地', key: 'originPlace' },
    { label: '结算价格', key: 'settlementPrice' },
    { label: '合同金额小计', key: 'priceTotal' },
    { label: '结算金额小计', key: 'settlementAmount' },
    { label: '包装重量', key: 'packagingWeight' },
    { label: '包装', key: 'packaging' },
    { label: '卷内径(mm)', key: 'coilInnerDiameter' },
    { label: '加工项', key: 'processingItems' },
    { label: '备注', key: 'remark' }
  ])
}

onMounted(async () => {
  await Promise.all([getList(), loadOptions()])
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.search-wrap {
  margin-bottom: 16px;
}
.table-wrap {
  margin-top: 0;
}
.table-toolbar {
  margin-bottom: 16px;
}
.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.price-total {
  font-weight: bold;
  color: #e6a23c;
}
.group-divider {
  margin: 8px 0 16px;
}
</style>
