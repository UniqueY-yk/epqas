import request from '../utils/request'

export const getAuthStats = () => {
    return request({
        url: '/auth/dashboard/stats',
        method: 'get'
    })
}

export const getAcademicStats = () => {
    return request({
        url: '/academic/dashboard/stats',
        method: 'get'
    })
}

export const getExamStats = () => {
    return request({
        url: '/exam/dashboard/stats',
        method: 'get'
    })
}

export const getAnalysisStats = () => {
    return request({
        url: '/analysis/dashboard/stats',
        method: 'get'
    })
}
