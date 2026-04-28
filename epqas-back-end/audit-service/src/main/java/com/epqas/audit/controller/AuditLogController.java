package com.epqas.audit.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epqas.audit.entity.AuditLog;
import com.epqas.audit.service.AuditLogService;
import com.epqas.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 审计日志控制器
 * <p>
 * 提供审计日志的 CRUD 操作及多条件分页查询接口。
 * </p>
 */
@RestController
@RequestMapping("/audit-logs")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    /**
     * 创建审计日志（供 AOP 切面通过 Feign 异步调用）
     *
     * @param auditLog 审计日志
     * @return 是否创建成功
     */
    @PostMapping
    public Result<Boolean> createAuditLog(@RequestBody AuditLog auditLog) {
        return Result.success(auditLogService.save(auditLog));
    }

    /**
     * 根据ID获取审计日志
     *
     * @param id 审计日志ID
     * @return 审计日志
     */
    @GetMapping("/{id}")
    public Result<AuditLog> getAuditLogById(@PathVariable("id") Long id) {
        return Result.success(auditLogService.getById(id));
    }

    /**
     * 更新审计日志
     *
     * @param auditLog 审计日志
     * @return 是否更新成功
     */
    @PutMapping
    public Result<Boolean> updateAuditLog(@RequestBody AuditLog auditLog) {
        return Result.success(auditLogService.updateById(auditLog));
    }

    /**
     * 删除审计日志
     *
     * @param id 审计日志ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteAuditLog(@PathVariable("id") Long id) {
        return Result.success(auditLogService.removeById(id));
    }

    /**
     * 多条件分页查询审计日志
     * <p>
     * 支持按操作人ID、操作类型、操作模块、时间范围筛选，按操作时间倒序排列。
     * </p>
     *
     * @param current   当前页码（默认1）
     * @param size      每页数量（默认10）
     * @param userId    操作人ID（可选）
     * @param actionType 操作类型（可选）
     * @param targetTable 操作模块描述（可选，模糊匹配）
     * @param startTime 开始时间（可选，格式 yyyy-MM-dd HH:mm:ss）
     * @param endTime   结束时间（可选，格式 yyyy-MM-dd HH:mm:ss）
     * @return 分页审计日志
     */
    @GetMapping("/page")
    public Result<Page<AuditLog>> getAuditLogPage(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "actionType", required = false) String actionType,
            @RequestParam(value = "targetTable", required = false) String targetTable,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime) {

        QueryWrapper<AuditLog> queryWrapper = new QueryWrapper<>();

        // 按操作人ID精确筛选
        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        }
        // 按操作类型精确筛选
        if (actionType != null && !actionType.isEmpty()) {
            queryWrapper.eq("action_type", actionType);
        }
        // 按操作模块模糊匹配
        if (targetTable != null && !targetTable.isEmpty()) {
            queryWrapper.like("target_table", targetTable);
        }
        // 按时间范围筛选
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (startTime != null && !startTime.isEmpty()) {
            queryWrapper.ge("action_time", LocalDateTime.parse(startTime, formatter));
        }
        if (endTime != null && !endTime.isEmpty()) {
            queryWrapper.le("action_time", LocalDateTime.parse(endTime, formatter));
        }

        // 按操作时间倒序排列
        queryWrapper.orderByDesc("action_time");

        return Result.success(auditLogService.page(new Page<>(current, size), queryWrapper));
    }

    /**
     * 查询所有审计日志
     *
     * @return 所有审计日志
     */
    @GetMapping
    public Result<List<AuditLog>> listAuditLogs() {
        return Result.success(auditLogService.list());
    }
}
