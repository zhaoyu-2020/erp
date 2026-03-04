<template>
  <div class="app-container">
    <el-card shadow="never" class="search-wrap">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="收款单号">
          <el-input v-model="queryParams.receiptNo" placeholder="输入收款单号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="选择状态" clearable>
            <el-option label="未认领" :value="1" />
            <el-option label="已认领" :value="2" />
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
        <el-button type="primary" icon="Plus">新建收款单</el-button>
        <el-button type="success" icon="Download">导出</el-button>
      </div>

      <el-table v-loading="loading" :data="dataList" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="收款单号" prop="receiptNo" width="150" />
        <el-table-column label="客户" prop="customerName" min-width="150" />
        <el-table-column label="金额" width="150" align="right">
          <template #default="{ row }">
            <b>{{ row.currency }}</b> {{ row.amount }}
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="认领时间" prop="claimTime" width="160" />
        <el-table-column label="创建时间" prop="createTime" width="160" />
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" v-if="scope.row.status === 1">认领</el-button>
            <el-button link type="primary">详情</el-button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getFinanceReceiptPage } from '@/api/advancedModules'

const loading = ref(false)
const dataList = ref([])
const total = ref(0)
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  receiptNo: '',
  status: undefined
})

const getList = async () => {
  loading.value = true
  try {
    const res = await getFinanceReceiptPage(queryParams)
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
  queryParams.receiptNo = ''
  queryParams.status = undefined
  handleQuery()
}

const getStatusLabel = (status: number) => {
  const map: Record<number, string> = { 1: '未认领', 2: '已认领' }
  return map[status] || '未知'
}
const getStatusType = (status: number) => {
  const map: Record<number, string> = { 1: 'danger', 2: 'success' }
  return map[status] || ''
}

onMounted(() => {
  // getList()
})
</script>

<style scoped>
.app-container { padding: 0; }
.search-wrap { margin-bottom: 16px; }
.table-toolbar { margin-bottom: 16px; }
.pagination-container { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
