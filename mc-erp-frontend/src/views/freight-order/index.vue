<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-wrap">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="订单编号">
          <el-input v-model="queryParams.orderCode" placeholder="输入订单编号" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="销售订单号">
          <el-input v-model="queryParams.saleOrderCode" placeholder="输入销售订单号" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="queryParams.supplierName" placeholder="输入供应商名称" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="运输类型">
          <el-select v-model="queryParams.transportType" clearable placeholder="全部" style="width: 130px">
            <el-option label="集装箱船" :value="1" />
            <el-option label="散货船" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="queryParams.orderStatus" clearable placeholder="全部" style="width: 120px">
            <el-option label="草稿" :value="0" />
            <el-option label="已提交" :value="1" />
            <el-option label="已结算" :value="2" />
            <el-option label="已作废" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表区域 -->
    <el-card shadow="never" class="table-wrap">
      <div class="table-toolbar">
        <el-button type="primary" icon="Plus" @click="handleAdd">新建货代订单</el-button>
        <el-button type="success" icon="Download" @click="handleExport">导出</el-button>
      </div>

      <el-table v-loading="loading" :data="dataList" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="订单编号" prop="orderCode" width="180" />
        <el-table-column label="销售订单号" prop="saleOrderCode" width="150" />
        <el-table-column label="供应商" prop="supplierName" min-width="140" show-overflow-tooltip />
        <el-table-column label="运输类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.transportType === 1 ? 'primary' : 'warning'" size="small">
              {{ row.transportType === 1 ? '集装箱船' : '散货船' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="柜型/重量" width="120" align="center">
          <template #default="{ row }">
            <span v-if="row.transportType === 1">{{ row.containerType }} × {{ row.containerQty }}</span>
            <span v-else>{{ row.bulkWeight }}T / {{ row.bulkVolume }}CBM</span>
          </template>
        </el-table-column>
        <el-table-column label="海运费" prop="totalOceanFreight" width="110" align="right">
          <template #default="{ row }">{{ formatMoney(row.totalOceanFreight) }}</template>
        </el-table-column>
        <el-table-column label="地面费用" prop="totalGroundFee" width="110" align="right">
          <template #default="{ row }">{{ formatMoney(row.totalGroundFee) }}</template>
        </el-table-column>
        <el-table-column label="保费" width="100" align="right">
          <template #default="{ row }">
            <span v-if="row.needInsurance === 1">{{ formatMoney(row.premium) }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="总费用" prop="totalAmount" width="120" align="right">
          <template #default="{ row }">
            <span style="font-weight: bold; color: #409EFF">{{ formatMoney(row.totalAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.orderStatus)" size="small">{{ statusLabel(row.orderStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="170" />
        <el-table-column label="操作" width="260" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
            <el-button link type="primary" v-if="row.orderStatus === 0" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="success" v-if="row.orderStatus === 0" @click="handleSubmit(row)">提交</el-button>
            <el-button link type="success" v-if="row.orderStatus === 1" @click="handleSettle(row)">结算</el-button>
            <el-button link type="danger" v-if="row.orderStatus !== 3" @click="handleCancel(row)">作废</el-button>
            <el-button link type="danger" v-if="row.orderStatus === 0" @click="handleDelete(row)">删除</el-button>
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

    <!-- 新建/编辑 对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="900px" destroy-on-close @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <!-- 基础信息 -->
        <el-divider content-position="left">基础关联信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="销售订单号" prop="saleOrderCode">
              <el-select v-model="form.saleOrderCode" filterable remote :remote-method="searchSalesOrders"
                placeholder="输入搜索销售订单" style="width: 100%" :disabled="isEditingSubmitted">
                <el-option v-for="so in salesOrderList" :key="so.orderNo" :label="so.orderNo" :value="so.orderNo" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商" prop="supplierId">
              <el-select v-model="form.supplierId" filterable placeholder="选择货代供应商" style="width: 100%"
                @change="onSupplierChange" :disabled="isEditingSubmitted">
                <el-option v-for="s in supplierList" :key="s.id" :label="s.name" :value="s.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 运输类型 -->
        <el-divider content-position="left">运输信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="运输类型" prop="transportType">
              <el-radio-group v-model="form.transportType" :disabled="isEditingSubmitted">
                <el-radio :value="1">集装箱船</el-radio>
                <el-radio :value="2">散货船</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 集装箱船专用字段 -->
        <el-row v-if="form.transportType === 1" :gutter="20">
          <el-col :span="8">
            <el-form-item label="柜型" prop="containerType">
              <el-select v-model="form.containerType" placeholder="选择柜型" :disabled="isEditingSubmitted">
                <el-option label="20GP" value="20GP" />
                <el-option label="40GP" value="40GP" />
                <el-option label="40HQ" value="40HQ" />
                <el-option label="45HQ" value="45HQ" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="柜量" prop="containerQty">
              <el-input-number v-model="form.containerQty" :min="1" :disabled="isEditingSubmitted" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="是否拼柜">
              <el-radio-group v-model="form.isLcl" :disabled="isEditingSubmitted">
                <el-radio :value="0">否</el-radio>
                <el-radio :value="1">是</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="柜号">
              <el-input v-model="form.containerNo" placeholder="多个柜号用逗号分隔" :disabled="isEditingSubmitted" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 散货船专用字段 -->
        <el-row v-if="form.transportType === 2" :gutter="20">
          <el-col :span="8">
            <el-form-item label="散货重量(吨)" prop="bulkWeight">
              <el-input-number v-model="form.bulkWeight" :min="0" :precision="2" :disabled="isEditingSubmitted" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="散货体积(CBM)" prop="bulkVolume">
              <el-input-number v-model="form.bulkVolume" :min="0" :precision="2" :disabled="isEditingSubmitted" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="舱位/船名航次">
              <el-input v-model="form.shippingSpace" placeholder="舱位或船名航次" :disabled="isEditingSubmitted" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="起运港">
              <el-input v-model="form.departurePort" placeholder="起运港" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="目的港">
              <el-input v-model="form.destinationPort" placeholder="目的港" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="主币种">
              <el-select v-model="form.orderCurrency" :disabled="isEditingSubmitted">
                <el-option label="USD" value="USD" />
                <el-option label="CNY" value="CNY" />
                <el-option label="EUR" value="EUR" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 保险信息 -->
        <el-divider content-position="left">货运保险</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="购买保险">
              <el-radio-group v-model="form.needInsurance"
                :disabled="isEditingSubmitted && editingOrder?.needInsurance !== undefined">
                <el-radio :value="0">否</el-radio>
                <el-radio :value="1">是</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row v-if="form.needInsurance === 1" :gutter="20">
          <el-col :span="8">
            <el-form-item label="保额" prop="insuredAmount">
              <el-input-number v-model="form.insuredAmount" :min="0" :precision="2" :disabled="isEditingSubmitted" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="保费" prop="premium">
              <el-input-number v-model="form.premium" :min="0" :precision="2" :disabled="isEditingSubmitted" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="保险币种">
              <el-select v-model="form.insuranceCurrency" :disabled="isEditingSubmitted">
                <el-option label="USD" value="USD" />
                <el-option label="CNY" value="CNY" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="保险备注">
              <el-input v-model="form.insuranceRemark" placeholder="保险公司、保单号等" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 费用明细 -->
        <el-divider content-position="left">海运费</el-divider>
        <div class="fee-section">
          <el-button type="primary" size="small" icon="Plus" @click="addFeeItem(1)"
            :disabled="isSettled">新增海运费</el-button>
          <el-table :data="oceanFeeItems" border size="small" class="fee-table">
            <el-table-column label="费用名称" min-width="140">
              <template #default="{ row }">
                <el-select v-model="row.feeName" filterable allow-create placeholder="选择或输入" size="small"
                  :disabled="isSettled">
                  <el-option label="海运基本费" value="海运基本费" />
                  <el-option label="燃油附加费" value="燃油附加费" />
                  <el-option label="货币附加费" value="货币附加费" />
                  <el-option label="旺季附加费" value="旺季附加费" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="金额" width="140">
              <template #default="{ row }">
                <el-input-number v-model="row.feeAmount" :min="0" :precision="2" size="small" controls-position="right"
                  :disabled="isSettled" />
              </template>
            </el-table-column>
            <el-table-column label="币种" width="100">
              <template #default="{ row }">
                <el-select v-model="row.currency" size="small" :disabled="isSettled">
                  <el-option label="USD" value="USD" />
                  <el-option label="CNY" value="CNY" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="计费方式" width="120">
              <template #default="{ row }">
                <el-select v-model="row.billingMethod" size="small" clearable :disabled="isSettled">
                  <el-option label="按柜" value="按柜" />
                  <el-option label="按吨" value="按吨" />
                  <el-option label="按体积" value="按体积" />
                  <el-option label="固定金额" value="固定金额" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="备注" min-width="120">
              <template #default="{ row }">
                <el-input v-model="row.remark" size="small" placeholder="备注" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="70" align="center" v-if="!isSettled">
              <template #default="{ $index }">
                <el-button link type="danger" size="small" @click="removeFeeItem(1, $index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="fee-total">海运费合计：<b>{{ formatMoney(oceanFeeTotal) }}</b></div>
        </div>

        <el-divider content-position="left">地面费用</el-divider>
        <div class="fee-section">
          <el-button type="primary" size="small" icon="Plus" @click="addFeeItem(2)"
            :disabled="isSettled">新增地面费用</el-button>
          <el-table :data="groundFeeItems" border size="small" class="fee-table">
            <el-table-column label="费用名称" min-width="140">
              <template #default="{ row }">
                <el-select v-model="row.feeName" filterable allow-create placeholder="选择或输入" size="small"
                  :disabled="isSettled">
                  <el-option label="报关费" value="报关费" />
                  <el-option label="拖车费" value="拖车费" />
                  <el-option label="码头操作费" value="码头操作费" />
                  <el-option label="文件费" value="文件费" />
                  <el-option label="港杂费" value="港杂费" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="金额" width="140">
              <template #default="{ row }">
                <el-input-number v-model="row.feeAmount" :min="0" :precision="2" size="small" controls-position="right"
                  :disabled="isSettled" />
              </template>
            </el-table-column>
            <el-table-column label="币种" width="100">
              <template #default="{ row }">
                <el-select v-model="row.currency" size="small" :disabled="isSettled">
                  <el-option label="USD" value="USD" />
                  <el-option label="CNY" value="CNY" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="计费方式" width="120">
              <template #default="{ row }">
                <el-select v-model="row.billingMethod" size="small" clearable :disabled="isSettled">
                  <el-option label="按柜" value="按柜" />
                  <el-option label="按吨" value="按吨" />
                  <el-option label="按体积" value="按体积" />
                  <el-option label="固定金额" value="固定金额" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="备注" min-width="120">
              <template #default="{ row }">
                <el-input v-model="row.remark" size="small" placeholder="备注" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="70" align="center" v-if="!isSettled">
              <template #default="{ $index }">
                <el-button link type="danger" size="small" @click="removeFeeItem(2, $index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="fee-total">地面费用合计：<b>{{ formatMoney(groundFeeTotal) }}</b></div>
        </div>

        <el-divider />
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="订单备注">
              <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="特殊物流要求、费用说明" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleFormSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="货代订单详情" width="900px" destroy-on-close>
      <template v-if="detailData">
        <!-- 区域1：基础关联 -->
        <el-divider content-position="left">基础关联信息</el-divider>
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="订单编号">{{ detailData.orderCode }}</el-descriptions-item>
          <el-descriptions-item label="销售订单号">{{ detailData.saleOrderCode }}</el-descriptions-item>
          <el-descriptions-item label="供应商">{{ detailData.supplierName }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(detailData.orderStatus)" size="small">{{ statusLabel(detailData.orderStatus)
              }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建人">{{ detailData.createUser }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ detailData.updateTime }}</el-descriptions-item>
          <el-descriptions-item label="主币种">{{ detailData.orderCurrency }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="3">{{ detailData.remark || '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="detailData.orderStatus === 3" label="作废原因" :span="3">
            <span style="color: #F56C6C">{{ detailData.cancelReason }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 区域2：运输信息 -->
        <el-divider content-position="left">运输信息</el-divider>
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="运输类型">
            <el-tag :type="detailData.transportType === 1 ? 'primary' : 'warning'" size="small">
              {{ detailData.transportType === 1 ? '集装箱船' : '散货船' }}
            </el-tag>
          </el-descriptions-item>
          <template v-if="detailData.transportType === 1">
            <el-descriptions-item label="柜型">{{ detailData.containerType }}</el-descriptions-item>
            <el-descriptions-item label="柜量">{{ detailData.containerQty }}</el-descriptions-item>
            <el-descriptions-item label="拼柜">{{ detailData.isLcl === 1 ? '是' : '否' }}</el-descriptions-item>
            <el-descriptions-item label="柜号" :span="2">{{ detailData.containerNo || '-' }}</el-descriptions-item>
          </template>
          <template v-else>
            <el-descriptions-item label="散货重量(吨)">{{ detailData.bulkWeight }}</el-descriptions-item>
            <el-descriptions-item label="散货体积(CBM)">{{ detailData.bulkVolume }}</el-descriptions-item>
            <el-descriptions-item label="舱位/船名航次">{{ detailData.shippingSpace || '-' }}</el-descriptions-item>
          </template>
          <el-descriptions-item label="起运港">{{ detailData.departurePort || '-' }}</el-descriptions-item>
          <el-descriptions-item label="目的港">{{ detailData.destinationPort || '-' }}</el-descriptions-item>
          <el-descriptions-item label="发货日期">{{ detailData.shipDate || '-' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 区域3：费用明细 -->
        <el-divider content-position="left">费用明细</el-divider>
        <h4 style="margin: 8px 0">海运费 <span style="color: #409EFF">(合计：{{ formatMoney(detailData.totalOceanFreight)
            }})</span></h4>
        <el-table :data="detailOceanFees" border size="small" v-if="detailOceanFees.length">
          <el-table-column label="费用名称" prop="feeName" />
          <el-table-column label="金额" prop="feeAmount" width="120" align="right">
            <template #default="{ row }">{{ formatMoney(row.feeAmount) }}</template>
          </el-table-column>
          <el-table-column label="币种" prop="currency" width="80" />
          <el-table-column label="计费方式" prop="billingMethod" width="100" />
          <el-table-column label="备注" prop="remark" show-overflow-tooltip />
        </el-table>
        <el-empty v-else description="暂无海运费明细" :image-size="40" />

        <h4 style="margin: 16px 0 8px">地面费用 <span style="color: #E6A23C">(合计：{{ formatMoney(detailData.totalGroundFee)
            }})</span></h4>
        <el-table :data="detailGroundFees" border size="small" v-if="detailGroundFees.length">
          <el-table-column label="费用名称" prop="feeName" />
          <el-table-column label="金额" prop="feeAmount" width="120" align="right">
            <template #default="{ row }">{{ formatMoney(row.feeAmount) }}</template>
          </el-table-column>
          <el-table-column label="币种" prop="currency" width="80" />
          <el-table-column label="计费方式" prop="billingMethod" width="100" />
          <el-table-column label="备注" prop="remark" show-overflow-tooltip />
        </el-table>
        <el-empty v-else description="暂无地面费用明细" :image-size="40" />

        <div class="total-summary">
          订单总费用：<b style="font-size: 16px; color: #409EFF">{{ formatMoney(detailData.totalAmount) }}</b>
          <span style="margin-left: 8px; color: #999">{{ detailData.orderCurrency }}</span>
        </div>

        <!-- 区域4：保险信息 -->
        <el-divider content-position="left">保险信息</el-divider>
        <el-descriptions :column="3" border size="small" v-if="detailData.needInsurance === 1">
          <el-descriptions-item label="购买保险">是</el-descriptions-item>
          <el-descriptions-item label="保额">{{ formatMoney(detailData.insuredAmount) }}</el-descriptions-item>
          <el-descriptions-item label="保费">{{ formatMoney(detailData.premium) }}</el-descriptions-item>
          <el-descriptions-item label="保险币种">{{ detailData.insuranceCurrency }}</el-descriptions-item>
          <el-descriptions-item label="保险备注" :span="2">{{ detailData.insuranceRemark || '-' }}</el-descriptions-item>
        </el-descriptions>
        <el-empty v-else description="未购买保险" :image-size="40" />

        <!-- 区域5：操作日志 -->
        <el-divider content-position="left">操作日志</el-divider>
        <el-table :data="detailData.logs || []" border size="small" max-height="250">
          <el-table-column label="操作类型" prop="operateType" width="100" />
          <el-table-column label="操作人" prop="operator" width="100" />
          <el-table-column label="操作时间" prop="operateTime" width="170" />
          <el-table-column label="操作备注" prop="operateRemark" show-overflow-tooltip />
        </el-table>
      </template>
    </el-dialog>

    <!-- 作废原因对话框 -->
    <el-dialog v-model="cancelVisible" title="作废原因" width="450px">
      <el-input v-model="cancelReason" type="textarea" :rows="3" placeholder="请填写作废原因（必填）" />
      <template #footer>
        <el-button @click="cancelVisible = false">取消</el-button>
        <el-button type="danger" :loading="cancelLoading" @click="confirmCancel">确认作废</el-button>
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
  getFreightOrderPage,
  getFreightOrderDetail,
  createFreightOrder,
  updateFreightOrder,
  deleteFreightOrder,
  submitFreightOrder,
  settleFreightOrder,
  cancelFreightOrder
} from '@/api/freightOrder'
import { getOrderPage } from '@/api/salesOrder'
import { getSupplierPage } from '@/api/supplier'

// ============ 状态映射 ============
const statusMap: Record<number, { label: string; tagType: string }> = {
  0: { label: '草稿', tagType: 'info' },
  1: { label: '已提交', tagType: '' },
  2: { label: '已结算', tagType: 'success' },
  3: { label: '已作废', tagType: 'danger' }
}
const statusLabel = (s: number) => statusMap[s]?.label || '未知'
const statusTagType = (s: number) => statusMap[s]?.tagType || 'info'

const formatMoney = (v: any) => {
  if (v === null || v === undefined) return '0.00'
  return Number(v).toFixed(2)
}

// ============ 列表查询 ============
const loading = ref(false)
const dataList = ref<any[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  orderCode: '',
  saleOrderCode: '',
  supplierName: '',
  transportType: undefined as number | undefined,
  orderStatus: undefined as number | undefined
})

const getList = async () => {
  loading.value = true
  try {
    const res = await getFreightOrderPage(queryParams)
    dataList.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => { queryParams.pageNum = 1; getList() }
const resetQuery = () => {
  queryParams.orderCode = ''
  queryParams.saleOrderCode = ''
  queryParams.supplierName = ''
  queryParams.transportType = undefined
  queryParams.orderStatus = undefined
  handleQuery()
}

// ============ 下拉数据 ============
const salesOrderList = ref<any[]>([])
const supplierList = ref<any[]>([])

const searchSalesOrders = async (keyword: string) => {
  if (!keyword) return
  const res = await getOrderPage({ pageNum: 1, pageSize: 50, orderNo: keyword })
  salesOrderList.value = res.data.list || []
}

const loadSuppliers = async () => {
  const res = await getSupplierPage({ pageNum: 1, pageSize: 1000 })
  supplierList.value = res.data.list || []
}

const onSupplierChange = (id: number) => {
  const s = supplierList.value.find((i: any) => i.id === id)
  if (s) form.supplierName = s.name
}

// ============ 表单 ============
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const editingOrder = ref<any>(null)

const isEditingSubmitted = computed(() => editingOrder.value?.orderStatus === 1)
const isSettled = computed(() => editingOrder.value?.orderStatus === 2)

const form = reactive<any>({
  orderId: null,
  saleOrderCode: '',
  supplierId: null,
  supplierName: '',
  transportType: 1,
  containerType: '40HQ',
  containerQty: 1,
  isLcl: 0,
  containerNo: '',
  bulkWeight: 0,
  bulkVolume: 0,
  shippingSpace: '',
  needInsurance: 0,
  insuredAmount: 0,
  premium: 0,
  insuranceCurrency: 'USD',
  insuranceRemark: '',
  orderCurrency: 'USD',
  departurePort: '',
  destinationPort: '',
  remark: ''
})

const feeItems = ref<any[]>([])

const oceanFeeItems = computed(() => feeItems.value.filter(i => i.feeType === 1))
const groundFeeItems = computed(() => feeItems.value.filter(i => i.feeType === 2))
const oceanFeeTotal = computed(() => oceanFeeItems.value.reduce((s, i) => s + (Number(i.feeAmount) || 0), 0))
const groundFeeTotal = computed(() => groundFeeItems.value.reduce((s, i) => s + (Number(i.feeAmount) || 0), 0))

const addFeeItem = (type: number) => {
  feeItems.value.push({
    feeType: type,
    feeName: '',
    feeAmount: 0,
    currency: form.orderCurrency || 'USD',
    billingMethod: '',
    remark: ''
  })
}

const removeFeeItem = (type: number, index: number) => {
  const items = type === 1 ? oceanFeeItems.value : groundFeeItems.value
  const item = items[index]
  const idx = feeItems.value.indexOf(item)
  if (idx >= 0) feeItems.value.splice(idx, 1)
}

const rules = {
  saleOrderCode: [{ required: true, message: '请选择销售订单号', trigger: 'change' }],
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  transportType: [{ required: true, message: '请选择运输类型', trigger: 'change' }]
}

const resetForm = () => {
  form.orderId = null
  form.saleOrderCode = ''
  form.supplierId = null
  form.supplierName = ''
  form.transportType = 1
  form.containerType = '40HQ'
  form.containerQty = 1
  form.isLcl = 0
  form.containerNo = ''
  form.bulkWeight = 0
  form.bulkVolume = 0
  form.shippingSpace = ''
  form.needInsurance = 0
  form.insuredAmount = 0
  form.premium = 0
  form.insuranceCurrency = 'USD'
  form.insuranceRemark = ''
  form.orderCurrency = 'USD'
  form.departurePort = ''
  form.destinationPort = ''
  form.remark = ''
  feeItems.value = []
  editingOrder.value = null
  formRef.value?.clearValidate()
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新建货代订单'
  dialogVisible.value = true
}

const handleEdit = async (row: any) => {
  resetForm()
  const res = await getFreightOrderDetail(row.orderId)
  const d = res.data
  editingOrder.value = d
  Object.assign(form, {
    orderId: d.orderId,
    saleOrderCode: d.saleOrderCode,
    supplierId: d.supplierId,
    supplierName: d.supplierName,
    transportType: d.transportType,
    containerType: d.containerType,
    containerQty: d.containerQty,
    isLcl: d.isLcl,
    containerNo: d.containerNo,
    bulkWeight: d.bulkWeight,
    bulkVolume: d.bulkVolume,
    shippingSpace: d.shippingSpace,
    needInsurance: d.needInsurance,
    insuredAmount: d.insuredAmount,
    premium: d.premium,
    insuranceCurrency: d.insuranceCurrency,
    insuranceRemark: d.insuranceRemark,
    orderCurrency: d.orderCurrency,
    departurePort: d.departurePort,
    destinationPort: d.destinationPort,
    remark: d.remark
  })
  feeItems.value = (d.feeItems || []).map((i: any) => ({ ...i }))
  // 将当前销售订单号加入列表以便回显
  if (d.saleOrderCode) {
    salesOrderList.value = [{ orderNo: d.saleOrderCode }]
  }
  dialogTitle.value = '编辑货代订单'
  dialogVisible.value = true
}

const handleFormSubmit = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    ElMessage.warning('请完善必填项')
    return
  }
  submitLoading.value = true
  try {
    const payload = { order: { ...form }, feeItems: feeItems.value }
    if (form.orderId) {
      await updateFreightOrder(payload)
      ElMessage.success('更新成功')
    } else {
      await createFreightOrder(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    getList()
  } finally {
    submitLoading.value = false
  }
}

// ============ 详情 ============
const detailVisible = ref(false)
const detailData = ref<any>(null)
const detailOceanFees = computed(() => (detailData.value?.feeItems || []).filter((i: any) => i.feeType === 1))
const detailGroundFees = computed(() => (detailData.value?.feeItems || []).filter((i: any) => i.feeType === 2))

const handleDetail = async (row: any) => {
  const res = await getFreightOrderDetail(row.orderId)
  detailData.value = res.data
  detailVisible.value = true
}

// ============ 状态操作 ============
const handleSubmit = (row: any) => {
  ElMessageBox.confirm('确认提交该货代订单？提交后核心字段将不可修改。', '提示', { type: 'warning' }).then(async () => {
    await submitFreightOrder(row.orderId)
    ElMessage.success('提交成功')
    getList()
  })
}

const handleSettle = (row: any) => {
  ElMessageBox.confirm('确认结算该货代订单？结算后所有费用和核心字段将锁定。', '提示', { type: 'warning' }).then(async () => {
    await settleFreightOrder(row.orderId)
    ElMessage.success('结算成功')
    getList()
  })
}

// 作废
const cancelVisible = ref(false)
const cancelReason = ref('')
const cancelLoading = ref(false)
const cancelOrderId = ref<number | null>(null)

const handleCancel = (row: any) => {
  cancelOrderId.value = row.orderId
  cancelReason.value = ''
  cancelVisible.value = true
}

const confirmCancel = async () => {
  if (!cancelReason.value.trim()) {
    ElMessage.warning('请填写作废原因')
    return
  }
  cancelLoading.value = true
  try {
    await cancelFreightOrder(cancelOrderId.value!, cancelReason.value)
    ElMessage.success('作废成功')
    cancelVisible.value = false
    getList()
  } finally {
    cancelLoading.value = false
  }
}

// ============ 删除 ============
const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该货代订单？删除后将移入回收站，可由管理员恢复。', '提示', { type: 'warning' }).then(async () => {
    await deleteFreightOrder(row.orderId)
    ElMessage.success('删除成功')
    getList()
  })
}

// ============ 导出 ============
const handleExport = async () => {
  const res = await getFreightOrderPage({ ...queryParams, pageNum: 1, pageSize: 10000 })
  const rows = res.data.list || []
  exportToCsv('货代订单导出', rows, [
    { label: '订单编号', key: 'orderCode' },
    { label: '销售订单号', key: 'saleOrderCode' },
    { label: '供应商', key: 'supplierName' },
    { label: '运输类型', key: 'transportTypeLabel' },
    { label: '海运费', key: 'totalOceanFreight' },
    { label: '地面费用', key: 'totalGroundFee' },
    { label: '保费', key: 'premium' },
    { label: '总费用', key: 'totalAmount' },
    { label: '状态', key: 'orderStatusLabel' },
    { label: '创建时间', key: 'createTime' }
  ])
}

// ============ 初始化 ============
onMounted(() => {
  getList()
  loadSuppliers()
})
</script>

<style scoped>
.app-container { padding: 0; }
.search-wrap { margin-bottom: 16px; }
.table-toolbar { margin-bottom: 16px; }
.pagination-container { margin-top: 16px; display: flex; justify-content: flex-end; }
.fee-section { margin-bottom: 8px; }
.fee-table { margin-top: 8px; }
.fee-total { text-align: right; margin-top: 8px; color: #606266; }
.total-summary { text-align: right; margin-top: 16px; padding: 12px; background: #f5f7fa; border-radius: 4px; }
.text-muted { color: #999; }
</style>
