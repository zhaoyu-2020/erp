import request from '@/utils/request'

export function getStockPage(params: any) {
    return request({
        url: '/stocks/page',
        method: 'get',
        params
    })
}

export function saveStock(data: any) {
    return request({ url: '/stocks', method: 'post', data })
}

export function updateStock(data: any) {
    return request({ url: '/stocks', method: 'put', data })
}

export function deleteStock(id: number) {
    return request({ url: `/stocks/${id}`, method: 'delete' })
}

export function getCustomsDocPage(params: any) {
    return request({
        url: '/customs-docs/page',
        method: 'get',
        params
    })
}

export function saveCustomsDoc(data: any) {
    return request({ url: '/customs-docs', method: 'post', data })
}

export function updateCustomsDoc(data: any) {
    return request({ url: '/customs-docs', method: 'put', data })
}

export function deleteCustomsDoc(id: number) {
    return request({ url: `/customs-docs/${id}`, method: 'delete' })
}

export function getFinanceReceiptPage(params: any) {
    return request({
        url: '/finance-receipts/page',
        method: 'get',
        params
    })
}

export function getFinanceReceiptById(id: number) {
    return request({ url: `/finance-receipts/${id}`, method: 'get' })
}

export function saveFinanceReceipt(data: any) {
    return request({ url: '/finance-receipts', method: 'post', data })
}

export function updateFinanceReceipt(data: any) {
    return request({ url: '/finance-receipts', method: 'put', data })
}

export function deleteFinanceReceipt(id: number) {
    return request({ url: `/finance-receipts/${id}`, method: 'delete' })
}

// ========== 财务付款 ==========
export function getFinancePaymentPage(params: any) {
    return request({
        url: '/finance-payments/page',
        method: 'get',
        params
    })
}

export function getFinancePaymentById(id: number) {
    return request({ url: `/finance-payments/${id}`, method: 'get' })
}

export function saveFinancePayment(data: any) {
    return request({ url: '/finance-payments', method: 'post', data })
}

export function updateFinancePayment(data: any) {
    return request({ url: '/finance-payments', method: 'put', data })
}

export function deleteFinancePayment(id: number) {
    return request({ url: `/finance-payments/${id}`, method: 'delete' })
}

export function auditFinancePayment(data: any) {
    return request({ url: '/finance-payments/audit', method: 'post', data })
}
