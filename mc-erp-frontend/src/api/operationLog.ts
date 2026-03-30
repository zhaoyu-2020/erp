import request from '@/utils/request'

// 操作日志 API

/**
 * 分页查询操作日志
 */
export function getOperationLogPage(params: any) {
    return request({ url: '/operation-logs/page', method: 'get', params })
}

/**
 * 查看日志详情
 */
export function getOperationLogDetail(id: number) {
    return request({ url: `/operation-logs/${id}`, method: 'get' })
}

/**
 * 导出操作日志 (返回 Blob)
 */
export function exportOperationLogs(params: any) {
    return request({
        url: '/operation-logs/export',
        method: 'get',
        params,
        responseType: 'blob'
    } as any)
}

/**
 * 清理日志
 */
export function cleanOperationLogs(days: number) {
    return request({ url: '/operation-logs/clean', method: 'delete', params: { days } })
}
