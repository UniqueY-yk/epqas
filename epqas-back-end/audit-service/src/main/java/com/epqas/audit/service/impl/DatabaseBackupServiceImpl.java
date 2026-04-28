package com.epqas.audit.service.impl;

import com.epqas.audit.dto.BackupFileDTO;
import com.epqas.audit.service.DatabaseBackupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 数据库备份服务实现类
 * <p>
 * 通过调用服务器端 mysqldump 命令行工具导出 MySQL 数据库为 .sql 文件，
 * 存储在指定的备份目录中。
 * </p>
 *
 * @author EPQAS
 */
@Slf4j
@Service
public class DatabaseBackupServiceImpl implements DatabaseBackupService {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    /**
     * 备份文件存储目录（相对于应用运行目录）
     */
    private static final String BACKUP_DIR = "./backups";

    /**
     * mysqldump 可执行文件路径。如果 mysqldump 已加入系统 PATH，直接使用 "mysqldump"。
     * 若未加入 PATH，可改为绝对路径，例如 "C:/Program Files/MySQL/MySQL Server 8.0/bin/mysqldump"
     */
    private static final String MYSQLDUMP_PATH = "mysqldump";

    @Override
    public List<BackupFileDTO> listBackupFiles() {
        List<BackupFileDTO> result = new ArrayList<>();
        File dir = new File(BACKUP_DIR);
        if (!dir.exists() || !dir.isDirectory()) {
            return result;
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith(".sql"));
        if (files == null) {
            return result;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (File file : files) {
            BackupFileDTO dto = new BackupFileDTO();
            dto.setFileName(file.getName());
            dto.setFileSize(file.length());
            dto.setFileSizeReadable(formatFileSize(file.length()));
            try {
                BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                LocalDateTime createTime = LocalDateTime.ofInstant(
                        attrs.creationTime().toInstant(), ZoneId.systemDefault());
                dto.setCreateTime(createTime.format(formatter));
            } catch (Exception e) {
                dto.setCreateTime("未知");
            }
            result.add(dto);
        }

        // 按创建时间倒序排列（最新的在前）
        result.sort(Comparator.comparing(BackupFileDTO::getCreateTime).reversed());
        return result;
    }

    @Override
    public String createBackup() {
        // 确保备份目录存在
        Path backupPath = Paths.get(BACKUP_DIR);
        try {
            Files.createDirectories(backupPath);
        } catch (Exception e) {
            throw new RuntimeException("创建备份目录失败: " + e.getMessage(), e);
        }

        // 从 JDBC URL 中解析数据库名称和主机端口
        String dbName = extractDatabaseName(datasourceUrl);
        String host = extractHost(datasourceUrl);
        int port = extractPort(datasourceUrl);

        // 生成备份文件名（带时间戳）
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "epqas_backup_" + timestamp + ".sql";
        String filePath = backupPath.resolve(fileName).toAbsolutePath().toString();

        // 构建 mysqldump 命令
        List<String> command = new ArrayList<>();
        command.add(MYSQLDUMP_PATH);
        command.add("-h" + host);
        command.add("-P" + port);
        command.add("-u" + dbUsername);
        command.add("-p" + dbPassword);
        command.add("--default-character-set=utf8mb4");
        command.add("--single-transaction"); // InnoDB 一致性快照，避免锁表
        command.add("--routines");           // 包含存储过程和函数
        command.add("--triggers");           // 包含触发器
        command.add("--result-file=" + filePath);
        command.add(dbName);

        log.info("开始执行数据库备份: {}", String.join(" ", command));

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // 读取命令输出（用于排错）
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("mysqldump 执行失败，退出码: {}，输出: {}", exitCode, output);
                throw new RuntimeException("数据库备份失败，mysqldump 退出码: " + exitCode + "，详情: " + output);
            }

            // 验证文件是否生成
            File backupFile = new File(filePath);
            if (!backupFile.exists() || backupFile.length() == 0) {
                throw new RuntimeException("备份文件生成失败或文件为空");
            }

            log.info("数据库备份完成: {} ({})", fileName, formatFileSize(backupFile.length()));
            return fileName;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("数据库备份异常", e);
            throw new RuntimeException("数据库备份异常: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteBackup(String fileName) {
        // 安全检查：防止路径遍历攻击
        if (fileName == null || fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new IllegalArgumentException("非法文件名: " + fileName);
        }

        File file = new File(BACKUP_DIR, fileName);
        if (!file.exists()) {
            throw new RuntimeException("备份文件不存在: " + fileName);
        }

        boolean deleted = file.delete();
        if (deleted) {
            log.info("已删除备份文件: {}", fileName);
        } else {
            log.warn("删除备份文件失败: {}", fileName);
        }
        return deleted;
    }

    // ==================== 工具方法 ====================

    /**
     * 从 JDBC URL 中解析数据库名称
     * 示例: jdbc:mysql://localhost:3306/epqas_db?... -> epqas_db
     */
    private String extractDatabaseName(String url) {
        String withoutParams = url.split("\\?")[0];
        return withoutParams.substring(withoutParams.lastIndexOf('/') + 1);
    }

    /**
     * 从 JDBC URL 中解析主机地址
     */
    private String extractHost(String url) {
        String afterProtocol = url.substring(url.indexOf("//") + 2);
        String hostPort = afterProtocol.split("/")[0];
        return hostPort.split(":")[0];
    }

    /**
     * 从 JDBC URL 中解析端口号
     */
    private int extractPort(String url) {
        try {
            String afterProtocol = url.substring(url.indexOf("//") + 2);
            String hostPort = afterProtocol.split("/")[0];
            String[] parts = hostPort.split(":");
            if (parts.length > 1) {
                return Integer.parseInt(parts[1]);
            }
        } catch (Exception e) {
            log.warn("解析端口失败，使用默认端口3306");
        }
        return 3306;
    }

    /**
     * 将字节数格式化为人类可读的文件大小
     */
    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.##").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
