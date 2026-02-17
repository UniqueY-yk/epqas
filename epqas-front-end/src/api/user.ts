import request from '@/utils/request'

export function getUsers(params: any) {
    return request({
        url: '/users',
        method: 'get',
        params
    })
}

export function addUser(data: any) {
    return request({
        url: '/users',
        method: 'post',
        data
    })
}

export function updateUser(data: any) {
    return request({
        url: '/users',
        method: 'put',
        data
    })
}

export function deleteUser(id: number) {
    return request({
        url: `/users/${id}`,
        method: 'delete'
    })
}
