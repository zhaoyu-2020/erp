import request from '@/utils/request'

export function getForwarderAccountList(forwarderId: number) {
    return request({
        url: '/forwarder-accounts/list',
        method: 'get',
        params: { forwarderId }
    })
}

export function saveForwarderAccount(data: any) {
    return request({ url: '/forwarder-accounts', method: 'post', data })
}

export function updateForwarderAccount(data: any) {
    return request({ url: '/forwarder-accounts', method: 'put', data })
}

export function deleteForwarderAccount(id: number) {
    return request({ url: `/forwarder-accounts/${id}`, method: 'delete' })
}
