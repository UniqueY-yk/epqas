package com.epqas.common.annotation;

import java.lang.annotation.*;

/**
 * 系统操作日志注解
 * <p>
 * 标注在 Controller 方法上，由 {@link com.epqas.common.aspect.SystemLogAspect} 切面拦截，
 * 自动采集操作信息并通过 Feign 异步发送至 audit-service 进行持久化存储。
 * </p>
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SystemLog {

    /**
     * 操作模块描述，例如 "用户管理-新增用户"
     */
    String description() default "";

    /**
     * 操作类型，例如 "CREATE" / "UPDATE" / "DELETE" / "QUERY"
     * 如不指定，切面将根据请求方法自动推断
     */
    String actionType() default "";
}
