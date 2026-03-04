import request from '@/utils/request'

export function getPurchaseOrderPage(params: any) {
    return request({
        url: '/purchase-orders/page',
        method: 'get',
        params
    })
}
