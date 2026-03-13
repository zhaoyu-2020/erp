<template>
  <div class="app-container">
    <el-card shadow="never" class="search-wrap">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="采购单号">
          <el-input v-model="queryParams.poNo" placeholder="输入采购单号" clearable />
        </el-form-item>
        <el-form-item label="关联销售单号">
          <el-input v-model="queryParams.salesOrderNo" placeholder="输入销售单号" clearable />
        </el-form-item>
        <el-form-item label="创建人">
          <el-autocomplete
            v-model="queryParams.createUserName"
            :fetch-suggestions="queryCreateUser"
            placeholder="输入或选择创建人"
            clearable
            style="width: 160px"
            @select="onCreateUserSelect"
          />
        </el-form-item>
        <el-form-item label="业务人员">
          <el-autocomplete
            v-model="queryParams.salespersonName"
            :fetch-suggestions="querySalesperson"
            placeholder="输入或选择业务人员"
            clearable
            style="width: 160px"
            @select="onSalespersonSelect"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="选择状态" clearable>
            <el-option v-for="s in statusList" :key="s.code" :label="s.label" :value="s.code" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-wrap">
      <div class="table-toolbar">
        <el-button type="primary" icon="Plus" @click="handleAdd">新增采购单</el-button>
        <el-button type="success" icon="Download" @click="handleExport">导出</el-button>
      </div>

      <el-table v-loading="loading" :data="dataList" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="采购单号" prop="poNo" width="140" />
        <el-table-column label="订单日期" prop="orderDate" width="120" />
        <el-table-column label="供应商" prop="supplierName" width="140" />
        <el-table-column label="关联销售单号" prop="salesOrderNo" width="160" />
        <el-table-column label="总金额(RMB)" prop="totalAmount" width="160" align="right" />
        <el-table-column label="实际金额(RMB)" prop="actualAmount" width="160" align="right" />
        <el-table-column label="定金比例(%)" prop="depositRate" width="120" align="right" />
        <el-table-column label="定金金额(RMB)" prop="depositAmount" width="160" align="right" />
        <el-table-column label="状态" prop="status" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getTagType(row.status)">{{ getLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="160" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" @click="handleDetail(scope.row)">详情</el-button>
            <el-button link type="primary" @click="goToDetail(scope.row)">明细</el-button>
            <el-button link type="primary" v-if="scope.row.status === 1" @click="handleEdit(scope.row)">编辑</el-button>
            <el-dropdown
              v-if="getAllowedNextStatuses(scope.row.status).length > 0"
              @command="(code: number) => changeStatus(scope.row.id, code, getList)"
            >
              <el-button link type="primary">
                变更状态<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="s in getAllowedNextStatuses(scope.row.status)"
                    :key="s.code"
                    :command="s.code"
                  >{{ s.label }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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
      />
    </el-card>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="900px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-divider content-position="left" class="group-divider">必填信息</el-divider>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="采购单号" prop="poNo">
              <el-input v-model="form.poNo" placeholder="输入采购单号" :disabled="!!form.id" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商ID" prop="supplierId">
              <el-select
                v-model="form.supplierId"
                filterable
                remote
                reserve-keyword
                clearable
                placeholder="输入关键字搜索供应商"
                :remote-method="loadSupplierOptions"
                :loading="supplierOptionsLoading"
                style="width: 100%"
                @focus="loadSupplierOptions('')"
              >
                <el-option
                  v-for="item in supplierOptions"
                  :key="item.id"
                  :label="`${item.name}（${item.supplierCode || item.id}）`"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="关联销售单号" prop="salesOrderNo">
              <el-input v-model="form.salesOrderNo" placeholder="输入关联销售单号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="总金额(RMB)" prop="totalAmount">
              <el-input v-model="form.totalAmount" placeholder="输入订单金额" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          
          <el-col :span="12">
            <el-form-item label="定金比例(%)" prop="depositRate">
              <el-input v-model="form.depositRate" placeholder="输入定金比例" />
            </el-form-item>
          </el-col>
           <el-col :span="12">
            <el-form-item label="定金金额(RMB)" prop="depositAmount">
              <el-input v-model="form.depositAmount" placeholder="输入定金金额" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="订单日期" prop="orderDate">
              <el-date-picker v-model="form.orderDate" type="date" placeholder="选择订单日期" format="YYYY-MM-DD" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="交货日期" prop="deliveryDate">
              <el-date-picker v-model="form.deliveryDate" type="date" placeholder="选择交货日期" format="YYYY-MM-DD" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left" class="group-divider">非必填信息</el-divider>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="实际金额(RMB)" prop="actualAmount">
              <el-input v-model="form.actualAmount" placeholder="输入实际金额" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          
          <el-col :span="12">
            <el-form-item label="运输备注" prop="transportRemark">
              <el-input v-model="form.transportRemark" placeholder="输入运输备注" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="总运费(RMB)" prop="totalFreight">
              <el-input v-model="form.totalFreight" placeholder="输入总运费" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="照片">
              <div class="file-upload-area">
                <el-upload
                  multiple
                  :auto-upload="false"
                  :on-change="(f: any) => handleFileChange(f, 'photosFileList')"
                  :on-remove="(f: any) => handleFileRemove(f, 'photosFileList')"
                  :file-list="photosFileList"
                  :show-file-list="false"
                >
                  <el-button size="small" icon="Upload">选择文件</el-button>
                </el-upload>
                <div v-if="photosFileList.length" class="file-list">
                  <div v-for="(f, i) in photosFileList" :key="i" class="file-item">
                    <span>{{ f.name }}</span>
                    <el-button link type="danger" size="small" @click="removeFile(i, 'photosFileList')">删除</el-button>
                  </div>
                </div>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="材质单">
              <div class="file-upload-area">
                <el-upload
                  multiple
                  :auto-upload="false"
                  :on-change="(f: any) => handleFileChange(f, 'materialFileList')"
                  :on-remove="(f: any) => handleFileRemove(f, 'materialFileList')"
                  :file-list="materialFileList"
                  :show-file-list="false"
                >
                  <el-button size="small" icon="Upload">选择文件</el-button>
                </el-upload>
                <div v-if="materialFileList.length" class="file-list">
                  <div v-for="(f, i) in materialFileList" :key="i" class="file-item">
                    <span>{{ f.name }}</span>
                    <el-button link type="danger" size="small" @click="removeFile(i, 'materialFileList')">删除</el-button>
                  </div>
                </div>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="发票">
              <div class="file-upload-area">
                <el-upload
                  multiple
                  :auto-upload="false"
                  :on-change="(f: any) => handleFileChange(f, 'invoiceFileList')"
                  :on-remove="(f: any) => handleFileRemove(f, 'invoiceFileList')"
                  :file-list="invoiceFileList"
                  :show-file-list="false"
                >
                  <el-button size="small" icon="Upload">选择文件</el-button>
                </el-upload>
                <div v-if="invoiceFileList.length" class="file-list">
                  <div v-for="(f, i) in invoiceFileList" :key="i" class="file-item">
                    <span>{{ f.name }}</span>
                    <el-button link type="danger" size="small" @click="removeFile(i, 'invoiceFileList')">删除</el-button>
                  </div>
                </div>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="定金水单">
              <div class="file-upload-area">
                <el-upload multiple :auto-upload="false"
                  :on-change="(f: any) => handleFileChange(f, 'depositSlipFileList')"
                  :on-remove="(f: any) => handleFileRemove(f, 'depositSlipFileList')"
                  :file-list="depositSlipFileList" :show-file-list="false">
                  <el-button size="small" icon="Upload">选择文件</el-button>
                </el-upload>
                <div v-if="depositSlipFileList.length" class="file-list">
                  <div v-for="(f, i) in depositSlipFileList" :key="i" class="file-item">
                    <span>{{ f.name }}</span>
                    <el-button link type="danger" size="small" @click="removeFile(i, 'depositSlipFileList')">删除</el-button>
                  </div>
                </div>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="尾款水单">
              <div class="file-upload-area">
                <el-upload multiple :auto-upload="false"
                  :on-change="(f: any) => handleFileChange(f, 'finalPaymentSlipFileList')"
                  :on-remove="(f: any) => handleFileRemove(f, 'finalPaymentSlipFileList')"
                  :file-list="finalPaymentSlipFileList" :show-file-list="false">
                  <el-button size="small" icon="Upload">选择文件</el-button>
                </el-upload>
                <div v-if="finalPaymentSlipFileList.length" class="file-list">
                  <div v-for="(f, i) in finalPaymentSlipFileList" :key="i" class="file-item">
                    <span>{{ f.name }}</span>
                    <el-button link type="danger" size="small" @click="removeFile(i, 'finalPaymentSlipFileList')">删除</el-button>
                  </div>
                </div>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="运费水单">
              <div class="file-upload-area">
                <el-upload multiple :auto-upload="false"
                  :on-change="(f: any) => handleFileChange(f, 'freightSlipFileList')"
                  :on-remove="(f: any) => handleFileRemove(f, 'freightSlipFileList')"
                  :file-list="freightSlipFileList" :show-file-list="false">
                  <el-button size="small" icon="Upload">选择文件</el-button>
                </el-upload>
                <div v-if="freightSlipFileList.length" class="file-list">
                  <div v-for="(f, i) in freightSlipFileList" :key="i" class="file-item">
                    <span>{{ f.name }}</span>
                    <el-button link type="danger" size="small" @click="removeFile(i, 'freightSlipFileList')">删除</el-button>
                  </div>
                </div>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="选择状态">
                <el-option v-for="s in statusList" :key="s.code" :label="s.label" :value="s.code" />
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
    <el-dialog v-model="detailDialogVisible" title="采购订单详情" width="900px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="采购单号">{{ detailData.poNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ getLabel(detailData.status) }}</el-descriptions-item>
        <el-descriptions-item label="供应商">{{ detailData.supplierName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="关联销售单号">{{ detailData.salesOrderNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="总金额(RMB)">{{ detailData.totalAmount ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="实际金额(RMB)">{{ detailData.actualAmount ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="定金比例(%)">{{ detailData.depositRate ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="定金金额(RMB)">{{ detailData.depositAmount ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="订单日期">{{ detailData.orderDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="交货日期">{{ detailData.deliveryDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="运输备注">{{ detailData.transportRemark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="总运费(RMB)">{{ detailData.totalFreight ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="照片" :span="2">
          <template v-if="detailData.photos">
            <a v-for="(url, i) in detailData.photos.split(',').filter((u: string) => u)" :key="i"
               :href="url" target="_blank" class="file-link">文件{{ Number(i) + 1 }}</a>
          </template>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="材质单" :span="2">
          <template v-if="detailData.materialSheet">
            <a v-for="(url, i) in detailData.materialSheet.split(',').filter((u: string) => u)" :key="i"
               :href="url" target="_blank" class="file-link">文件{{ Number(i) + 1 }}</a>
          </template>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="发票" :span="2">
          <template v-if="detailData.invoice">
            <a v-for="(url, i) in detailData.invoice.split(',').filter((u: string) => u)" :key="i"
               :href="url" target="_blank" class="file-link">文件{{ Number(i) + 1 }}</a>
          </template>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="定金水单" :span="2">
          <template v-if="detailData.depositSlip">
            <a v-for="(url, i) in detailData.depositSlip.split(',').filter((u: string) => u)" :key="i"
               :href="url" target="_blank" class="file-link">文件{{ Number(i) + 1 }}</a>
          </template>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="尾款水单" :span="2">
          <template v-if="detailData.finalPaymentSlip">
            <a v-for="(url, i) in detailData.finalPaymentSlip.split(',').filter((u: string) => u)" :key="i"
               :href="url" target="_blank" class="file-link">文件{{ Number(i) + 1 }}</a>
          </template>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="运费水单" :span="2">
          <template v-if="detailData.freightSlip">
            <a v-for="(url, i) in detailData.freightSlip.split(',').filter((u: string) => u)" :key="i"
               :href="url" target="_blank" class="file-link">文件{{ Number(i) + 1 }}</a>
          </template>
          <span v-else>-</span>
        </el-descriptions-item>
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
import type { FormInstance, UploadFile } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { exportToCsv } from '@/utils/export'
import { getPurchaseOrderPage, savePurchaseOrder, updatePurchaseOrder, uploadPurchaseFiles } from '@/api/purchaseOrder'
import { getSupplierPage } from '@/api/supplier'
import { getUserListWithRoles } from '@/api/system'
import { useOrderStatus } from '@/composables/useOrderStatus'

const { statusList, getLabel, getTagType, getAllowedNextStatuses, changeStatus } = useOrderStatus('purchase')

const router = useRouter()
const loading = ref(false)
const submitLoading = ref(false)
const dataList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const detailDialogVisible = ref(false)
const formRef = ref<FormInstance>()
const supplierOptionsLoading = ref(false)
const supplierOptions = ref<any[]>([])
const photosFileList = ref<UploadFile[]>([])
const materialFileList = ref<UploadFile[]>([])
const invoiceFileList = ref<UploadFile[]>([])
const depositSlipFileList = ref<UploadFile[]>([])
const finalPaymentSlipFileList = ref<UploadFile[]>([])
const freightSlipFileList = ref<UploadFile[]>([])
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  poNo: '',
  salesOrderNo: '',
  status: undefined,
  createId: undefined,
  createUserName: '',
  salespersonId: undefined,
  salespersonName: ''
})

const allUsers = ref<any[]>([])
const businessUsers = ref<any[]>([])

const form = reactive<any>({
  id: null,
  poNo: '',
  supplierId: null,
  salesOrderNo: '',
  totalAmount: 0,
  actualAmount: 0,
  depositRate: 0,
  depositAmount: 0,
  orderDate: null,
  deliveryDate: null,
  transportRemark: '',
  totalFreight: 0,
  photos: '',
  materialSheet: '',
  invoice: '',
  depositSlip: '',
  finalPaymentSlip: '',
  freightSlip: '',
  status: 1
})

const detailData = reactive<any>({
  id: null,
  poNo: '',
  supplierId: null,
  supplierName: '',
  salesOrderNo: '',
  totalAmount: 0,
  actualAmount: 0,
  depositRate: 0,
  depositAmount: 0,
  orderDate: null,
  deliveryDate: null,
  transportRemark: '',
  totalFreight: 0,
  photos: '',
  materialSheet: '',
  invoice: '',
  depositSlip: '',
  finalPaymentSlip: '',
  freightSlip: '',
  status: 1,
  createTime: '',
  updateTime: ''
})

const rules = {
  poNo: [{ required: true, message: '请输入采购单号', trigger: 'blur' }],
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  salesOrderNo: [{ required: true, message: '请输入关联销售单号', trigger: 'blur' }],
  totalAmount: [{ required: true, message: '请输入订单金额', trigger: 'blur' }],
  depositRate: [{ required: true, message: '请输入定金比例', trigger: 'blur' }],
  depositAmount: [{ required: true, message: '请输入定金金额', trigger: 'blur' }],
  orderDate: [{ required: true, message: '请选择订单日期', trigger: 'change' }],
  deliveryDate: [{ required: true, message: '请选择交货日期', trigger: 'change' }]
}


const getList = async () => {
  loading.value = true
  try {
    // Prepare query
    const params: any = { ...queryParams }
    if (params.createUserName && params.createId) params.createId = params.createId
    else params.createId = undefined
    if (params.salespersonName && params.salespersonId) params.salespersonId = params.salespersonId
    else params.salespersonId = undefined
    const res = await getPurchaseOrderPage(params)
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
  queryParams.poNo = ''
  queryParams.salesOrderNo = ''
  queryParams.status = undefined
  queryParams.createId = undefined
  queryParams.createUserName = ''
  queryParams.salespersonId = undefined
  queryParams.salespersonName = ''
  handleQuery()
}

// User dropdown logic (copied from sales order)
const loadUserOptions = async () => {
  const res = await getUserListWithRoles()
  const list: any[] = res.data || []
  allUsers.value = list
  businessUsers.value = list.filter((user: any) =>
    user.roleNames?.some((name: string) => name.includes('业务'))
  )
}

const queryCreateUser = (queryString: string, cb: (results: any[]) => void) => {
  const results = allUsers.value.filter((user: any) => {
    return (
      user.realName?.toLowerCase().includes(queryString.toLowerCase()) ||
      user.username?.toLowerCase().includes(queryString.toLowerCase())
    )
  })
  cb(results.map(u => ({ value: u.realName || u.username, id: u.id })))
}
const querySalesperson = (queryString: string, cb: (results: any[]) => void) => {
  const results = businessUsers.value.filter((user: any) => {
    return (
      user.realName?.toLowerCase().includes(queryString.toLowerCase()) ||
      user.username?.toLowerCase().includes(queryString.toLowerCase())
    )
  })
  cb(results.map(u => ({ value: u.realName || u.username, id: u.id })))
}
const onCreateUserSelect = (item: any) => {
  queryParams.createUserName = item.value
  queryParams.createId = item.id
}
const onSalespersonSelect = (item: any) => {
  queryParams.salespersonName = item.value
  queryParams.salespersonId = item.id
}


type FileListKey = 'photosFileList' | 'materialFileList' | 'invoiceFileList' | 'depositSlipFileList' | 'finalPaymentSlipFileList' | 'freightSlipFileList'

const handleFileChange = (file: UploadFile, listKey: FileListKey) => {
  const listMap = { photosFileList, materialFileList, invoiceFileList, depositSlipFileList, finalPaymentSlipFileList, freightSlipFileList }
  const list = listMap[listKey]
  if (!list.value.find(f => f.uid === file.uid)) {
    list.value.push(file)
  }
}

const handleFileRemove = (file: UploadFile, listKey: FileListKey) => {
  const listMap = { photosFileList, materialFileList, invoiceFileList, depositSlipFileList, finalPaymentSlipFileList, freightSlipFileList }
  const list = listMap[listKey]
  list.value = list.value.filter(f => f.uid !== file.uid)
}

const removeFile = (index: number, listKey: FileListKey) => {
  const listMap = { photosFileList, materialFileList, invoiceFileList, depositSlipFileList, finalPaymentSlipFileList, freightSlipFileList }
  listMap[listKey].value.splice(index, 1)
}

// Convert stored URL string back to UploadFile list for editing
const urlsToFileList = (urlStr: string): UploadFile[] => {
  if (!urlStr) return []
  return urlStr.split(',').filter(u => u).map((url, i) => ({
    uid: i,
    name: url.split('/').pop() || `文件${i + 1}`,
    url,
    status: 'success'
  } as unknown as UploadFile))
}

const uploadFilesForField = async (fileList: UploadFile[]): Promise<string> => {
  // Separate already-uploaded (have url, no raw) from new files (have raw)
  const newFiles: File[] = fileList
    .filter(f => f.raw)
    .map(f => f.raw as File)
  const existingList: string[] = fileList
    .filter(f => !f.raw && (f as any).url)
    .map(f => (f as any).url as string)
  if (newFiles.length === 0) {
    return existingList.join(',')
  }
  const res = await uploadPurchaseFiles(newFiles)
  const uploaded: string[] = res.data || []
  return [...existingList, ...uploaded].join(',')
}

const resetForm = () => {
  form.id = null
  form.poNo = ''
  form.supplierId = null
  form.salesOrderNo = ''
  form.totalAmount = 0
  form.actualAmount = 0
  form.depositRate = 0
  form.depositAmount = 0
  form.orderDate = null
  form.deliveryDate = null
  form.transportRemark = ''
  form.totalFreight = 0
  form.photos = ''
  form.materialSheet = ''
  form.invoice = ''
  form.depositSlip = ''
  form.finalPaymentSlip = ''
  form.freightSlip = ''
  form.status = 1
  photosFileList.value = []
  materialFileList.value = []
  invoiceFileList.value = []
  depositSlipFileList.value = []
  finalPaymentSlipFileList.value = []
  freightSlipFileList.value = []
  formRef.value?.clearValidate()
}

const handleAdd = () => {
  resetForm()
  loadSupplierOptions('')
  dialogTitle.value = '新增采购单'
  dialogVisible.value = true
}

const handleDetail = (row: any) => {
  Object.assign(detailData, {
    id: row.id,
    poNo: row.poNo,
    supplierId: row.supplierId,
    supplierName: row.supplierName,
    salesOrderNo: row.salesOrderNo,
    totalAmount: row.totalAmount ?? 0,
    actualAmount: row.actualAmount ?? 0,
    depositRate: row.depositRate ?? 0,
    depositAmount: row.depositAmount ?? 0,
    orderDate: row.orderDate ?? null,
    deliveryDate: row.deliveryDate ?? null,
    transportRemark: row.transportRemark ?? '',
    totalFreight: row.totalFreight ?? 0,
    photos: row.photos ?? '',
    materialSheet: row.materialSheet ?? '',
    invoice: row.invoice ?? '',
    depositSlip: row.depositSlip ?? '',
    finalPaymentSlip: row.finalPaymentSlip ?? '',
    freightSlip: row.freightSlip ?? '',
    status: row.status ?? 1,
    createTime: row.createTime ?? '',
    updateTime: row.updateTime ?? ''
  })
  detailDialogVisible.value = true
}

const goToDetail = (row: any) => {
  router.push({ name: 'PurchaseOrderDetail', params: { orderId: row.id }, query: { poNo: row.poNo } })
}

const handleEdit = (row: any) => {
  resetForm()
  supplierOptions.value = row.supplierId
    ? [{ id: row.supplierId, name: row.supplierName, supplierCode: '' }]
    : []
  Object.assign(form, {
    id: row.id,
    poNo: row.poNo,
    supplierId: row.supplierId,
    salesOrderNo: row.salesOrderNo,
    totalAmount: row.totalAmount ?? 0,
    actualAmount: row.actualAmount ?? 0,
    depositRate: row.depositRate ?? 0,
    depositAmount: row.depositAmount ?? 0,
    orderDate: row.orderDate ?? null,
    deliveryDate: row.deliveryDate ?? null,
    transportRemark: row.transportRemark ?? '',
    totalFreight: row.totalFreight ?? 0,
    photos: row.photos ?? '',
    materialSheet: row.materialSheet ?? '',
    invoice: row.invoice ?? '',
    depositSlip: row.depositSlip ?? '',
    finalPaymentSlip: row.finalPaymentSlip ?? '',
    freightSlip: row.freightSlip ?? '',
    status: row.status ?? 1
  })
  photosFileList.value = urlsToFileList(row.photos ?? '')
  materialFileList.value = urlsToFileList(row.materialSheet ?? '')
  invoiceFileList.value = urlsToFileList(row.invoice ?? '')
  depositSlipFileList.value = urlsToFileList(row.depositSlip ?? '')
  finalPaymentSlipFileList.value = urlsToFileList(row.finalPaymentSlip ?? '')
  freightSlipFileList.value = urlsToFileList(row.freightSlip ?? '')
  loadSupplierOptions('')
  dialogTitle.value = '编辑采购单'
  dialogVisible.value = true
}

const loadSupplierOptions = async (keyword: string) => {
  supplierOptionsLoading.value = true
  try {
    const res = await getSupplierPage({
      pageNum: 1,
      pageSize: 50,
      name: keyword
    })
    supplierOptions.value = res.data?.list || []
  } finally {
    supplierOptionsLoading.value = false
  }
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    form.photos = await uploadFilesForField(photosFileList.value)
    form.materialSheet = await uploadFilesForField(materialFileList.value)
    form.invoice = await uploadFilesForField(invoiceFileList.value)
    form.depositSlip = await uploadFilesForField(depositSlipFileList.value)
    form.finalPaymentSlip = await uploadFilesForField(finalPaymentSlipFileList.value)
    form.freightSlip = await uploadFilesForField(freightSlipFileList.value)
    if (form.id) {
      await updatePurchaseOrder({ ...form })
    } else {
      const payload = { ...form }
      delete payload.id
      await savePurchaseOrder(payload)
    }
    ElMessage.success(form.id ? '更新成功' : '创建成功')
    dialogVisible.value = false
    getList()
  } finally {
    submitLoading.value = false
  }
}

const handleExport = async () => {
  const res = await getPurchaseOrderPage({ ...queryParams, pageNum: 1, pageSize: 10000 })
  const rows = res.data.list || []
  exportToCsv('采购订单导出', rows, [
    { label: '采购单号', key: 'poNo' },
    { label: '供应商', key: 'supplierName' },
    { label: '关联销售单号', key: 'salesOrderNo' },
    { label: '总金额(RMB)', key: 'totalAmount' },
    { label: '实际金额(RMB)', key: 'actualAmount' },
    { label: '定金比例(%)', key: 'depositRate' },
    { label: '定金金额(RMB)', key: 'depositAmount' },
    { label: '订单日期', key: 'orderDate' },
    { label: '交货日期', key: 'deliveryDate' },
    { label: '运输备注', key: 'transportRemark' },
    { label: '总运费(RMB)', key: 'totalFreight' },
    { label: '发票', key: 'invoice' },
    { label: '状态', value: (r: any) => getLabel(r.status) },
    { label: '创建时间', key: 'createTime' }
  ])
}

onMounted(() => {
  loadUserOptions()
  getList()
})
</script>

<style scoped>
.app-container { padding: 0; }
.search-wrap { margin-bottom: 16px; }
.table-toolbar { margin-bottom: 16px; }
.pagination-container { margin-top: 16px; display: flex; justify-content: flex-end; }
.file-upload-area { width: 100%; }
.file-list { margin-top: 8px; }
.file-item { display: flex; align-items: center; gap: 8px; padding: 2px 0; font-size: 13px; }
.file-link { margin-right: 12px; color: #409eff; text-decoration: none; }
.file-link:hover { text-decoration: underline; }
.group-divider { margin: 8px 0 16px; }
</style>
