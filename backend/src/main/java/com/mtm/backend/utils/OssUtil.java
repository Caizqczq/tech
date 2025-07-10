package com.mtm.backend.utils;

import com.aliyun.oss.OSS;
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
        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
            : "";
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String filename = timestamp + "_" + uuid + extension;
        
        // 构建OSS对象key
        String ossKey = folder + "/" + filename;
        
        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                ossConfig.getBucketName(), 
                ossKey, 
                inputStream
            );
            
            PutObjectResult result = ossClient.putObject(putObjectRequest);
            log.info("文件上传成功: {}, ETag: {}", ossKey, result.getETag());
            
            return ossKey;
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
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
}