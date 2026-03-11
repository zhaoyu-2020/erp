import request from '@/utils/request'

export function getPurchaseDetailPage(params: any) {
  return request({
    url: '/purchase-order-details/page',
    method: 'get',
    params
  })
}

export function getPurchaseDetailListByOrderId(orderId: number | string) {
  return request({
    url: `/purchase-order-details/order/${orderId}`,
    method: 'get'
  })
}

export function savePurchaseOrderDetail(data: any) {
  return request({ url: '/purchase-order-details', method: 'post', data })
}

export function updatePurchaseOrderDetail(data: any) {
  return request({ url: '/purchase-order-details', method: 'put', data })
}

export function deletePurchaseOrderDetail(id: number) {
  return request({ url: `/purchase-order-details/${id}`, method: 'delete' })
}
