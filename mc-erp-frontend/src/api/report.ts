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
