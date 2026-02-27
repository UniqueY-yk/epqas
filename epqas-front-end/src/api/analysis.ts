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
