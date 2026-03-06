import request from '@/utils/request'

export function getProductPage(params: any) {
    return request({
        url: '/products/page',
        method: 'get',
        params
    })
}

export function saveProduct(data: any) {
    return request({ url: '/products', method: 'post', data })
}

export function updateProduct(data: any) {
    return request({ url: '/products', method: 'put', data })
}

export function deleteProduct(id: number) {
    return request({ url: `/products/${id}`, method: 'delete' })
}
