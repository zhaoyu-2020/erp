<template>
  <div class="app-container">
    <el-card shadow="never" class="order-info-card">
      <div class="order-header">
        <el-button icon="ArrowLeft" @click="goBack" style="margin-right: 16px">返回</el-button>
        <span class="order-title">采购订单明细 — {{ poNo }}</span>
      </div>
    </el-card>

    <el-card shadow="never" class="table-wrap">
      <div class="table-toolbar">
        <el-button type="primary" icon="Plus" @click="handleAdd">新增明细</el-button>
        <el-button type="success" icon="Download" @click="handleExport">导出</el-button>
      </div>

      <el-table v-loading="loading" :data="detailList" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="产品规格" prop="spec" min-width="120" />
        <el-table-column label="产品类型" prop="productType" width="120" />
        <el-table-column label="材质" prop="material" width="100" />
        <el-table-column label="长度(m)" prop="length" width="100" align="center" />
        <el-table-column label="公差" prop="tolerance" width="100" />
        <el-table-column label="数量(t)" prop="quantityTon" width="100" align="right" />
        <el-table-column label="数量(pc)" prop="quantityPc" width="100" align="right" />
        <el-table-column label="数量(m)" prop="quantityMeter" width="100" align="right" />
        <el-table-column label="结算价格" prop="settlementPrice" width="110" align="right" />
        <el-table-column label="价格汇总" prop="priceTotal" width="120" align="right">
          <template #default="{ row }">
            <span class="price-total">{{ row.priceTotal ?? '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="包装重量" prop="packagingWeight" width="110" align="right" />
        <el-table-column label="包装" prop="packaging" width="120" />
        <el-table-column label="卷内径(mm)" prop="coilInnerDiameter" width="110" align="center" />
        <el-table-column label="详情序号" prop="detailSeq" width="90" align="center" />
        <el-table-column label="订货数量" prop="orderedQuantity" width="100" align="right" />
        <el-table-column label="实际数量" prop="actualQuantity" width="100" align="right" />
        <el-table-column label="捆数" prop="bundleCount" width="80" align="right" />
        <el-table-column label="净重" prop="netWeight" width="100" align="right" />
        <el-table-column label="毛重" prop="grossWeight" width="100" align="right" />
        <el-table-column label="体积" prop="volume" width="100" align="right" />
        <el-table-column label="实际理论重量" prop="actualTheoreticalWeight" width="120" align="right" />
        <el-table-column label="货源地" prop="originPlace" width="120" show-overflow-tooltip />
        <el-table-column label="加工项" prop="processingItems" min-width="150" show-overflow-tooltip />
        <el-table-column label="备注" prop="remark" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="140" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="820px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
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
              <el-input v-model="form.length" placeholder="输入长度，单位：米" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="公差" prop="tolerance">
              <el-input v-model="form.tolerance" placeholder="输入公差" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left" class="group-divider">数量</el-divider>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="数量（t）" prop="quantityTon">
              <el-input v-model="form.quantityTon" placeholder="吨数" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="数量（pc）" prop="quantityPc">
              <el-input v-model="form.quantityPc" placeholder="片数" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="数量（m）" prop="quantityMeter">
              <el-input v-model="form.quantityMeter" placeholder="米数" />
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
            <el-form-item label="价格汇总">
              <el-input :model-value="computedPriceTotal" disabled placeholder="结算价格 × 吨数（自动计算）" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left" class="group-divider">数量明细 / 重量 / 体积</el-divider>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="详情序号" prop="detailSeq">
              <el-input v-model="form.detailSeq" placeholder="输入序号" />
            </el-form-item>
          </el-col>
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
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="捆数" prop="bundleCount">
              <el-input v-model="form.bundleCount" placeholder="输入捆数" />
            </el-form-item>
          </el-col>
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
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="实际理论重量" prop="actualTheoreticalWeight">
              <el-input v-model="form.actualTheoreticalWeight" placeholder="输入实际理论重量" />
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
            <el-form-item label="包装" prop="packaging">
              <el-input v-model="form.packaging" placeholder="输入包装方式" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="卷内径(mm)" prop="coilInnerDiameter">
              <el-input v-model="form.coilInnerDiameter" placeholder="输入卷内径，单位：mm" />
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
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { exportToCsv } from '@/utils/export'
import {
  getPurchaseDetailListByOrderId,
  savePurchaseOrderDetail,
  updatePurchaseOrderDetail,
  deletePurchaseOrderDetail
} from '@/api/purchaseOrderDetail'
import { getProductTypeList } from '@/api/productType'

const route = useRoute()
const router = useRouter()

const orderId = computed(() => Number(route.params.orderId))
const poNo = ref('')
const loading = ref(false)
const submitLoading = ref(false)
const detailList = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const productTypeOptions = ref<string[]>([])

const form = reactive<any>({
  id: null,
  purchaseOrderId: null,
  spec: '',
  productType: '',
  material: '',
  length: '',
  tolerance: '',
  quantityTon: null,
  quantityPc: null,
  quantityMeter: null,
  settlementPrice: null,
  packagingWeight: null,
  packaging: '',
  coilInnerDiameter: '',
  processingItems: '',
  remark: '',
  detailSeq: null,
  orderedQuantity: null,
  actualQuantity: null,
  bundleCount: null,
  netWeight: null,
  grossWeight: null,
  volume: null,
  originPlace: '',
  actualTheoreticalWeight: null
})

const rules = {
  spec: [{ required: true, message: '请输入产品规格', trigger: 'blur' }],
  productType: [{ required: true, message: '请输入产品类型', trigger: 'blur' }],
  material: [{ required: true, message: '请输入材质', trigger: 'blur' }],
  length: [{ required: true, message: '请输入长度', trigger: 'blur' }],
  tolerance: [{ required: true, message: '请输入公差', trigger: 'blur' }],
  quantityTon: [{ required: true, message: '请输入吨数', trigger: 'blur' }]
}

const computedPriceTotal = computed(() => {
  const price = parseFloat(form.settlementPrice)
  const ton = parseFloat(form.quantityTon)
  if (!isNaN(price) && !isNaN(ton)) {
    return (price * ton).toFixed(2)
  }
  return ''
})

const getList = async () => {
  loading.value = true
  try {
    const res = await getPurchaseDetailListByOrderId(orderId.value)
    detailList.value = res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const loadProductTypes = async () => {
  try {
    const res = await getProductTypeList()
    productTypeOptions.value = (res.data || []).map((pt: any) => pt.typeName)
  } catch (e) {
    console.error(e)
  }
}

const queryProductType = (queryString: string, cb: (results: { value: string }[]) => void) => {
  const opts = productTypeOptions.value
  const results = queryString
    ? opts.filter(t => t.toLowerCase().includes(queryString.toLowerCase())).map(t => ({ value: t }))
    : opts.map(t => ({ value: t }))
  cb(results)
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
    spec: row.spec ?? '',
    productType: row.productType ?? '',
    material: row.material ?? '',
    length: row.length ?? '',
    tolerance: row.tolerance ?? '',
    quantityTon: row.quantityTon ?? null,
    quantityPc: row.quantityPc ?? null,
    quantityMeter: row.quantityMeter ?? null,
    settlementPrice: row.settlementPrice ?? null,
    packagingWeight: row.packagingWeight ?? null,
    packaging: row.packaging ?? '',
    coilInnerDiameter: row.coilInnerDiameter ?? '',
    processingItems: row.processingItems ?? '',
    remark: row.remark ?? '',
    detailSeq: row.detailSeq ?? null,
    orderedQuantity: row.orderedQuantity ?? null,
    actualQuantity: row.actualQuantity ?? null,
    bundleCount: row.bundleCount ?? null,
    netWeight: row.netWeight ?? null,
    grossWeight: row.grossWeight ?? null,
    volume: row.volume ?? null,
    originPlace: row.originPlace ?? '',
    actualTheoreticalWeight: row.actualTheoreticalWeight ?? null
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
  form.purchaseOrderId = orderId.value
  form.spec = ''
  form.productType = ''
  form.material = ''
  form.length = ''
  form.tolerance = ''
  form.quantityTon = null
  form.quantityPc = null
  form.quantityMeter = null
  form.settlementPrice = null
  form.packagingWeight = null
  form.packaging = ''
  form.coilInnerDiameter = ''
  form.processingItems = ''
  form.remark = ''
  form.detailSeq = null
  form.orderedQuantity = null
  form.actualQuantity = null
  form.bundleCount = null
  form.netWeight = null
  form.grossWeight = null
  form.volume = null
  form.originPlace = ''
  form.actualTheoreticalWeight = null
  formRef.value?.clearValidate()
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    const payload = { ...form, purchaseOrderId: orderId.value }
    if (payload.id) {
      await updatePurchaseOrderDetail(payload)
    } else {
      delete payload.id
      await savePurchaseOrderDetail(payload)
    }
    ElMessage.success(form.id ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadProductTypes()
    getList()
  } finally {
    submitLoading.value = false
  }
}

const handleExport = async () => {
  exportToCsv('采购订单明细导出', detailList.value, [
    { label: '产品规格', key: 'spec' },
    { label: '产品类型', key: 'productType' },
    { label: '材质', key: 'material' },
    { label: '长度(m)', key: 'length' },
    { label: '公差', key: 'tolerance' },
    { label: '数量(t)', key: 'quantityTon' },
    { label: '数量(pc)', key: 'quantityPc' },
    { label: '数量(m)', key: 'quantityMeter' },
    { label: '结算价格', key: 'settlementPrice' },
    { label: '价格汇总', key: 'priceTotal' },
    { label: '包装重量', key: 'packagingWeight' },
    { label: '包装', key: 'packaging' },
    { label: '卷内径(mm)', key: 'coilInnerDiameter' },
    { label: '加工项', key: 'processingItems' },
    { label: '备注', key: 'remark' },
    { label: '详情序号', key: 'detailSeq' },
    { label: '订货数量', key: 'orderedQuantity' },
    { label: '实际数量', key: 'actualQuantity' },
    { label: '捆数', key: 'bundleCount' },
    { label: '净重', key: 'netWeight' },
    { label: '毛重', key: 'grossWeight' },
    { label: '体积', key: 'volume' },
    { label: '货源地', key: 'originPlace' },
    { label: '实际理论重量', key: 'actualTheoreticalWeight' }
  ])
}

const goBack = () => {
  router.back()
}

onMounted(async () => {
  const no = route.query.poNo as string
  poNo.value = no || `采购单 #${orderId.value}`
  await Promise.all([getList(), loadProductTypes()])
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.order-info-card {
  margin-bottom: 16px;
}
.order-header {
  display: flex;
  align-items: center;
}
.order-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.table-wrap {
  margin-top: 0;
}
.table-toolbar {
  margin-bottom: 16px;
}
.price-total {
  font-weight: bold;
  color: #e6a23c;
}
.group-divider {
  margin: 8px 0 16px;
}
</style>
