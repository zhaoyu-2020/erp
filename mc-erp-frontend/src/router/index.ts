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
                path: 'purchase-orders',
                name: 'PurchaseOrderList',
                component: () => import('@/views/purchase/index.vue'),
                meta: { title: '采购订单', icon: 'ShoppingCart' }
            },
            {
                path: 'wms',
                name: 'WMSList',
                component: () => import('@/views/wms/index.vue'),
                meta: { title: '仓储管理(WMS)', icon: 'Box' }
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
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

const whiteList = ['/login'] // Routes that don't need authentication

router.beforeEach((to, from, next) => {
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
