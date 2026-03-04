import request from '@/utils/request'

export function getOrderPage(params: any) {
    return request({
        url: '/sales-orders/page',
        method: 'get',
        params
    })
}
