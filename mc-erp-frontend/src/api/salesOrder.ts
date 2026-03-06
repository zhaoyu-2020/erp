import request from '@/utils/request'

export function getOrderPage(params: any) {
    return request({
        url: '/sales-orders/page',
        method: 'get',
        params
    })
}

export function saveSalesOrder(data: any) {
    return request({ url: '/sales-orders', method: 'post', data })
}

export function updateSalesOrder(data: any) {
    return request({ url: '/sales-orders', method: 'put', data })
}

export function deleteSalesOrder(id: number) {
    return request({ url: `/sales-orders/${id}`, method: 'delete' })
}
