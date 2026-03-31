import request from '../utils/request'

export interface PaperQuestionDTO {
    questionId: number;
    scoreValue: number;
    questionOrder: number;
    questionContent?: string;
    questionType?: string;
}

export interface ExaminationPaperDTO {
    paperId?: number;
    title: string;
    courseId: number;
    setterId?: number;
    setterName?: string;
    totalScore: number;
    durationMinutes: number;
    targetDifficulty: number;
    status?: string;
    createdAt?: string;
    questions: PaperQuestionDTO[];
}

export interface SetterInfo {
    userId: number;
    realName: string;
    username: string;
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

export const getSetters = () => {
    return request({
        url: '/exam/papers/setters',
        method: 'get'
    })
}

export interface ExaminationDTO {
    examId?: number;
    paperId: number | null;
    classId: number | null;
    examDate: string;
    totalCandidates: number;
    actualExaminees: number;
}

export const getExaminationsPage = (params: any) => {
    return request({
        url: '/exam/examinations/page',
        method: 'get',
        params
    })
}

export const createExamination = (data: ExaminationDTO) => {
    return request({
        url: '/exam/examinations',
        method: 'post',
        data
    })
}

export const updateExamination = (data: ExaminationDTO) => {
    return request({
        url: '/exam/examinations',
        method: 'put',
        data
    })
}

export const deleteExamination = (id: number) => {
    return request({
        url: `/exam/examinations/${id}`,
        method: 'delete'
    })
}

export const batchSaveStudentResult = (data: any) => {
    return request({
        url: '/exam/results/batch',
        method: 'post',
        data
    })
}

export const getResultsByExamId = (examId: number) => {
    return request({
        url: `/exam/results/exam/${examId}`,
        method: 'get'
    })
}

export const getResultByExamAndStudent = (examId: number, studentId: number) => {
    return request({
        url: `/exam/results/exam/${examId}/student/${studentId}`,
        method: 'get'
    })
}

export const getAnswersByResultId = (resultId: number) => {
    return request({
        url: `/exam/answers/result/${resultId}`,
        method: 'get'
    })
}

// Class Analysis APIs
export const getQuestionStats = (examId: number) => {
    return request({
        url: `/exam/class-analysis/${examId}/questions`,
        method: 'get'
    })
}

export const getKnowledgeMastery = (examId: number) => {
    return request({
        url: `/exam/class-analysis/${examId}/knowledge-mastery`,
        method: 'get'
    })
}

export const getAbnormalDetection = (examId: number) => {
    return request({
        url: `/exam/class-analysis/${examId}/abnormal-detection`,
        method: 'get'
    })
}
