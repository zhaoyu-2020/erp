import request from '@/utils/request'

export function getStockPage(params: any) {
    return request({
        url: '/stocks/page',
        method: 'get',
        params
    })
}

export function getCustomsDocPage(params: any) {
    return request({
        url: '/customs-docs/page',
        method: 'get',
        params
    })
}

export function getFinanceReceiptPage(params: any) {
    return request({
        url: '/finance-receipts/page',
        method: 'get',
        params
    })
}
