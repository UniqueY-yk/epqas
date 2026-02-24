import request from '../utils/request'

export interface PaperQuestionDTO {
    questionId: number;
    scoreValue: number;
    questionOrder: number;
}

export interface ExaminationPaperDTO {
    paperId?: number;
    title: string;
    courseId: number;
    setterId?: number;
    totalScore: number;
    durationMinutes: number;
    targetDifficulty: number;
    status?: string;
    createdAt?: string;
    questions: PaperQuestionDTO[];
}

export const getPapers = (params: any) => {
    return request({
        url: '/exam/papers/page',
        method: 'get',
        params
    })
}

export const getPaperById = (id: number) => {
    return request({
        url: `/exam/papers/${id}`,
        method: 'get'
    })
}

export const addPaper = (data: ExaminationPaperDTO) => {
    return request({
        url: '/exam/papers',
        method: 'post',
        data
    })
}

export const updatePaper = (data: ExaminationPaperDTO) => {
    return request({
        url: '/exam/papers',
        method: 'put',
        data
    })
}

export const deletePaper = (id: number) => {
    return request({
        url: `/exam/papers/${id}`,
        method: 'delete'
    })
}
