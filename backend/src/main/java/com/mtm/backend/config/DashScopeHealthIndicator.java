package com.mtm.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * DashScope API健康检查指示器
 * 监控DashScope API连接状态
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DashScopeHealthIndicator {

    private final WebClient dashScopeWebClient;
    
    /**
     * 检查DashScope API连接健康状态
     * @return 连接状态信息
     */
    public HealthStatus checkHealth() {
        try {
            // 执行简单的连接测试
            boolean isHealthy = checkDashScopeConnection();
            
            if (isHealthy) {
                return HealthStatus.up("DashScope API连接正常");
            } else {
                return HealthStatus.down("DashScope API连接异常");
            }
        } catch (Exception e) {
            log.error("DashScope健康检查失败", e);
            return HealthStatus.down("健康检查失败: " + e.getMessage());
        }
    }

    private boolean checkDashScopeConnection() {
        try {
            // 简单的连接测试 - 不发送实际请求，只检查连接
            String testEndpoint = "https://dashscope.aliyuncs.com/api/v1/services/embeddings/text-embedding/text-embedding";
            
            Boolean result = dashScopeWebClient
                    .get()
                    .uri(testEndpoint)
                    .retrieve()
                    .bodyToMono(String.class)
                    .map(response -> true)
                    .onErrorReturn(false)
                    .timeout(Duration.ofSeconds(10))
                    .block();
                    
            return result != null && result;
        } catch (Exception e) {
            log.warn("DashScope连接检查异常: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 健康状态数据类
     */
    public static class HealthStatus {
        private final boolean healthy;
        private final String message;
        
        private HealthStatus(boolean healthy, String message) {
            this.healthy = healthy;
            this.message = message;
        }
        
        public static HealthStatus up(String message) {
            return new HealthStatus(true, message);
        }
        
        public static HealthStatus down(String message) {
            return new HealthStatus(false, message);
        }
        
        public boolean isHealthy() {
            return healthy;
        }
        
        public String getMessage() {
            return message;
        }
    }
}