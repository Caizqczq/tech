package com.mtm.backend.controller;

import com.mtm.backend.model.DTO.AudioUploadDTO;
import com.mtm.backend.model.DTO.BatchUploadDTO;
import com.mtm.backend.model.DTO.DocumentUploadDTO;
import com.mtm.backend.model.DTO.KnowledgeBaseCreateDTO;
import com.mtm.backend.model.DTO.RAGQueryDTO;
import com.mtm.backend.model.DTO.ResourceQueryDTO;
import com.mtm.backend.model.VO.ResourceUploadVO;
import com.mtm.backend.model.VO.ResourceDetailVO;
import com.mtm.backend.service.KnowledgeBaseService;
import com.mtm.backend.service.RAGService;
import com.mtm.backend.service.ResourceService;
import com.mtm.backend.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 智能教学资源管理模块控制器
 * 符合接口文档模块5的规范要求
 */
@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
@Slf4j
public class ResourceController {

    private final ResourceService resourceService;
    private final KnowledgeBaseService knowledgeBaseService;
    private final RAGService ragService;
    
    private static final String[] ALLOWED_DOCUMENT_TYPES = {
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        "text/plain",
        "text/markdown"
    };
    
    private static final String[] ALLOWED_AUDIO_TYPES = {
        "audio/mpeg",     // mp3
        "audio/wav",      // wav
        "audio/mp4",      // m4a
        "audio/x-m4a",    // m4a
        "audio/flac"      // flac
    };
    
    private static final long MAX_DOCUMENT_SIZE = 50 * 1024 * 1024; // 50MB
    private static final long MAX_AUDIO_SIZE = 100 * 1024 * 1024; // 100MB
    
    /** 5.1.1 学术文档上传 */
    @PostMapping("/upload/document")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("subject") String subject,
            @RequestParam("courseLevel") String courseLevel,
            @RequestParam("resourceType") String resourceType,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "keywords", required = false) String keywords,
            @RequestParam(value = "autoVectorize", defaultValue = "true") Boolean autoVectorize,
            @RequestParam(value = "autoExtractKeywords", defaultValue = "true") Boolean autoExtractKeywords) {
        
        try {
            // 参数验证
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("文件不能为空"));
            }
            
            if (file.getSize() > MAX_DOCUMENT_SIZE) {
                return ResponseEntity.badRequest().body(createErrorResponse("文件大小超过50MB限制"));
            }
            
            if (!Arrays.asList(ALLOWED_DOCUMENT_TYPES).contains(file.getContentType())) {
                return ResponseEntity.badRequest().body(createErrorResponse("不支持的文件类型"));
            }
            
            if (subject == null || subject.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("学科分类不能为空"));
            }
            
            if (courseLevel == null || courseLevel.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("课程层次不能为空"));
            }
            
            if (resourceType == null || resourceType.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("资源类型不能为空"));
            }
            
            // 验证courseLevel枚举值
            if (!Arrays.asList("undergraduate", "graduate", "doctoral").contains(courseLevel)) {
                return ResponseEntity.badRequest().body(createErrorResponse("课程层次必须是undergraduate、graduate或doctoral"));
            }
            
            // 验证resourceType枚举值
            if (!Arrays.asList("lesson_plan", "syllabus", "paper", "textbook", "exercise").contains(resourceType)) {
                return ResponseEntity.badRequest().body(createErrorResponse("资源类型必须是lesson_plan、syllabus、paper、textbook或exercise"));
            }
            
            // 获取当前用户ID
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
            }
            
            // 构建DTO
            DocumentUploadDTO uploadDTO = new DocumentUploadDTO();
            uploadDTO.setSubject(subject);
            uploadDTO.setCourseLevel(courseLevel);
            uploadDTO.setResourceType(resourceType);
            uploadDTO.setTitle(title);
            uploadDTO.setDescription(description);
            uploadDTO.setKeywords(keywords);
            uploadDTO.setAutoVectorize(autoVectorize);
            uploadDTO.setAutoExtractKeywords(autoExtractKeywords);
            
            // 上传文档
            ResourceUploadVO result = resourceService.uploadDocument(file, uploadDTO, userId);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("文档上传失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("文档上传失败: " + e.getMessage()));
        }
    }
    
    /** 5.1.2 学术语音上传及转文字 */
    @PostMapping("/upload/audio")
    public ResponseEntity<?> uploadAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "transcriptionMode", defaultValue = "sync") String transcriptionMode,
            @RequestParam(value = "needTranscription", defaultValue = "true") Boolean needTranscription,
            @RequestParam(value = "subject", required = false) String subject,
            @RequestParam(value = "resourceType", required = false) String resourceType,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "speaker", required = false) String speaker,
            @RequestParam(value = "language", defaultValue = "zh") String language,
            @RequestParam(value = "autoVectorize", defaultValue = "true") Boolean autoVectorize) {
        
        try {
            // 参数验证
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("文件不能为空"));
            }
            
            if (file.getSize() > MAX_AUDIO_SIZE) {
                return ResponseEntity.badRequest().body(createErrorResponse("文件大小超过100MB限制"));
            }
            
            if (!Arrays.asList(ALLOWED_AUDIO_TYPES).contains(file.getContentType())) {
                return ResponseEntity.badRequest().body(createErrorResponse("不支持的音频文件类型"));
            }
            
            // 验证枚举值
            if (!Arrays.asList("sync", "async", "stream").contains(transcriptionMode)) {
                return ResponseEntity.badRequest().body(createErrorResponse("转录模式必须是sync、async或stream"));
            }
            
            if (resourceType != null && !Arrays.asList("lecture", "seminar", "discussion", "interview").contains(resourceType)) {
                return ResponseEntity.badRequest().body(createErrorResponse("资源类型必须是lecture、seminar、discussion或interview"));
            }
            
            if (!Arrays.asList("zh", "en").contains(language)) {
                return ResponseEntity.badRequest().body(createErrorResponse("语言必须是zh或en"));
            }
            
            // 获取当前用户ID
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
            }
            
            // 构建DTO
            AudioUploadDTO uploadDTO = new AudioUploadDTO();
            uploadDTO.setTranscriptionMode(transcriptionMode);
            uploadDTO.setNeedTranscription(needTranscription);
            uploadDTO.setSubject(subject);
            uploadDTO.setResourceType(resourceType);
            uploadDTO.setDescription(description);
            uploadDTO.setSpeaker(speaker);
            uploadDTO.setLanguage(language);
            uploadDTO.setAutoVectorize(autoVectorize);
            
            // 上传音频
            Object result = resourceService.uploadAudio(file, uploadDTO, userId);
            
            if ("async".equals(transcriptionMode)) {
                return ResponseEntity.status(202).body(result); // 202 Accepted
            } else {
                return ResponseEntity.ok(result);
            }
            
        } catch (Exception e) {
            log.error("音频上传失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("音频上传失败: " + e.getMessage()));
        }
    }
    
    /** 5.1.3 批量资源上传 */
    @PostMapping("/upload/batch")
    public ResponseEntity<?> uploadBatch(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("subject") String subject,
            @RequestParam("courseLevel") String courseLevel,
            @RequestParam(value = "autoVectorize", defaultValue = "true") Boolean autoVectorize) {
        
        try {
            // 参数验证
            if (files == null || files.length == 0) {
                return ResponseEntity.badRequest().body(createErrorResponse("文件列表不能为空"));
            }
            
            if (files.length > 10) {
                return ResponseEntity.badRequest().body(createErrorResponse("最多只能上传10个文件"));
            }
            
            if (subject == null || subject.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("学科分类不能为空"));
            }
            
            if (courseLevel == null || courseLevel.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("课程层次不能为空"));
            }
            
            // 验证courseLevel枚举值
            if (!Arrays.asList("undergraduate", "graduate", "doctoral").contains(courseLevel)) {
                return ResponseEntity.badRequest().body(createErrorResponse("课程层次必须是undergraduate、graduate或doctoral"));
            }
            
            // 获取当前用户ID
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
            }
            
            // 构建DTO
            BatchUploadDTO uploadDTO = new BatchUploadDTO();
            uploadDTO.setSubject(subject);
            uploadDTO.setCourseLevel(courseLevel);
            uploadDTO.setAutoVectorize(autoVectorize);
            
            // 批量上传
            Object result = resourceService.uploadBatch(files, uploadDTO, userId);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("批量上传失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("批量上传失败: " + e.getMessage()));
        }
    }
    
    /** 5.2.1 分页查询教学资源 */
    @GetMapping
    public ResponseEntity<?> getResources(
            @RequestParam(value = "resourceType", required = false) String resourceType,
            @RequestParam(value = "subject", required = false) String subject,
            @RequestParam(value = "courseLevel", required = false) String courseLevel,
            @RequestParam(value = "keywords", required = false) String keywords,
            @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        try {
            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
            }
            
            // 构建查询DTO
            ResourceQueryDTO queryDTO = ResourceQueryDTO.builder()
                    .resourceType(resourceType)
                    .subject(subject)
                    .courseLevel(courseLevel)
                    .keywords(keywords)
                    .build();
            
            Object result = resourceService.getResources(queryDTO, pageable, userId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("查询教学资源失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("查询教学资源失败: " + e.getMessage()));
        }
    }
    
    /** 5.2.2 语义搜索资源 */
    @GetMapping("/search/semantic")
    public ResponseEntity<?> searchResourcesSemantic(
            @RequestParam("query") String query,
            @RequestParam(value = "subject", required = false) String subject,
            @RequestParam(value = "courseLevel", required = false) String courseLevel,
            @RequestParam(value = "topK", defaultValue = "10") Integer topK,
            @RequestParam(value = "threshold", defaultValue = "0.7") Double threshold) {
        
        try {
            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
            }
            
            // 参数验证
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("搜索查询不能为空"));
            }
            
            if (topK < 1 || topK > 100) {
                return ResponseEntity.badRequest().body(createErrorResponse("返回结果数量必须在1-100之间"));
            }
            
            if (threshold < 0.0 || threshold > 1.0) {
                return ResponseEntity.badRequest().body(createErrorResponse("相似度阈值必须在0.0-1.0之间"));
            }
            
            Object result = resourceService.searchResourcesSemantic(query, subject, courseLevel, topK, threshold, userId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("语义搜索失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("语义搜索失败: " + e.getMessage()));
        }
    }
    
    /** 5.2.3 获取资源详情 */
    @GetMapping("/{resourceId}")
    public ResponseEntity<?> getResourceDetail(@PathVariable String resourceId) {
        try {
            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
            }
            
            // 参数验证
            if (resourceId == null || resourceId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("资源ID不能为空"));
            }
            
            ResourceDetailVO result = resourceService.getResourceDetail(resourceId, userId);
            return ResponseEntity.ok(result);
            
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在") || e.getMessage().contains("无权访问")) {
                return ResponseEntity.notFound().build();
            }
            log.error("获取资源详情失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取资源详情失败: " + e.getMessage()));
        } catch (Exception e) {
            log.error("获取资源详情失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取资源详情失败: " + e.getMessage()));
        }
    }
    
    /** 5.5.1 删除教学资源 */
    @DeleteMapping("/{resourceId}")
    public ResponseEntity<?> deleteResource(@PathVariable String resourceId) {
        try {
            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
            }
            
            // 参数验证
            if (resourceId == null || resourceId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("资源ID不能为空"));
            }
            
            resourceService.deleteResource(resourceId, userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("message", "资源删除成功");
            result.put("resourceId", resourceId);
            return ResponseEntity.ok(result);
            
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在") || e.getMessage().contains("无权删除")) {
                return ResponseEntity.notFound().build();
            }
            log.error("删除资源失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("删除资源失败: " + e.getMessage()));
        } catch (Exception e) {
            log.error("删除资源失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("删除资源失败: " + e.getMessage()));
        }
    }
    
    /** 5.5.2 获取资源下载链接 */
    @GetMapping("/{resourceId}/download")
    public ResponseEntity<?> getResourceDownloadUrl(@PathVariable String resourceId) {
        try {
            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
            }
            
            // 参数验证
            if (resourceId == null || resourceId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("资源ID不能为空"));
            }
            
            String downloadUrl = resourceService.getResourceDownloadUrl(resourceId, userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("resourceId", resourceId);
            result.put("downloadUrl", downloadUrl);
            result.put("expiresIn", 3600); // 1小时有效期
            return ResponseEntity.ok(result);
            
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在") || e.getMessage().contains("无权访问")) {
                return ResponseEntity.notFound().build();
            }
            log.error("获取下载链接失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取下载链接失败: " + e.getMessage()));
        } catch (Exception e) {
            log.error("获取下载链接失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取下载链接失败: " + e.getMessage()));
        }
    }
    
    /** 5.5.3 资源统计信息 */
    @GetMapping("/statistics")
    public ResponseEntity<?> getResourceStatistics() {
        try {
            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
            }

            Object result = resourceService.getResourceStatistics(userId);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("获取资源统计失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取资源统计失败: " + e.getMessage()));
        }
    }

    /** 5.3.1 构建知识库 */
    @PostMapping("/knowledge-base")
    public ResponseEntity<?> createKnowledgeBase(@RequestBody KnowledgeBaseCreateDTO createDTO) {
        try {
            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
            }

            // 参数验证
            if (createDTO.getName() == null || createDTO.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("知识库名称不能为空"));
            }

            if (createDTO.getResourceIds() == null || createDTO.getResourceIds().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("资源ID列表不能为空"));
            }

            if (createDTO.getSubject() == null || createDTO.getSubject().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("学科领域不能为空"));
            }

            if (createDTO.getCourseLevel() == null || createDTO.getCourseLevel().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("课程层次不能为空"));
            }

            // 验证courseLevel枚举值
            if (!Arrays.asList("undergraduate", "graduate", "doctoral").contains(createDTO.getCourseLevel())) {
                return ResponseEntity.badRequest().body(createErrorResponse("课程层次必须是undergraduate、graduate或doctoral"));
            }

            Object result = knowledgeBaseService.createKnowledgeBase(createDTO, userId);
            return ResponseEntity.status(202).body(result); // 202 Accepted

        } catch (Exception e) {
            log.error("创建知识库失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("创建知识库失败: " + e.getMessage()));
        }
    }

    /** 5.3.2 知识库状态查询 */
    @GetMapping("/knowledge-base/{knowledgeBaseId}/status")
    public ResponseEntity<?> getKnowledgeBaseStatus(@PathVariable String knowledgeBaseId) {
        try {
            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
            }

            // 参数验证
            if (knowledgeBaseId == null || knowledgeBaseId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("知识库ID不能为空"));
            }

            Object result = knowledgeBaseService.getKnowledgeBaseStatus(knowledgeBaseId, userId);
            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在") || e.getMessage().contains("无权访问")) {
                return ResponseEntity.notFound().build();
            }
            log.error("查询知识库状态失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("查询知识库状态失败: " + e.getMessage()));
        } catch (Exception e) {
            log.error("查询知识库状态失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("查询知识库状态失败: " + e.getMessage()));
        }
    }

    /** 5.3.3 知识库列表 */
    @GetMapping("/knowledge-base")
    public ResponseEntity<?> getKnowledgeBaseList(
            @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
            }

            Object result = knowledgeBaseService.getKnowledgeBaseList(userId, pageable);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("获取知识库列表失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取知识库列表失败: " + e.getMessage()));
        }
    }

    /** 5.4.1 基于RAG的智能问答 */
    @PostMapping("/qa")
    public ResponseEntity<?> ragQuery(@RequestBody RAGQueryDTO queryDTO) {
        try {
            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
            }

            // 参数验证
            if (queryDTO.getQuery() == null || queryDTO.getQuery().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("用户问题不能为空"));
            }

            // 验证回答模式
            if (queryDTO.getAnswerMode() != null &&
                !Arrays.asList("detailed", "concise", "tutorial").contains(queryDTO.getAnswerMode())) {
                return ResponseEntity.badRequest().body(createErrorResponse("回答模式必须是detailed、concise或tutorial"));
            }

            // 设置默认值
            if (queryDTO.getTopK() == null) {
                queryDTO.setTopK(5);
            }
            if (queryDTO.getIncludeReferences() == null) {
                queryDTO.setIncludeReferences(true);
            }

            Object result = ragService.ragQuery(queryDTO, userId);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("RAG问答失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("RAG问答失败: " + e.getMessage()));
        }
    }

    /** 5.4.2 流式智能问答 */
    @PostMapping(value = "/qa/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> ragQueryStream(@RequestBody RAGQueryDTO queryDTO) {
        try {
            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return Flux.error(new RuntimeException("用户未登录"));
            }

            // 参数验证
            if (queryDTO.getQuery() == null || queryDTO.getQuery().trim().isEmpty()) {
                return Flux.error(new RuntimeException("用户问题不能为空"));
            }

            // 设置默认值
            if (queryDTO.getTopK() == null) {
                queryDTO.setTopK(5);
            }

            return ragService.ragQueryStream(queryDTO, userId);

        } catch (Exception e) {
            log.error("流式RAG问答失败", e);
            return Flux.error(new RuntimeException("流式RAG问答失败: " + e.getMessage()));
        }
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", new Date().toString());
        error.put("status", 400);
        error.put("error", "Bad Request");
        error.put("message", message);
        error.put("path", "/api/resources");
        return error;
    }
}