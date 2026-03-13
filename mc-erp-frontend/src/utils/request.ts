import axios from 'axios'
import type { AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

export type ApiResult<T> = {
    code: number
    message: string
    data: T
}

const request = axios.create({
    baseURL: '/api/v1',
    timeout: 5000
})

request.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers['Authorization'] = 'Bearer ' + token
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

request.interceptors.response.use(
    ((response: any) => {
        // Blob responses (e.g. file downloads) are returned as-is so callers can access response.data directly
        if (response.config?.responseType === 'blob') {
            return response
        }
        const res = response.data as ApiResult<any>
        if (res.code !== 200) {
            ElMessage.error(res.message || '请求失败')
            return Promise.reject(new Error(res.message || '请求失败'))
        }
        return res
    }) as any,
    error => {
        const status = error.response?.status
        const msg = error.response?.data?.message || error.message

        // 未登录 / 登录过期：清除token并跳转登录
        if (status === 401) {
            ElMessage.error(msg || '登录已过期，请重新登录')
            localStorage.removeItem('token')
            localStorage.removeItem('userInfo')
            window.location.href = '/login'
            // 标记为已处理，避免 globalErrorHandler 重复弹窗
            error.__handled = true
            return Promise.reject(error)
        }

        // 已登录但无权限：只提示，不强制退出
        if (status === 403) {
            ElMessage.error(msg || '无操作权限')
            error.__handled = true
            return Promise.reject(error)
        }

        ElMessage.error(msg || '请求失败')
        error.__handled = true
        return Promise.reject(error)
    }
)

type Request = <T = any>(config: AxiosRequestConfig) => Promise<ApiResult<T>>

export default request as unknown as Request
