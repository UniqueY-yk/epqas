package com.epqas.common.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Spring 组件：在应用启动时将 application.yml 中的
 * encryption.aes.secret-key 和 encryption.aes.iv 注入到 AESUtil 静态类中。
 * 仅在配置了 encryption.aes.secret-key 的服务中激活。
 */
@Component
@ConditionalOnProperty(name = "encryption.aes.secret-key")
public class AESUtilInitializer {

    @Value("${encryption.aes.secret-key}")
    private String secretKey;

    @Value("${encryption.aes.iv}")
    private String iv;

    @PostConstruct
    public void init() {
        AESUtil.init(secretKey, iv);
    }
}
