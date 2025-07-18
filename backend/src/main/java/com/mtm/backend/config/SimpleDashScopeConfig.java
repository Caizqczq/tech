package com.mtm.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

/**
 * 简化的DashScope配置
 * 重点解决连接重置问题
 */
@Configuration
@Slf4j
public class SimpleDashScopeConfig {

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    /**
     * 优化的连接池配置
     */
    @Bean("dashScopeConnectionProvider")
    public ConnectionProvider connectionProvider() {
        return ConnectionProvider.builder("dashscope-pool")
                .maxConnections(20)
                .maxIdleTime(Duration.ofSeconds(30))
                .maxLifeTime(Duration.ofMinutes(5))
                .pendingAcquireTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * 优化的HttpClient配置
     */
    @Bean("dashScopeHttpClient") 
    public HttpClient httpClient() {
        return HttpClient.create(connectionProvider())
                .responseTimeout(Duration.ofSeconds(60))
                .doOnRequest((req, conn) -> log.debug("DashScope请求: {} {}", req.method(), req.uri()))
                .doOnResponse((res, conn) -> log.debug("DashScope响应: {}", res.status()));
    }

    /**
     * 专用的WebClient配置
     */
    @Bean("dashScopeWebClient")
    public WebClient webClient() {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient()))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Connection", "keep-alive")
                .build();
    }
}