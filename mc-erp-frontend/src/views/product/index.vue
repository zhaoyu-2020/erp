<template>
  <div class="app-container">
    <el-card shadow="never" class="search-wrap">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="SPU编码">
          <el-input v-model="queryParams.spuCode" placeholder="输入SPU编码" clearable />
        </el-form-item>
        <el-form-item label="产品名称">
          <el-input v-model="queryParams.nameCn" placeholder="输入产品中文名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-wrap">
      <div class="table-toolbar">
        <el-button type="primary" icon="Plus">新增产品</el-button>
      </div>

      <el-table v-loading="loading" :data="dataList" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="SPU编码" prop="spuCode" width="120" />
        <el-table-column label="HS海关编码" prop="hsCode" width="120" />
        <el-table-column label="中文名称" prop="nameCn" min-width="150" />
        <el-table-column label="英文名称" prop="nameEn" min-width="150" />
        <el-table-column label="单位" prop="unit" width="80" align="center" />
        <el-table-column label="退税率" prop="taxRefundRate" width="140" align="right">
          <template #default="{ row }">
            {{ (row.taxRefundRate * 100).toFixed(2) }}%
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary">编辑</el-button>
            <el-button link type="danger">删除</el-button>
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
import { getProductPage } from '@/api/product'

const loading = ref(false)
const dataList = ref([])
const total = ref(0)
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  spuCode: '',
  nameCn: ''
})

const getList = async () => {
  loading.value = true
  try {
    const res = await getProductPage(queryParams)
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
  queryParams.spuCode = ''
  queryParams.nameCn = ''
  handleQuery()
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
