import request from '@/utils/request'

export function getCustomerPage(params: any) {
    return request({
        url: '/customers/page',
        method: 'get',
        params
    })
}
