import request from '@/utils/request'

export function getProductTypeList() {
  return request({
    url: '/product-types/list',
    method: 'get'
  })
}

export function saveProductType(data: { typeName: string; typeNameEn?: string }) {
  return request({
    url: '/product-types',
    method: 'post',
    data
  })
}

export function updateProductType(id: number, data: { typeName: string; typeNameEn?: string }) {
  return request({
    url: `/product-types/${id}`,
    method: 'put',
    data
  })
}

export function deleteProductType(id: number) {
  return request({
    url: `/product-types/${id}`,
    method: 'delete'
  })
}
