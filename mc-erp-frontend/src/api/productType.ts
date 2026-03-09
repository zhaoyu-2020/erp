import request from '@/utils/request'

export function getProductTypeList() {
  return request({
    url: '/product-types/list',
    method: 'get'
  })
}

export function saveProductType(data: { typeName: string }) {
  return request({
    url: '/product-types',
    method: 'post',
    data
  })
}
