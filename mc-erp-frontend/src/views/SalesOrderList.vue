<template>
  <div class="app-container">
    <!-- 1. Search Form -->
    <el-card shadow="never" class="search-wrap">
      <el-form :inline="true" :model="queryParams" ref="queryRef">
        <el-form-item label="订单号" prop="orderNo">
          <el-input v-model="queryParams.orderNo" placeholder="输入订单号" clearable />
        </el-form-item>
        <el-form-item label="贸易条款" prop="tradeTerm">
          <el-select v-model="queryParams.tradeTerm" placeholder="选择贸易条款" clearable>
            <el-option label="FOB" value="FOB" />
            <el-option label="CIF" value="CIF" />
            <el-option label="EXW" value="EXW" />
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
        <el-button type="success" icon="Download">导出</el-button>
      </div>

      <el-table v-loading="loading" :data="orderList" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="订单号" prop="orderNo" min-width="150" />
        <el-table-column label="贸易条款" prop="tradeTerm" width="100" />
        <el-table-column label="总金额" width="150" align="right">
          <template #default="{ row }">
            <span class="currency-text">{{ row.currency }} </span>
            <span>{{ row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="160" />
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" @click="handleDetail(scope.row)">详情</el-button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getOrderPage } from '@/api/salesOrder'

// Data definitions
const loading = ref(false)
const orderList = ref([])
const total = ref(0)
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  orderNo: '',
  tradeTerm: ''
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

// Search and reset
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}
const resetQuery = () => {
  queryParams.orderNo = ''
  queryParams.tradeTerm = ''
  handleQuery()
}

const handleAdd = () => {
  console.log('Add Order')
}
const handleDetail = (row: any) => {
  console.log('Detail', row)
}
const handleEdit = (row: any) => {
  console.log('Edit', row)
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
  // getList() // uncomment when backend is ready
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
</style>
