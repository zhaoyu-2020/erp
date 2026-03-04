import request from '@/utils/request'

// User API
export function getUserPage(params: any) {
    return request({ url: '/users/page', method: 'get', params })
}
export function getUserById(id: number) {
    return request({ url: `/users/${id}`, method: 'get' })
}
export function saveUser(data: any) {
    return request({ url: '/users', method: 'post', data })
}
export function updateUser(data: any) {
    return request({ url: '/users', method: 'put', data })
}
export function deleteUser(id: number) {
    return request({ url: `/users/${id}`, method: 'delete' })
}

// Role API
export function getRolePage(params: any) {
    return request({ url: '/roles/page', method: 'get', params })
}
export function getRoleById(id: number) {
    return request({ url: `/roles/${id}`, method: 'get' })
}
export function saveRole(data: any) {
    return request({ url: '/roles', method: 'post', data })
}
export function updateRole(data: any) {
    return request({ url: '/roles', method: 'put', data })
}
export function deleteRole(id: number) {
    return request({ url: `/roles/${id}`, method: 'delete' })
}

// Menu API
export function getMenuTree() {
    return request({ url: '/menus/tree', method: 'get' })
}
export function getMenuById(id: number) {
    return request({ url: `/menus/${id}`, method: 'get' })
}
export function saveMenu(data: any) {
    return request({ url: '/menus', method: 'post', data })
}
export function updateMenu(data: any) {
    return request({ url: '/menus', method: 'put', data })
}
export function deleteMenu(id: number) {
    return request({ url: `/menus/${id}`, method: 'delete' })
}
