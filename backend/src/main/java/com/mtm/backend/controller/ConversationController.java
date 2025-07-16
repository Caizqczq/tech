package com.mtm.backend.controller;

import com.mtm.backend.enums.ConversationScenario;
import com.mtm.backend.service.ConversationService;
import com.mtm.backend.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ConversationController {
    
    private final ConversationService conversationService;
    
    /** 4.1 获取对话列表 */
    @GetMapping("/conversations")
    public ResponseEntity<?> getConversations(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String scenario,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        
        try {
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body("用户未登录");
            }
            
            // 验证scenario参数是否有效
            if (scenario != null && !scenario.trim().isEmpty()) {
                try {
                    ConversationScenario.fromCode(scenario);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body("无效的scenario参数: " + scenario);
                }
            }
            
            Map<String, Object> result = conversationService.getUserConversations(
                    userId, page, limit, scenario, startDate, endDate);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取对话列表失败", e);
            return ResponseEntity.internalServerError().body("获取对话列表失败: " + e.getMessage());
        }
    }
    
    /** 4.2 获取对话详情 */
    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<?> getConversationDetail(@PathVariable String conversationId) {
        try {
            log.info("收到获取对话详情请求: conversationId={}", conversationId);
            
            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                log.warn("用户未登录");
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录", "/api/chat/conversations/" + conversationId));
            }
            
            log.info("用户ID: {}", userId);
            
            // 参数验证
            if (conversationId == null || conversationId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("对话ID不能为空", "/api/chat/conversations/" + conversationId));
            }
            
            Object result = conversationService.getConversationDetail(conversationId, userId);
            log.info("返回结果: {}", result);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取对话详情失败: conversationId={}", conversationId, e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取对话详情失败: " + e.getMessage(), "/api/chat/conversations/" + conversationId));
        }
    }
    
    /** 4.3 删除对话 */
    @DeleteMapping("/conversations/{conversationId}")
    public ResponseEntity<?> deleteConversation(@PathVariable String conversationId) {
        try {
            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录", "/api/chat/conversations/" + conversationId));
            }
            
            // 参数验证
            if (conversationId == null || conversationId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("对话ID不能为空", "/api/chat/conversations/" + conversationId));
            }
            
            conversationService.deleteConversation(conversationId, userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("message", "对话删除成功");
            result.put("conversationId", conversationId);
            return ResponseEntity.ok(result);
            
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在") || e.getMessage().contains("无权删除")) {
                return ResponseEntity.notFound().build();
            }
            log.error("删除对话失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("删除对话失败: " + e.getMessage(), "/api/chat/conversations/" + conversationId));
        } catch (Exception e) {
            log.error("删除对话失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("删除对话失败: " + e.getMessage(), "/api/chat/conversations/" + conversationId));
        }
    }
    
    /** 4.4 清空所有对话 */
    @DeleteMapping("/conversations")
    public ResponseEntity<?> clearAllConversations() {
        try {
            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录", "/api/chat/conversations"));
            }
            
            conversationService.clearAllConversations(userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("message", "所有对话已清空");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("清空对话失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("清空对话失败: " + e.getMessage(), "/api/chat/conversations"));
        }
    }
    
    /**
     * 更新对话标题
     */
    @PutMapping("/conversations/{id}/title")
    public ResponseEntity<?> updateConversationTitle(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {
        try {
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body("用户未登录");
            }
            
            String title = request.get("title");
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("标题不能为空");
            }
            
            conversationService.updateConversationTitle(id, title, userId);
            return ResponseEntity.ok("标题更新成功");
            
        } catch (Exception e) {
            log.error("更新对话标题失败", e);
            return ResponseEntity.status(500).body("更新标题失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取对话统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getConversationStats() {
        try {
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body("用户未登录");
            }
            
            Map<String, Object> stats = conversationService.getConversationStats(userId);
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("获取对话统计失败", e);
            return ResponseEntity.status(500).body("获取统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建标准错误响应
     */
    private Map<String, Object> createErrorResponse(String message, String path) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", new Date().toString());
        error.put("status", 500);
        error.put("error", "Internal Server Error");
        error.put("message", message);
        error.put("path", path);
        return error;
    }
}
