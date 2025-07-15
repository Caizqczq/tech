# 设计文档

## 概述

本设计文档描述了将后端资源模块从阿里云OSS存储改为本地文件系统存储的技术方案。设计目标是提供一个简单、可靠、高性能的本地文件存储解决方案，同时保持与现有API的兼容性。

## 架构

### 整体架构
```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Controller    │───▶│   Service        │───▶│  LocalFileUtil  │
│                 │    │                  │    │                 │
│ ResourceController   │ ResourceService   │    │ 本地文件操作     │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                │                        │
                                ▼                        ▼
                       ┌──────────────────┐    ┌─────────────────┐
                       │   Database       │    │  Local Storage  │
                       │                  │    │                 │
                       │ 文件元数据存储    │    │ 实际文件存储     │
                       └──────────────────┘    └─────────────────┘
```

### 存储架构
```
uploads/
├── {userId}/
│   ├── documents/
│   │   ├── lesson_plan/
│   │   ├── paper/
│   │   ├── textbook/
│   │   └── exercise/
│   └── audio/
│       ├── lecture/
│       ├── seminar/
│       └── general/
└── temp/
    └── {临时文件}
```

## 组件和接口

### 1. LocalFileConfig 配置类
负责本地文件存储的配置管理。

**主要属性：**
- `basePath`: 文件存储根目录
- `baseUrl`: 文件访问基础URL
- `maxFileSize`: 文件大小限制配置
- `allowedTypes`: 允许的文件类型

**配置示例：**
```yaml
local:
  file:
    storage:
      base-path: ./uploads
      base-url: http://localhost:8082/files
      max-file-size:
        document: 50MB
        audio: 100MB
      allowed-types:
        document: ["pdf", "doc", "docx", "ppt", "pptx", "txt", "md"]
        audio: ["mp3", "wav", "m4a", "flac"]
```

### 2. LocalFileUtil 工具类
替换现有的 OssUtil，提供本地文件操作功能。

**主要方法：**
```java
public class LocalFileUtil {
    // 上传文件
    public String uploadFile(MultipartFile file, String folder) throws IOException
    
    // 生成文件访问URL
    public String generateUrl(String filePath)
    
    // 删除文件
    public void deleteFile(String filePath)
    
    // 检查文件是否存在
    public boolean doesFileExist(String filePath)
    
    // 获取文件信息
    public FileInfo getFileInfo(String filePath)
}
```

### 3. FileAccessController 文件访问控制器
提供文件的HTTP访问接口。

**主要接口：**
```java
@RestController
@RequestMapping("/files")
public class FileAccessController {
    // 获取文件内容
    @GetMapping("/{userId}/{resourceType}/**")
    public ResponseEntity<Resource> getFile(HttpServletRequest request)
    
    // 下载文件
    @GetMapping("/download/{userId}/{resourceType}/**")
    public ResponseEntity<Resource> downloadFile(HttpServletRequest request)
}
```

### 4. 静态资源配置
在 WebConfig 中添加静态资源映射配置。

```java
@Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/files/**")
            .addResourceLocations("file:" + localFileConfig.getBasePath() + "/")
            .setCachePeriod(3600);
}
```

## 数据模型

### 文件路径存储
现有的 `TeachingResource` 实体中的字段调整：
- `ossKey` → `filePath`: 存储相对文件路径
- 保留其他字段不变，确保数据库兼容性

### 文件路径格式
```
{userId}/{resourceType}/{timestamp}_{uuid}.{extension}
```

示例：
```
123/documents/lesson_plan/20240115_143022_a1b2c3d4e5f6.pdf
456/audio/lecture/20240115_143022_g7h8i9j0k1l2.mp3
```

## 错误处理

### 文件操作异常处理
1. **文件上传失败**
   - 磁盘空间不足
   - 权限不足
   - 文件类型不支持

2. **文件访问失败**
   - 文件不存在
   - 权限验证失败
   - 文件损坏

3. **配置错误**
   - 存储路径不存在
   - 权限配置错误

### 异常处理策略
```java
@ControllerAdvice
public class FileStorageExceptionHandler {
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<?> handleFileStorageException(FileStorageException e)
    
    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<?> handleFileNotFoundException(FileNotFoundException e)
    
    @ExceptionHandler(FileAccessDeniedException.class)
    public ResponseEntity<?> handleFileAccessDeniedException(FileAccessDeniedException e)
}
```

## 测试策略

### 单元测试
1. **LocalFileUtil 测试**
   - 文件上传功能测试
   - 文件删除功能测试
   - URL生成测试
   - 文件存在性检查测试

2. **FileAccessController 测试**
   - 文件访问权限测试
   - 文件下载测试
   - 错误处理测试

### 集成测试
1. **完整上传流程测试**
   - 文档上传 → 本地存储 → 数据库记录 → URL访问
   - 音频上传 → 本地存储 → 转录处理 → 文件访问

2. **权限验证测试**
   - 用户只能访问自己的文件
   - 未授权访问返回403错误

### 性能测试
1. **并发上传测试**
   - 多用户同时上传文件
   - 大文件上传性能测试

2. **文件访问性能测试**
   - 静态文件服务性能
   - 大量文件访问的响应时间

## 迁移策略

### 从OSS到本地存储的迁移
1. **配置切换**
   - 添加本地存储配置
   - 保留OSS配置作为备用

2. **数据迁移**
   - 现有OSS文件下载到本地
   - 更新数据库中的文件路径
   - 验证迁移完整性

3. **渐进式切换**
   - 新上传文件使用本地存储
   - 现有文件保持OSS访问
   - 逐步迁移历史文件

### 回滚策略
如果本地存储出现问题，可以快速回滚到OSS：
1. 修改配置文件
2. 重启应用服务
3. 验证OSS功能正常

## 安全考虑

### 文件访问控制
1. **路径遍历攻击防护**
   - 验证文件路径合法性
   - 禁止访问上级目录

2. **文件类型验证**
   - 检查文件扩展名
   - 验证文件MIME类型
   - 防止恶意文件上传

3. **用户权限验证**
   - 验证用户身份
   - 确保用户只能访问自己的文件

### 存储安全
1. **文件系统权限**
   - 设置适当的文件夹权限
   - 限制应用程序访问范围

2. **备份策略**
   - 定期备份重要文件
   - 实现文件版本控制

## 性能优化

### 文件服务优化
1. **静态资源缓存**
   - 设置HTTP缓存头
   - 使用ETag进行缓存验证

2. **文件压缩**
   - 对文本文件启用Gzip压缩
   - 图片文件优化

3. **并发处理**
   - 使用异步文件操作
   - 线程池管理文件I/O

### 存储优化
1. **目录结构优化**
   - 避免单个目录文件过多
   - 按日期进一步分组

2. **磁盘空间管理**
   - 监控磁盘使用情况
   - 实现文件清理策略