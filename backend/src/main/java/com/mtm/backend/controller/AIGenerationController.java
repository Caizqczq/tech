package com.mtm.backend.controller;

import com.mtm.backend.model.DTO.ExplanationRequestDTO;
import com.mtm.backend.model.DTO.PPTGenerationDTO;
import com.mtm.backend.model.DTO.QuizGenerationDTO;
import com.mtm.backend.service.AIGenerationService;
import com.mtm.backend.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/generate")
@RequiredArgsConstructor
@Slf4j
public class AIGenerationController {
    
    private final AIGenerationService aiGenerationService;
    
    /**
     * 生成教学解释内容
     * 对应前端 generateExplanation() 方法
     */
    @PostMapping("/explanation")
    public ResponseEntity<?> generateExplanation(@RequestBody ExplanationRequestDTO request) {
        try {
            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录", "/api/ai/generate/explanation"));
            }
            
            // 验证必要参数
            if (request.getTopic() == null || request.getTopic().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("主题不能为空", "/api/ai/generate/explanation"));
            }
            
            if (request.getSubject() == null || request.getSubject().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("学科不能为空", "/api/ai/generate/explanation"));
            }
            
            if (request.getCourseLevel() == null || request.getCourseLevel().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("课程层次不能为空", "/api/ai/generate/explanation"));
            }
            
            log.info("用户{}请求生成教学解释，主题：{}，学科：{}", userId, request.getTopic(), request.getSubject());
            
            // 调用服务层生成解释
            Object result = aiGenerationService.generateExplanation(request, userId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("生成教学解释失败", e);
            return ResponseEntity.status(500).body(createErrorResponse("生成教学解释失败: " + e.getMessage(), "/api/ai/generate/explanation"));
        }
    }
    
    /**
     * 生成PPT课件
     * 对应前端 generatePPT() 方法
     */
    @PostMapping("/ppt")
    public ResponseEntity<?> generatePPT(@RequestBody PPTGenerationDTO request) {
        try {
            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录", "/api/ai/generate/ppt"));
            }
            
            // 验证必要参数
            if (request.getTopic() == null || request.getTopic().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("主题不能为空", "/api/ai/generate/ppt"));
            }
            
            if (request.getSubject() == null || request.getSubject().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("学科不能为空", "/api/ai/generate/ppt"));
            }
            
            if (request.getCourseLevel() == null || request.getCourseLevel().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("课程层次不能为空", "/api/ai/generate/ppt"));
            }
            
            log.info("用户{}请求生成PPT，主题：{}，学科：{}", userId, request.getTopic(), request.getSubject());
            
            // 调用服务层生成PPT
            Object result = aiGenerationService.generatePPT(request, userId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("生成PPT失败", e);
            return ResponseEntity.status(500).body(createErrorResponse("生成PPT失败: " + e.getMessage(), "/api/ai/generate/ppt"));
        }
    }
    
    /**
     * 生成习题
     * 对应前端 generateQuiz() 方法
     */
    @PostMapping("/quiz")
    public ResponseEntity<?> generateQuiz(@RequestBody QuizGenerationDTO request) {
        try {
            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录", "/api/ai/generate/quiz"));
            }
            
            // 验证必要参数
            if (request.getTopic() == null || request.getTopic().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("主题不能为空", "/api/ai/generate/quiz"));
            }
            
            if (request.getSubject() == null || request.getSubject().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("学科不能为空", "/api/ai/generate/quiz"));
            }
            
            if (request.getCourseLevel() == null || request.getCourseLevel().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("课程层次不能为空", "/api/ai/generate/quiz"));
            }
            
            log.info("用户{}请求生成习题，主题：{}，学科：{}", userId, request.getTopic(), request.getSubject());
            
            // 调用服务层生成习题
            Object result = aiGenerationService.generateQuiz(request, userId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("生成习题失败", e);
            return ResponseEntity.status(500).body(createErrorResponse("生成习题失败: " + e.getMessage(), "/api/ai/generate/quiz"));
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