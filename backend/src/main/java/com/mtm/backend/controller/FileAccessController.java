package com.mtm.backend.controller;

import com.mtm.backend.config.LocalFileConfig;
import com.mtm.backend.utils.LocalFileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Slf4j
public class FileAccessController {

    private final LocalFileConfig localFileConfig;
    private final LocalFileUtil localFileUtil;

    /**
     * 文件访问端点 - 支持在线预览和下载
     * GET /files/{filePath}
     */
    @GetMapping("/**")
    public ResponseEntity<Resource> getFile(
            HttpServletRequest request,
            @RequestParam(value = "download", defaultValue = "false") boolean download) {
        
        try {
            // 从请求路径中提取文件路径
            String requestPath = request.getRequestURI();
            String filePath = requestPath.substring("/files/".length());
            
            log.info("文件访问请求 - 路径: {}, 下载模式: {}", filePath, download);
            
            // 参数验证
            if (filePath == null || filePath.trim().isEmpty()) {
                log.warn("文件路径为空");
                return ResponseEntity.badRequest().build();
            }
            
            // 安全验证 - 防止路径遍历攻击
            if (filePath.contains("..") || filePath.contains("\\") || filePath.startsWith("/")) {
                log.warn("检测到不安全的文件路径: {}", filePath);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // 检查文件是否存在
            if (!localFileUtil.doesFileExist(filePath)) {
                log.warn("文件不存在: {}", filePath);
                return ResponseEntity.notFound().build();
            }
            
            // 构建完整文件路径
            Path fullPath = Paths.get(localFileConfig.getAbsoluteBasePath(), filePath);
            
            // 验证文件确实在存储目录内（双重安全检查）
            Path basePath = Paths.get(localFileConfig.getAbsoluteBasePath()).toAbsolutePath().normalize();
            Path resolvedPath = fullPath.toAbsolutePath().normalize();
            
            if (!resolvedPath.startsWith(basePath)) {
                log.warn("文件路径超出存储目录范围: {}", resolvedPath);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // 获取文件信息
            LocalFileUtil.FileInfo fileInfo = localFileUtil.getFileInfo(filePath);
            
            // 创建文件资源
            Resource resource = new FileSystemResource(resolvedPath);
            
            // 构建响应头
            HttpHeaders headers = new HttpHeaders();
            
            // 设置内容类型
            String contentType = fileInfo.getContentType();
            if (contentType != null) {
                headers.setContentType(MediaType.parseMediaType(contentType));
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
            
            // 设置文件大小
            headers.setContentLength(fileInfo.getFileSize());
            
            // 设置缓存控制
            headers.setCacheControl("public, max-age=3600"); // 1小时缓存
            
            // 根据下载参数设置Content-Disposition
            if (download) {
                // 强制下载
                String encodedFileName = URLEncoder.encode(fileInfo.getFileName(), StandardCharsets.UTF_8);
                headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + fileInfo.getFileName() + "\"; filename*=UTF-8''" + encodedFileName);
            } else {
                // 在线预览（如果浏览器支持）
                String encodedFileName = URLEncoder.encode(fileInfo.getFileName(), StandardCharsets.UTF_8);
                headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                    "inline; filename=\"" + fileInfo.getFileName() + "\"; filename*=UTF-8''" + encodedFileName);
            }
            
            log.info("文件访问成功 - 文件: {}, 大小: {} bytes, 类型: {}", 
                    fileInfo.getFileName(), fileInfo.getFileSize(), contentType);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("文件访问失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    /**
 
    * 获取文件信息
     * GET /files/info/{filePath}
     */
    @GetMapping("/info/**")
    public ResponseEntity<?> getFileInfo(HttpServletRequest request) {
        try {
            // 从请求路径中提取文件路径
            String requestPath = request.getRequestURI();
            String filePath = requestPath.substring("/files/info/".length());
            
            log.info("文件信息查询 - 路径: {}", filePath);
            
            // 参数验证
            if (filePath == null || filePath.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("文件路径不能为空"));
            }
            
            // 安全验证
            if (filePath.contains("..") || filePath.contains("\\") || filePath.startsWith("/")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("不安全的文件路径"));
            }
            
            // 检查文件是否存在
            if (!localFileUtil.doesFileExist(filePath)) {
                return ResponseEntity.notFound().build();
            }
            
            // 获取文件信息
            LocalFileUtil.FileInfo fileInfo = localFileUtil.getFileInfo(filePath);
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("filePath", fileInfo.getFilePath());
            response.put("fileName", fileInfo.getFileName());
            response.put("fileSize", fileInfo.getFileSize());
            response.put("lastModified", fileInfo.getLastModified());
            response.put("contentType", fileInfo.getContentType());
            response.put("downloadUrl", localFileUtil.generateUrl(filePath) + "?download=true");
            response.put("previewUrl", localFileUtil.generateUrl(filePath));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取文件信息失败", e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("获取文件信息失败: " + e.getMessage()));
        }
    }

    /**
     * 健康检查端点
     * GET /files/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("storageBasePath", localFileConfig.getAbsoluteBasePath());
            health.put("baseUrl", localFileConfig.getBaseUrl());
            
            // 检查存储目录是否可访问
            Path storagePath = Paths.get(localFileConfig.getAbsoluteBasePath());
            boolean storageAccessible = Files.exists(storagePath) && Files.isDirectory(storagePath) && Files.isReadable(storagePath);
            health.put("storageAccessible", storageAccessible);
            
            if (!storageAccessible) {
                health.put("status", "DOWN");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);
            }
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            log.error("健康检查失败", e);
            Map<String, Object> health = new HashMap<>();
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);
        }
    }

    /**
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", System.currentTimeMillis());
        error.put("status", 400);
        error.put("error", "Bad Request");
        error.put("message", message);
        error.put("path", "/files");
        return error;
    }
}