package com.mtm.backend.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "local.file.storage")
@Data
@Slf4j
public class LocalFileConfig {

    /**
     * 文件存储根目录
     */
    private String basePath = "./uploads";

    /**
     * 文件访问基础URL
     */
    private String baseUrl = "http://localhost:8082/files";

    /**
     * 文件大小限制配置 (单位: MB)
     */
    private Map<String, Integer> maxFileSize = Map.of(
            "document", 50,
            "audio", 100
    );

    /**
     * 允许的文件类型
     */
    private Map<String, List<String>> allowedTypes = Map.of(
            "document", List.of("pdf", "doc", "docx", "ppt", "pptx", "txt", "md"),
            "audio", List.of("mp3", "wav", "m4a", "flac")
    );

    @PostConstruct
    public void validateConfig() {
        log.info("=== 本地文件存储配置验证 ===");
        log.info("存储根目录: {}", basePath);
        log.info("访问基础URL: {}", baseUrl);
        log.info("文件大小限制: {}", maxFileSize);
        log.info("允许的文件类型: {}", allowedTypes);

        // 验证存储路径
        if (!StringUtils.hasText(basePath)) {
            log.error("本地存储路径未配置或为空");
            throw new IllegalStateException("本地存储路径未配置");
        }

        // 验证访问URL
        if (!StringUtils.hasText(baseUrl)) {
            log.error("文件访问基础URL未配置或为空");
            throw new IllegalStateException("文件访问基础URL未配置");
        }

        // 创建存储目录
        try {
            Path storagePath = Paths.get(basePath);
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
                log.info("创建存储目录: {}", storagePath.toAbsolutePath());
            }

            // 验证目录是否可写
            if (!Files.isWritable(storagePath)) {
                log.error("存储目录不可写: {}", storagePath.toAbsolutePath());
                throw new IllegalStateException("存储目录不可写: " + storagePath.toAbsolutePath());
            }

            log.info("存储目录验证通过: {}", storagePath.toAbsolutePath());
        } catch (Exception e) {
            log.error("存储目录创建或验证失败: {}", e.getMessage(), e);
            throw new IllegalStateException("存储目录创建或验证失败: " + e.getMessage(), e);
        }

        // 验证文件大小限制配置
        if (maxFileSize == null || maxFileSize.isEmpty()) {
            log.warn("文件大小限制未配置，使用默认值");
        } else {
            for (Map.Entry<String, Integer> entry : maxFileSize.entrySet()) {
                if (entry.getValue() == null || entry.getValue() <= 0) {
                    log.error("文件类型 {} 的大小限制配置无效: {}", entry.getKey(), entry.getValue());
                    throw new IllegalStateException("文件大小限制配置无效");
                }
            }
        }

        // 验证允许的文件类型配置
        if (allowedTypes == null || allowedTypes.isEmpty()) {
            log.warn("允许的文件类型未配置，将允许所有类型");
        } else {
            for (Map.Entry<String, List<String>> entry : allowedTypes.entrySet()) {
                if (entry.getValue() == null || entry.getValue().isEmpty()) {
                    log.error("文件类型 {} 的允许扩展名列表为空", entry.getKey());
                    throw new IllegalStateException("文件类型配置无效");
                }
            }
        }

        log.info("本地文件存储配置验证通过");
    }

    /**
     * 获取绝对存储路径
     */
    public String getAbsoluteBasePath() {
        return Paths.get(basePath).toAbsolutePath().toString();
    }

    /**
     * 检查文件类型是否被允许
     */
    public boolean isFileTypeAllowed(String resourceType, String fileExtension) {
        if (allowedTypes == null || allowedTypes.isEmpty()) {
            return true; // 如果没有配置限制，则允许所有类型
        }

        List<String> allowedExtensions = allowedTypes.get(resourceType);
        if (allowedExtensions == null || allowedExtensions.isEmpty()) {
            return false;
        }

        return allowedExtensions.contains(fileExtension.toLowerCase());
    }

    /**
     * 获取指定资源类型的文件大小限制（字节）
     */
    public long getMaxFileSizeBytes(String resourceType) {
        if (maxFileSize == null || maxFileSize.isEmpty()) {
            return 50 * 1024 * 1024L; // 默认50MB
        }

        Integer sizeMB = maxFileSize.get(resourceType);
        if (sizeMB == null || sizeMB <= 0) {
            return 50 * 1024 * 1024L; // 默认50MB
        }

        return sizeMB * 1024 * 1024L;
    }
}