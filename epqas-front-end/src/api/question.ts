import request from '@/utils/request'

export function getQuestions(params: any) {
    return request({
        url: '/question/questions/page',
        method: 'get',
        params
    })
}

export function getQuestionById(id: number) {
    return request({
        url: `/question/questions/${id}`,
        method: 'get'
    })
}

export function addQuestion(data: any) {
    return request({
        url: '/question/questions',
        method: 'post',
        data
    })
}

export function updateQuestion(data: any) {
    return request({
        url: '/question/questions',
        method: 'put',
        data
    })
}

export function deleteQuestion(id: number) {
    return request({
        url: `/question/questions/${id}`,
        method: 'delete'
    })
}
