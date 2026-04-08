package com.epqas.audit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epqas.audit.entity.AuditLog;
import com.epqas.audit.service.AuditLogService;
import com.epqas.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/audit-logs")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    /**
     * 创建审计日志
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
     * 分页查询审计日志
     * 
     * @param current 当前页
     * @param size    每页数量
     * @return 分页审计日志
     */
    @GetMapping("/page")
    public Result<Page<AuditLog>> getAuditLogPage(@RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return Result.success(auditLogService.page(new Page<>(current, size)));
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
