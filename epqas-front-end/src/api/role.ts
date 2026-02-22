import request from '@/utils/request'

export function getRolesPage(params: any) {
    return request({
        url: '/auth/roles/page',
        method: 'get',
        params
    })
}

export function getAllRoles() {
    return request({
        url: '/auth/roles',
        method: 'get'
    })
}

export function addRole(data: any) {
    return request({
        url: '/auth/roles',
        method: 'post',
        data
    })
}

export function updateRole(data: any) {
    return request({
        url: '/auth/roles',
        method: 'put',
        data
    })
}

export function deleteRole(id: number) {
    return request({
        url: `/auth/roles/${id}`,
        method: 'delete'
    })
}
