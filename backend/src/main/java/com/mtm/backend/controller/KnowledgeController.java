package com.mtm.backend.controller;

import com.mtm.backend.service.KnowledgeService;
import com.mtm.backend.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
@Slf4j
public class KnowledgeController {
    
    private final KnowledgeService knowledgeService;
    
    /**
     * 获取知识库项目列表
     * 对应前端 getKnowledgeItems() 方法
     */
    @GetMapping("/items")
    public ResponseEntity<?> getKnowledgeItems(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {
        try {
            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录", "/api/knowledge/items"));
            }
            
            log.info("用户{}获取知识库项目列表，页码：{}，大小：{}", userId, page, size);
            
            // 调用服务层获取知识库项目
            Object result = knowledgeService.getKnowledgeItems(page, size, subject, category, search, userId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取知识库项目失败", e);
            return ResponseEntity.status(500).body(createErrorResponse("获取知识库项目失败: " + e.getMessage(), "/api/knowledge/items"));
        }
    }
    
    /**
     * 上传文档到知识库
     * 对应前端 uploadDocument() 方法
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String tags) {
        try {
            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录", "/api/knowledge/upload"));
            }
            
            // 验证文件
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("上传文件不能为空", "/api/knowledge/upload"));
            }
            
            log.info("用户{}上传知识库文档，文件名：{}", userId, file.getOriginalFilename());
            
            // 调用服务层处理文档上传
            Object result = knowledgeService.uploadDocument(file, subject, category, title, content, tags, userId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("上传知识库文档失败", e);
            return ResponseEntity.status(500).body(createErrorResponse("上传知识库文档失败: " + e.getMessage(), "/api/knowledge/upload"));
        }
    }
    
    /**
     * 创建统一的错误响应格式
     */
    private Map<String, Object> createErrorResponse(String message, String path) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        errorResponse.put("path", path);
        errorResponse.put("timestamp", System.currentTimeMillis());
        return errorResponse;
    }
}