import request from '@/utils/request'

/**
 * 获取经营驾驶舱数据
 */
export function getDashboard(params?: any) {
  return request({
    url: '/reports/dashboard',
    method: 'get',
    params
  })
}

/**
 * 获取销售报表
 */
export function getSalesReport(params: any) {
  return request({
    url: '/reports/sales',
    method: 'get',
    params
  })
}

/**
 * 获取财务报表
 */
export function getFinanceReport(params: any) {
  return request({
    url: '/reports/finance',
    method: 'get',
    params
  })
}

/**
 * 获取订单状态报表（未完订单状态图）
 */
export function getOrderStatusReport() {
  return request({
    url: '/reports/order-status',
    method: 'get'
  })
}

/**
 * 获取所有未完成订单的状态进度列表
 */
export function getIncompleteOrdersStatus() {
  return request({
    url: '/reports/incomplete-orders',
    method: 'get'
  })
}

/**
 * 获取所有未完成订单的资金数据（用于资金图）
 */
export function getIncompleteOrdersFinance() {
  return request({
    url: '/reports/incomplete-finance',
    method: 'get'
  })
}

/**
 * 获取资金收付预期半月图数据
 */
export function getHalfMonthCashFlow() {
  return request({
    url: '/reports/half-month-cashflow',
    method: 'get'
  })
}
