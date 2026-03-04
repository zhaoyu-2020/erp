<template>
  <div class="app-container">
    <el-card shadow="never" class="search-wrap">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="客户编码">
          <el-input v-model="queryParams.customerCode" placeholder="输入编码" clearable />
        </el-form-item>
        <el-form-item label="客户名称">
          <el-input v-model="queryParams.name" placeholder="输入名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-wrap">
      <div class="table-toolbar">
        <el-button type="primary" icon="Plus">新增客户</el-button>
      </div>

      <el-table v-loading="loading" :data="dataList" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="客户编码" prop="customerCode" width="120" />
        <el-table-column label="名称" prop="name" min-width="150" />
        <el-table-column label="国家/地区" prop="country" width="120" />
        <el-table-column label="联系人" prop="contactPerson" width="120" />
        <el-table-column label="邮箱" prop="email" min-width="150" />
        <el-table-column label="电话" prop="phone" width="150" />
        <el-table-column label="级别" prop="level" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.level === 'VIP' ? 'danger' : 'info'">{{ row.level }}</el-tag>
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
import { getCustomerPage } from '@/api/customer'

const loading = ref(false)
const dataList = ref([])
const total = ref(0)
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  customerCode: '',
  name: ''
})

const getList = async () => {
  loading.value = true
  try {
    const res = await getCustomerPage(queryParams)
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
  queryParams.customerCode = ''
  queryParams.name = ''
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
