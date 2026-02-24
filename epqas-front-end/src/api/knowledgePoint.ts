import request from '@/utils/request'

// Knowledge Points
export function getKnowledgePoints(params: any) {
    return request({
        url: '/academic/knowledge-points',
        method: 'get',
        params
    })
}

export function addKnowledgePoint(data: any) {
    return request({
        url: '/academic/knowledge-points',
        method: 'post',
        data
    })
}

export function updateKnowledgePoint(data: any) {
    return request({
        url: '/academic/knowledge-points',
        method: 'put',
        data
    })
}

export function deleteKnowledgePoint(id: number) {
    return request({
        url: `/academic/knowledge-points/${id}`,
        method: 'delete'
    })
}
