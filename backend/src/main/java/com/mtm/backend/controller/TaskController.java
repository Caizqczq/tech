package com.mtm.backend.controller;

import com.mtm.backend.service.TaskService;
import com.mtm.backend.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {
    
    private final TaskService taskService;
    
    /**
     * 获取任务状态
     * 对应前端 getTaskStatus() 方法
     */
    @GetMapping("/{taskId}/status")
    public ResponseEntity<?> getTaskStatus(@PathVariable String taskId) {
        try {
            // 验证用户登录
            Integer userId = ThreadLocalUtil.get();
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录", "/api/tasks/" + taskId + "/status"));
            }
            
            // 验证任务ID
            if (taskId == null || taskId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("任务ID不能为空", "/api/tasks/" + taskId + "/status"));
            }
            
            log.info("用户{}查询任务状态，任务ID：{}", userId, taskId);
            
            // 调用服务层获取任务状态
            Object result = taskService.getTaskStatus(taskId, userId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取任务状态失败，任务ID：{}", taskId, e);
            return ResponseEntity.status(500).body(createErrorResponse("获取任务状态失败: " + e.getMessage(), "/api/tasks/" + taskId + "/status"));
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
