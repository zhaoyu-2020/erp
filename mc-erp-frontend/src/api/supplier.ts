import request from '@/utils/request'

export function getSupplierPage(params: any) {
    return request({
        url: '/suppliers/page',
        method: 'get',
        params
    })
}
