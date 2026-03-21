import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import Layout from '@/layout/index.vue'

const routes: Array<RouteRecordRaw> = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/login/index.vue'),
        meta: { hidden: true }
    },
    {
        path: '/',
        component: Layout,
        redirect: '/sales-orders',
        children: [
            {
                path: 'sales-orders',
                name: 'SalesOrderList',
                component: () => import('@/views/SalesOrderList.vue'),
                meta: { title: '销售订单', icon: 'Document' }
            },
            {
                path: 'dashboard',
                name: 'Dashboard',
                component: () => import('@/views/reports/dashboard.vue'),
                meta: { title: '经营驾驶舱', icon: 'DataAnalysis' }
            },
            {
                path: 'reports/sales',
                name: 'SalesReport',
                component: () => import('@/views/reports/sales.vue'),
                meta: { title: '销售报表', icon: 'TrendCharts' }
            },
            {
                path: 'reports/finance',
                name: 'FinanceReport',
                component: () => import('@/views/reports/finance.vue'),
                meta: { title: '财务报表', icon: 'Wallet' }
            },
            {
                path: 'reports/order-status',
                name: 'OrderStatusReport',
                component: () => import('@/views/reports/order-status.vue'),
                meta: { title: '未完订单状态图', icon: 'PieChart' }
            },
            {
                path: 'reports/order-finance',
                name: 'OrderFinanceReport',
                component: () => import('@/views/reports/order-finance.vue'),
                meta: { title: '未完订单资金图', icon: 'Money' }
            },
            {
                path: 'reports/cashflow',
                name: 'CashFlowReport',
                component: () => import('@/views/reports/cashflow.vue'),
                meta: { title: '资金收付预期半月图', icon: 'Timer' }
            },
            {
                path: 'customers',
                name: 'CustomerList',
                component: () => import('@/views/customer/index.vue'),
                meta: { title: '客户管理(CRM)', icon: 'User' }
            },
            {
                path: 'products',
                name: 'ProductList',
                component: () => import('@/views/product/index.vue'),
                meta: { title: '产品管理(PIM)', icon: 'Goods' }
            },
            {
                path: 'suppliers',
                name: 'SupplierList',
                component: () => import('@/views/supplier/index.vue'),
                meta: { title: '供应商管理', icon: 'Van' }
            },
            {
                path: 'freight-forwarders',
                name: 'FreightForwarderList',
                component: () => import('@/views/freight-forwarder/index.vue'),
                meta: { title: '货代管理', icon: 'Ship' }
            },
            {
                path: 'purchase-orders',
                name: 'PurchaseOrderList',
                component: () => import('@/views/purchase/index.vue'),
                meta: { title: '采购订单', icon: 'ShoppingCart' }
            },
            {
                path: 'purchase-orders/:orderId/details',
                name: 'PurchaseOrderDetail',
                component: () => import('@/views/purchase-order/detail.vue'),
                meta: { title: '采购订单明细', hidden: true }
            },
            {
                path: 'purchase-order-details',
                name: 'PurchaseOrderDetailList',
                component: () => import('@/views/purchase-order-detail/index.vue'),
                meta: { title: '采购订单明细', icon: 'DocumentChecked' }
            },
            {
                path: 'wms',
                name: 'WMSList',
                component: () => import('@/views/wms/index.vue'),
                meta: { title: '仓储管理(WMS)', icon: 'Box' }
            },
            {
                path: 'freight-orders',
                name: 'FreightOrderList',
                component: () => import('@/views/freight-order/index.vue'),
                meta: { title: '货代订单', icon: 'Ship' }
            },
            {
                path: 'documents',
                name: 'CustomsDocList',
                component: () => import('@/views/documents/index.vue'),
                meta: { title: '报关单证', icon: 'DocumentCopy' }
            },
            {
                path: 'finance',
                name: 'FinanceList',
                component: () => import('@/views/finance/index.vue'),
                meta: { title: '财务收据', icon: 'Money' }
            },
            {
                path: 'system/user',
                name: 'UserList',
                component: () => import('@/views/system/user/index.vue'),
                meta: { title: '用户管理', icon: 'User' }
            },
            {
                path: 'system/role',
                name: 'RoleList',
                component: () => import('@/views/system/role/index.vue'),
                meta: { title: '角色管理', icon: 'UserFilled' }
            },
            {
                path: 'system/menu',
                name: 'MenuList',
                component: () => import('@/views/system/menu/index.vue'),
                meta: { title: '菜单管理', icon: 'Menu' }
            },
            {
                path: 'profile',
                name: 'Profile',
                component: () => import('@/views/profile/index.vue'),
                meta: { title: '个人中心', hidden: true }
            },
            {
                path: 'sales-orders/:orderId/details',
                name: 'SalesOrderDetail',
                component: () => import('@/views/sales-order/detail.vue'),
                meta: { title: '销售订单明细', hidden: true }
            },
            {
                path: 'sales-order-details',
                name: 'SalesOrderDetailList',
                component: () => import('@/views/sales-order-detail/index.vue'),
                meta: { title: '销售订单明细', icon: 'DocumentChecked' }
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

const whiteList = ['/login'] // Routes that don't need authentication

router.beforeEach((to, _from, next) => {
    const token = localStorage.getItem('token')

    if (token) {
        if (to.path === '/login') {
            next({ path: '/' })
        } else {
            next()
        }
    } else {
        if (whiteList.indexOf(to.path) !== -1) {
            next()
        } else {
            next(`/login?redirect=${to.path}`)
        }
    }
})

export default router
