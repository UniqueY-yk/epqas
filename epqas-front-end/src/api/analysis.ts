import request from '../utils/request'

export interface PaperAnalysisVO {
    analysisId: number;
    examId: number;
    paperId: number;
    paperTitle: string;
    courseName: string;
    examDate: string;
    averageScore: number;
    stdDeviation: number;
    highestScore: number;
    lowestScore: number;
    reliabilityCoefficient: number;
    validityCoefficient: number;
    knowledgeCoverageRate: number;
    overallDifficulty: number;
    overallDiscrimination: number;
    isAbnormal: boolean;
    skewness: number;
    kurtosis: number;
    reliabilityEvaluation: string;
    difficultyEvaluation: string;
    discriminationEvaluation: string;
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
    correctResponseRate: number; // legacy simple ratio
    difficultyIndex: number; // Extreme Group P value (X axis)
    discriminationIndex: number; // Extreme Group D value (Y axis)
    validityIndex: number; // Pearson r(i,T)
    isTooEasy: boolean;
    isLowDiscrimination: boolean;
    diagnosisTag: string;
    isAbnormal: boolean;
    selectionDistributionJson?: string;
    suggestions?: string[];
    difficultyEvaluation?: string;
    discriminationEvaluation?: string;
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
export const getPropositionTrend = (setterId?: number, courseId?: number) => {
    const params: any = {}
    if (setterId) params.setterId = setterId
    if (courseId) params.courseId = courseId
    
    return request<PaperAnalysisVO[]>({
        url: '/analysis/paper-analysis/trend',
        method: 'get',
        params
    })
}

// Trend Prediction
export interface TrendPredictionVO {
    predictionId: number;
    setterId: number;
    courseId: number;
    method: string;               // "WMA" or "OLS"
    dataPointsUsed: number;

    predictedDifficulty: number;
    predictedDiscrimination: number;
    predictedReliability: number;
    predictedValidity: number;

    difficultyLower: number;
    difficultyUpper: number;
    discriminationLower: number;
    discriminationUpper: number;
    reliabilityLower: number;
    reliabilityUpper: number;
    validityLower: number;
    validityUpper: number;

    difficultyTrend: string;
    discriminationTrend: string;
    reliabilityTrend: string;
    validityTrend: string;

    predictedAt: string;
}

export const getTrendPrediction = (setterId?: number, courseId?: number) => {
    const params: any = {}
    if (setterId) params.setterId = setterId
    if (courseId) params.courseId = courseId

    return request<TrendPredictionVO>({
        url: '/analysis/paper-analysis/trend/predict',
        method: 'get',
        params
    })
}
