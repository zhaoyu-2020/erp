import request from '@/utils/request'

export function getSupplierAccountList(supplierId: number) {
    return request({
        url: '/supplier-accounts/list',
        method: 'get',
        params: { supplierId }
    })
}

export function saveSupplierAccount(data: any) {
    return request({ url: '/supplier-accounts', method: 'post', data })
}

export function updateSupplierAccount(data: any) {
    return request({ url: '/supplier-accounts', method: 'put', data })
}

export function deleteSupplierAccount(id: number) {
    return request({ url: `/supplier-accounts/${id}`, method: 'delete' })
}
