import request from '@/utils/request'

/**
 * 审计日志 API 接口
 */

/**
 * 分页查询审计日志（支持多条件筛选）
 * @param params { current, size, userId, actionType, targetTable, startTime, endTime }
 */
export function getAuditLogPage(params: any) {
    return request({
        url: '/audit/audit-logs/page',
        method: 'get',
        params
    })
}

/**
 * 根据ID获取审计日志详情
 */
export function getAuditLogById(id: number) {
    return request({
        url: `/audit/audit-logs/${id}`,
        method: 'get'
    })
}
