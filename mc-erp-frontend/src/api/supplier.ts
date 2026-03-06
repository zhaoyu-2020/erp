import request from '@/utils/request'

export function getSupplierPage(params: any) {
    return request({
        url: '/suppliers/page',
        method: 'get',
        params
    })
}

export function saveSupplier(data: any) {
    return request({ url: '/suppliers', method: 'post', data })
}

export function updateSupplier(data: any) {
    return request({ url: '/suppliers', method: 'put', data })
}

export function deleteSupplier(id: number) {
    return request({ url: `/suppliers/${id}`, method: 'delete' })
}
