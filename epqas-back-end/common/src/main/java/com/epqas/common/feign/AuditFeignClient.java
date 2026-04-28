package com.epqas.common.feign;

import com.epqas.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 审计日志 Feign 客户端
 * <p>
 * 用于从其他微服务异步发送审计日志到 audit-service 进行持久化。
 * </p>
 *
 * @author EPQAS
 */
@FeignClient(name = "audit-service")
public interface AuditFeignClient {

    /**
     * 创建审计日志
     *
     * @param auditLog 审计日志数据（使用 Map 避免 common 模块循环依赖 audit-service 的实体类）
     * @return 操作结果
     */
    @PostMapping("/audit-logs")
    Result<Boolean> createAuditLog(@RequestBody Map<String, Object> auditLog);
}
