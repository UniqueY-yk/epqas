package com.epqas.gateway.filter;

import com.epqas.common.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 认证过滤器
     * 
     * @param exchange 交换
     * @param chain    链
     * @return 过滤结果
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 跳过登录/注册的认证
        if (path.startsWith("/auth/") || path.contains("login") || path.contains("register")) {
            return chain.filter(exchange);
        }

        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        token = token.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        Claims claims = jwtUtils.parseToken(token);
        // 将用户信息传递给下游服务
        ServerHttpRequest request = exchange.getRequest().mutate()
                .header("X-User-Id", String.valueOf(claims.get("userId")))
                .header("X-Role-Id", String.valueOf(claims.get("roleId")))
                .build();

        return chain.filter(exchange.mutate().request(request).build());
    }

    /**
     * 错误处理
     * 
     * @param exchange 交换
     * @param status   状态
     * @return 错误结果
     */
    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }

    /**
     * 获取过滤器顺序，在同类过滤器里较靠前执行
     * 
     * @return 顺序
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
