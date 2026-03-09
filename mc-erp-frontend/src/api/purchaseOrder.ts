import request from '@/utils/request'

export function uploadPurchaseFiles(files: File[]) {
    const formData = new FormData()
    files.forEach(f => formData.append('files', f))
    return request({ url: '/files/upload', method: 'post', data: formData })
}

export function getPurchaseOrderPage(params: any) {
    return request({
        url: '/purchase-orders/page',
        method: 'get',
        params
    })
}

export function savePurchaseOrder(data: any) {
    return request({ url: '/purchase-orders', method: 'post', data })
}

export function updatePurchaseOrder(data: any) {
    return request({ url: '/purchase-orders', method: 'put', data })
}

export function deletePurchaseOrder(id: number) {
    return request({ url: `/purchase-orders/${id}`, method: 'delete' })
}
