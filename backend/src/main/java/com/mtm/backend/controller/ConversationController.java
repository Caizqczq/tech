package com.mtm.backend.controller;

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
    
    /**
     * 获取对话列表
     */
    @GetMapping("/conversations")
    public ResponseEntity<?> getConversations(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit,
            @RequestParam(value = "scenario", required = false) String scenario,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        try {
            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录", "/api/chat/conversations"));
            }
            
            // 参数验证
            if (page < 1) {
                return ResponseEntity.badRequest().body(createErrorResponse("页码必须大于0", "/api/chat/conversations"));
            }
            
            if (limit < 1 || limit > 100) {
                return ResponseEntity.badRequest().body(createErrorResponse("每页数量必须在1-100之间", "/api/chat/conversations"));
            }
            
            Object result = conversationService.getUserConversations(userId, page, limit, scenario, startDate, endDate);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取对话列表失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取对话列表失败: " + e.getMessage(), "/api/chat/conversations"));
        }
    }
    
    /**
     * 获取对话详情
     */
    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<?> getConversationDetail(@PathVariable String conversationId) {
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
            
            Object result = conversationService.getConversationDetail(conversationId, userId);
            return ResponseEntity.ok(result);
            
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在") || e.getMessage().contains("无权访问")) {
                return ResponseEntity.notFound().build();
            }
            log.error("获取对话详情失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取对话详情失败: " + e.getMessage(), "/api/chat/conversations/" + conversationId));
        } catch (Exception e) {
            log.error("获取对话详情失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取对话详情失败: " + e.getMessage(), "/api/chat/conversations/" + conversationId));
        }
    }
    
    /**
     * 删除对话
     */
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
    
    /**
     * 清空所有对话
     */
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
    @PutMapping("/conversations/{conversationId}/title")
    public ResponseEntity<?> updateConversationTitle(@PathVariable String conversationId, 
                                                    @RequestBody Map<String, String> request) {
        try {
            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录", "/api/chat/conversations/" + conversationId + "/title"));
            }
            
            // 参数验证
            if (conversationId == null || conversationId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("对话ID不能为空", "/api/chat/conversations/" + conversationId + "/title"));
            }
            
            String newTitle = request.get("title");
            if (newTitle == null || newTitle.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("新标题不能为空", "/api/chat/conversations/" + conversationId + "/title"));
            }
            
            if (newTitle.length() > 200) {
                return ResponseEntity.badRequest().body(createErrorResponse("标题长度不能超过200字符", "/api/chat/conversations/" + conversationId + "/title"));
            }
            
            conversationService.updateConversationTitle(conversationId, userId, newTitle);
            
            Map<String, Object> result = new HashMap<>();
            result.put("message", "对话标题更新成功");
            result.put("conversationId", conversationId);
            result.put("newTitle", newTitle);
            return ResponseEntity.ok(result);
            
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在") || e.getMessage().contains("无权修改")) {
                return ResponseEntity.notFound().build();
            }
            log.error("更新对话标题失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("更新对话标题失败: " + e.getMessage(), "/api/chat/conversations/" + conversationId + "/title"));
        } catch (Exception e) {
            log.error("更新对话标题失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("更新对话标题失败: " + e.getMessage(), "/api/chat/conversations/" + conversationId + "/title"));
        }
    }
    
    /**
     * 获取对话统计信息
     */
    @GetMapping("/conversations/statistics")
    public ResponseEntity<?> getConversationStatistics() {
        try {
            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录", "/api/chat/conversations/statistics"));
            }
            
            Object result = conversationService.getConversationStatistics(userId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取对话统计失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取对话统计失败: " + e.getMessage(), "/api/chat/conversations/statistics"));
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