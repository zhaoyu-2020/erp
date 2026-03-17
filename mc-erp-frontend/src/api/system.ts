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
/**
 * 查询所有用户（含角色名称），用于前端搜索下拉
 */
export function getUserListWithRoles() {
    return request({ url: '/users/list-with-roles', method: 'get' })
}

// Role API
export function getRolePage(params: any) {
    return request({ url: '/roles/page', method: 'get', params })
}
export function getRoleList() {
    return request({ url: '/roles/list', method: 'get' })
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

// User-Role API
export function getUserRoleIds(userId: number) {
    return request({ url: `/users/${userId}/roles`, method: 'get' })
}
export function updateUserRoles(userId: number, roleIds: number[]) {
    return request({ url: `/users/${userId}/roles`, method: 'put', data: { roleIds } })
}

// Role-Menu API（权限配置）
export function getRoleMenuIds(roleId: number) {
    return request({ url: `/roles/${roleId}/menus`, method: 'get' })
}
export function updateRoleMenus(roleId: number, menuIds: number[]) {
    return request({ url: `/roles/${roleId}/menus`, method: 'put', data: menuIds })
}

// Profile API
export function changePassword(data: { oldPassword: string; newPassword: string }) {
    return request({ url: '/users/change-password', method: 'put', data })
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
