package com.mtm.backend.controller;

import com.mtm.backend.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {
    
    private final TaskService taskService;
    
    /** 6.1 查询任务状态 */
    @GetMapping("/{taskId}/status")
    public ResponseEntity<?> getTaskStatus(@PathVariable String taskId) {
        try {
            Object result = taskService.getTaskStatus(taskId);
            if (result == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("查询任务状态失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("timestamp", new Date().toString());
            error.put("status", 500);
            error.put("error", "Internal Server Error");
            error.put("message", "查询任务状态失败: " + e.getMessage());
            error.put("path", "/api/tasks/" + taskId + "/status");
            return ResponseEntity.internalServerError().body(error);
        }
    }
}