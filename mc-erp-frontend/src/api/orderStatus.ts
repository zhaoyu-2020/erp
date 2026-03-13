import request from '@/utils/request'

export function getSalesOrderStatusMeta() {
    return request({ url: '/status-meta/sales-order', method: 'get' })
}

export function getPurchaseOrderStatusMeta() {
    return request({ url: '/status-meta/purchase-order', method: 'get' })
}

export function updateSalesOrderStatus(id: number, status: number) {
    return request({ url: `/sales-orders/${id}/status`, method: 'patch', data: { status } })
}

export function updatePurchaseOrderStatus(id: number, status: number) {
    return request({ url: `/purchase-orders/${id}/status`, method: 'patch', data: { status } })
}
