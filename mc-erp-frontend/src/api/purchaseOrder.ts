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

export function importPurchaseOrderContract(file: File) {
    const form = new FormData()
    form.append('file', file)
    return request({ url: '/purchase-orders/import/contract', method: 'post', data: form })
}

export function importPurchaseOrderDetails(file: File) {
    const form = new FormData()
    form.append('file', file)
    return request({ url: '/purchase-orders/import/details', method: 'post', data: form })
}

export function downloadPurchaseContractTemplate() {
    return request({ url: '/purchase-orders/import/contract/template', method: 'get', responseType: 'blob' })
}

export function downloadPurchaseDetailsTemplate() {
    return request({ url: '/purchase-orders/import/details/template', method: 'get', responseType: 'blob' })
}
