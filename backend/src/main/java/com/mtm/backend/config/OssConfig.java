package com.mtm.backend.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
@ConfigurationProperties(prefix = "aliyun.oss")
@Data
@Slf4j
public class OssConfig {

    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private String endpoint;
    private String customDomain;

    @PostConstruct
    public void validateConfig() {
        log.info("=== OSS配置验证 ===");
        log.info("AccessKeyId: {}", accessKeyId != null ? accessKeyId.substring(0, Math.min(8, accessKeyId.length())) + "***" : "null");
        log.info("AccessKeySecret: {}", accessKeySecret != null ? "***" + accessKeySecret.substring(Math.max(0, accessKeySecret.length() - 4)) : "null");
        log.info("BucketName: {}", bucketName);
        log.info("Endpoint: {}", endpoint);
        log.info("CustomDomain: {}", customDomain);

        if (accessKeyId == null || accessKeyId.trim().isEmpty()) {
            log.error("OSS AccessKeyId未配置或为空");
            throw new IllegalStateException("OSS AccessKeyId未配置");
        }

        if (accessKeySecret == null || accessKeySecret.trim().isEmpty()) {
            log.error("OSS AccessKeySecret未配置或为空");
            throw new IllegalStateException("OSS AccessKeySecret未配置");
        }

        if (bucketName == null || bucketName.trim().isEmpty()) {
            log.error("OSS BucketName未配置或为空");
            throw new IllegalStateException("OSS BucketName未配置");
        }

        if (endpoint == null || endpoint.trim().isEmpty()) {
            log.error("OSS Endpoint未配置或为空");
            throw new IllegalStateException("OSS Endpoint未配置");
        }

        log.info("OSS配置验证通过");
    }

    @Bean
    public OSS ossClient() {
        // 确保endpoint格式正确
        String normalizedEndpoint = endpoint;
        if (!normalizedEndpoint.startsWith("http://") && !normalizedEndpoint.startsWith("https://")) {
            normalizedEndpoint = "https://" + normalizedEndpoint;
        }

        log.info("创建OSS客户端 - Endpoint: {}, BucketName: {}", normalizedEndpoint, bucketName);

        try {
            OSS ossClient = new OSSClientBuilder().build(normalizedEndpoint, accessKeyId, accessKeySecret);
            log.info("OSS客户端创建成功");
            return ossClient;
        } catch (Exception e) {
            log.error("OSS客户端创建失败: {}", e.getMessage(), e);
            throw new RuntimeException("OSS客户端创建失败: " + e.getMessage(), e);
        }
    }
}