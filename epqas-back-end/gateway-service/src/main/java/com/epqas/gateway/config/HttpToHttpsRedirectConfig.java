package com.epqas.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.WebFilter;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.net.URI;

/**
 * HTTP → HTTPS 自动重定向配置
 * <p>
 * 使用原生 Reactor Netty 在 HTTP 端口 (8080) 上启动一个独立的轻量级服务器，
 * 将所有 HTTP 请求以 301 永久重定向到 HTTPS (8443)。
 * <p>
 * 这不会与 Spring Boot 的主 WebFlux 服务器冲突，因为它是一个独立的 Reactor Netty 实例。
 * <p>
 * 效果：
 * - http://localhost:8080/any/path  →  301  →  https://localhost:8443/any/path
 * - https://localhost:8443/any/path  →  正常处理
 */
@Configuration
public class HttpToHttpsRedirectConfig {

    private static final int HTTP_PORT = 8080;
    private static final int HTTPS_PORT = 8443;

    private DisposableServer redirectServer;

    /**
     * 在 Spring 容器初始化后，启动一个独立的 Reactor Netty HTTP 服务器，
     * 专门用于将 HTTP 请求重定向到 HTTPS。
     */
    @PostConstruct
    public void startHttpRedirectServer() {
        redirectServer = HttpServer.create()
                .port(HTTP_PORT)
                .handle((request, response) -> {
                    String httpsUrl = "https://" + request.requestHeaders().get("Host", "localhost:" + HTTPS_PORT)
                            .replace(String.valueOf(HTTP_PORT), String.valueOf(HTTPS_PORT))
                            + request.uri();
                    response.status(301);
                    response.header("Location", httpsUrl);
                    return response.send();
                })
                .bindNow();

        System.out.println("HTTP redirect server started on port " + HTTP_PORT + " → HTTPS " + HTTPS_PORT);
    }

    @PreDestroy
    public void stopHttpRedirectServer() {
        if (redirectServer != null) {
            redirectServer.disposeNow();
        }
    }
}
