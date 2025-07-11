package com.mtm.backend.controller;

import com.mtm.backend.model.DTO.AudioUploadDTO;
import com.mtm.backend.model.DTO.DocumentUploadDTO;
import com.mtm.backend.model.DTO.MaterialQueryDTO;
import com.mtm.backend.model.VO.MaterialUploadVO;
import com.mtm.backend.model.VO.MaterialDetailVO;
import com.mtm.backend.service.MaterialService;
import com.mtm.backend.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
@Slf4j
public class MaterialController {
    
    private final MaterialService materialService;
    
    private static final String[] ALLOWED_DOCUMENT_TYPES = {
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation"
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
    
    /**
     * 学术文档上传
     */
    @PostMapping("/upload/document")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("subject") String subject,
            @RequestParam("courseLevel") String courseLevel,
            @RequestParam("documentType") String documentType,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "keywords", required = false) String keywords) {
        
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
            
            if (documentType == null || documentType.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("文档类型不能为空"));
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
            uploadDTO.setDocumentType(documentType);
            uploadDTO.setTitle(title);
            uploadDTO.setDescription(description);
            uploadDTO.setKeywords(keywords);
            
            // 上传文档
            MaterialUploadVO result = materialService.uploadDocument(file, uploadDTO, userId);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("文档上传失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("文档上传失败: " + e.getMessage()));
        }
    }
    
    /**
     * 学术语音素材上传及转文字
     */
    @PostMapping("/upload/audio")
    public ResponseEntity<?> uploadAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "transcriptionMode", defaultValue = "sync") String transcriptionMode,
            @RequestParam(value = "needTranscription", defaultValue = "true") Boolean needTranscription,
            @RequestParam(value = "subject", required = false) String subject,
            @RequestParam(value = "audioType", required = false) String audioType,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "speaker", required = false) String speaker,
            @RequestParam(value = "language", defaultValue = "zh") String language) {
        
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
            uploadDTO.setAudioType(audioType);
            uploadDTO.setDescription(description);
            uploadDTO.setSpeaker(speaker);
            uploadDTO.setLanguage(language);
            
            // 上传音频
            Object result = materialService.uploadAudio(file, uploadDTO, userId);
            
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
    
    /**
     * 分页查询教学素材列表
     */
    @GetMapping
    public ResponseEntity<?> getMaterials(
            @RequestParam(value = "materialType", required = false) String materialType,
            @RequestParam(value = "subject", required = false) String subject,
            @RequestParam(value = "courseLevel", required = false) String courseLevel,
            @RequestParam(value = "documentType", required = false) String documentType,
            @RequestParam(value = "audioType", required = false) String audioType,
            @RequestParam(value = "keywords", required = false) String keywords,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit,
            @RequestParam(value = "sortBy", defaultValue = "created_at") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "desc") String sortOrder) {
        
        try {
            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
            }
            
            // 参数验证
            if (page < 1) {
                return ResponseEntity.badRequest().body(createErrorResponse("页码必须大于0"));
            }
            
            if (limit < 1 || limit > 100) {
                return ResponseEntity.badRequest().body(createErrorResponse("每页数量必须在1-100之间"));
            }
            
            // 构建查询DTO
            MaterialQueryDTO queryDTO = MaterialQueryDTO.builder()
                    .materialType(materialType)
                    .subject(subject)
                    .courseLevel(courseLevel)
                    .documentType(documentType)
                    .audioType(audioType)
                    .keywords(keywords)
                    .page(page)
                    .limit(limit)
                    .sortBy(sortBy)
                    .sortOrder(sortOrder)
                    .build();
            
            Object result = materialService.getUserMaterials(queryDTO, userId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("查询教学素材失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("查询教学素材失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取教学素材详情
     */
    @GetMapping("/{materialId}")
    public ResponseEntity<?> getMaterialDetail(@PathVariable String materialId) {
        try {
            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
            }
            
            // 参数验证
            if (materialId == null || materialId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("素材ID不能为空"));
            }
            
            MaterialDetailVO result = materialService.getMaterialDetail(materialId, userId);
            return ResponseEntity.ok(result);
            
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在") || e.getMessage().contains("无权访问")) {
                return ResponseEntity.notFound().build();
            }
            log.error("获取素材详情失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取素材详情失败: " + e.getMessage()));
        } catch (Exception e) {
            log.error("获取素材详情失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取素材详情失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除教学素材
     */
    @DeleteMapping("/{materialId}")
    public ResponseEntity<?> deleteMaterial(@PathVariable String materialId) {
        try {
            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
            }
            
            // 参数验证
            if (materialId == null || materialId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("素材ID不能为空"));
            }
            
            materialService.deleteMaterial(materialId, userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("message", "素材删除成功");
            result.put("materialId", materialId);
            return ResponseEntity.ok(result);
            
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在") || e.getMessage().contains("无权删除")) {
                return ResponseEntity.notFound().build();
            }
            log.error("删除素材失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("删除素材失败: " + e.getMessage()));
        } catch (Exception e) {
            log.error("删除素材失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("删除素材失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取素材下载链接
     */
    @GetMapping("/{materialId}/download")
    public ResponseEntity<?> getMaterialDownloadUrl(@PathVariable String materialId) {
        try {
            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
            }
            
            // 参数验证
            if (materialId == null || materialId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("素材ID不能为空"));
            }
            
            String downloadUrl = materialService.getMaterialDownloadUrl(materialId, userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("materialId", materialId);
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
    
    /**
     * 获取用户素材统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getMaterialStatistics() {
        try {
            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
            }
            
            Object result = materialService.getUserMaterialStatistics(userId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取素材统计失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取素材统计失败: " + e.getMessage()));
        }
    }
    
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", new Date().toString());
        error.put("status", 400);
        error.put("error", "Bad Request");
        error.put("message", message);
        error.put("path", "/api/materials");
        return error;
    }
}