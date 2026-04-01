<template>
  <div class="form-page">
    <!-- 顶部导航栏 -->
    <div class="form-page-header">
      <div class="form-page-back" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </div>
      <span class="form-page-title">{{ isEdit ? '编辑销售订单' : '新增销售订单' }}</span>
    </div>

    <!-- 表单内容 -->
    <div class="form-page-body">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0">

        <!-- ===== 基本信息 ===== -->
        <div class="form-section">
          <div class="form-section-header" @click="toggleSection('basic')">
            <span class="form-section-title">基本信息</span>
            <el-icon :class="['form-section-arrow', { collapsed: collapsedSections.basic }]"><ArrowDown /></el-icon>
          </div>
          <div :class="['form-section-body', { collapsed: collapsedSections.basic }]" :style="{ maxHeight: collapsedSections.basic ? '0' : '600px' }">
            <div class="form-grid">
              <div class="form-field">
                <label class="form-field-label"><span class="required">*</span>订单号</label>
                <el-form-item prop="orderNo">
                  <el-input v-model="form.orderNo" placeholder="请输入订单号" :disabled="isEdit" />
                </el-form-item>
              </div>
              <div class="form-field">
                <label class="form-field-label"><span class="required">*</span>业务员</label>
                <el-form-item prop="salespersonId">
                  <el-select v-model="form.salespersonId" placeholder="请选择业务员" filterable clearable style="width:100%" @change="onSalespersonChange">
                    <el-option v-for="item in salespersonOptions" :key="item.id" :label="item.realName || item.username" :value="item.id" />
                  </el-select>
                </el-form-item>
              </div>
              <div class="form-field">
                <label class="form-field-label"><span class="required">*</span>操作员</label>
                <el-form-item prop="operatorId">
                  <el-select v-model="form.operatorId" placeholder="请选择操作员" filterable clearable style="width:100%">
                    <el-option v-for="item in operatorOptions" :key="item.id" :label="item.realName || item.username" :value="item.id" />
                  </el-select>
                </el-form-item>
              </div>
              <div class="form-field">
                <label class="form-field-label"><span class="required">*</span>客户</label>
                <el-form-item prop="customerId">
                  <el-select v-model="form.customerId" placeholder="请选择客户" filterable clearable style="width:100%">
                    <el-option v-for="item in customerOptions" :key="item.id" :label="item.name" :value="item.id" />
                  </el-select>
                </el-form-item>
              </div>
              <div class="form-field">
                <label class="form-field-label"><span class="required">*</span>贸易条款</label>
                <el-form-item prop="tradeTerm">
                  <el-select v-model="form.tradeTerm" placeholder="请选择贸易条款" style="width:100%">
                    <el-option label="FOB" value="FOB" />
                    <el-option label="CIF" value="CIF" />
                    <el-option label="CFR" value="CFR" />
                  </el-select>
                </el-form-item>
              </div>
              <div class="form-field">
                <label class="form-field-label"><span class="required">*</span>付款方式</label>
                <el-form-item prop="paymentMethod">
                  <el-select v-model="form.paymentMethod" placeholder="请选择付款方式" clearable style="width:100%">
                    <el-option label="TT" value="TT" />
                    <el-option label="LC" value="LC" />
                  </el-select>
                </el-form-item>
              </div>
              <div class="form-field">
                <label class="form-field-label"><span class="required">*</span>币种</label>
                <el-form-item prop="currency">
                  <el-autocomplete v-model="form.currency" :fetch-suggestions="queryCurrency" placeholder="输入或选择币种" clearable style="width:100%" />
                </el-form-item>
              </div>
              <div class="form-field">
                <label class="form-field-label"><span class="required">*</span>目的港</label>
                <el-form-item prop="destinationPort">
                  <el-input v-model="form.destinationPort" placeholder="请输入目的港（英文）" />
                </el-form-item>
              </div>
              <div class="form-field">
                <label class="form-field-label"><span class="required">*</span>运输方式</label>
                <el-form-item prop="transportType">
                  <el-select v-model="form.transportType" placeholder="请选择运输方式" style="width:100%">
                    <el-option label="20'集装箱" value="20'集装箱" />
                    <el-option label="40'集装箱" value="40'集装箱" />
                    <el-option label="散货" value="散货" />
                    <el-option label="铁路" value="铁路" />
                  </el-select>
                </el-form-item>
              </div>
              <div class="form-field">
                <label class="form-field-label"><span class="required">*</span>交货期</label>
                <el-form-item prop="deliveryDate">
                  <el-date-picker v-model="form.deliveryDate" type="date" placeholder="请选择交货期" format="YYYY-MM-DD" value-format="YYYY-MM-DD" style="width:100%" />
                </el-form-item>
              </div>
              <div class="form-field">
                <label class="form-field-label"><span class="required">*</span>预计尾款日期</label>
                <el-form-item prop="expectedReceiptDays">
                  <el-date-picker v-model="form.expectedReceiptDays" type="date" placeholder="请选择日期" format="YYYY-MM-DD" value-format="YYYY-MM-DD" style="width:100%" />
                </el-form-item>
              </div>
              <div class="form-field">
                <label class="form-field-label">状态</label>
                <el-form-item prop="status">
                  <el-select v-model="form.status" placeholder="选择状态" style="width:100%">
                    <el-option v-for="s in statusList" :key="s.code" :label="s.label" :value="s.code" />
                  </el-select>
                </el-form-item>
              </div>
            </div>
          </div>
        </div>

        <!-- ===== 预收（合同 & 定金） ===== -->
        <div class="form-section">
          <div class="form-section-header" @click="toggleSection('deposit')">
            <span class="form-section-title">预收（合同 &amp; 定金）</span>
            <el-icon :class="['form-section-arrow', { collapsed: collapsedSections.deposit }]"><ArrowDown /></el-icon>
          </div>
          <div :class="['form-section-body', { collapsed: collapsedSections.deposit }]" :style="{ maxHeight: collapsedSections.deposit ? '0' : '400px' }">
            <div class="form-grid">
              <div class="form-field">
                <label class="form-field-label">合同金额</label>
                <el-form-item prop="contractAmount">
                  <el-input v-model="form.contractAmount" placeholder="输入合同金额，上传明细可自动填写" />
                </el-form-item>
              </div>
              <div class="form-field">
                <label class="form-field-label">合同总数量</label>
                <el-form-item prop="contractTotalQuantity">
                  <el-input v-model="form.contractTotalQuantity" placeholder="输入合同总数量，上传明细可自动填写" />
                </el-form-item>
              </div>
              <div class="form-field">
                <label class="form-field-label"><span class="required">*</span>定金比例(%)</label>
                <el-form-item prop="depositRate">
                  <el-input v-model="form.depositRate" placeholder="请输入定金比例" />
                </el-form-item>
              </div>
              <div class="form-field">
                <label class="form-field-label">定金汇率</label>
                <el-form-item prop="depositExchangeRate">
                  <el-input v-model="form.depositExchangeRate" placeholder="请输入定金汇率" />
                </el-form-item>
              </div>
              <div class="form-field">
                <label class="form-field-label">定金收款金额</label>
                <el-form-item prop="receivedAmount">
                  <el-input v-model="form.receivedAmount" placeholder="输入金额，绑定收款明细可自动填写" />
                </el-form-item>
              </div>
            </div>
          </div>
        </div>

        <!-- ===== 尾款 & 费用 ===== -->
        <div class="form-section">
          <div class="form-section-header" @click="toggleSection('final')">
            <span class="form-section-title">尾款 &amp; 费用</span>
            <el-icon :class="['form-section-arrow', { collapsed: collapsedSections.final }]"><ArrowDown /></el-icon>
          </div>
          <div :class="['form-section-body', { collapsed: collapsedSections.final }]" :style="{ maxHeight: collapsedSections.final ? '0' : '500px' }">
            <div class="form-grid">
              <div class="form-field">
                <label class="form-field-label">总收款</label>
                <el-form-item prop="actualAmount">
                  <el-input v-model="form.actualAmount" placeholder="输入总收款，绑定收款自动计算" />
                </el-form-item>
              </div>
              <div class="form-field">
                <label class="form-field-label">尾款金额</label>
                <el-form-item prop="finalPaymentAmount">
                  <el-input v-model="form.finalPaymentAmount" placeholder="输入尾款金额，绑定收款自动计算" />
                </el-form-item>
              </div>
              <div class="form-field">
                <label class="form-field-label">尾款汇率</label>
                <el-form-item prop="finalExchangeRate">
                  <el-input v-model="form.finalExchangeRate" placeholder="请输入尾款汇率" />
                </el-form-item>
              </div>
              <div class="form-field">
                <label class="form-field-label">结算总数量</label>
                <el-form-item prop="settlementTotalQuantity">
                  <el-input v-model="form.settlementTotalQuantity" placeholder="完成明细可自动填写" />
                </el-form-item>
              </div>
              <div class="form-field">
                <label class="form-field-label">结算总金额</label>
                <el-form-item prop="settlementTotalAmount">
                  <el-input v-model="form.settlementTotalAmount" disabled placeholder="明细结算金额之和（自动计算）" />
                </el-form-item>
              </div>
              <div class="form-field">
                <label class="form-field-label">海运费(USD)</label>
                <el-form-item prop="seaFreight">
                  <el-input v-model="form.seaFreight" placeholder="关联海运订单自动计算" />
                </el-form-item>
              </div>
              <div class="form-field">
                <label class="form-field-label">港杂费</label>
                <el-form-item prop="portFee">
                  <el-input v-model="form.portFee" placeholder="关联海运订单自动计算" />
                </el-form-item>
              </div>
              <div class="form-field">
                <label class="form-field-label">保额</label>
                <el-form-item prop="insuranceAmount">
                  <el-input v-model="form.insuranceAmount" placeholder="关联海运订单自动计算" />
                </el-form-item>
              </div>
              <div class="form-field">
                <label class="form-field-label">保险费用</label>
                <el-form-item prop="insuranceFee">
                  <el-input v-model="form.insuranceFee" placeholder="关联海运订单自动计算" />
                </el-form-item>
              </div>
            </div>
          </div>
        </div>

        <!-- ===== 订单后（利润 & 损耗） ===== -->
        <div class="form-section">
          <div class="form-section-header" @click="toggleSection('after')">
            <span class="form-section-title">订单后（利润 &amp; 损耗）</span>
            <el-icon :class="['form-section-arrow', { collapsed: collapsedSections.after }]"><ArrowDown /></el-icon>
          </div>
          <div :class="['form-section-body', { collapsed: collapsedSections.after }]" :style="{ maxHeight: collapsedSections.after ? '0' : '300px' }">
            <div class="form-grid">
              <div class="form-field">
                <label class="form-field-label">利润</label>
                <el-form-item prop="profit">
                  <el-input v-model="form.profit" placeholder="请输入利润" />
                </el-form-item>
              </div>
              <div class="form-field">
                <label class="form-field-label">增值税</label>
                <el-form-item prop="vat">
                  <el-input v-model="form.vat" placeholder="请输入增值税" />
                </el-form-item>
              </div>
              <div class="form-field">
                <label class="form-field-label">损耗</label>
                <el-form-item prop="loss">
                  <el-input v-model="form.loss" disabled placeholder="状态变为已完成时自动计算" />
                </el-form-item>
              </div>
            </div>
          </div>
        </div>

      </el-form>

      <!-- ===== 订单产品（明细） ===== -->
      <div class="form-section" style="margin-top: 16px;">
        <div class="form-section-header" @click="toggleSection('products')">
          <div style="display:flex;align-items:center;gap:8px;">
            <span class="form-section-title">订单产品({{ detailList.length }})</span>
          </div>
          <div style="display:flex;align-items:center;gap:8px;">
            <el-button size="small" @click.stop="handleAddDetail">添加产品</el-button>
            <el-icon :class="['form-section-arrow', { collapsed: collapsedSections.products }]"><ArrowDown /></el-icon>
          </div>
        </div>
        <div :class="['form-section-body', { collapsed: collapsedSections.products }]" :style="{ maxHeight: collapsedSections.products ? '0' : '2000px' }">
          <el-table :data="detailList" style="width:100%" :header-cell-style="{ background: '#fafafa', color: '#333', fontWeight: 500 }">
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column label="产品信息" min-width="200">
              <template #default="{ row }">
                <div style="line-height:1.6">
                  <div style="font-weight:500;">{{ row.spec || '-' }}</div>
                  <div style="font-size:12px;color:#999;">{{ [row.productType, row.material, row.length ? row.length + 'm' : ''].filter(Boolean).join(' / ') }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="结算价格" prop="settlementPrice" width="120" align="right">
              <template #header>
                <span><span style="color:#f56c6c;margin-right:2px;">*</span>结算价格</span>
              </template>
            </el-table-column>
            <el-table-column label="订货数量" prop="orderedQuantity" width="110" align="right">
              <template #header>
                <span><span style="color:#f56c6c;margin-right:2px;">*</span>订货数量</span>
              </template>
            </el-table-column>
            <el-table-column label="合同金额小计" prop="priceTotal" width="130" align="right">
              <template #default="{ row }">
                <span style="font-weight:500;color:#e6a23c;">{{ row.priceTotal ?? '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="包装" prop="packaging" width="120" />
            <el-table-column label="操作" width="120" align="center" fixed="right">
              <template #default="{ $index }">
                <el-button link type="primary" @click="handleEditDetail($index)">编辑</el-button>
                <el-button link type="danger" @click="handleDeleteDetail($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 总计 -->
          <div class="detail-summary">
            总计：产品总数量 <b>{{ detailTotalQuantity }}</b>
          </div>
        </div>
      </div>
    </div>

    <!-- 明细 添加/编辑 弹窗 -->
    <el-dialog v-model="detailDialogVisible" :title="detailDialogTitle" width="820px" destroy-on-close @close="resetDetailForm">
      <el-form ref="detailFormRef" :model="detailForm" :rules="detailRules" label-width="120px">
        <el-divider content-position="left" class="group-divider">产品信息</el-divider>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="产品规格" prop="spec">
              <el-input v-model="detailForm.spec" placeholder="输入产品规格" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品类型" prop="productType">
              <el-autocomplete v-model="detailForm.productType" :fetch-suggestions="queryProductType" placeholder="输入或选择产品类型" clearable style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="材质" prop="material">
              <el-input v-model="detailForm.material" placeholder="输入材质" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="长度(米)" prop="length">
              <el-input v-model="detailForm.length" placeholder="输入长度（卷材输入0）" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="公差" prop="tolerance">
              <el-input v-model="detailForm.tolerance" placeholder="输入公差" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left" class="group-divider">价格 &amp; 数量</el-divider>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="结算价格" prop="settlementPrice">
              <el-input v-model="detailForm.settlementPrice" placeholder="输入结算价格" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="订货数量" prop="orderedQuantity">
              <el-input v-model="detailForm.orderedQuantity" placeholder="输入订货数量" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="实际数量" prop="actualQuantity">
              <el-input v-model="detailForm.actualQuantity" placeholder="输入实际数量" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="合同金额小计">
              <el-input :model-value="computedPriceTotal" disabled placeholder="自动计算" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="结算金额小计">
              <el-input :model-value="computedSettlementAmount" disabled placeholder="自动计算" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left" class="group-divider">重量 / 体积 / 包装</el-divider>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="捆数" prop="bundleCount">
              <el-input v-model="detailForm.bundleCount" placeholder="输入捆数" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="净重" prop="netWeight">
              <el-input v-model="detailForm.netWeight" placeholder="输入净重" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="毛重" prop="grossWeight">
              <el-input v-model="detailForm.grossWeight" placeholder="输入毛重" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="体积" prop="volume">
              <el-input v-model="detailForm.volume" placeholder="输入体积" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="包装重量" prop="packagingWeight">
              <el-input v-model="detailForm.packagingWeight" placeholder="输入包装重量" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="包装" prop="packaging">
              <el-input v-model="detailForm.packaging" placeholder="输入包装方式" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="卷内径(mm)" prop="coilInnerDiameter">
              <el-input v-model="detailForm.coilInnerDiameter" placeholder="输入卷内径" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="实际理论重量" prop="actualTheoreticalWeight">
              <el-input v-model="detailForm.actualTheoreticalWeight" placeholder="输入" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="货源地" prop="originPlace">
              <el-input v-model="detailForm.originPlace" placeholder="输入货源地" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="加工项" prop="processingItems">
              <el-input v-model="detailForm.processingItems" placeholder="输入加工项" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="detailForm.remark" type="textarea" :rows="2" placeholder="输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="detailDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="detailSubmitLoading" @click="handleDetailSubmit">确认</el-button>
      </template>
    </el-dialog>

    <!-- 底部操作栏 -->
    <div class="form-page-footer">
      <el-button @click="goBack">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, ArrowDown } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import { getSalesOrderById, saveSalesOrder, updateSalesOrder } from '@/api/salesOrder'
import { getDetailListByOrderId, saveSalesOrderDetail, updateSalesOrderDetail, deleteSalesOrderDetail } from '@/api/salesOrderDetail'
import { getUserListWithRoles } from '@/api/system'
import { getCustomerPage } from '@/api/customer'
import { getProductTypeList } from '@/api/productType'
import { useOrderStatus } from '@/composables/useOrderStatus'

const route = useRoute()
const router = useRouter()
const { statusList } = useOrderStatus('sales')

const formRef = ref<FormInstance>()
const submitLoading = ref(false)
const salespersonOptions = ref<any[]>([])
const operatorOptions = ref<any[]>([])
const customerOptions = ref<any[]>([])

// 是否编辑模式
const isEdit = ref(false)
const editId = ref<number | null>(null)

// 折叠状态
const collapsedSections = reactive<Record<string, boolean>>({
  basic: false,
  deposit: false,
  final: false,
  after: false,
  products: false
})

const toggleSection = (key: string) => {
  collapsedSections[key] = !collapsedSections[key]
}

// 表单数据
const form = reactive<any>({
  id: null,
  orderNo: '',
  customerId: null,
  salespersonId: null,
  operatorId: null,
  createId: null,
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
  status: 1
})

const rules = {
  orderNo: [{ required: true, message: '请输入订单号', trigger: 'blur' }],
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  tradeTerm: [{ required: true, message: '请选择贸易条款', trigger: 'change' }],
  salespersonId: [{ required: true, message: '请选择业务员', trigger: 'change' }],
  operatorId: [{ required: true, message: '请选择操作员', trigger: 'change' }],
  currency: [{ required: true, message: '请输入币种', trigger: 'blur' }],
  destinationPort: [{ required: true, message: '请输入目的港', trigger: 'blur' }],
  depositRate: [{ required: true, message: '请输入定金比例', trigger: 'blur' }],
  deliveryDate: [{ required: true, message: '请选择交货日期', trigger: 'blur' }],
  transportType: [{ required: true, message: '请选择运输方式', trigger: 'change' }],
  expectedReceiptDays: [{ required: true, message: '请选择预计尾款日期', trigger: 'blur' }],
  paymentMethod: [{ required: true, message: '请选择付款方式', trigger: 'change' }]
}

// 币种自动补全
const currencyOptions = ['USD', 'RMB']
const queryCurrency = (queryString: string, cb: (results: { value: string }[]) => void) => {
  const results = queryString
    ? currencyOptions.filter(c => c.toLowerCase().includes(queryString.toLowerCase())).map(c => ({ value: c }))
    : currencyOptions.map(c => ({ value: c }))
  cb(results)
}

// 返回列表
const goBack = () => {
  router.push({ name: 'SalesOrderList' })
}

// 加载业务员 / 操作员选项
const loadUserOptions = async () => {
  const res = await getUserListWithRoles()
  const list: any[] = res.data || []
  salespersonOptions.value = list
  operatorOptions.value = list
}

// 根据业务员加载客户
const loadCustomerOptions = async (salespersonId: number | null | undefined) => {
  if (!salespersonId) {
    customerOptions.value = []
    return
  }
  const res = await getCustomerPage({ pageNum: 1, pageSize: 1000, salesUserId: salespersonId })
  customerOptions.value = res.data?.list || []
}

const onSalespersonChange = async (salespersonId: number | null) => {
  form.customerId = null
  await loadCustomerOptions(salespersonId)
}

// 加载编辑数据
const loadEditData = async (id: number) => {
  try {
    const res = await getSalesOrderById(id)
    const data = res.data
    Object.assign(form, {
      id: data.id,
      orderNo: data.orderNo,
      customerId: data.customerId,
      salespersonId: data.salespersonId,
      operatorId: data.operatorId,
      tradeTerm: data.tradeTerm,
      paymentMethod: data.paymentMethod,
      currency: data.currency,
      depositExchangeRate: data.depositExchangeRate ?? null,
      finalExchangeRate: data.finalExchangeRate ?? null,
      contractAmount: data.contractAmount ?? null,
      actualAmount: data.actualAmount ?? null,
      depositRate: data.depositRate ?? null,
      receivedAmount: data.receivedAmount ?? null,
      finalPaymentAmount: data.finalPaymentAmount ?? null,
      insuranceFee: data.insuranceFee ?? null,
      insuranceAmount: data.insuranceAmount ?? null,
      expectedReceiptDays: data.expectedReceiptDays ?? null,
      deliveryDate: data.deliveryDate ?? null,
      destinationPort: data.destinationPort ?? '',
      transportType: data.transportType ?? '',
      seaFreight: data.seaFreight ?? null,
      portFee: data.portFee ?? null,
      vat: data.vat ?? null,
      profit: data.profit ?? null,
      loss: data.loss ?? null,
      contractTotalQuantity: data.contractTotalQuantity ?? null,
      settlementTotalQuantity: data.settlementTotalQuantity ?? null,
      settlementTotalAmount: data.settlementTotalAmount ?? null,
      status: data.status ?? 1
    })
    await loadCustomerOptions(form.salespersonId)
  } catch {
    ElMessage.error('加载订单数据失败')
    goBack()
  }
}

// 提交
const handleSubmit = async () => {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (isEdit.value && editId.value) {
      await updateSalesOrder({ ...form })
      ElMessage.success('更新成功')
    } else {
      const payload = { ...form }
      delete payload.id
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      payload.createId = userInfo.userId || null
      const res = await saveSalesOrder(payload)
      // 新建订单后，把本地暂存的明细批量提交
      const newOrderId = res.data?.id || res.data
      if (newOrderId && detailList.value.length > 0) {
        for (const item of detailList.value) {
          const detailPayload = { ...item, orderId: newOrderId }
          delete detailPayload.id
          delete detailPayload.priceTotal
          delete detailPayload.settlementAmount
          await saveSalesOrderDetail(detailPayload)
        }
      }
      ElMessage.success('创建成功')
    }
    goBack()
  } finally {
    submitLoading.value = false
  }
}

// ==================== 订单产品（明细）====================
const detailList = ref<any[]>([])
const detailDialogVisible = ref(false)
const detailDialogTitle = ref('')
const detailSubmitLoading = ref(false)
const detailFormRef = ref<FormInstance>()
const detailEditIndex = ref<number | null>(null) // 编辑时的行索引
const productTypeOptions = ref<string[]>([])

const detailForm = reactive<any>({
  id: null,
  orderId: null,
  spec: '',
  productType: '',
  material: '',
  length: '',
  tolerance: '',
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

const detailRules = {
  spec: [{ required: true, message: '请输入产品规格', trigger: 'blur' }],
  productType: [{ required: true, message: '请输入产品类型', trigger: 'blur' }],
  material: [{ required: true, message: '请输入材质', trigger: 'blur' }],
  length: [{ required: true, message: '请输入长度', trigger: 'blur' }],
  tolerance: [{ required: true, message: '请输入公差', trigger: 'blur' }]
}

const detailTotalQuantity = computed(() => {
  return detailList.value.reduce((sum: number, row: any) => sum + (Number(row.orderedQuantity) || 0), 0)
})

const computedPriceTotal = computed(() => {
  const price = parseFloat(detailForm.settlementPrice)
  const qty = parseFloat(detailForm.orderedQuantity)
  if (!isNaN(price) && !isNaN(qty)) return (price * qty).toFixed(2)
  return ''
})

const computedSettlementAmount = computed(() => {
  const price = parseFloat(detailForm.settlementPrice)
  const qty = parseFloat(detailForm.actualQuantity)
  if (!isNaN(price) && !isNaN(qty)) return (price * qty).toFixed(2)
  return ''
})

const loadProductTypes = async () => {
  try {
    const res = await getProductTypeList()
    productTypeOptions.value = (res.data || []).map((pt: any) => pt.typeName)
  } catch { /* ignore */ }
}

const queryProductType = (queryString: string, cb: (results: { value: string }[]) => void) => {
  const opts = productTypeOptions.value
  const results = queryString
    ? opts.filter(t => t.toLowerCase().includes(queryString.toLowerCase())).map(t => ({ value: t }))
    : opts.map(t => ({ value: t }))
  cb(results)
}

const resetDetailForm = () => {
  detailForm.id = null
  detailForm.orderId = null
  detailForm.spec = ''
  detailForm.productType = ''
  detailForm.material = ''
  detailForm.length = ''
  detailForm.tolerance = ''
  detailForm.settlementPrice = null
  detailForm.packagingWeight = null
  detailForm.packaging = ''
  detailForm.coilInnerDiameter = ''
  detailForm.processingItems = ''
  detailForm.remark = ''
  detailForm.detailSeq = null
  detailForm.orderedQuantity = null
  detailForm.actualQuantity = null
  detailForm.bundleCount = null
  detailForm.netWeight = null
  detailForm.grossWeight = null
  detailForm.volume = null
  detailForm.originPlace = ''
  detailForm.actualTheoreticalWeight = null
  detailEditIndex.value = null
  detailFormRef.value?.clearValidate()
}

const handleAddDetail = () => {
  resetDetailForm()
  detailDialogTitle.value = '添加产品'
  detailDialogVisible.value = true
}

const handleEditDetail = (index: number) => {
  resetDetailForm()
  const row = detailList.value[index]
  Object.assign(detailForm, { ...row })
  detailEditIndex.value = index
  detailDialogTitle.value = '编辑产品'
  detailDialogVisible.value = true
}

const handleDeleteDetail = async (index: number) => {
  await ElMessageBox.confirm('确认删除该产品？', '提示', { type: 'warning' })
  const row = detailList.value[index]
  // 如果已保存到后端则调API删除
  if (row.id) {
    await deleteSalesOrderDetail(row.id)
  }
  detailList.value.splice(index, 1)
  ElMessage.success('删除成功')
}

// 加载已保存的明细（编辑模式）
const loadDetails = async (orderId: number) => {
  try {
    const res = await getDetailListByOrderId(orderId)
    detailList.value = res.data || []
  } catch { /* ignore */ }
}

const handleDetailSubmit = async () => {
  await detailFormRef.value?.validate()
  detailSubmitLoading.value = true
  try {
    const payload = { ...detailForm }

    // 如果是编辑模式且订单已保存，直接调API
    if (isEdit.value && editId.value) {
      payload.orderId = editId.value
      if (payload.id) {
        await updateSalesOrderDetail(payload)
        ElMessage.success('更新成功')
      } else {
        delete payload.id
        await saveSalesOrderDetail(payload)
        ElMessage.success('添加成功')
      }
      // 刷新列表
      await loadDetails(editId.value)
    } else {
      // 新建模式 — 暂存在本地列表中，保存订单后再提交
      // 计算 priceTotal
      const price = parseFloat(payload.settlementPrice) || 0
      const qty = parseFloat(payload.orderedQuantity) || 0
      const actualQty = parseFloat(payload.actualQuantity) || 0
      payload.priceTotal = price * qty || null
      payload.settlementAmount = price * actualQty || null

      if (detailEditIndex.value !== null) {
        detailList.value[detailEditIndex.value] = { ...payload }
      } else {
        detailList.value.push({ ...payload })
      }
      ElMessage.success(detailEditIndex.value !== null ? '已更新' : '已添加')
    }
    detailDialogVisible.value = false
  } finally {
    detailSubmitLoading.value = false
  }
}

// ==================== 订单产品 END ====================

// 初始化
onMounted(async () => {
  await Promise.all([loadUserOptions(), loadProductTypes()])

  // 如果路由带了 id 参数，则为编辑模式
  const id = route.query.id ? Number(route.query.id) : null
  if (id) {
    isEdit.value = true
    editId.value = id
    await loadEditData(id)
    await loadDetails(id)
  }
})
</script>

<style scoped>
.detail-summary {
  padding: 12px 0;
  font-size: 13px;
  color: #4066e0;
  border-top: 1px solid #f0f0f0;
  margin-top: 8px;
}
.detail-summary b {
  font-weight: 600;
  color: #333;
}
.group-divider {
  margin: 8px 0 16px;
}
</style>
