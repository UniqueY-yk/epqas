package com.epqas.suggestion;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.epqas.suggestion.mapper")
public class SuggestionApplication {
    public static void main(String[] args) {
        SpringApplication.run(SuggestionApplication.class, args);
    }
}
