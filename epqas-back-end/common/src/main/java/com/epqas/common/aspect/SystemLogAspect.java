package com.epqas.common.aspect;

import com.epqas.common.annotation.SystemLog;
import com.epqas.common.feign.AuditFeignClient;
import com.epqas.common.utils.JwtUtils;
import cn.hutool.json.JSONUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

/**
 * 系统操作日志切面
 * <p>
 * 拦截标注了 {@link SystemLog} 注解的方法，自动采集操作信息并通过 Feign 异步发送至 audit-service。
 * </p>
 * <p>
 * 使用 @ConditionalOnClass 确保仅在 Servlet 环境（非 WebFlux 网关）中加载。
 * </p>
 *
 * @author EPQAS
 */
@Slf4j
@Aspect
@Component
@ConditionalOnClass(name = "org.springframework.web.servlet.DispatcherServlet")
public class SystemLogAspect {

    @Autowired(required = false)
    private AuditFeignClient auditFeignClient;

    @Autowired(required = false)
    private JwtUtils jwtUtils;



    /**
     * 切入点：所有标注了 @SystemLog 注解的方法
     */
    @Pointcut("@annotation(com.epqas.common.annotation.SystemLog)")
    public void systemLogPointcut() {}

    /**
     * 环绕通知：采集请求信息、执行耗时、操作结果等，异步发送审计日志
     */
    @Around("systemLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        SystemLog systemLog = method.getAnnotation(SystemLog.class);

        // 获取 HTTP 请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        // 构建日志数据
        Map<String, Object> logData = new HashMap<>();
        logData.put("targetTable", systemLog.description());
        logData.put("actionTime", LocalDateTime.now().toString());

        // 解析操作类型
        String actionType = systemLog.actionType();
        if (actionType == null || actionType.isEmpty()) {
            actionType = inferActionType(request);
        }
        logData.put("actionType", actionType);

        if (request != null) {
            logData.put("ipAddress", getClientIp(request));
            logData.put("requestMethod", request.getMethod());
            logData.put("requestUrl", request.getRequestURI());

            // 解析当前操作人（从 JWT Token 中获取 userId）
            Long userId = extractUserId(request);
            logData.put("userId", userId);

            // 采集请求参数
            try {
                String[] paramNames = signature.getParameterNames();
                Object[] args = joinPoint.getArgs();
                Map<String, Object> params = new HashMap<>();
                if (paramNames != null) {
                    for (int i = 0; i < paramNames.length; i++) {
                        // 跳过不可序列化的 Servlet 对象
                        if (args[i] instanceof HttpServletRequest
                                || args[i] instanceof jakarta.servlet.http.HttpServletResponse
                                || args[i] instanceof org.springframework.web.multipart.MultipartFile) {
                            params.put(paramNames[i], "[File/Stream]");
                        } else {
                            params.put(paramNames[i], args[i]);
                        }
                    }
                }
                String paramsJson = JSONUtil.toJsonStr(params);
                // 截断过长参数，避免超出数据库 TEXT 字段限制
                if (paramsJson.length() > 2000) {
                    paramsJson = paramsJson.substring(0, 2000) + "...(truncated)";
                }
                logData.put("requestParams", paramsJson);
            } catch (Exception e) {
                logData.put("requestParams", "参数序列化失败: " + e.getMessage());
            }
        }

        // 执行目标方法
        Object result;
        try {
            result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            logData.put("duration", duration);
            logData.put("status", 0); // 0 = 成功
            // 异步发送日志
            sendAuditLogAsync(logData);
            return result;
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - startTime;
            logData.put("duration", duration);
            logData.put("status", 1); // 1 = 失败
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.length() > 2000) {
                errorMsg = errorMsg.substring(0, 2000);
            }
            logData.put("errorMsg", errorMsg);
            // 异步发送日志
            sendAuditLogAsync(logData);
            throw e;
        }
    }

    /**
     * 异步发送审计日志到 audit-service
     */
    @Async
    public void sendAuditLogAsync(Map<String, Object> logData) {
        try {
            if (auditFeignClient != null) {
                auditFeignClient.createAuditLog(logData);
            } else {
                log.warn("AuditFeignClient 未注入，审计日志无法发送: {}", logData.get("targetTable"));
            }
        } catch (Exception e) {
            // 审计日志发送失败不应影响主业务流程，仅打印警告日志
            log.warn("异步发送审计日志失败: {}", e.getMessage());
        }
    }

    /**
     * 从请求头中的 Authorization Token 解析用户ID
     */
    private Long extractUserId(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtUtils != null) {
                    Claims claims = jwtUtils.parseToken(token);
                    if (claims != null && claims.get("userId") != null) {
                        return Long.valueOf(claims.get("userId").toString());
                    }
                }
            }
        } catch (Exception e) {
            log.debug("解析用户ID失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 根据 HTTP 请求方法自动推断操作类型
     */
    private String inferActionType(HttpServletRequest request) {
        if (request == null) return "UNKNOWN";
        return switch (request.getMethod().toUpperCase()) {
            case "POST" -> "CREATE";
            case "PUT", "PATCH" -> "UPDATE";
            case "DELETE" -> "DELETE";
            case "GET" -> "QUERY";
            default -> "OTHER";
        };
    }

    /**
     * 获取客户端真实 IP 地址（考虑反向代理场景）
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理场景取第一个非 unknown 的 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
