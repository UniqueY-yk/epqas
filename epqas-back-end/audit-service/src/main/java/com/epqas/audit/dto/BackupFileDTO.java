package com.epqas.audit.dto;

import lombok.Data;

/**
 * 数据库备份文件信息DTO
 * <p>
 * 用于向前端返回备份文件的基本信息。
 * </p>
 */
@Data
public class BackupFileDTO {

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件大小（人类可读格式，如 "12.5 MB"）
     */
    private String fileSizeReadable;

    /**
     * 创建时间（ISO格式字符串）
     */
    private String createTime;
}
