package com.mtm.backend.utils;

import com.mtm.backend.config.LocalFileConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class LocalFileUtil {
    
    private final LocalFileConfig localFileConfig;
    
    /**
     * 上传文件到本地存储
     * @param file 文件
     * @param folder 文件夹路径，如 "documents/1/lesson_plan" 或 "audio/1/lecture"
     * @return 本地文件路径
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        log.info("开始上传文件到本地存储 - 原始文件名: {}, 大小: {} bytes, 文件夹: {}",
                file.getOriginalFilename(), file.getSize(), folder);

        // 验证文件
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        // 获取文件扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 验证文件类型
        String resourceType = extractResourceType(folder);
        if (!localFileConfig.isFileTypeAllowed(resourceType, extension.substring(1))) {
            throw new IllegalArgumentException("不支持的文件类型: " + extension);
        }

        // 验证文件大小
        long maxSize = localFileConfig.getMaxFileSizeBytes(resourceType);
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小超过限制: " + (maxSize / 1024 / 1024) + "MB");
        }

        // 生成唯一文件名
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String filename = timestamp + "_" + uuid + extension;

        // 构建完整的文件路径
        String relativePath = folder + "/" + filename;
        Path fullPath = Paths.get(localFileConfig.getAbsoluteBasePath(), relativePath);

        log.info("生成文件路径: {}", relativePath);
        log.info("完整存储路径: {}", fullPath.toAbsolutePath());

        try {
            // 创建目录结构
            Files.createDirectories(fullPath.getParent());
            
            // 保存文件
            Files.copy(file.getInputStream(), fullPath, StandardCopyOption.REPLACE_EXISTING);
            
            log.info("文件上传成功: {}", relativePath);
            return relativePath;
            
        } catch (IOException e) {
            log.error("文件上传失败 - 路径: {}, 错误信息: {}", relativePath, e.getMessage(), e);
            throw new IOException("文件上传失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 生成文件访问URL
     * @param filePath 本地文件路径
     * @return 访问URL
     */
    public String generateUrl(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("文件路径不能为空");
        }
        
        // 确保路径以正斜杠开头，用于URL
        String normalizedPath = filePath.replace("\\", "/");
        if (!normalizedPath.startsWith("/")) {
            normalizedPath = "/" + normalizedPath;
        }
        
        String url = localFileConfig.getBaseUrl() + normalizedPath;
        log.debug("生成文件访问URL: {} -> {}", filePath, url);
        return url;
    }
    
    /**
     * 删除本地文件
     * @param filePath 本地文件路径
     */
    public void deleteFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            log.warn("删除文件失败: 文件路径为空");
            return;
        }
        
        try {
            Path fullPath = Paths.get(localFileConfig.getAbsoluteBasePath(), filePath);
            
            if (Files.exists(fullPath)) {
                Files.delete(fullPath);
                log.info("文件删除成功: {}", filePath);
                
                // 尝试删除空的父目录
                cleanupEmptyDirectories(fullPath.getParent());
            } else {
                log.warn("文件不存在，无需删除: {}", filePath);
            }
            
        } catch (IOException e) {
            log.error("文件删除失败: {}, 错误信息: {}", filePath, e.getMessage(), e);
            throw new RuntimeException("文件删除失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 检查文件是否存在
     * @param filePath 本地文件路径
     * @return 是否存在
     */
    public boolean doesFileExist(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }
        
        try {
            Path fullPath = Paths.get(localFileConfig.getAbsoluteBasePath(), filePath);
            boolean exists = Files.exists(fullPath) && Files.isRegularFile(fullPath);
            log.debug("检查文件是否存在: {} -> {}", filePath, exists);
            return exists;
        } catch (Exception e) {
            log.error("检查文件是否存在失败: {}, 错误信息: {}", filePath, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 检查文件是否存在 (兼容OssUtil接口)
     * @param filePath 本地文件路径
     * @return 是否存在
     */
    public boolean doesObjectExist(String filePath) {
        return doesFileExist(filePath);
    }    
 
   /**
     * 获取文件信息
     * @param filePath 本地文件路径
     * @return 文件信息
     */
    public FileInfo getFileInfo(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("文件路径不能为空");
        }
        
        try {
            Path fullPath = Paths.get(localFileConfig.getAbsoluteBasePath(), filePath);
            
            if (!Files.exists(fullPath)) {
                throw new RuntimeException("文件不存在: " + filePath);
            }
            
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFilePath(filePath);
            fileInfo.setFileName(fullPath.getFileName().toString());
            fileInfo.setFileSize(Files.size(fullPath));
            fileInfo.setLastModified(Files.getLastModifiedTime(fullPath).toMillis());
            fileInfo.setContentType(Files.probeContentType(fullPath));
            
            log.debug("获取文件信息: {}", fileInfo);
            return fileInfo;
            
        } catch (IOException e) {
            log.error("获取文件信息失败: {}, 错误信息: {}", filePath, e.getMessage(), e);
            throw new RuntimeException("获取文件信息失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 生成签名URL（本地存储不需要签名，直接返回普通URL）
     * @param filePath 本地文件路径
     * @param expireHours 过期时间(小时) - 本地存储忽略此参数
     * @return 访问URL
     */
    public String generateSignedUrl(String filePath, int expireHours) {
        log.debug("本地存储不需要签名URL，返回普通访问URL: {}", filePath);
        return generateUrl(filePath);
    }
    
    /**
     * 生成签名URL（默认2小时过期）- 本地存储版本
     * @param filePath 本地文件路径
     * @return 访问URL
     */
    public String generateSignedUrl(String filePath) {
        return generateSignedUrl(filePath, 2);
    }
    
    /**
     * 读取文件字节内容
     * @param filePath 本地文件路径
     * @return 文件字节数组
     * @throws IOException 读取失败时抛出异常
     */
    public byte[] readFileBytes(String filePath) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("文件路径不能为空");
        }
        
        try {
            Path fullPath = Paths.get(localFileConfig.getAbsoluteBasePath(), filePath);
            
            if (!Files.exists(fullPath)) {
                throw new IllegalArgumentException("文件不存在: " + filePath);
            }
            
            if (!Files.isRegularFile(fullPath)) {
                throw new IllegalArgumentException("不是有效的文件: " + filePath);
            }
            
            byte[] content = Files.readAllBytes(fullPath);
            log.debug("成功读取文件内容: {}, 大小: {} bytes", filePath, content.length);
            return content;
            
        } catch (IOException e) {
            log.error("读取文件内容失败: {}, 错误信息: {}", filePath, e.getMessage(), e);
            throw new IOException("读取文件内容失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 从文件夹路径中提取资源类型
     * 例如: "documents/1/lesson_plan" -> "document"
     *      "audio/1/lecture" -> "audio"
     */
    private String extractResourceType(String folder) {
        if (folder == null || folder.trim().isEmpty()) {
            return "document"; // 默认类型
        }
        
        String[] parts = folder.split("/");
        if (parts.length > 0) {
            String firstPart = parts[0].toLowerCase();
            // 处理复数形式
            if (firstPart.equals("documents")) {
                return "document";
            } else if (firstPart.equals("audios")) {
                return "audio";
            }
            return firstPart;
        }
        
        return "document"; // 默认类型
    }
    
    /**
     * 清理空的父目录
     */
    private void cleanupEmptyDirectories(Path directory) {
        if (directory == null) {
            return;
        }
        
        try {
            // 不要删除根存储目录
            Path basePath = Paths.get(localFileConfig.getAbsoluteBasePath());
            if (directory.equals(basePath) || !directory.startsWith(basePath)) {
                return;
            }
            
            // 检查目录是否为空
            if (Files.exists(directory) && Files.isDirectory(directory)) {
                try (var stream = Files.list(directory)) {
                    if (stream.findAny().isEmpty()) {
                        Files.delete(directory);
                        log.debug("删除空目录: {}", directory);
                        
                        // 递归清理父目录
                        cleanupEmptyDirectories(directory.getParent());
                    }
                }
            }
        } catch (IOException e) {
            log.debug("清理空目录失败: {}, 错误信息: {}", directory, e.getMessage());
            // 不抛出异常，因为这不是关键操作
        }
    }
    
    /**
     * 文件信息类
     */
    public static class FileInfo {
        private String filePath;
        private String fileName;
        private long fileSize;
        private long lastModified;
        private String contentType;
        
        // Getters and Setters
        public String getFilePath() {
            return filePath;
        }
        
        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }
        
        public String getFileName() {
            return fileName;
        }
        
        public void setFileName(String fileName) {
            this.fileName = fileName;
        }
        
        public long getFileSize() {
            return fileSize;
        }
        
        public void setFileSize(long fileSize) {
            this.fileSize = fileSize;
        }
        
        public long getLastModified() {
            return lastModified;
        }
        
        public void setLastModified(long lastModified) {
            this.lastModified = lastModified;
        }
        
        public String getContentType() {
            return contentType;
        }
        
        public void setContentType(String contentType) {
            this.contentType = contentType;
        }
        
        @Override
        public String toString() {
            return "FileInfo{" +
                    "filePath='" + filePath + '\'' +
                    ", fileName='" + fileName + '\'' +
                    ", fileSize=" + fileSize +
                    ", lastModified=" + lastModified +
                    ", contentType='" + contentType + '\'' +
                    '}';
        }
    }
}