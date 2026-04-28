package com.epqas.audit.service;

import com.epqas.audit.dto.BackupFileDTO;

import java.util.List;

/**
 * 数据库备份服务接口
 */
public interface DatabaseBackupService {

    /**
     * 获取所有备份文件列表
     *
     * @return 备份文件信息列表
     */
    List<BackupFileDTO> listBackupFiles();

    /**
     * 创建数据库备份
     *
     * @return 生成的备份文件名
     */
    String createBackup();

    /**
     * 删除指定备份文件
     *
     * @param fileName 备份文件名
     * @return 是否删除成功
     */
    boolean deleteBackup(String fileName);
}
