package com.mtm.backend.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.mtm.backend.config.OssConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OssUtil {
    
    private final OSS ossClient;
    private final OssConfig ossConfig;
    
    /**
     * 上传文件到OSS
     * @param file 文件
     * @param folder 文件夹路径，如 "documents/1/lesson_plan"
     * @return OSS文件key
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        log.info("开始上传文件到OSS - 原始文件名: {}, 大小: {} bytes, 文件夹: {}",
                file.getOriginalFilename(), file.getSize(), folder);

        // 验证文件
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
            ? originalFilename.substring(originalFilename.lastIndexOf("."))
            : "";

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String filename = timestamp + "_" + uuid + extension;

        // 构建OSS对象key，规范化路径避免双斜杠
        String ossKey = normalizePath(folder + "/" + filename);

        log.info("生成OSS Key: {}", ossKey);
        log.info("使用Bucket: {}", ossConfig.getBucketName());

        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                ossConfig.getBucketName(),
                ossKey,
                inputStream
            );

            // 设置文件元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            putObjectRequest.setMetadata(metadata);

            log.info("开始执行OSS上传操作...");
            PutObjectResult result = ossClient.putObject(putObjectRequest);
            log.info("文件上传成功: {}, ETag: {}", ossKey, result.getETag());

            return ossKey;
        } catch (Exception e) {
            log.error("文件上传失败 - OSS Key: {}, 错误信息: {}", ossKey, e.getMessage(), e);

            // 提供更详细的错误信息
            if (e.getMessage().contains("SignatureDoesNotMatch")) {
                log.error("OSS签名验证失败，请检查:");
                log.error("1. AccessKeyId和AccessKeySecret是否正确");
                log.error("2. 服务器时间是否与阿里云服务器时间同步");
                log.error("3. Endpoint配置是否正确");
                log.error("4. Bucket名称是否正确");
            }

            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 生成文件访问URL
     * @param ossKey OSS对象key
     * @return 访问URL
     */
    public String generateUrl(String ossKey) {
        if (ossConfig.getCustomDomain() != null && !ossConfig.getCustomDomain().isEmpty()) {
            return "https://" + ossConfig.getCustomDomain() + "/" + ossKey;
        } else {
            return "https://" + ossConfig.getBucketName() + "." + ossConfig.getEndpoint() + "/" + ossKey;
        }
    }
    
    /**
     * 删除OSS文件
     * @param ossKey OSS对象key
     */
    public void deleteFile(String ossKey) {
        try {
            ossClient.deleteObject(ossConfig.getBucketName(), ossKey);
            log.info("文件删除成功: {}", ossKey);
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查文件是否存在
     * @param ossKey OSS对象key
     * @return 是否存在
     */
    public boolean doesObjectExist(String ossKey) {
        try {
            return ossClient.doesObjectExist(ossConfig.getBucketName(), ossKey);
        } catch (Exception e) {
            log.error("检查文件是否存在失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 生成OSS签名URL
     * @param ossKey OSS对象key
     * @param expireHours 过期时间(小时)
     * @return 签名URL
     */
    public String generateSignedUrl(String ossKey, int expireHours) {
        try {
            Date expiration = new Date(System.currentTimeMillis() + (long) expireHours * 60 * 60 * 1000);
            String signedUrl = ossClient.generatePresignedUrl(ossConfig.getBucketName(), ossKey, expiration).toString();
            
            // 强制使用HTTPS协议，DashScope API要求HTTPS
            if (signedUrl.startsWith("http://")) {
                signedUrl = signedUrl.replace("http://", "https://");
            }
            
            log.info("生成OSS签名URL成功: {} (过期时间: {}小时)", ossKey, expireHours);
            return signedUrl;
        } catch (Exception e) {
            log.error("生成OSS签名URL失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成OSS签名URL失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成OSS签名URL（默认2小时过期）
     * @param ossKey OSS对象key
     * @return 签名URL
     */
    public String generateSignedUrl(String ossKey) {
        return generateSignedUrl(ossKey, 2);
    }

    /**
     * 获取OSS配置的endpoint
     * @return endpoint
     */
    public String getEndpoint() {
        return ossConfig.getEndpoint();
    }

    /**
     * 获取OSS配置的bucket名称
     * @return bucket名称
     */
    public String getBucketName() {
        return ossConfig.getBucketName();
    }

    /**
     * 获取OSS配置的自定义域名
     * @return 自定义域名
     */
    public String getCustomDomain() {
        return ossConfig.getCustomDomain();
    }

    /**
     * 规范化路径，移除多余的斜杠
     * @param path 原始路径
     * @return 规范化后的路径
     */
    private String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }

        // 移除多余的斜杠，但保留开头的斜杠（如果有的话）
        String normalized = path.replaceAll("/+", "/");

        // 移除开头的斜杠（OSS Key不应该以斜杠开头）
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        // 移除结尾的斜杠
        if (normalized.endsWith("/") && normalized.length() > 1) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }
}