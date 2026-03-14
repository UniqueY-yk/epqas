import request from '../utils/request'

export interface PaperAnalysisVO {
    analysisId: number;
    examId: number;
    paperId: number;
    paperTitle: string;
    courseName: string;
    examDate: string;
    averageScore: number;
    highestScore: number;
    lowestScore: number;
    reliabilityCoefficient: number;
    validityCoefficient: number;
    knowledgeCoverageRate: number;
    overallDifficulty: number;
    overallDiscrimination: number;
    isAbnormal: boolean;
}

export const getMyPaperAnalyses = (params: any) => {
    return request({
        url: '/analysis/paper-analysis/my-papers',
        method: 'get',
        params
    })
}

export const calculateExamIndicators = (examId: number) => {
    return request({
        url: `/analysis/paper-analysis/calculate/${examId}`,
        method: 'post'
    })
}

// Question Analysis Data for Charting
export interface QuestionAnalysisVO {
    questionId: number;
    stem: string;
    questionType: string;
    correctResponseRate: number; // Difficulty (X Axis)
    discriminationIndex: number; // Discrimination (Y Axis)
    isTooEasy: boolean;
    isLowDiscrimination: boolean;
    diagnosisTag: string;
    isAbnormal: boolean;
    selectionDistributionJson?: string;
    suggestions?: string[];
}

export const getQuestionAnalysisByExamId = (examId: number) => {
    return request<QuestionAnalysisVO[]>({
        url: `/analysis/question-analysis/exam/${examId}`,
        method: 'get'
    })
}

// Improvement Suggestions
export interface ImprovementSuggestion {
    suggestionId: number;
    examId: number;
    questionId: number;
    suggestionType: string;
    suggestionText: string;
    generatedRule: string;
    isImplemented: boolean;
    createdAt: string;
    updatedAt: string;
}

export const getSuggestionsByExamId = (examId: number, questionId?: number) => {
    return request<ImprovementSuggestion[]>({
        url: '/suggestions',
        method: 'get',
        params: {
            examId,
            questionId
        }
    })
}

// Trend Analysis
export const getPropositionTrend = (setterId: number) => {
    return request<PaperAnalysisVO[]>({
        url: '/analysis/paper-analysis/trend',
        method: 'get',
        params: {
            setterId
        }
    })
}
