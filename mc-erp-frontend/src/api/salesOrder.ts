import request from '@/utils/request'

export function getOrderPage(params: any) {
    return request({
        url: '/sales-orders/page',
        method: 'get',
        params
    })
}

export function getSalesOrderFreightDefaults(orderNo: string) {
    return request({ url: `/sales-orders/freight-defaults/${encodeURIComponent(orderNo)}`, method: 'get' })
}

export function saveSalesOrder(data: any) {
    return request({ url: '/sales-orders', method: 'post', data })
}

export function updateSalesOrder(data: any) {
    return request({ url: '/sales-orders', method: 'put', data })
}

export function deleteSalesOrder(id: number) {
    return request({ url: `/sales-orders/${id}`, method: 'delete' })
}

export function importSalesOrderContract(file: File) {
    const form = new FormData()
    form.append('file', file)
    return request({ url: '/sales-orders/import/contract', method: 'post', data: form })
}

export function importSalesOrderDetails(file: File) {
    const form = new FormData()
    form.append('file', file)
    return request({ url: '/sales-orders/import/details', method: 'post', data: form })
}

export function downloadSalesContractTemplate() {
    return request({ url: '/sales-orders/import/contract/template', method: 'get', responseType: 'blob' })
}

export function downloadSalesDetailsTemplate() {
    return request({ url: '/sales-orders/import/details/template', method: 'get', responseType: 'blob' })
}
