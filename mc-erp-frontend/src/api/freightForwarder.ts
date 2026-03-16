import request from '@/utils/request'

export function getFreightForwarderPage(params: any) {
  return request({
    url: '/freight-forwarders/page',
    method: 'get',
    params
  })
}

export function saveFreightForwarder(data: any) {
  return request({ url: '/freight-forwarders', method: 'post', data })
}

export function updateFreightForwarder(data: any) {
  return request({ url: '/freight-forwarders', method: 'put', data })
}

export function deleteFreightForwarder(id: number) {
  return request({ url: `/freight-forwarders/${id}`, method: 'delete' })
}
