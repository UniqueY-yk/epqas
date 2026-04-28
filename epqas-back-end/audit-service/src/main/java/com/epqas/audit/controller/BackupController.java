package com.epqas.audit.controller;

import com.epqas.audit.dto.BackupFileDTO;
import com.epqas.audit.service.DatabaseBackupService;
import com.epqas.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据备份管理控制器
 * <p>
 * 提供数据库备份的创建、查询、删除功能。
 * 仅限管理员角色（roleId=1）操作。
 * </p>
 *
 * @author EPQAS
 */
@RestController
@RequestMapping("/backup")
public class BackupController {

    @Autowired
    private DatabaseBackupService databaseBackupService;

    /**
     * 获取备份文件列表
     *
     * @return 备份文件信息列表
     */
    @GetMapping("/list")
    public Result<List<BackupFileDTO>> listBackups() {
        return Result.success(databaseBackupService.listBackupFiles());
    }

    /**
     * 创建数据库备份
     * <p>
     * 执行 mysqldump 导出当前数据库为 .sql 文件。
     * </p>
     *
     * @return 生成的备份文件名
     */
    @PostMapping("/create")
    public Result<String> createBackup() {
        try {
            String fileName = databaseBackupService.createBackup();
            return Result.success(fileName);
        } catch (Exception e) {
            return Result.error("备份失败: " + e.getMessage());
        }
    }

    /**
     * 删除指定备份文件
     *
     * @param fileName 备份文件名
     * @return 是否删除成功
     */
    @DeleteMapping("/delete/{fileName}")
    public Result<Boolean> deleteBackup(@PathVariable("fileName") String fileName) {
        try {
            return Result.success(databaseBackupService.deleteBackup(fileName));
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }
}
