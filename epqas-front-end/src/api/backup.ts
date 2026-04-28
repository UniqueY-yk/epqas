import request from '@/utils/request'

/**
 * 数据备份管理 API 接口
 */

/**
 * 获取备份文件列表
 */
export function getBackupList() {
    return request({
        url: '/backup/list',
        method: 'get'
    })
}

/**
 * 创建数据库备份
 */
export function createBackup() {
    return request({
        url: '/backup/create',
        method: 'post'
    })
}

/**
 * 删除指定备份文件
 * @param fileName 备份文件名
 */
export function deleteBackup(fileName: string) {
    return request({
        url: `/backup/delete/${fileName}`,
        method: 'delete'
    })
}
