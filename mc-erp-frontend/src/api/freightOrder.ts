import request from '@/utils/request'

// ============ 货代订单 ============

export function getFreightOrderPage(params: any) {
    return request({ url: '/freight-orders/page', method: 'get', params })
}

export function getFreightOrderDetail(orderId: number) {
    return request({ url: `/freight-orders/${orderId}`, method: 'get' })
}

export function createFreightOrder(data: any) {
    return request({ url: '/freight-orders', method: 'post', data })
}

export function updateFreightOrder(data: any) {
    return request({ url: '/freight-orders', method: 'put', data })
}

export function deleteFreightOrder(orderId: number) {
    return request({ url: `/freight-orders/${orderId}`, method: 'delete' })
}

export function submitFreightOrder(orderId: number) {
    return request({ url: `/freight-orders/${orderId}/submit`, method: 'put' })
}

export function settleFreightOrder(orderId: number) {
    return request({ url: `/freight-orders/${orderId}/settle`, method: 'put' })
}

export function cancelFreightOrder(orderId: number, cancelReason: string) {
    return request({ url: `/freight-orders/${orderId}/cancel`, method: 'put', data: { cancelReason } })
}

// ============ 费用明细 ============

export function getFreightFeeItems(orderId: number) {
    return request({ url: `/freight-orders/${orderId}/fee-items`, method: 'get' })
}

export function saveFreightFeeItems(orderId: number, items: any[]) {
    return request({ url: `/freight-orders/${orderId}/fee-items`, method: 'post', data: items })
}

export function deleteFreightFeeItem(itemId: number) {
    return request({ url: `/freight-orders/fee-items/${itemId}`, method: 'delete' })
}

// ============ 操作日志 ============

export function getFreightOrderLogs(orderId: number) {
    return request({ url: `/freight-orders/${orderId}/logs`, method: 'get' })
}

// ============ 状态元数据 ============

export function getFreightOrderStatusMeta() {
    return request({ url: '/freight-orders/status-meta', method: 'get' })
}
