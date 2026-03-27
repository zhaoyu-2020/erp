<template>
  <el-container class="app-wrapper">
    <!-- Sidebar -->
    <el-aside width="210px" class="sidebar-container">
      <div class="logo">
        <el-icon :size="24" color="#fff"><OfficeBuilding /></el-icon>
        <span class="logo-text">MC ERP 系统</span>
      </div>
      <el-scrollbar>
        <el-menu
          :default-active="$route.path"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          router
        >
          <el-menu-item index="/sales-orders">
            <el-icon><Document /></el-icon>
            <template #title>销售订单</template>
          </el-menu-item>
          <el-menu-item index="/sales-order-details">
            <el-icon><DocumentChecked /></el-icon>
            <template #title>销售订单明细</template>
          </el-menu-item>
          <el-menu-item index="/purchase-orders">
            <el-icon><ShoppingCart /></el-icon>
            <template #title>采购订单</template>
          </el-menu-item>
          <el-menu-item index="/purchase-order-details">
            <el-icon><DocumentChecked /></el-icon>
            <template #title>采购订单明细</template>
          </el-menu-item>
            <el-menu-item index="/freight-orders">
            <el-icon><Ship /></el-icon>
            <template #title>货代订单</template>
          </el-menu-item>
          <el-menu-item index="/customers">
            <el-icon><User /></el-icon>
            <template #title>客户管理(CRM)</template>
          </el-menu-item>
          <el-menu-item index="/suppliers">
            <el-icon><Van /></el-icon>
            <template #title>供应商管理</template>
          </el-menu-item>
          <el-menu-item index="/freight-forwarders">
            <el-icon><Ship /></el-icon>
            <template #title>货代管理</template>
          </el-menu-item>
              <el-menu-item index="/products">
            <el-icon><Goods /></el-icon>
            <template #title>产品管理(PIM)</template>
          </el-menu-item>
          <el-menu-item index="/wms">
            <el-icon><Box /></el-icon>
            <template #title>仓储管理(WMS)</template>
          </el-menu-item>
        
          <el-menu-item index="/documents">
            <el-icon><DocumentCopy /></el-icon>
            <template #title>报关单证</template>
          </el-menu-item>
          <el-menu-item index="/finance">
            <el-icon><Money /></el-icon>
            <template #title>财务收据</template>
          </el-menu-item>
          <el-sub-menu index="/reports">
            <template #title>
              <el-icon><DataAnalysis /></el-icon>
              <span>报表管理</span>
            </template>
            <!-- <el-menu-item index="/dashboard">
              <el-icon><Odometer /></el-icon>
              <template #title>经营驾驶舱</template>
            </el-menu-item>
            <el-menu-item index="/reports/sales">
              <el-icon><TrendCharts /></el-icon>
              <template #title>销售报表</template>
            </el-menu-item>
            <el-menu-item index="/reports/finance">
              <el-icon><Wallet /></el-icon>
              <template #title>财务报表</template>
            </el-menu-item> -->
            <el-menu-item index="/reports/order-status">
              <el-icon><PieChart /></el-icon>
              <template #title>未完订单状态图</template>
            </el-menu-item>
            <el-menu-item index="/reports/order-finance">
              <el-icon><Money /></el-icon>
              <template #title>未完订单资金图</template>
            </el-menu-item>
            <el-menu-item index="/reports/cashflow">
              <el-icon><Timer /></el-icon>
              <template #title>资金收付预期半月图</template>
            </el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="/system">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/system/user">
              <el-icon><User /></el-icon>
              <template #title>用户管理</template>
            </el-menu-item>
            <el-menu-item index="/system/role">
              <el-icon><UserFilled /></el-icon>
              <template #title>角色管理</template>
            </el-menu-item>
            <el-menu-item index="/system/menu">
              <el-icon><Menu /></el-icon>
              <template #title>菜单管理</template>
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <!-- Main Container -->
    <el-container class="main-container">
      <!-- Navbar -->
      <el-header class="navbar">
        <div class="breadcrumb-container">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ $route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="right-menu">
          <el-dropdown trigger="click">
            <span class="avatar-wrapper">
              {{ displayName }} <el-icon><CaretBottom /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/profile')">个人中心</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- App Main -->
      <el-main class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { ref, computed, onMounted } from 'vue'

const $route = useRoute()
const router = useRouter()

const userInfo = ref<{ realName?: string; username?: string }>({})
const displayName = computed(() => userInfo.value.realName || userInfo.value.username || '管理员')

onMounted(() => {
  try {
    const stored = localStorage.getItem('userInfo')
    if (stored) {
      userInfo.value = JSON.parse(stored)
    }
  } catch {}
})

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    ElMessage.success('已退出登录')
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
.app-wrapper {
  height: 100vh;
  width: 100%;
}
.sidebar-container {
  background-color: #304156;
  height: 100%;
  display: flex;
  flex-direction: column;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2b3643;
}
.logo-text {
  color: #fff;
  font-weight: 600;
  font-size: 16px;
  margin-left: 10px;
}
.el-scrollbar {
  flex: 1;
  height: calc(100vh - 60px);
}
.el-menu {
  border-right: none;
}
.navbar {
  height: 50px;
  overflow: hidden;
  position: relative;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}
.breadcrumb-container {
  float: left;
}
.right-menu {
  float: right;
  display: flex;
  align-items: center;
}
.avatar-wrapper {
  cursor: pointer;
  display: flex;
  align-items: center;
  font-size: 14px;
}
.app-main {
  height: calc(100vh - 50px);
  width: 100%;
  position: relative;
  overflow-y: auto;
  background-color: #f0f2f5;
  padding: 20px;
}

/* transition */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all .3s;
}
.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}
.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
