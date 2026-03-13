<template>
  <div class="app-container">
    <!-- 1. Search Form -->
    <el-card shadow="never" class="search-wrap">
      <el-form :inline="true" :model="queryParams" ref="queryRef">
        <el-form-item label="订单号" prop="orderNo">
          <el-input v-model="queryParams.orderNo" placeholder="输入订单号" clearable />
        </el-form-item>
       
        <el-form-item label="操作人员" prop="operatorName">
          <el-autocomplete
            v-model="queryParams.operatorName"
            :fetch-suggestions="queryOperator"
            placeholder="输入或选择操作人员"
            clearable
            style="width: 160px"
            @select="onOperatorSelect"
          />
        </el-form-item>
        <el-form-item label="业务人员" prop="salespersonName">
          <el-autocomplete
            v-model="queryParams.salespersonName"
            :fetch-suggestions="querySalesperson"
            placeholder="输入或选择业务人员"
            clearable
            style="width: 160px"
            @select="onSalespersonSelect"
          />
        </el-form-item>

        <el-form-item label="订单状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="选择订单状态" clearable style="width: 120px">
            <el-option label="待处理" :value="1" />
            <el-option label="采购中" :value="2" />
            <el-option label="生产中" :value="3" />
            <el-option label="已发货" :value="4" />
            <el-option label="已完成" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 2. Toolbar & Table -->
    <el-card shadow="never" class="table-wrap">
      <div class="table-toolbar">
        <el-button type="primary" icon="Plus" @click="handleAdd">新建订单</el-button>
        <el-button type="success" icon="Download" @click="handleExport">导出</el-button>
      </div>

      <el-table v-loading="loading" :data="orderList" border stripe>
        <el-table-column label="订单号" prop="orderNo" min-width="150" />

        <el-table-column label="日期" prop="createTime" min-width="120">
          <template #default="{ row }">
            {{ row.createTime ? row.createTime.slice(0, 10) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="业务员" prop="salespersonName" width="120" />
        <el-table-column label="操作人员" prop="operatorName" width="120" />
        <el-table-column label="合同总数量" prop="contractTotalQuantity" width="120" align="right" />
        <el-table-column label="合同金额" width="160" align="right">
          <template #default="{ row }">
            <span class="currency-text">{{ row.currency }} </span>
            <span>{{ row.contractAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="贸易条款" prop="tradeTerm" width="100" />
        <el-table-column label="付款方式" prop="paymentMethod" width="100" />
        <!-- <el-table-column label="币种" prop="currency" width="100" /> -->

        <el-table-column label="目的港" prop="destinationPort" width="120" align="center" />
        <el-table-column label="运输方式" prop="transportType" width="120" />
        <el-table-column label="交货期" prop="deliveryDate" width="120" align="center" />
        <el-table-column label="总收款" width="160" align="right">
          <template #default="{ row }">
            <span class="currency-text">{{ row.currency }} </span>
            <span>{{ row.actualAmount }}</span>
          </template>
        </el-table-column>
       

        
       
       

        <el-table-column label="定金收款金额" prop="receivedAmount" width="120" align="right" />
        <el-table-column label="尾款金额" prop="finalPaymentAmount" width="120" align="right" />
        <!-- <el-table-column label="保险费用" prop="insuranceFee" width="120" align="right" />
        <el-table-column label="保额" prop="insuranceAmount" width="120" align="right" /> -->
        <el-table-column label="预计尾款日期" prop="expectedReceiptDays" width="140" align="center" />
        
        <el-table-column label="运输方式" prop="transportType" width="120" />
        <el-table-column label="损耗" prop="loss" width="120" align="right" />
        <el-table-column label="合同总数量" prop="contractTotalQuantity" width="120" align="right" />
        <el-table-column label="结算总数量" prop="settlementTotalQuantity" width="120" align="right" />
        <el-table-column label="状态" prop="status" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="160" />
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" @click="handleDetail(scope.row)">详情</el-button>
            <el-button link type="success" @click="handleGoDetail(scope.row)">明细</el-button>
            <el-button link type="primary" v-if="scope.row.status === 1" @click="handleEdit(scope.row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 3. Pagination -->
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="900px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-divider content-position="left" class="group-divider">预收</el-divider>


        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="订单号" prop="orderNo">
              <el-input v-model="form.orderNo" placeholder="输入订单号" :disabled="!!form.id" />
            </el-form-item>
          </el-col>
           <el-col :span="12">
            <el-form-item label="合同金额" prop="contractAmount">
              <el-input v-model="form.contractAmount" placeholder="输入合同金额" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="业务员" prop="salespersonId">
              <el-select
                v-model="form.salespersonId"
                placeholder="选择业务员"
                filterable
                clearable
                style="width: 100%"
                @change="onSalespersonChange"
              >
                <el-option
                  v-for="item in salespersonOptions"
                  :key="item.id"
                  :label="item.realName || item.username"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
         <el-col :span="12">
            <el-form-item label="定金比例(%)" prop="depositRate">
              <el-input v-model="form.depositRate" placeholder="输入定金比例" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
           <el-col :span="12">
            <el-form-item label="操作员" prop="operatorId">
              <el-select
                v-model="form.operatorId"
                placeholder="选择操作员"
                filterable
                clearable
                style="width: 100%"
              >
                <el-option
                  v-for="item in operatorOptions"
                  :key="item.id"
                  :label="item.realName || item.username"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="付款方式" prop="paymentMethod">
              <el-select v-model="form.paymentMethod" placeholder="选择付款方式" clearable>
                <el-option label="TT" value="TT" />
                <el-option label="LC" value="LC" />
              </el-select>
            </el-form-item>
          </el-col>
           
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="客户" prop="customerId">
              <el-select v-model="form.customerId" placeholder="选择客户" filterable clearable style="width: 100%">
                <el-option
                  v-for="item in customerOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
           <el-col :span="12">
            <el-form-item label="定金收款金额" prop="receivedAmount">
              <el-input v-model="form.receivedAmount" placeholder="输入定金收款金额" />
            </el-form-item>
          </el-col>
         
         
        </el-row>

        <el-row :gutter="16">
         
         
        </el-row>

        <el-row :gutter="16">
           <el-col :span="12">
            <el-form-item label="币种" prop="currency">
              <el-autocomplete
                v-model="form.currency"
                :fetch-suggestions="queryCurrency"
                placeholder="输入或选择币种"
                clearable
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
           <el-col :span="12">
            <el-form-item label="定金汇率" prop="depositExchangeRate">
              <el-input v-model="form.depositExchangeRate" placeholder="输入定金汇率" />
            </el-form-item>
          </el-col>
         
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="贸易条款" prop="tradeTerm">
              <el-select v-model="form.tradeTerm" placeholder="选择贸易条款">
                <el-option label="FOB" value="FOB" />
                <el-option label="CIF" value="CIF" />
                <el-option label="CFR" value="CFR" />
              </el-select>
            </el-form-item>
          </el-col>
          
            <el-col :span="12">
            <el-form-item label="预计尾款日期" prop="expectedReceiptDays">
              <el-date-picker
              v-model="form.expectedReceiptDays"
              type="date"
              placeholder="选择日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 100%"
              />
            </el-form-item>
            </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="目的港" prop="destinationPort">
              <el-input v-model="form.destinationPort" placeholder="输入目的港（英文）" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="交货期" prop="deliveryDate">
              <el-date-picker
                v-model="form.deliveryDate"
                type="date"
                placeholder="选择交货期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>



          
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="合同总数量" prop="contractTotalQuantity">
              <el-input v-model="form.contractTotalQuantity" placeholder="输入合同总数量" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="运输方式" prop="transportType">
              <el-select v-model="form.transportType" placeholder="选择运输方式">
                <el-option label="20‘集装箱" value="20'集装箱" />
                <el-option label="40‘集装箱" value="40'集装箱" />
                <el-option label="散货" value="散货" />
                <el-option label="铁路" value="铁路" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>



        <el-divider content-position="left" class="group-divider">尾款</el-divider>

        <el-row :gutter="16">
           <el-col :span="12">
            <el-form-item label="总收款" prop="actualAmount">
              <el-input v-model="form.actualAmount" placeholder="输入总收款" />
            </el-form-item>

          </el-col>
         <el-col :span="12">
            <el-form-item label="结算总数量" prop="settlementTotalQuantity">
              <el-input v-model="form.settlementTotalQuantity" placeholder="输入结算总数量" />
            </el-form-item>
          </el-col>
        </el-row>
         
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="尾款金额" prop="finalPaymentAmount">
              <el-input v-model="form.finalPaymentAmount" placeholder="输入尾款金额" />
            </el-form-item>
          </el-col>

           <el-col :span="12">
            <el-form-item label="海运费(USD)" prop="seaFreight">
              <el-input v-model="form.seaFreight" placeholder="输入海运费(USD)" />
            </el-form-item>
          </el-col>
          
          
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="尾款汇率" prop="finalExchangeRate">
              <el-input v-model="form.finalExchangeRate" placeholder="输入尾款汇率" />
            </el-form-item>
          </el-col>
           <el-col :span="12">
            <el-form-item label="港杂费" prop="portFee">
              <el-input v-model="form.portFee" placeholder="输入港杂费" />
            </el-form-item>
          </el-col>
           
        </el-row>
      
        <el-row :gutter="16">
           <el-col :span="12">
            <el-form-item label="保额" prop="insuranceAmount">
              <el-input v-model="form.insuranceAmount" placeholder="输入保额" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="保险费用" prop="insuranceFee">
              <el-input v-model="form.insuranceFee" placeholder="输入保险费用" />
            </el-form-item>
          </el-col>
         
        </el-row>

        <el-divider content-position="left" class="group-divider">订单后</el-divider>        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="利润" prop="profit">
              <el-input v-model="form.profit" placeholder="输入利润" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="增值税" prop="vat">
              <el-input v-model="form.vat" placeholder="输入增值税" />
            </el-form-item>
          </el-col>
         
        </el-row>


        <el-row :gutter="16" >
          <el-col :span="12">
            <el-form-item label="损耗" prop="loss">
              <el-input v-model="form.loss" placeholder="状态变为已完成时自动计算" disabled />
            </el-form-item>
          </el-col>
           <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="选择状态">
                <el-option label="待处理" :value="1" />
                <el-option label="采购中" :value="2" />
                <el-option label="生产中" :value="3" />
                <el-option label="已发货" :value="4" />
                <el-option label="已完成" :value="5" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailDialogVisible" title="订单详情" width="900px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ detailData.orderNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ getStatusLabel(detailData.status) }}</el-descriptions-item>
        <el-descriptions-item label="客户ID">{{ detailData.customerId ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="业务员ID">{{ detailData.salespersonId ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作员ID">{{ detailData.operatorId ?? '-' }}</el-descriptions-item>
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
        <el-descriptions-item label="创建时间">{{ detailData.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailData.updateTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { exportToCsv } from '@/utils/export'
import { getOrderPage, saveSalesOrder, updateSalesOrder } from '@/api/salesOrder'
import { getUserListWithRoles } from '@/api/system'
import { getCustomerPage } from '@/api/customer'

// Data definitions
const loading = ref(false)
const router = useRouter()
const submitLoading = ref(false)
const orderList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const detailDialogVisible = ref(false)
const formRef = ref<FormInstance>()
const salespersonOptions = ref<any[]>([])
const operatorOptions = ref<any[]>([])
const customerOptions = ref<any[]>([])
const allUsers = ref<any[]>([])
const businessUsers = ref<any[]>([])
const operatorUsers = ref<any[]>([])
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
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
  status: 1
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
  status: 1,
  createTime: '',
  updateTime: ''
})

const rules = {
  orderNo: [{ required: true, message: '请输入订单号', trigger: 'blur' }],
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  tradeTerm: [{ required: true, message: '请选择贸易条款', trigger: 'change' }],
  salespersonId: [{ required: true, message: '请选择业务员', trigger: 'change' }],
  operatorId: [{ required: true, message: '请选择操作员', trigger: 'change' }],
  currency: [{ required: true, message: '请输入币种', trigger: 'blur' }],
  destinationPort: [{ required: true, message: '请输入目的港', trigger: 'blur' }],
  depositExchangeRate: [{ required: true, message: '请输入定金汇率', trigger: 'blur' }],
  contractAmount: [{ required: true, message: '请输入合同金额', trigger: 'blur' }],
  depositRate: [{ required: true, message: '请输入定金比例', trigger: 'blur' }],
  receivedAmount: [{ required: true, message: '请输入定金收款金额', trigger: 'blur' }],
  deliveryDate: [{ required: true, message: '请输入交货日期', trigger: 'blur' }],
  contractTotalQuantity: [{ required: true, message: '请输入合同总数量', trigger: 'blur' }],
  transportType: [{ required: true, message: '请选择运输方式', trigger: 'change' }],
  expectedReceiptDays: [{ required: true, message: '请输入预计收尾款天数', trigger: 'blur' }],
  paymentMethod: [{ required: true, message: '请选择付款方式', trigger: 'change' }]
}

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

// Currency autocomplete
const currencyOptions = ['USD', 'RMB']
const queryCurrency = (queryString: string, cb: (results: { value: string }[]) => void) => {
  const results = queryString
    ? currencyOptions.filter(c => c.toLowerCase().includes(queryString.toLowerCase())).map(c => ({ value: c }))
    : currencyOptions.map(c => ({ value: c }))
  cb(results)
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
  resetForm()
  dialogTitle.value = '新建订单'
  dialogVisible.value = true
}
const handleGoDetail = (row: any) => {
  router.push({ name: 'SalesOrderDetail', params: { orderId: row.id }, query: { orderNo: row.orderNo } })
}
const handleDetail = (row: any) => {
  Object.assign(detailData, {
    id: row.id,
    orderNo: row.orderNo,
    customerId: row.customerId,
    salespersonId: row.salespersonId,
    operatorId: row.operatorId,
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
    status: row.status ?? 1,
    createTime: row.createTime ?? '',
    updateTime: row.updateTime ?? ''
  })
  detailDialogVisible.value = true
}
const handleEdit = async (row: any) => {
  resetForm()
  Object.assign(form, {
    id: row.id,
    orderNo: row.orderNo,
    customerId: row.customerId,
    salespersonId: row.salespersonId,
    operatorId: row.operatorId,
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
    loss: row.loss ?? null,
    profit: row.profit ?? null,
    contractTotalQuantity: row.contractTotalQuantity ?? null,
    settlementTotalQuantity: row.settlementTotalQuantity ?? null,
    status: row.status ?? 1
  })
  await loadCustomerOptionsBySalespersonId(form.salespersonId)
  dialogTitle.value = '编辑订单'
  dialogVisible.value = true
}

const resetForm = () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  form.id = null
  form.orderNo = ''
  form.customerId = null
  form.salespersonId = null
  form.operatorId = null
  form.createId = userInfo.userId || null
  form.tradeTerm = ''
  form.paymentMethod = ''
  form.currency = ''
  form.depositExchangeRate = null
  form.finalExchangeRate = null
  form.contractAmount = null
  form.actualAmount = null
  form.depositRate = null
  form.receivedAmount = null
  form.finalPaymentAmount = null
  form.insuranceFee = null
  form.insuranceAmount = null
  form.expectedReceiptDays = null
  form.deliveryDate = null
  form.destinationPort = ''
  form.transportType = ''
  form.seaFreight = null
  form.portFee = null
  form.vat = null
  form.loss = null
  form.profit = null
  form.contractTotalQuantity = null
  form.settlementTotalQuantity = null
  form.status = 1
  customerOptions.value = []
  formRef.value?.clearValidate()
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (form.id) {
      await updateSalesOrder({ ...form })
    } else {
      const payload = { ...form }
      delete payload.id
      await saveSalesOrder(payload)
    }
    ElMessage.success(form.id ? '更新成功' : '创建成功')
    dialogVisible.value = false
    getList()
  } finally {
    submitLoading.value = false
  }
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
    { label: '状态', value: (r: any) => getStatusLabel(r.status) },
    { label: '创建时间', key: 'createTime' }
  ])
}

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
  operatorUsers.value = list.filter((user: any) =>
    user.roleNames?.some((name: string) => name.includes('操作'))
  )
  // 供新建/编辑表单的业务员下拉
  salespersonOptions.value = list
  // 供新建/编辑表单的操作员下拉
  operatorOptions.value = list.filter((user: any) =>
    user.roleNames?.some((name: string) => name.includes('操作'))
  )
}

const loadCustomerOptionsBySalespersonId = async (salespersonId: number | null | undefined) => {
  if (!salespersonId) {
    customerOptions.value = []
    return
  }
  const res = await getCustomerPage({
    pageNum: 1,
    pageSize: 1000,
    salesUserId: salespersonId
  })
  customerOptions.value = res.data?.list || []
}

const onSalespersonChange = async (salespersonId: number | null) => {
  form.customerId = null
  await loadCustomerOptionsBySalespersonId(salespersonId)
}

// Status dictionary
const getStatusLabel = (status: number) => {
  const map: Record<number, string> = { 1: '待处理', 2: '采购中', 3: '生产中', 4: '已发货', 5: '已完成' }
  return map[status] || '未知'
}
const getStatusType = (status: number) => {
  const map: Record<number, string> = { 1: 'warning', 2: 'primary', 3: 'info', 4: 'success', 5: 'info' }
  return map[status] || ''
}

// Init
onMounted(() => {
  loadSalespersonOptions()
  getList()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.search-wrap {
  margin-bottom: 16px;
}
.table-toolbar {
  margin-bottom: 16px;
}
.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.currency-text {
  font-weight: bold;
  color: #606266;
}
.group-divider {
  margin: 8px 0 16px;
}
</style>
