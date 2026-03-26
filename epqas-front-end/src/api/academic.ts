import request from '@/utils/request'

// Classes
export function getClasses(params: any) {
    return request({
        url: '/academic/classes',
        method: 'get',
        params
    })
}

export function getStudentsByClassId(classId: number) {
    return request({
        url: `/academic/students/class/${classId}`,
        method: 'get'
    })
}

export function addClass(data: any) {
    return request({
        url: '/academic/classes',
        method: 'post',
        data
    })
}

export function updateClass(data: any) {
    return request({
        url: '/academic/classes',
        method: 'put',
        data
    })
}

export function deleteClass(id: number) {
    return request({
        url: `/academic/classes/${id}`,
        method: 'delete'
    })
}

// Courses
export function getCourses(params: any) {
    return request({
        url: '/academic/courses',
        method: 'get',
        params
    })
}

export function addCourse(data: any) {
    return request({
        url: '/academic/courses',
        method: 'post',
        data
    })
}

export function updateCourse(data: any) {
    return request({
        url: '/academic/courses',
        method: 'put',
        data
    })
}

export function deleteCourse(id: number) {
    return request({
        url: `/academic/courses/${id}`,
        method: 'delete'
    })
}
