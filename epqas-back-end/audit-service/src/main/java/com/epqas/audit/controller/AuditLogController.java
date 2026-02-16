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

    @PostMapping
    public Result<Boolean> createAuditLog(@RequestBody AuditLog auditLog) {
        return Result.success(auditLogService.save(auditLog));
    }

    @GetMapping("/{id}")
    public Result<AuditLog> getAuditLogById(@PathVariable Long id) {
        return Result.success(auditLogService.getById(id));
    }

    @PutMapping
    public Result<Boolean> updateAuditLog(@RequestBody AuditLog auditLog) {
        return Result.success(auditLogService.updateById(auditLog));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteAuditLog(@PathVariable Long id) {
        return Result.success(auditLogService.removeById(id));
    }

    @GetMapping("/page")
    public Result<Page<AuditLog>> getAuditLogPage(@RequestParam(defaultValue = "1") Integer current,
                                          @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(auditLogService.page(new Page<>(current, size)));
    }
    
    @GetMapping
    public Result<List<AuditLog>> listAuditLogs() {
        return Result.success(auditLogService.list());
    }
}
