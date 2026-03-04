import request from '@/utils/request'

export function getProductPage(params: any) {
    return request({
        url: '/products/page',
        method: 'get',
        params
    })
}
