import request from '@/utils/request'

export function getCustomerPage(params: any) {
    return request({
        url: '/customers/page',
        method: 'get',
        params
    })
}

export function saveCustomer(data: any) {
    return request({ url: '/customers', method: 'post', data })
}

export function updateCustomer(data: any) {
    return request({ url: '/customers', method: 'put', data })
}

export function deleteCustomer(id: number) {
    return request({ url: `/customers/${id}`, method: 'delete' })
}
