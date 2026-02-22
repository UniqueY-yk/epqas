import request from '@/utils/request'

export function getStudents(params: any) {
    return request({
        url: '/academic/students',
        method: 'get',
        params
    })
}

export function addStudent(data: any) {
    return request({
        url: '/academic/students',
        method: 'post',
        data
    })
}

export function importStudents(formData: FormData) {
    return request({
        url: '/academic/students/import',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

export function updateStudent(data: any) {
    return request({
        url: '/academic/students',
        method: 'put',
        data
    })
}

export function deleteStudent(id: number) {
    return request({
        url: `/academic/students/${id}`,
        method: 'delete'
    })
}
