import request from '@/utils/request'

export function getDetailPage(params: any) {
  return request({
    url: '/sales-order-details/page',
    method: 'get',
    params
  })
}

export function getDetailListByOrderId(orderId: number | string) {
  return request({
    url: `/sales-order-details/order/${orderId}`,
    method: 'get'
  })
}

export function saveSalesOrderDetail(data: any) {
  return request({ url: '/sales-order-details', method: 'post', data })
}

export function updateSalesOrderDetail(data: any) {
  return request({ url: '/sales-order-details', method: 'put', data })
}

export function deleteSalesOrderDetail(id: number) {
  return request({ url: `/sales-order-details/${id}`, method: 'delete' })
}
